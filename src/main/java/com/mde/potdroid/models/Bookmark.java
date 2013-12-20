package com.mde.potdroid.models;

import java.io.Serializable;

/**
 * The Bookmark model.
 */
public class Bookmark implements Serializable {

    private static final long serialVersionUID = 6L;
    
    private Integer mId;
    private Integer mNumberOfNewPosts;
    private Post mLastPost;
    private Topic mThread;
    private String  mRemovetoken;

    public Bookmark(Integer id) {
        mId = id;
    }

    public Integer getNumberOfNewPosts() {
        return mNumberOfNewPosts;
    }

    public void setNumberOfNewPosts(Integer numberOfNewPosts) {
        mNumberOfNewPosts = numberOfNewPosts;
    }

    public Post getLastPost() {
        return mLastPost;
    }

    public void setLastPost(Post lastPost) {
        mLastPost = lastPost;
    }

    public Topic getThread() {
        return mThread;
    }

    public void setThread(Topic thread) {
        mThread = thread;
    }

    public String getRemovetoken() {
        return mRemovetoken;
    }

    public void setRemovetoken(String removetoken) {
        mRemovetoken = removetoken;
    }

    public Integer getId() {
        return mId;
    }

    public static class Xml {
        public static String TAG = "bookmark";
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

    }

}
