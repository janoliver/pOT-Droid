package com.mde.potdroid.parsers;

import android.sax.*;
import android.util.Xml;
import com.mde.potdroid.models.Board;
import com.mde.potdroid.models.Bookmark;
import com.mde.potdroid.models.Post;
import com.mde.potdroid.models.Topic;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

import static com.mde.potdroid.helpers.Utils.NotLoggedInException;

/**
 * An XML Handler class to parse the API XML code of the bookmarks.php. A little bit messy,
 * but a long stare at the code should make it clear.
 */
public class BookmarkParser extends DefaultHandler {

    public static String TAG = "bookmark";
    public static String NOT_LOGGEDIN_STRING = "<not-logged-in/>";
    public static String BOOKMARK_ATTRIBUTE_ID = "BMID";
    public static String BOOKMARK_ATTRIBUTE_NEW = "newposts";
    public static String BOOKMARK_ATTRIBUTE_POST = "PID";
    public static String THREAD_TAG = "thread";
    public static String THREAD_ATTRIBUTE_ID = "TID";
    public static String THREAD_ATTRIBUTE_CLOSED = "closed";
    public static String THREAD_ATTRIBUTE_PAGES = "pages";
    public static String BOARD_TAG = "board";
    public static String BOARD_ATTRIBUTE_ID = "BID";
    public static String REMOVE_TAG = "token-removebookmark";
    public static String REMOVE_ATTRIBUTE_VALUE = "value";
    public static String ROOT_TAG = "bookmarks";
    public static String BOOKMARKS_TAG = "bookmarks";
    public static String BOOKMARKS_ATTRIBUTE_USER = "current-user-id";
    public static String BOOKMARKS_ATTRIBUTE_NEW = "newposts";
    public static String BOOKMARKS_ATTRIBUTE_COUNT = "count";
    public static String URL = "xml/bookmarks.php";
    private Bookmark mCurrentBookmark;
    private Topic mCurrentTopic;
    private Board mCurrentBoard;

    public BookmarksContainer parse(String input) throws NotLoggedInException {
        // check for login
        if (input.contains(NOT_LOGGEDIN_STRING)) {
            throw new NotLoggedInException();
        }

        RootElement bookmarks = new RootElement(ROOT_TAG);

        final BookmarksContainer container = new BookmarksContainer();

        // find the board information
        bookmarks.setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                container.setNumberOfNewPosts(Integer.parseInt(attributes.getValue
                        (BOOKMARKS_ATTRIBUTE_COUNT)));
            }
        });

        Element bm = bookmarks.getChild(TAG);
        bm.setElementListener(new ElementListener() {

            @Override
            public void end() {
                container.addBookmark(mCurrentBookmark);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentBookmark = new Bookmark(Integer.parseInt(attributes.getValue
                        (BOOKMARK_ATTRIBUTE_ID)));
                mCurrentBookmark.setNumberOfNewPosts(Integer.parseInt(attributes.getValue
                        (BOOKMARK_ATTRIBUTE_NEW)));
                mCurrentBookmark.setLastPost(new Post(Integer.parseInt(attributes.getValue
                        (BOOKMARK_ATTRIBUTE_POST))));
            }
        });
        bm.requireChild(THREAD_TAG).setTextElementListener(new TextElementListener() {

            @Override
            public void end(String body) {
                mCurrentTopic.setTitle(body);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentTopic = new Topic(Integer.parseInt(attributes.getValue
                        (THREAD_ATTRIBUTE_ID)));
                mCurrentTopic.setIsClosed(Integer.parseInt(attributes.getValue
                        (THREAD_ATTRIBUTE_CLOSED)) == 1);
                mCurrentTopic.setNumberOfPages(Integer.parseInt(attributes.getValue
                        (THREAD_ATTRIBUTE_PAGES)));
            }
        });
        bm.requireChild(BOARD_TAG).setTextElementListener(new TextElementListener() {

            @Override
            public void end(String body) {
                mCurrentBoard.setName(body);
                mCurrentTopic.setBoard(mCurrentBoard);
                mCurrentBookmark.setThread(mCurrentTopic);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentBoard = new Board(Integer.parseInt(attributes.getValue
                        (BOARD_ATTRIBUTE_ID)));
            }
        });
        bm.requireChild(REMOVE_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentBookmark.setRemovetoken(attributes.getValue(REMOVE_ATTRIBUTE_VALUE));
            }
        });

        try {
            Xml.parse(input, bookmarks.getContentHandler());
        } catch (SAXException e) {
            return null;
        }

        return container;
    }

    public static class BookmarksContainer {

        private ArrayList<Bookmark> mBookmarks = new ArrayList<Bookmark>();
        private Integer mNumberOfNewPosts;

        public void addBookmark(Bookmark b) {
            mBookmarks.add(b);
        }

        public ArrayList<Bookmark> getBookmarks() {
            return mBookmarks;
        }

        public Integer getNumberOfNewPosts() {
            return mNumberOfNewPosts;
        }

        public void setNumberOfNewPosts(int new_posts) {
            mNumberOfNewPosts = new_posts;
        }
    }
}
