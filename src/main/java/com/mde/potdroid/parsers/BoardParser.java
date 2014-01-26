package com.mde.potdroid.parsers;

import android.net.Uri;
import android.sax.*;
import android.util.Xml;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.models.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * An XML Handler class to parse the API XML code of a Board. A little bit messy,
 * but a long stare at the code should make it clear.
 */
public class BoardParser extends DefaultHandler {

    public static String TAG = "board";
    public static String DESCRIPTION_TAG = "description";
    public static String NAME_TAG = "name";
    public static String LASTPOST_TAG = "lastpost";
    public static String ID_ATTRIBUTE = "id";
    public static String THREADS_TAG = "threads";
    public static String CURRENT_USER_ID = "current-user-id";
    public static String THREADS_ATTRIBUTE_PAGE = "page";
    public static String IN_CATEGORY_TAG = "in-category";
    public static String IN_CATEGORY_ID_ATTRIBUTE = "id";
    public static String NUMBER_OF_THREADS_TAG = "number-of-threads";
    public static String NUMBER_OF_THREADS_ATTRIBUTE = "value";
    public static String NUMBER_OF_REPLIES_TAG = "number-of-replies";
    public static String NUMBER_OF_REPLIES_ATTRIBUTE = "value";
    public static String URL = "xml/board.php";
    private Post mCurrentPost;
    private User mCurrentUser;
    private Topic mCurrentThread;
    private Board mBoard;

    public BoardParser() {
        mBoard = new Board();
    }

    public static String getUrl(int bid, int page) {
        Uri.Builder b = Uri.parse(URL).buildUpon();
        b.appendQueryParameter("page", String.valueOf(page));
        b.appendQueryParameter("BID", String.valueOf(bid));
        return b.build().toString();
    }

    public Board parse(String input) {
        RootElement board = new RootElement(TAG);

        // find the board information
        board.setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mBoard.setId(Integer.parseInt(attributes.getValue(ID_ATTRIBUTE)));

                if (Integer.parseInt(attributes.getValue(CURRENT_USER_ID)) == 0) {
                    Utils.setNotLoggedIn();
                }
            }
        });
        board.requireChild(NAME_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mBoard.setName(body);
            }
        });
        board.requireChild(DESCRIPTION_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mBoard.setDescription(body);
            }
        });
        board.requireChild(NUMBER_OF_THREADS_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mBoard.setNumberOfThreads(Integer.parseInt(attributes.getValue
                        (NUMBER_OF_THREADS_ATTRIBUTE)));
            }
        });
        board.requireChild(NUMBER_OF_REPLIES_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mBoard.setNumberOfReplies(Integer.parseInt(attributes.getValue
                        (NUMBER_OF_REPLIES_ATTRIBUTE)));
            }
        });
        board.requireChild(IN_CATEGORY_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                Category c = new Category(Integer.parseInt(attributes.getValue
                        (IN_CATEGORY_ID_ATTRIBUTE)));
                mBoard.setCategory(c);
            }
        });

        Element threads = board.getChild(THREADS_TAG);
        threads.setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mBoard.setPage(Integer.parseInt(attributes.getValue(THREADS_ATTRIBUTE_PAGE)));
            }
        });

        Element thread = threads.getChild(TopicParser.TAG);
        thread.setElementListener(new ElementListener() {

            @Override
            public void end() {
                mBoard.addTopic(mCurrentThread);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentThread = new Topic(Integer.parseInt(attributes.getValue(TopicParser
                        .ID_ATTRIBUTE)));
                mCurrentThread.setNumberOfPages(1);
                mCurrentThread.setBoard(mBoard);
            }
        });
        thread.requireChild(TopicParser.TITLE_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mCurrentThread.setTitle(body);
            }
        });
        thread.requireChild(TopicParser.SUBTITLE_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mCurrentThread.setSubTitle(body);
            }
        });
        thread.requireChild(TopicParser.NUMBER_OF_HITS_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setNumberOfHits(Integer.parseInt(attributes.getValue(TopicParser
                        .NUMBER_OF_HITS_ATTRIBUTE)));
            }
        });
        thread.requireChild(TopicParser.NUMBER_OF_REPLIES_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setNumberOfPosts(Integer.parseInt(attributes.getValue(TopicParser
                        .NUMBER_OF_REPLIES_ATTRIBUTE)));
            }
        });
        thread.getChild(TopicParser.NUMBER_OF_PAGES_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setNumberOfPages(Integer.parseInt(attributes.getValue(TopicParser
                        .NUMBER_OF_PAGES_ATTRIBUTE)));
            }
        });
        Element first_post = thread.getChild(TopicParser.FIRSTPOST_TAG).getChild(TopicParser
                .POST_TAG);
        first_post.setElementListener(new ElementListener() {

            @Override
            public void end() {
                mCurrentThread.setFirstPost(mCurrentPost);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentPost = new Post();
                mCurrentPost.setBoard(mBoard);
                mCurrentPost.setTopic(mCurrentThread);
            }
        });
        first_post.requireChild(TopicParser.DATE_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setDateFromTimestamp(Integer.parseInt(attributes.getValue
                        (TopicParser.DATE_TIMESTAMP_ATTRIBUTE)));
            }
        });
        first_post.getChild(TopicParser.ICON_TAG).setTextElementListener(new TextElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setIconId(Integer.parseInt(attributes.getValue(TopicParser
                        .ICON_ATTRIBUTE)));
            }

            @Override
            public void end(String body) {
                mCurrentPost.setIconFile(body);
            }
        });
        first_post.requireChild(TopicParser.USER_TAG).setTextElementListener(new TextElementListener() {

            @Override
            public void end(String body) {
                mCurrentUser.setNick(body);
                mCurrentPost.setAuthor(mCurrentUser);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentUser = new User(Integer.parseInt(attributes.getValue(TopicParser
                        .ID_ATTRIBUTE)));
            }
        });

        Element flags = thread.getChild(TopicParser.FLAGS_TAG);

        flags.requireChild(TopicParser.IS_CLOSED_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setIsClosed(attributes.getValue(TopicParser.IS_CLOSED_ATTRIBUTE)
                        .equals("1"));
            }
        });
        flags.requireChild(TopicParser.IS_STICKY_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setIsSticky(attributes.getValue(TopicParser.IS_STICKY_ATTRIBUTE)
                        .equals("1"));
            }
        });
        flags.requireChild(TopicParser.IS_ANNOUNCEMENT_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setIsAnnouncement(attributes.getValue(TopicParser
                        .IS_ANNOUNCEMENT_ATTRIBUTE).equals("1"));
            }
        });
        flags.requireChild(TopicParser.IS_IMPORTANT_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setIsImportant(attributes.getValue(TopicParser
                        .IS_IMPORTANT_ATTRIBUTE).equals("1"));
            }
        });
        flags.requireChild(TopicParser.IS_GLOBAL_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setIsGlobal(attributes.getValue(TopicParser.IS_GLOBAL_ATTRIBUTE)
                        .equals("1"));
            }
        });

        try {
            Xml.parse(input, board.getContentHandler());
        } catch (SAXException e) {
            Utils.printException(e);
            return null;
        }

        return mBoard;
    }
}
