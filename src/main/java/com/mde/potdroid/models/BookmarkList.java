package com.mde.potdroid.models;

import android.content.Context;
import com.mde.potdroid.helpers.DatabaseWrapper;

import java.io.Serializable;
import java.util.ArrayList;

public class BookmarkList implements Serializable {

    private static final long serialVersionUID = 8L;

    private Integer mNumberOfNewPosts;
    private Context mContext;
    protected DatabaseWrapper mBookmarkDatabase;

    public BookmarkList(Context cx) {
        mContext = cx;

        mBookmarkDatabase = new DatabaseWrapper(mContext);
    }

    public void clearBookmarksCache() {
        mBookmarkDatabase.clearBookmarks();
    }

    public Integer getNumberOfNewPosts() {
        if(mNumberOfNewPosts == null) {
            mNumberOfNewPosts = 0;
            // try to count them
            for(Bookmark b: getUnreadBookmarks(false)) {
                mNumberOfNewPosts += b.getNumberOfNewPosts();
            }
        }
        return mNumberOfNewPosts;
    }

    public void setNumberOfNewPosts(Integer numberOfNewPosts) {
        mNumberOfNewPosts = numberOfNewPosts;
    }

    public ArrayList<Bookmark> getBookmarks() {
        return mBookmarkDatabase.getBookmarks();
    }

    public ArrayList<Bookmark> getUnreadBookmarks(boolean with_read) {
        ArrayList<Bookmark> unread = new ArrayList<Bookmark>();
        for (Bookmark b : getBookmarks()) {
            if (b.getNumberOfNewPosts() > 0)
                unread.add(b);
        }

        if(with_read)
            for (Bookmark b : getBookmarks()) {
                if (b.getNumberOfNewPosts() == 0)
                    unread.add(b);
            }
        return unread;
    }

    public void refresh(ArrayList<Bookmark> bookmarks, Integer numberOfNewPosts) {
        mNumberOfNewPosts = numberOfNewPosts;
        mBookmarkDatabase.refreshBookmarks(bookmarks);
    }


}
