package com.mde.potdroid3.models;

import android.content.Context;
import com.mde.potdroid3.helpers.DatabaseWrapper;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by oli on 9/13/13.
 */
public class BookmarkList implements Serializable {

    private static final long serialVersionUID = 8L;

    private Integer mNumberOfNewPosts;
    private Context mContext;
    protected DatabaseWrapper mBookmarkDatabase;

    public BookmarkList(Context cx) {
        mContext = cx;

        mBookmarkDatabase = new DatabaseWrapper(mContext);
    }

    public Integer getNumberOfNewPosts() {
        return mNumberOfNewPosts;
    }

    public void setNumberOfNewPosts(Integer numberOfNewPosts) {
        mNumberOfNewPosts = numberOfNewPosts;
    }

    public ArrayList<Bookmark> getBookmarks() {
        return mBookmarkDatabase.getBookmarks();
    }

    public ArrayList<Bookmark> getUnreadBookmarks() {
        ArrayList<Bookmark> unread = new ArrayList<Bookmark>();
        for(Bookmark b : getBookmarks()) {
            if(b.getNumberOfNewPosts() > 0)
                unread.add(b);
        }
        return unread;
    }

    public void refresh(ArrayList<Bookmark> bookmarks, Integer numberOfNewPosts) {
        mNumberOfNewPosts = numberOfNewPosts;
        mBookmarkDatabase.refreshBookmarks(bookmarks);
    }

    public static class Xml {
        public static String TAG = "bookmarks";
        public static String BOOKMARKS_TAG = "bookmarks";
        public static String BOOKMARKS_ATTRIBUTE_USER = "current-user-id";
        public static String BOOKMARKS_ATTRIBUTE_NEW = "newposts";
        public static String BOOKMARKS_ATTRIBUTE_COUNT = "count";

        public static String URL = "xml/bookmarks.php";

        public static String getUrl() {
            return Xml.URL;
        }
    }

}
