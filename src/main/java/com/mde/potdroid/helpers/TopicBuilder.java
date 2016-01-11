package com.mde.potdroid.helpers;

import android.content.Context;
import android.webkit.URLUtil;
import com.mde.potdroid.R;
import com.mde.potdroid.models.Post;
import com.mde.potdroid.models.Topic;
import com.mde.potdroid.models.User;
import com.samskivert.mustache.Mustache;

import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class generates the Topic HTML from a Topic object.
 */
public class TopicBuilder {

    // context reference
    private Context mContext;
    private Map<User, String> mAvatarCache = new HashMap<>();

    // a HashMap with the smileys
    public static HashMap<String, String> mSmileys = new HashMap<String, String>();
    static {
        mSmileys.put(":bang:", "banghead.gif");
        mSmileys.put(":D", "biggrin.gif");
        mSmileys.put(":confused:", "confused.gif");
        mSmileys.put(":huch:", "freaked.gif");
        mSmileys.put(":hm:", "hm.gif");
        mSmileys.put(":mata:", "mata.gif");
        mSmileys.put(":what:", "sceptic.gif");
        mSmileys.put(":moo:", "smiley-pillepalle.gif");
        mSmileys.put(":wurgs:", "urgs.gif");
        mSmileys.put(";)", "wink.gif");
        mSmileys.put(":zyklop:", "icon1.gif");
        mSmileys.put(":p", "icon2.gif");
        mSmileys.put("^^", "icon5.gif");
        mSmileys.put(":)", "icon7.gif");
        mSmileys.put(":|", "icon8.gif");
        mSmileys.put(":(", "icon12.gif");
        mSmileys.put(":mad:", "icon13.gif");
        mSmileys.put(":eek:", "icon15.gif");
        mSmileys.put(":o", "icon16.gif");
        mSmileys.put(":roll:", "icon18.gif");
        mSmileys.put("8|", "icon3.gif");
        mSmileys.put(":0:", "icon4.gif");
        mSmileys.put(":ugly:", "ugly.gif");
        mSmileys.put(":xx:", "icon11.gif");
        mSmileys.put(":zzz:", "sleepy.gif");
        mSmileys.put(":tourette:", "tourette.gif");
        mSmileys.put("[img]http://forum.mods.de/bb/img/icons/icon6.gif[/img]", "icon6.png");
        mSmileys.put("[img]http://forum.mods.de/bb/img/icons/thumbsup.gif[/img]", "thumbsup.png");
        mSmileys.put("[img]http://forum.mods.de/bb/img/icons/thumbsdown.gif[/img]", "thumbsdown.png");
        mSmileys.put("[img]http://forum.mods.de/bb/img/icons/pfeil.gif[/img]", "pfeil.png");
        mSmileys.put("[img]http://forum.mods.de/bb/img/icons/icon10.gif[/img]", "icon10.png");
        mSmileys.put("[img]http://i.imgur.com/lNddx3D.gif[/img]", "matolf.gif");

    }

    public static HashMap<String, Integer> mIcons = new HashMap<String, Integer>();
    static {
        mIcons.put("icon2.gif", 32);
        mIcons.put("icon11.gif", 40);
        mIcons.put("icon4.gif", 34);
        mIcons.put("icon3.gif", 33);
        mIcons.put("icon12.gif", 41);
        mIcons.put("thumbsup.gif", 2);
        mIcons.put("thumbsdown.gif", 1);
        mIcons.put("pfeil.gif", 54);
        mIcons.put("icon8.gif", 38);
        mIcons.put("icon5.gif", 35);
        mIcons.put("icon9.gif", 28);
        mIcons.put("icon13.gif", 42);
        mIcons.put("icon6.gif", 36);
        mIcons.put("icon10.gif", 39);
        mIcons.put("icon7.gif", 37);
    }

    // a reference to the Settings class
    private SettingsWrapper mSettings;

    // a BBCodeParser reference
    private static BBCodeParser mParser;

    protected BenderHandler mBenderHandler;


