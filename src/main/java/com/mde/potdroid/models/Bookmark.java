package com.mde.potdroid.models;

import java.io.Serializable;

/**
 * The Bookmark model.
 */
public class Bookmark implements Serializable
{

    private static final long serialVersionUID = 6L;
    private Integer mId;
    private Integer mNumberOfNewPosts;
    private Post mLastPost;
    private Topic mThread;
    private String mRemovetoken;

    public Bookmark(Integer id) {
        mId = id;
    }

    public Integer getId() {
        return mId;
    }

    public Post getLastPost() {
        return mLastPost;
    }

    public void setLastPost(Post lastPost) {
        mLastPost = lastPost;
    }

    public Integer getNumberOfNewPosts() {
        return mNumberOfNewPosts;
    }

    public void setNumberOfNewPosts(Integer numberOfNewPosts) {
        mNumberOfNewPosts = numberOfNewPosts;
    }

    public String getRemovetoken() {
        return mRemovetoken;
    }

    public void setRemovetoken(String removetoken) {
        mRemovetoken = removetoken;
    }

    public Topic getThread() {
        return mThread;
    }

    public void setThread(Topic thread) {
        mThread = thread;
    }


}
