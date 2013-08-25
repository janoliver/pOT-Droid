package com.mde.potdroid3.models;

import java.io.Serializable;

/**
 * The Bookmark model.
 */
public class Bookmark implements Serializable {

    private static final long serialVersionUID = 6L;
    
    private Integer mId;
    private Integer mNumberOfNewPosts = 0;
    private Post mLastPost = null;
    private Topic mThread = null;
    private String  mRemovetoken = "";

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

}
