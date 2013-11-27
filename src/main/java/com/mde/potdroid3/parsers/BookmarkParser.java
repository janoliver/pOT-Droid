package com.mde.potdroid3.parsers;

import android.sax.*;
import android.util.Xml;
import com.mde.potdroid3.helpers.Utils;
import com.mde.potdroid3.models.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

public class BookmarkParser extends DefaultHandler {

    private Bookmark mCurrentBookmark;
    private Topic mCurrentTopic;
    private Board mCurrentBoard;

    public BookmarksContainer parse(String input) {
        RootElement bookmarks = new RootElement(BookmarkList.Xml.BOOKMARKS_TAG);

        final BookmarksContainer container = new BookmarksContainer();

        // find the board information
        bookmarks.setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                container.setNumberOfNewPosts(Integer.parseInt(attributes.getValue(BookmarkList.Xml.BOOKMARKS_ATTRIBUTE_COUNT)));
            }
        });

        Element bm = bookmarks.getChild(Bookmark.Xml.TAG);
        bm.setElementListener(new ElementListener() {

            @Override
            public void end() {
                container.addBookmark(mCurrentBookmark);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentBookmark = new Bookmark(Integer.parseInt(attributes.getValue(Bookmark.Xml.BOOKMARK_ATTRIBUTE_ID)));
                mCurrentBookmark.setNumberOfNewPosts(Integer.parseInt(attributes.getValue(Bookmark.Xml.BOOKMARK_ATTRIBUTE_NEW)));
                mCurrentBookmark.setLastPost(new Post(Integer.parseInt(attributes.getValue(Bookmark.Xml.BOOKMARK_ATTRIBUTE_POST))));
            }
        });
        bm.requireChild(Bookmark.Xml.THREAD_TAG).setTextElementListener(new TextElementListener() {

            @Override
            public void end(String body) {
                mCurrentTopic.setTitle(body);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentTopic = new Topic(Integer.parseInt(attributes.getValue(Bookmark.Xml.THREAD_ATTRIBUTE_ID)));
                mCurrentTopic.setIsClosed(Integer.parseInt(attributes.getValue(Bookmark.Xml.THREAD_ATTRIBUTE_CLOSED)) == 1);
                mCurrentTopic.setNumberOfPages(Integer.parseInt(attributes.getValue(Bookmark.Xml.THREAD_ATTRIBUTE_PAGES)));
            }
        });
        bm.requireChild(Bookmark.Xml.BOARD_TAG).setTextElementListener(new TextElementListener() {

            @Override
            public void end(String body) {
                mCurrentBoard.setName(body);
                mCurrentTopic.setBoard(mCurrentBoard);
                mCurrentBookmark.setThread(mCurrentTopic);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentBoard = new Board(Integer.parseInt(attributes.getValue(Bookmark.Xml.BOARD_ATTRIBUTE_ID)));
            }
        });
        bm.requireChild(Bookmark.Xml.REMOVE_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentBookmark.setRemovetoken(attributes.getValue(Bookmark.Xml.REMOVE_ATTRIBUTE_VALUE));
            }
        });

        try {
            Xml.parse(input, bookmarks.getContentHandler());
        } catch (SAXException e) {
            Utils.log(e.getMessage());
            return null;
        }

        return container;
    }

    public static class BookmarksContainer {
        private ArrayList<Bookmark> mBookmarks = new ArrayList<Bookmark>();
        private Integer mNumberOfNewPosts;

        public void setNumberOfNewPosts(int new_posts) {
            mNumberOfNewPosts = new_posts;
        }

        public void addBookmark(Bookmark b) {
            mBookmarks.add(b);
        }

        public ArrayList<Bookmark> getBookmarks() {
            return mBookmarks;
        }

        public Integer getNumberOfNewPosts() {
            return mNumberOfNewPosts;
        }
    }
}