    public TopicBuilder(Context cx) {
        mContext = cx;
        mSettings = new SettingsWrapper(cx);
        mBenderHandler = new BenderHandler(cx);
    }

    /**
     * Use JMoustache to generate the Topic HTML from a Topic object topic
     *
     * @param topic the topic object
     * @return the HTML code
     * @throws IOException
     */
    public String parse(Topic topic) throws IOException {
        InputStream is = mContext.getResources().getAssets().open("thread.html");
        Reader reader = new InputStreamReader(is);
        StringWriter sw = new StringWriter();
        Mustache.compiler().compile(reader).execute(new TopicContext(topic, mContext), sw);
        mBenderHandler.updateLastSeenBenderInformation(new ArrayList(mAvatarCache.keySet()));
        return sw.toString();
    }

    /**
     * Replace the text smileys with images
     *
     * @param code the HTML code
     * @return HTML code with smileys
     */
    private String parseSmileys(String code) {
        String template = "<img src=\"smileys/%1$s\" alt=\"%2$s\" />";
        Iterator<Map.Entry<String, String>> i = mSmileys.entrySet().iterator();

        while(i.hasNext()) {
            Map.Entry<String, String> me = i.next();
            code = code.replace(me.getKey(), String.format(template, me.getValue(), me.getKey()));
        }
        return code;
    }

    /**
     * A wrapper object for JMoustache, providing some getters for the Topic object
     */
    class TopicContext {

        private Topic mTopic;
        private Context mContext;

        public TopicContext(Topic t, Context cx) {
            mTopic = t;
            mContext = cx;
        }

        public String getCssFile() {
            return Utils.getStringByAttr(mContext, R.attr.bbTopicCssFile);
        }

        public String getThemeVariant() {
            return mSettings.getThemeVariant();
        }

        public List<PostContext> getPosts() {
            List<PostContext> pc = new ArrayList<PostContext>();
            int user_id = mSettings.getUserId();
            boolean show_post_info = mSettings.showPostInfo();
            boolean parse_bbcode = mSettings.isParseBBCode();
            boolean parse_smileys = mSettings.isParseSmileys();
            boolean show_edited = mSettings.isShowEdited();
            boolean show_number = mSettings.showPostNumbers();
            for (Post p : mTopic.getPosts())
                pc.add(new PostContext(p, user_id, parse_bbcode, show_post_info, parse_smileys,
                        show_edited, show_number));
            return pc;
        }
    }

    /**
     * A wrapper object for JMoustache, providing some getters for the Post object
     */
    class PostContext {

        private int mUserId;
        private boolean mShowPostInfo;
        private boolean mParseBBcode;
        private boolean mShowEdited;
        private boolean mParseSmileys;
        private boolean mShowNumber;
        private Post mPost;

        public PostContext(Post p, int user_id, boolean parse_bbcode, boolean show_post_info,
                           boolean parse_smileys, boolean show_edited, boolean show_number) {
            mPost = p;
            mUserId = user_id;
            mParseBBcode = parse_bbcode;
            mParseSmileys = parse_smileys;
            mShowPostInfo = show_post_info;
            mShowEdited = show_edited;
            mShowNumber = show_number;
        }

        public Integer getId() {
            return mPost.getId();
        }

        public boolean getAuthorLocked() {
            return mPost.getAuthor().getLocked();
        }

        public String getAuthor() {
            return mPost.getAuthor().getNick();
        }

        public boolean isAuthor() {
            return mPost.getAuthor().getId() == mUserId;
        }

        public String getIcon() {
            if (mSettings.showPostInfo() && mPost.getIconId() != null)
                return String.format("<img class=\"posticon\" src=\"thread-icons/icon%1$d.png\" />",
                        mPost.getIconId());
            return "";
        }

