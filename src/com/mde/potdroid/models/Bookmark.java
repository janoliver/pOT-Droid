/*
 * Copyright (C) 2012 mods.de community 
 *
 * Everyone is permitted to copy and distribute verbatim or modified
 * copies of this software, and changing it is allowed as long as the 
 * name is changed.
 *
 *           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
 *  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 *  0. You just DO WHAT THE FUCK YOU WANT TO. 
 */

package com.mde.potdroid.models;

/**
 * The Bookmark model.
 */
public class Bookmark {

    private Integer mId;
    private Integer mNumberOfNewPosts = 0;
    private Post    mLastPost         = null;
    private Topic   mThread           = null;
    private String  mRemovetoken      = "";

    public Bookmark(Integer id) {
        mId = id;
    }

    /**
     * @return the numberOfNewPosts
     */
    public Integer getNumberOfNewPosts() {
        return mNumberOfNewPosts;
    }

    /**
     * @param numberOfNewPosts the numberOfNewPosts to set
     */
    public void setNumberOfNewPosts(Integer numberOfNewPosts) {
        mNumberOfNewPosts = numberOfNewPosts;
    }

    /**
     * @return the lastPost
     */
    public Post getLastPost() {
        return mLastPost;
    }

    /**
     * @param lastPost the lastPost to set
     */
    public void setLastPost(Post lastPost) {
        mLastPost = lastPost;
    }

    /**
     * @return the thread
     */
    public Topic getThread() {
        return mThread;
    }

    /**
     * @param thread the thread to set
     */
    public void setThread(Topic thread) {
        mThread = thread;
    }

    /**
     * @return the removetoken
     */
    public String getRemovetoken() {
        return mRemovetoken;
    }

    /**
     * @param removetoken the removetoken to set
     */
    public void setRemovetoken(String removetoken) {
        mRemovetoken = removetoken;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return mId;
    }

}
