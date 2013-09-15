package com.mde.potdroid3.models;

import android.content.Context;
import com.mde.potdroid3.helpers.BookmarkDatabase;
import com.mde.potdroid3.helpers.Network;
import com.mde.potdroid3.parsers.BookmarkParser;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by oli on 9/13/13.
 */
public class BookmarkList implements Serializable {

    private static final long serialVersionUID = 8L;

    private ArrayList<Bookmark> mBookmarks = new ArrayList<Bookmark>();
    private Integer mNumberOfNewPosts;
    private Context mContext;
    protected BookmarkDatabase mBookmarkDatabase;

    public BookmarkList(Context cx) {
        mContext = cx;

        mBookmarkDatabase = new BookmarkDatabase(mContext);
    }

    public Integer getNumberOfNewPosts() {
        return mNumberOfNewPosts;
    }

    public void setNumberOfNewPosts(Integer numberOfNewPosts) {
        mNumberOfNewPosts = numberOfNewPosts;
    }

    public ArrayList<Bookmark> getBookmarks() {
        return mBookmarks;
    }

    public ArrayList<Bookmark> getUnreadBookmarks() {
        ArrayList<Bookmark> unread = new ArrayList<Bookmark>();
        for(Bookmark b : mBookmarks) {
            if(b.getNumberOfNewPosts() > 0)
                unread.add(b);
        }
        return unread;
    }

    public void setBookmarks(ArrayList<Bookmark> bookmarks) {
        mBookmarks = bookmarks;
    }

    public void addBookmark(Bookmark bookmark) {
        mBookmarks.add(bookmark);
    }

    public void refresh() throws Network.NoConnectionException {
        Network n = new Network(mContext);
        InputStream xml = n.getDocument(Xml.getUrl());
        BookmarkParser parser = new BookmarkParser();

        BookmarkParser.BookmarksContainer c = parser.parse(xml);
        mBookmarks = c.getBookmarks();
        mNumberOfNewPosts = c.getNumberOfNewPosts();

        mBookmarkDatabase.refresh(this);
    }

    public static class Xml {
        public static String TAG = "bookmarks";
        public static String BOOKMARKS_TAG = "bookmarks";
        public static String BOOKMARKS_ATTRIBUTE_USER = "current-user-id";
        public static String BOOKMARKS_ATTRIBUTE_NEW = "newposts";
        public static String BOOKMARKS_ATTRIBUTE_COUNT = "count";

        public static String URL = "http://forum.mods.de/bb/xml/bookmarks.php";

        public static String getUrl() {
            return Xml.URL;
        }
    }

}
