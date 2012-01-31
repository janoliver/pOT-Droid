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

package com.mde.potdroid.models;

import java.util.HashMap;

/**
 * The Board model.
 */
public class Board {

    private Integer mId;
    private Integer mNumberOfThreads = 0;
    private Integer mNumberOfReplies = 0;
    private Integer mThreadsPerPage  = 30;
    private String  mName            = "";
    private String  mDescription     = "";
    private HashMap<Integer, Topic[]> mTopics = new HashMap<Integer, Topic[]>();
    private Category mCategory       = null;

    public Board(Integer id) {
        mId = id;
    }

    /**
     * @return the numberOfThreads
     */
    public Integer getNumberOfThreads() {
        return mNumberOfThreads;
    }
    
    /**
     * @return the numberOfPages
     */
    public Integer getNumberOfPages() {
        return  mNumberOfThreads / mThreadsPerPage + 1;
    }

    /**
     * @param numberOfThreads the numberOfThreads to set
     */
    public void setNumberOfThreads(Integer numberOfThreads) {
        mNumberOfThreads = numberOfThreads;
    }

    /**
     * @return the numberOfReplies
     */
    public Integer getNumberOfReplies() {
        return mNumberOfReplies;
    }

    /**
     * @param numberOfReplies the numberOfReplies to set
     */
    public void setNumberOfReplies(Integer numberOfReplies) {
        mNumberOfReplies = numberOfReplies;
    }

    /**
     * @return the threadsPerPage
     */
    public Integer getThreadsPerPage() {
        return mThreadsPerPage;
    }

    /**
     * @param threadsPerPage the threadsPerPage to set
     */
    public void setThreadsPerPage(Integer threadsPerPage) {
        mThreadsPerPage = threadsPerPage;
    }

    /**
     * @return the name
     */
    public String getName() {
        return mName;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        mDescription = description;
    }

    /**
     * @return the topics
     */
    public HashMap<Integer, Topic[]> getTopics() {
        return mTopics;
    }

    /**
     * @param topics the topics to set
     */
    public void setTopics(Integer page, Topic[] topics) {
        mTopics.put(page, topics);
    }

    /**
     * @return the category
     */
    public Category getCategory() {
        return mCategory;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(Category category) {
        mCategory = category;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return mId;
    }

}
