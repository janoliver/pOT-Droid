package com.mde.potdroid.parsers;

import android.sax.*;
import android.util.Xml;
import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.models.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * An XML Handler class to parse the API XML code of the boards.php. A little bit messy,
 * but a long stare at the code should make it clear.
 */
public class ForumParser extends DefaultHandler {

    public static String TAG = "category";
    public static String BOARDS_TAG = "boards";
    public static String DESCRIPTION_TAG = "description";
    public static String NAME_TAG = "name";
    public static String ID_ATTRIBUTE = "id";
    public static String FORUM_TAG = "categories";
    public static String URL = "xml/boards.php";
    private Category mCurrentCategory;
    private Board mCurrentBoard;
    private Post mCurrentPost;
    private User mCurrentUser;
    private Topic mCurrentThread;
    private Forum mForum;

    public ForumParser() {
        mForum = new Forum();
    }

    public Forum parse(String input) {
        RootElement categories = new RootElement(FORUM_TAG);

        categories.setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                if (Integer.parseInt(attributes.getValue(BoardParser.CURRENT_USER_ID)) == 0) {
                    Utils.setNotLoggedIn();
                }
            }
        });

        Element category = categories.getChild(TAG);
        category.setElementListener(new ElementListener() {

            @Override
            public void end() {
                mForum.addCategory(mCurrentCategory);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentCategory = new Category(Integer.parseInt(attributes.getValue
                        (ID_ATTRIBUTE)));
            }
        });
        category.requireChild(NAME_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mCurrentCategory.setName(body);
            }
        });
        category.requireChild(DESCRIPTION_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mCurrentCategory.setDescription(body);
            }
        });

        Element board = category.getChild(BOARDS_TAG).getChild(BoardParser.TAG);
        board.setElementListener(new ElementListener() {

            @Override
            public void end() {
                mCurrentCategory.addBoard(mCurrentBoard);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentBoard = new Board(Integer.parseInt(attributes.getValue(BoardParser
                        .ID_ATTRIBUTE)));
                mCurrentBoard.setCategory(mCurrentCategory);
            }
        });
        board.requireChild(BoardParser.NAME_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mCurrentBoard.setName(body);
            }
        });
        board.requireChild(BoardParser.DESCRIPTION_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mCurrentBoard.setDescription(body);
            }
        });
        board.requireChild(BoardParser.NUMBER_OF_THREADS_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentBoard.setNumberOfThreads(Integer.parseInt(attributes.getValue(BoardParser
                        .NUMBER_OF_THREADS_ATTRIBUTE)));
            }
        });
        board.requireChild(BoardParser.NUMBER_OF_REPLIES_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentBoard.setNumberOfReplies(Integer.parseInt(attributes.getValue(BoardParser
                        .NUMBER_OF_REPLIES_ATTRIBUTE)));
            }
        });

        // the last post part
        Element last_post = board.getChild(BoardParser.LASTPOST_TAG).getChild(TopicParser.POST_TAG);
        last_post.setElementListener(new ElementListener() {

            @Override
            public void end() {
                //mCurrentBoard.setLastPost(mCurrentPost);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentPost = new Post();
                mCurrentPost.setBoard(mCurrentBoard);
            }
        });
        last_post.requireChild(TopicParser.USER_TAG).setTextElementListener(new TextElementListener() {

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
        last_post.requireChild(TopicParser.DATE_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setDateFromTimestamp(Integer.parseInt(attributes.getValue
                        (TopicParser.DATE_TIMESTAMP_ATTRIBUTE)));
            }
        });
        last_post.requireChild(TopicParser.IN_THREAD_TAG).setTextElementListener(new TextElementListener() {

            @Override
            public void end(String body) {
                mCurrentThread.setTitle(body);
                mCurrentPost.setTopic(mCurrentThread);
                mCurrentBoard.setLastPost(mCurrentPost);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentThread = new Topic(Integer.parseInt(attributes.getValue(TopicParser
                        .IN_THREAD_ID_ATTRIBUTE)));
            }
        });


        try {
            Xml.parse(input, categories.getContentHandler());
        } catch (SAXException e) {
            Utils.printException(e);
            return null;
        }
        return mForum;
    }
}
