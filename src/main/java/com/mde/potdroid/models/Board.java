package com.mde.potdroid.models;

import android.content.Context;
import com.mde.potdroid.helpers.SettingsWrapper;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The Board model.
 */
public class Board implements Serializable {

    private static final long serialVersionUID = 7L;
    private Integer mId;
    private Integer mNumberOfThreads;
    private Integer mNumberOfReplies;
    private Integer mThreadsPerPage = 30;
    private Integer mPage;
    private String mName;
    private String mDescription;
    private String mNewthreadtoken;
    private ArrayList<Topic> mTopics = new ArrayList<Topic>();
    private Category mCategory;
    private Post mLastPost;

    public Board(Integer id) {
        mId = id;
    }

    public Board() {
    }

    public void addTopic(Topic topic) {
        mTopics.add(topic);
    }

    public Category getCategory() {
        return mCategory;
    }

    public void setCategory(Category category) {
        mCategory = category;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public Integer getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public Post getLastPost() {
        return mLastPost;
    }

    public void setLastPost(Post post) {
        mLastPost = post;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Integer getNumberOfPages() {
        return mNumberOfThreads / mThreadsPerPage + 1;
    }

    public String getNewthreadtoken() {
        return mNewthreadtoken;
    }

    public void setNewthreadtoken(String newthreadtoken) {
        mNewthreadtoken = newthreadtoken;
    }

    public Integer getNumberOfReplies() {
        return mNumberOfReplies;
    }

    public void setNumberOfReplies(Integer numberOfReplies) {
        mNumberOfReplies = numberOfReplies;
    }

    public Integer getNumberOfThreads() {
        return mNumberOfThreads;
    }

    public void setNumberOfThreads(Integer numberOfThreads) {
        mNumberOfThreads = numberOfThreads;
    }

    public Integer getPage() {
        return mPage;
    }

    public void setPage(Integer page) {
        mPage = page;
    }

    public Integer getThreadsPerPage() {
        return mThreadsPerPage;
    }

    public void setThreadsPerPage(Integer threadsPerPage) {
        mThreadsPerPage = threadsPerPage;
    }

    public ArrayList<Topic> getTopics() {
        return mTopics;
    }

    public ArrayList<Topic> getFilteredTopics(Context cx) {
        SettingsWrapper s = new SettingsWrapper(cx);
        if(!s.hideGlobalTopics())
            return mTopics;

        ArrayList<Topic> t = new ArrayList<Topic>();

        for(Topic topic: mTopics) {
            if(!topic.isAnnouncement() && !topic.isGlobal() && !topic.isImportant())
                t.add(topic);
        }
        return t;
    }

    public void setTopics(ArrayList<Topic> topics) {
        mTopics = topics;
    }

    public Boolean isLastPage() {
        return mPage.equals(getNumberOfPages());
    }

}
