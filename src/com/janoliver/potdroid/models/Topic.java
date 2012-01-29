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

import java.util.HashMap;

/**
 * Thread model.
 */
public class Topic {

    private Integer mId;
    private Integer mNumberOfPosts   = 0;
    private Integer mNumberOfHits    = 0;
    private Integer mPid             = 0;
    private Integer mLastPage        = 1;
    private Integer mPostsPerPage    = 30;
    private String  mTitle           = "";
    private String  mSubTitle        = "";
    private Board   mBoard           = null;
    private User    mAuthor          = null;
    private Boolean mIsImportant     = false;
    private Boolean mIsClosed        = false;
    private Boolean mIsAnnouncement  = false;
    private Boolean mIsGlobal        = false;
    private Boolean mIsSticky        = false;
    private String  mNewreplytoken   = "";
    private HashMap<Integer, Post[]> mPosts = new HashMap<Integer, Post[]>();

    // constructor for TopicActivity
    public Topic(Integer id) {
        mId = id;
    }

    /**
     * @return the numberOfPosts
     */
    public Integer getNumberOfPosts() {
        return mNumberOfPosts;
    }

    /**
     * @param numberOfPosts the numberOfPosts to set
     */
    public void setNumberOfPosts(Integer numberOfPosts) {
        mNumberOfPosts = numberOfPosts;
    }

    /**
     * @return the numberOfHits
     */
    public Integer getNumberOfHits() {
        return mNumberOfHits;
    }

    /**
     * @param numberOfHits the numberOfHits to set
     */
    public void setNumberOfHits(Integer numberOfHits) {
        mNumberOfHits = numberOfHits;
    }

    /**
     * @return the pid
     */
    public Integer getPid() {
        return mPid;
    }

    /**
     * @param pid the pid to set
     */
    public void setPid(Integer pid) {
        mPid = pid;
    }

    /**
     * @return the lastPage
     */
    public Integer getLastPage() {
        return mLastPage;
    }

    /**
     * @param lastPage the lastPage to set
     */
    public void setLastPage(Integer lastPage) {
        mLastPage = lastPage;
    }

    /**
     * @return the postsPerPage
     */
    public Integer getPostsPerPage() {
        return mPostsPerPage;
    }

    /**
     * @param postsPerPage the postsPerPage to set
     */
    public void setPostsPerPage(Integer postsPerPage) {
        mPostsPerPage = postsPerPage;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        mTitle = title;
    }

    /**
     * @return the subTitle
     */
    public String getSubTitle() {
        return mSubTitle;
    }

    /**
     * @param subTitle the subTitle to set
     */
    public void setSubTitle(String subTitle) {
        mSubTitle = subTitle;
    }

    /**
     * @return the board
     */
    public Board getBoard() {
        return mBoard;
    }

    /**
     * @param board the board to set
     */
    public void setBoard(Board board) {
        mBoard = board;
    }
    
    /**
     * @return the author
     */
    public User getAuthor() {
        return mAuthor;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(User author) {
        mAuthor = author;
    }

    /**
     * @return the isImportant
     */
    public Boolean isImportant() {
        return mIsImportant;
    }

    /**
     * @param isImportant the isImportant to set
     */
    public void setIsImportant(Boolean isImportant) {
        mIsImportant = isImportant;
    }

    /**
     * @return the isClosed
     */
    public Boolean isClosed() {
        return mIsClosed;
    }

    /**
     * @param isClosed the isClosed to set
     */
    public void setIsClosed(Boolean isClosed) {
        mIsClosed = isClosed;
    }

    /**
     * @return the isAnnouncement
     */
    public Boolean isAnnouncement() {
        return mIsAnnouncement;
    }

    /**
     * @param isAnnouncement the isAnnouncement to set
     */
    public void setIsAnnouncement(Boolean isAnnouncement) {
        mIsAnnouncement = isAnnouncement;
    }

    /**
     * @return the isGlobal
     */
    public Boolean isGlobal() {
        return mIsGlobal;
    }

    /**
     * @param isGlobal the isGlobal to set
     */
    public void setIsGlobal(Boolean isGlobal) {
        mIsGlobal = isGlobal;
    }
    
    /**
     * @return the isSticky
     */
    public Boolean isSticky() {
        return mIsSticky;
    }

    /**
     * @param isGlobal the isSticky to set
     */
    public void setIsSticky(Boolean isSticky) {
        mIsSticky = isSticky;
    }

    /**
     * @return the newreplytoken
     */
    public String getNewreplytoken() {
        return mNewreplytoken;
    }

    /**
     * @param newreplytoken the newreplytoken to set
     */
    public void setNewreplytoken(String newreplytoken) {
        mNewreplytoken = newreplytoken;
    }

    /**
     * @return the posts
     */
    public HashMap<Integer, Post[]> getPosts() {
        return mPosts;
    }

    /**
     * @param posts the posts to set
     */
    public void setPosts(Integer page, Post[] posts) {
        mPosts.put(page, posts);
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return mId;
    }

    
}