        public String getAvatarBackground() {
            if(!mAvatarCache.containsKey(mPost.getAuthor())) {
                String path = mBenderHandler.getAvatarFilePathIfExists(mPost.getAuthor());
                if(path == null)
                    mAvatarCache.put(mPost.getAuthor(), "");
                else
                    mAvatarCache.put(mPost.getAuthor(),
                            String.format("style=\"background-image:url(%s)\"", path));
            }

            return mAvatarCache.get(mPost.getAuthor());
        }

        public Integer getAuthorId() {
            return mPost.getAuthor().getId();
        }

        public String getAvatar() {
            return mPost.getAuthor().getAvatarFile();
        }

        public Integer getAvatarId() {
            return mPost.getAuthor().getAvatarId();
        }

        public String getDate() {
            if(!mShowPostInfo)
                return "";
            return new SimpleDateFormat(mContext.getString(R.string.default_time_format))
                    .format(mPost.getDate()) + " Uhr";
        }

        public String getTitle() {
            if(!mShowPostInfo)
                return "";
            return mPost.getTitle();
        }

        public String getText() {
            if(!mParseBBcode)
                return mPost.getText();
            String text = mPost.getText();
            try {
                text = getBBCodeParserInstance().parse(text);

                if(mParseSmileys)
                    text = parseSmileys(text);
            } catch (Exception e) {
                Utils.printException(e);
                text = "<div class=\"err\"> Post konnte nicht geparsed werden </div><br /><br/>"
                        + mPost.getText();
            }

            return text;
        }

        public boolean isEdited() {
            return mShowEdited && mPost.getEdited() != null && mPost.getEdited() > 0;
        }

        public boolean isNumber() {
            return mShowNumber;
        }

        public String getLastEditUser() {
            return mPost.getLastEditUser().getNick();
        }

        public String getLastEditDate() {
            return new SimpleDateFormat(mContext.getString(R.string.default_time_format))
                    .format(mPost.getLastEditDate()) + " Uhr";
        }

        public Integer numEdited() {
            return mPost.getEdited();
        }
    }

