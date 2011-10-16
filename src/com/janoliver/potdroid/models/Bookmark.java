/*
 * Copyright (C) 2011 Jan Oliver Oelerich <janoliver@oelerich.org>
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

package com.janoliver.potdroid.models;

/**
 * The Bookmark model.
 */
public class Bookmark {

    private Integer mId;
    private Integer mNumberOfNewPosts;
    private Integer mLastPost;
    private Topic mThread;
    private String mRemovetoken;

    public Bookmark(Integer id) {
        mId = id;
    }

    public void setId(Integer id) {
        mId = id;
    }

    public void setNumberOfNewPosts(Integer numberOfNewPosts) {
        mNumberOfNewPosts = numberOfNewPosts;
    }

    public void setLastPost(Integer lastPost) {
        mLastPost = lastPost;
    }

    public void setThread(Topic thread) {
        mThread = thread;
    }

    public void setRemovetoken(String removetoken) {
        mRemovetoken = removetoken;
    }

    public Integer getLastPost() {
        return mLastPost;
    }

    public Topic getThread() {
        return mThread;
    }

    public String getRemovetoken() {
        return mRemovetoken;
    }

    public Integer getId() {
        return mId;
    }

    public Integer getNumberOfNewPosts() {
        return mNumberOfNewPosts;
    }

}
