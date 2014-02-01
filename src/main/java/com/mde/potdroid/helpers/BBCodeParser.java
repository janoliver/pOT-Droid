package com.mde.potdroid.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author oli
 *         This class parses some bbcode formatted text and replaces the bbcode
 *         according to the BBCodeTag objects, that must be registered to the parser.
 *         <p/>
 *         It handles malformed input text as well as tag arguments.
 *         <p/>
 *         There are some hacks specific to the potdroid app for the mods.de forums.
 *         They are:
 *         - in Node.toString() the quote hack
 */
public class BBCodeParser {

    Pattern mArgsPattern = Pattern.compile("([^,\"]+)|(\"[^\"]+\")");
    // the skeleton regex for the bbcodes. %1$s must be replaced by
    // the allowed bbcodes
    private String mRegexSkeleton =
            "(.*?)((\\[\\s*(%1$s)\\s*(=((\\s*((\"[^\"]+?\")|" +
            "([^,\\]\"]+?))\\s*,)*(\\s*((\"[^\"]+?\")|([^,\"\\]]+?))\\s*)))?\\])|" +
            "(\\[/\\s*((%1$s))\\s*\\]))";

    // this map holds our registered tags
    private Map<String, BBCodeTag> mTags = new HashMap<String, BBCodeTag>();

    public static String getAllTags() {
        return "string, b, u, s, i, mod, spoiler, code, img, quote, url, list, table, m, tex";
    }

    // register a new tag
    public void registerTag(BBCodeTag tag) {
        mTags.put(tag.mTag, tag);
    }

