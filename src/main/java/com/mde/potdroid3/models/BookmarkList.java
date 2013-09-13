package com.mde.potdroid3.models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by oli on 9/13/13.
 */
public class BookmarkList implements Serializable {

    private static final long serialVersionUID = 8L;

    private ArrayList<Bookmark> mBookmarks = new ArrayList<Bookmark>();
    private Integer mNumberOfNewPosts;

    public Integer getNumberOfNewPosts() {
        return mNumberOfNewPosts;
    }

    public void setNumberOfNewPosts(Integer numberOfNewPosts) {
        mNumberOfNewPosts = numberOfNewPosts;
    }

    public ArrayList<Bookmark> getBookmarks() {
        return mBookmarks;
    }

    public void setBookmarks(ArrayList<Bookmark> bookmarks) {
        mBookmarks = bookmarks;
    }

    public void addBookmark(Bookmark bookmark) {
        mBookmarks.add(bookmark);
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