    /**
     * Initialize the BBCodeParser setting all the bbcodes. The parser is a singleton object.
     *
     * @return BBCodeParser object
     */
    public static BBCodeParser getBBCodeParserInstance() {
        if (mParser != null)
            return mParser;

        // instantiate it
        mParser = new BBCodeParser();

        // the tags allowed in links
        String inLinks = "string, b, u, s, i, mod, img, url, list, table, m";

        class SimpleTag extends BBCodeParser.BBCodeTag {

            public SimpleTag(String tag, String name) {
                super(tag, name);
                this.setInvalidEndRecovery(BBCodeParser.BBCodeTag.RECOVERY_REOPEN);
                this.setInvalidStartRecovery(BBCodeParser.BBCodeTag.RECOVERY_CLOSE);
            }

            public SimpleTag(String tag, String name, String allowed) {
                super(tag, name, allowed);
                this.setInvalidEndRecovery(BBCodeParser.BBCodeTag.RECOVERY_REOPEN);
                this.setInvalidStartRecovery(BBCodeParser.BBCodeTag.RECOVERY_CLOSE);
            }

            public String html(String content, List<String> args) {
                return "";
            }
        }

        mParser.registerTag(new SimpleTag("b", "bold") {
            @Override
            public String html(String content, List<String> args) {
                return String.format("<strong>%1$s</strong>", content);
            }
        });

        mParser.registerTag(new SimpleTag("m", "monotype", "string") {
            @Override
            public String html(String content, List<String> args) {
                return String.format("<pre class=\"inline\">%1$s</pre>", content);
            }
        });

        mParser.registerTag(new SimpleTag("u", "underline") {
            @Override
            public String html(String content, List<String> args) {
                return String.format("<u>%1$s</u>", content);
            }
        });

        mParser.registerTag(new SimpleTag("s", "strike") {
            @Override
            public String html(String content, List<String> args) {
                return String.format("<span class=\"strike\">%1$s</span>", content);
            }
        });

        mParser.registerTag(new SimpleTag("i", "italic") {
            @Override
            public String html(String content, List<String> args) {
                return String.format("<em>%1$s</em>", content);
            }
        });

        mParser.registerTag(new SimpleTag("code", "code", "string") {
            @Override
            public String html(String content, List<String> args) {
                content = content.replace("<br />", "");
                return String.format("<span class=\"code\">%1$s</span>", content);
            }
        });

        mParser.registerTag(new SimpleTag("spoiler", "spoiler") {
            @Override
            public String html(String content, List<String> args) {
                return String.format("<div class=\"spoiler\"><i class=\"fa fa-warning\"></i>" +
                        "<div>%1$s</div></div>", content);
            }
        });

        mParser.registerTag(new SimpleTag("mod", "mod") {
            @Override
            public String html(String content, List<String> args) {
                return String.format("<span class=\"mod\">%1$s</span>", content);
            }
        });

        mParser.registerTag(new SimpleTag("trigger", "trigger") {
            @Override
            public String html(String content, List<String> args) {
                return String.format("<span class=\"trigger\">%1$s</span>", content);
            }
        });

        BBCodeParser.BBCodeTag list = new BBCodeParser.BBCodeTag("list", "list", "*") {
            @Override
            public String html(String content, List<String> args) {
                return String.format("<ul>%1$s</ul>", content);
            }
        };
        list.setInvalidStartRecovery(BBCodeParser.BBCodeTag.RECOVERY_ADD);
        list.setInvalidEndRecovery(BBCodeParser.BBCodeTag.RECOVERY_CLOSE);
        list.setInvalidStringRecovery(BBCodeParser.BBCodeTag.RECOVERY_ADD);
        list.setInvalidRecoveryTag("*");
        mParser.registerTag(list);

        BBCodeParser.BBCodeTag item = new BBCodeParser.BBCodeTag("*", "listitem") {
            @Override
            public String html(String content, List<String> args) {
                if (content.replace("<br />", "").trim().length() == 0)
                    return "";
                else
                    return String.format("<li>%1$s</li>", content);
            }
        };
        item.setInvalidStartRecovery(BBCodeParser.BBCodeTag.RECOVERY_CLOSE);
        item.setInvalidEndRecovery(BBCodeParser.BBCodeTag.RECOVERY_CLOSE);
        mParser.registerTag(item);

        mParser.registerTag(new BBCodeParser.BBCodeTag("url", "link", inLinks) {
            @Override
            public String html(String content, List<String> args) {

                String url = content;

                Pattern image_link = Pattern.compile("^<div class=\"img\" data-src=\"(.*?)\"><i " +
                        "class=\".*?\"></i></div>$");
                Matcher m = image_link.matcher(content);

                if (m.find() && args.size() > 0) {
                    String extension = m.group(1).substring(m.group(1).length() - 3).toLowerCase();
                    String icon = "fa-picture-o nogif";
                    if (extension.equals("gif"))
                        icon = "fa-film gif";
                    return String.format("<div class=\"img-link\" data-src=\"%1$s\" " +
                            "data-href=\"%2$s\">"
                            + "<i class=\"link fa fa-external-link-square\"></i>"
                            + "<i class=\"img-loader fa %3$s\"></i>"
                            + "</div>", m.group(1), args.get(0), icon);
                }

                if (args.size() > 0)
                    url = args.get(0);

                // add protocol if the url is malformed.
                if(!url.contains("://"))
                    url = "http://" + url;

                return String.format("<a href=\"%1$s\">%2$s</a>", url, content);
            }
        });

        mParser.registerTag(new SimpleTag("quote", "quote") {
            @Override
            public String html(String content, List<String> args) {
                if (args.size() == 3)
                    return String.format("<blockquote><a href=\"http://forum.mods.de/bb/thread.php?TID=%3$s&PID=%4$s\" class=\"author\">%1$s</a>" +
                            "<div class=\"content\">%2$s</div></blockquote>", args.get(2), content, args.get(0), args.get(1));
                else
                    return String.format("<blockquote><div " +
                            "class=\"content\">%1$s</div></blockquote>", content);
            }
        });

        mParser.registerTag(new SimpleTag("img", "image", "string") {
            @Override
            public String html(String content, List<String> args) {
                if (!URLUtil.isValidUrl(content)) {
                    return content;
                } else if(content.contains("forum.mods.de/bb/img/icons")) {
                    String icon = content.substring( content.lastIndexOf('/')+1, content.length() );
                    return String.format("<img src=\"thread-icons/icon%1$d.png\" alt=\"icon%1$d.png\" />",
                            mIcons.get(icon));
                } else {
                    String extension = content.substring(content.length() - 3).toLowerCase();
                    String icon = "fa-picture-o nogif";
                    if (extension.equals("gif"))
                        icon = "fa-film gif";
                    return String.format("<div class=\"img\" data-src=\"%1$s\"><i class=\"fa " +
                            "%2$s img-loader\"></i></div>", content, icon);
                }
            }
        });

        mParser.registerTag(new SimpleTag("video", "video", "string") {
            @Override
            public String html(String content, List<String> args) {
                if(content.contains("youtube") || content.contains("youtu.be")) {
                    return String.format("<div class=\"video yt\" data-src=\"%1$s\">" +
                            "<i class=\"link fa fa-external-link-square\"></i><i class=\"fa vid " +
                            "fa-youtube\"></i></div>", content);
                } else {
                    return String.format("<div class=\"video\" data-src=\"%1$s\">" +
                            "<i class=\"link fa fa-external-link-square\"></i><i class=\"fa vid " +
                            "fa-video-camera\"></i></div>", content);
                }
            }
        });

        mParser.registerTag(new SimpleTag("tex", "latex", "string") {
            @Override
            public String html(String content, List<String> args) {
                String code = content;
                try {
                    code = URLEncoder.encode(code, "utf-8");
                    code = code.replace("<br />", "\n");
                    return String.format("<img src=\"" +
                            "http://chart.apis.google.com/chart?chco=000000&chf=bg,s," +
                            "ffffffff&cht=tx&chl=%1$s\" class=\"tex\" />", code);
                } catch (UnsupportedEncodingException e) {
                    return "";
                }

            }
        });

        BBCodeParser.BBCodeTag table = new BBCodeParser.BBCodeTag("table", "table", "--") {
            @Override
            public String html(String content, List<String> args) {
                return String.format("<table>%1$s</table>", content);
            }
        };
        table.setInvalidRecoveryTag("--");
        table.setInvalidStartRecovery(BBCodeParser.BBCodeTag.RECOVERY_ADD);
        table.setInvalidStringRecovery(BBCodeParser.BBCodeTag.RECOVERY_ADD);
        table.setInvalidEndRecovery(BBCodeParser.BBCodeTag.RECOVERY_CLOSE);
        mParser.registerTag(table);

        BBCodeParser.BBCodeTag row = new BBCodeParser.BBCodeTag("--", "tablerow", "||") {
            @Override
            public String html(String content, List<String> args) {
                return String.format("<tr>%1$s</tr>", content);
            }
        };
        row.setInvalidRecoveryTag("||");
        row.setInvalidStartRecovery(BBCodeParser.BBCodeTag.RECOVERY_ADD);
        row.setInvalidStringRecovery(BBCodeParser.BBCodeTag.RECOVERY_ADD);
        row.setInvalidEndRecovery(BBCodeParser.BBCodeTag.RECOVERY_CLOSE);
        mParser.registerTag(row);

        BBCodeParser.BBCodeTag col = new BBCodeParser.BBCodeTag("||", "tablecol") {
            @Override
            public String html(String content, List<String> args) {
                return String.format("<td>%1$s</td>", content);
            }
        };
        col.setInvalidStartRecovery(BBCodeParser.BBCodeTag.RECOVERY_CLOSE);
        col.setInvalidEndRecovery(BBCodeParser.BBCodeTag.RECOVERY_CLOSE);
        mParser.registerTag(col);

        return mParser;
    }

}