    // generate the regex by joining their names and compile the pattern
    private Pattern generatePattern() {
        String tags = "";
        for (Map.Entry<String, BBCodeTag> entry : mTags.entrySet())
            tags += Pattern.quote(entry.getKey()) + "|";
        tags = tags.substring(0, tags.length() - 1);

        return Pattern.compile(String.format(mRegexSkeleton, tags),
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    }

    // parse the input
    public String parse(String input) throws UnknownErrorException {
        Integer lastMatched = 0;
        List<Token> tokens = new ArrayList<Token>();

        // fix for the Rufus syndrome of incredibly long posts.
        int start_bbcode = input.indexOf('[');
        int end_bbcode = input.lastIndexOf(']');

        String beginning = "";
        String end = "";

        try {
            beginning = strToHtml(input.substring(0, start_bbcode));
            end = strToHtml(input.substring(end_bbcode+1));
            input = input.substring(start_bbcode, end_bbcode+1);
        } catch(Exception e) {
            // never mind, then we match against the original string.
        }

        Pattern pattern = generatePattern();
        Matcher matcher = pattern.matcher(input);

        Node root = new Node();
        Node current = root;

        // tokenize
        // matcher.group(0) contains all
        // matcher.group(1) contains the prefixing string
        // matcher.group(2) contains the raw bbcode tag (like [b])
        // matcher.group(4) contains the start bbcode tag (like b)
        // matcher.group(7) contains the arguments
        // matcher.group(16) contains the end tag (like b)
        while (matcher.find()) {

            // the node is prefixed by some text
            if (matcher.group(1).length() > 0) {
                Token t = new Token(Token.TYPE_STRING, strToHtml(matcher.group(1)));
                tokens.add(t);
            }

            if (matcher.group(2).indexOf("[/") > -1) {
                Token t = new Token(Token.TYPE_CLOSE,
                        matcher.group(16).toLowerCase(), matcher.group(2));
                tokens.add(t);
            } else {
                List<String> args = new ArrayList<String>();

                if (matcher.group(6) != null && matcher.group(6).length() > 0) {
                    Matcher args_matcher = mArgsPattern.matcher(matcher.group(6));
                    while (args_matcher.find())
                        args.add(args_matcher.group(0).replace("\"", ""));
                }
                Token t = new Token(Token.TYPE_OPEN,
                        matcher.group(4).toLowerCase(),
                        matcher.group(2), args);
                tokens.add(t);
            }

            lastMatched = matcher.end();
        }

        if (input.substring(lastMatched).length() > 0) {
            Token t = new Token(Token.TYPE_STRING, strToHtml(input.substring(lastMatched)));
            tokens.add(t);
        }

        // parse
        while (tokens.size() > 0) {
            Token t;

            if (tokens.get(0).mType == Token.TYPE_STRING) {

                try {
                    current = add_string(current, tokens.get(0).mText);
                } catch (InvalidTokenException e) {
                    switch (current.mTag.mInvalidStringRecovery) {
                        case BBCodeTag.RECOVERY_ADD:
                            // we add a new token to the list
                            t = new Token(Token.TYPE_OPEN, current.mTag.mInvalidRecoveryAddTag);
                            tokens.add(0, t);
                            break;
                        default:
                            throw new UnknownErrorException();
                    }
                    continue;
                }
            }

            if (tokens.get(0).mType == Token.TYPE_OPEN) {

                // we add a new opening tag
                try {
                    current = add_start(current, tokens.get(0).mTag,
                            tokens.get(0).mArgs, tokens.get(0).mText);
                } catch (InvalidTokenException e) {

                    int recovery;

                    // this tag is not allowed anywhere in this branch,
                    // so we make it a string.
                    if (!is_allowed_anywhere(current, tokens.get(0).mTag) &&
                            current.mTag.mInvalidStartRecovery != BBCodeTag.RECOVERY_ADD)
                        recovery = BBCodeTag.RECOVERY_STRING;
                    else
                        recovery = current.mTag.mInvalidStartRecovery;

                    // this hack is needed to prevent an infinite loop
                    // in certain cases.
                    if (current.mTag.mTag.equals(tokens.get(0).mTag) &&
                            current.mTag.mInvalidStartRecovery == BBCodeTag.RECOVERY_ADD)
                        recovery = BBCodeTag.RECOVERY_CLOSE;

                    // policy
                    switch (recovery) {
                        case BBCodeTag.RECOVERY_ADD:
                            t = new Token(Token.TYPE_OPEN, current.mTag.mInvalidRecoveryAddTag);
                            tokens.add(0, t);
                            break;
                        case BBCodeTag.RECOVERY_CLOSE:
                            t = new Token(Token.TYPE_CLOSE, current.mTag.mTag);
                            tokens.add(0, t);
                            break;
                        case BBCodeTag.RECOVERY_STRING:
                            tokens.get(0).mType = Token.TYPE_STRING;
                            break;
                        default:
                            throw new UnknownErrorException();
                    }
                    continue;
                } catch (InvalidParameterCountException e) {
                    tokens.get(0).mType = Token.TYPE_STRING;
                    continue;
                }
            }

            if (tokens.get(0).mType == Token.TYPE_CLOSE) {

                // new closing tag
                try {
                    current = add_end(current, tokens.get(0).mTag, tokens.get(0).mText);
                } catch (InvalidTokenException e) {
                    int recovery;

                    if (!is_open(current, tokens.get(0).mTag))
                        recovery = BBCodeTag.RECOVERY_STRING;
                    else
                        recovery = current.mTag.mInvalidEndRecovery;

                    switch (recovery) {
                        case BBCodeTag.RECOVERY_REOPEN:
                            t = new Token(Token.TYPE_CLOSE,
                                    current.mTag.mTag);
                            tokens.add(0, t);
                            t = new Token(Token.TYPE_OPEN,
                                    current.mTag.mTag);
                            tokens.add(2, t);
                            break;
                        case BBCodeTag.RECOVERY_CLOSE:
                            t = new Token(Token.TYPE_CLOSE,
                                    current.mTag.mTag);
                            tokens.add(0, t);
                            break;
                        case BBCodeTag.RECOVERY_STRING:
                            tokens.get(0).mType = Token.TYPE_STRING;
                            break;
                        default:
                            throw new UnknownErrorException();
                    }
                    continue;
                }

            }

            tokens.remove(0);

        }

        // build the string and return
        return beginning + root.toString() + end;
    }

    public Node add_string(Node current, String str) throws InvalidTokenException {

        if (!is_root(current) && !current.mTag.mAllowedTags.contains("string"))
            throw new InvalidTokenException();

        Node new_node = new Node(str);
        new_node.mParent = current;
        current.mChildren.add(new_node);
        return current;
    }

    public Node add_end(Node current, String tagStr, String raw) throws InvalidTokenException {

        if (is_root(current) || !tagStr.equals(current.mTag.mTag))
            throw new InvalidTokenException();

        return current.close(raw);
    }

    public Node close(Node current) {
        return current.close();
    }

    public Node add_start(Node current, String tagStr, List<String> args, String raw)
            throws InvalidTokenException, InvalidParameterCountException {

        // create the node
        BBCodeTag tag = mTags.get(tagStr);

        Node new_node = new Node(tag, args, raw);

        // check if bbcode is allowed here
        if (!is_root(current) && !current.mTag.mAllowedTags.contains(tagStr))
            throw new InvalidTokenException();

        // This check should be performed by the tag adding
        // check the parameter count
        //if(tag.mHtml.indexOfKey(args.size()) < 0)
        //    throw new InvalidParameterCountException();

        new_node.mParent = current;

        current.mChildren.add(new_node);
        return new_node;

    }

    public Boolean is_open(Node current, String tag) {
        if (is_root(current)) {
            return false;
        } else {

            if (current.mTag.mTag.equals(tag))
                return true;
            else
                return is_open(current.mParent, tag);
        }
    }

    public Boolean is_allowed_anywhere(Node current, String tag) {
        if (is_root(current)) {
            return false;
        } else {
            if (current.mTag.mAllowedTags.contains(tag))
                return true;
            else
                return is_allowed_anywhere(current.mParent, tag);
        }
    }

    public Boolean is_root(Node current) {
        return current.isRoot();
    }

    private String strToHtml(String input) {

        input = input.replace("<", "&lt;");
        input = input.replace(">", "&gt;");
        input = input.replace("\n", "<br />\n");

        return input;
    }

    public interface TextCallback {

        public void run(String text);
    }

    /**
     * This class describes one bbcode tag. For each allowed tag it must be
     * instanciated and its members filled. The tag is then registered to the
     * parser via the registerTag() function.
     */
    public static abstract class BBCodeTag {

        public static final int RECOVERY_NONE = 0;
        public static final int RECOVERY_STRING = 1;
        public static final int RECOVERY_CLOSE = 2;
        public static final int RECOVERY_REOPEN = 3;
        public static final int RECOVERY_ADD = 4;
        public int mInvalidEndRecovery = BBCodeTag.RECOVERY_STRING;
        public int mInvalidStringRecovery = BBCodeTag.RECOVERY_NONE;
        public int mInvalidStartRecovery = BBCodeTag.RECOVERY_STRING;
        public String mInvalidRecoveryAddTag = "";
        public String mTag;
        public String mDescription = "";
        public List<String> mAllowedTags = new ArrayList<String>();
        public String mText = "";

        public BBCodeTag(String tag, String name, String allowed) {
            mTag = tag;
            mDescription = name;
            allowTags(allowed);
        }

        public BBCodeTag(String tag, String name) {
            mTag = tag;
            mDescription = name;
            allowTags(BBCodeParser.getAllTags());
        }

        public BBCodeTag setInvalidStartRecovery(int recovery) {
            mInvalidStartRecovery = recovery;
            return this;
        }

        public BBCodeTag setInvalidEndRecovery(int recovery) {
            mInvalidEndRecovery = recovery;
            return this;
        }

        public BBCodeTag setInvalidStringRecovery(int recovery) {
            mInvalidStringRecovery = recovery;
            return this;
        }

        public BBCodeTag setInvalidRecoveryTag(String tag) {
            mInvalidRecoveryAddTag = tag;
            return this;
        }

        public abstract String html(String content, List<String> args);

        private BBCodeTag allowTags(String tags) {
            String[] t = {};
            t = tags.split(",");
            for (String tag : t)
                mAllowedTags.add(tag.replace(" ", ""));

            return this;
        }

        public void startCallback(List<String> args) {
        }

        public void endCallback(List<String> args) {
        }

        public void invalidCallback(List<String> args) {
        }

        public void emptyCallback(List<String> args) {
        }

    }

    public static class Token {

        public static final int TYPE_STRING = 0;
        public static final int TYPE_OPEN = 1;
        public static final int TYPE_CLOSE = 2;
        public int mType;
        public String mText;
        public String mTag;
        public List<String> mArgs = new ArrayList<String>();

        public Token(int type, String text) {
            mType = type;

            if (mType == Token.TYPE_STRING) {
                mText = text;
            } else {
                mTag = text;

                if (mType == Token.TYPE_OPEN) {
                    mText = "[" + mTag + "]";
                }

                if (mType == Token.TYPE_CLOSE) {
                    mText = "[/" + mTag + "]";
                }
            }

        }

        public Token(int type, String tag, String text) {
            mType = type;
            mText = text;
            mTag = tag;
        }

        public Token(int type, String tag, String text, List<String> args) {
            mType = type;
            mText = text;
            mTag = tag;
            mArgs = args;
        }

    }

    /**
     * This is one Node in our lexigraphical tree.
     */
    private class Node {

        public List<Node> mChildren = new ArrayList<Node>();
        public Node mParent;
        public BBCodeTag mTag = null;
        public String mText = null;
        public List<String> mArgs;
        public String mRawStart = null;
        public String mRawEnd = null;
        public Boolean mInvalid = false;

        // initializer for the root element
        public Node() {
        }

        // initializer for a new bbcode node
        public Node(BBCodeTag type, List<String> args, String raw) {
            mTag = type;
            mArgs = args;
            mRawStart = raw;
        }

        // initializer for a String node.
        public Node(String text) {
            mText = text;
        }

        public Node close(String raw) {
            mRawEnd = raw;
            return mParent;
        }

        public Node close() {
            mRawEnd = "[/" + mTag.mTag + "]";
            return mParent;
        }

        public boolean hasTag() {
            return mTag != null;
        }

        public boolean isRoot() {
            return mTag == null && mText == null;
        }

        public boolean isString() {
            return mText != null;
        }

        public boolean isInvalid() {
            return mInvalid;
        }

        // the creation of the html string
        @Override
        public String toString() {

            // is this a string?
            if (isString())
                return mText;

            // the start callback
            if (hasTag())
                mTag.startCallback(mArgs);

            // build the result string by concatenating all children
            String res = "";
            for (Node n : mChildren)
                res = res + n.toString();

            // this is just for the root element.
            if (isRoot())
                return res;

            // end element callback
            mTag.endCallback(mArgs);

            // return empty tags
            if (res.compareTo("") == 0) {
                mTag.emptyCallback(mArgs);
                return "";
            }

            // invalid?
            if (isInvalid()) {
                mTag.invalidCallback(mArgs);
                return String.format("%s" + res + "%s", mRawStart, mRawEnd);
            }

            // replace the arguments if there are some
            int num_args = 0;
            if (mArgs != null)
                num_args = mArgs.size();

            // this is a hack for the potdroid app: if the current tag
            // is [quote] and the containing string starts with [b] and ends
            // with [/b], remove the bold tags.
            if (mTag.mTag.equals("quote") && res.startsWith("<strong>") && res.endsWith
                    ("</strong>"))
                res = res.substring(8, res.length() - 9);

            // create the html
            String html = mTag.html(res, mArgs);

            return html;
        }
    }

    public class InvalidTokenException extends Exception {

        private static final long serialVersionUID = 42L;

        public InvalidTokenException() {
            super("Invalid token");
        }
    }

    public class InvalidParameterCountException extends Exception {

        private static final long serialVersionUID = 44L;

        public InvalidParameterCountException() {
            super("Invalid parameter count");
        }
    }

    public class UnknownErrorException extends Exception {

        private static final long serialVersionUID = 43L;

        public UnknownErrorException() {
            super("Unknown error");
        }
    }

}
