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

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import com.janoliver.potdroid.baseclasses.ModelBase;
import com.janoliver.potdroid.helpers.PotUtils;

/**
 * Thread model.
 */
public class Topic extends ModelBase {

    private Integer mId;
    private Integer mPage;
    private Integer mNumberOfPosts;
    private Integer mPid;
    private Integer mLastPage;
    private Integer mPostsPerPage = 30;
    private String mTitle;
    private String mSubTitle;
    private Post[] mPostList;
    private Board mBoard;
    public Boolean mIsImportant = false;
    public Boolean mIsClosed = false;
    public Boolean mIsAnnouncement = false;
    public Boolean mIsGlobal = false;
    public String mNewreplytoken = "";

    // constructor for TopicActivity
    public Topic(Integer id) {
        mId = id;
        mPid = 0;
        mPage = 1;
    }

    @Override
    public Boolean parse(Document doc) {
        Element root = doc.getRootElement();
        Element flags = root.getChild("flags");
        String numberOfPostsString = root.getChild("number-of-replies").getAttributeValue("value");
        @SuppressWarnings("unchecked")
        List<Element> threadElements = root.getChild("posts").getChildren();
        Post[] posts = new Post[threadElements.size()];

        // some static information of the thread
        mTitle = root.getChildText("title");
        mSubTitle = root.getChildText("subtitle");
        mNumberOfPosts = new Integer(numberOfPostsString).intValue() + 1;
        mIsClosed = flags.getChild("is-closed").getAttributeValue("value").equals("1");
        mIsImportant = flags.getChild("is-important").getAttributeValue("value").equals("1");
        mIsAnnouncement = flags.getChild("is-announcement").getAttributeValue("value").equals("1");
        mIsGlobal = flags.getChild("is-global").getAttributeValue("value").equals("1");
        mPage = new Integer(root.getChild("posts").getAttributeValue("page")).intValue();
        mLastPage = (int) Math.ceil((double) mNumberOfPosts / mPostsPerPage);
        mNewreplytoken = root.getChild("token-newreply").getAttributeValue("value");

        mBoard = new Board(new Integer(root.getChild("in-board").getAttributeValue("id")));

        int elementId, i = 0;
        for (Element el : threadElements) {
            elementId = new Integer(el.getAttributeValue("id"));
            Post newPost = new Post(elementId);
            User author = new User(new Integer(el.getChild("user").getAttributeValue("id")),
                    el.getChildText("user"));

            newPost.setId(elementId);
            newPost.setAuthor(author);
            newPost.setDate(el.getChildText("date"));
            newPost.setText(el.getChild("message").getChildText("content"));
            newPost.setTitle(el.getChild("message").getChildText("title"));
            newPost.setBookmarktoken(el.getChild("token-setbookmark").getAttributeValue("value"));
            if (el.getChild("token-editreply") != null) {
                newPost.setEdittoken(el.getChild("token-editreply").getAttributeValue("value"));
            }
            posts[i++] = newPost;
        }
        mPostList = posts;
        return true;
    }

    @Override
    public String getUrl() {
        if ((mPid > 0) && (mPage == 0)) {
            return PotUtils.THREAD_URL_BASE + mId + "&PID=" + mPid;
        } else {
            return PotUtils.THREAD_URL_BASE + mId + "&page=" + mPage;
        }
    }

    public void setPage(Integer page) {
        mPage = page;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setSubTitle(String subTitle) {
        mSubTitle = subTitle;
    }

    public void setBoard(Board board) {
        mBoard = board;
    }

    public void setIsImportant(Boolean isImportant) {
        mIsImportant = isImportant;
    }

    public void setIsClosed(Boolean isClosed) {
        mIsClosed = isClosed;
    }

    public void setIsAnnouncement(Boolean isAnnouncement) {
        mIsAnnouncement = isAnnouncement;
    }

    public void setIsGlobal(Boolean isGlobal) {
        mIsGlobal = isGlobal;
    }

    public void setPid(Integer pid) {
        mPid = pid;
    }

    public void setLastPage(Integer lastPage) {
        mLastPage = lastPage;
    }

    public Integer getId() {
        return mId;
    }

    public String getNewreplytoken() {
        return mNewreplytoken;
    }

    public Integer getPid() {
        return mPid;
    }

    public Post[] getPostList() {
        return mPostList;
    }

    public Integer getLastPage() {
        return mLastPage;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSubTitle() {
        return mSubTitle;
    }

    public Boolean isImportant() {
        return mIsImportant;
    }

    public Integer getPage() {
        return mPage;
    }

    public Integer getPostsPerPage() {
        return mPostsPerPage;
    }

    public Integer getNumberOfPosts() {
        return mNumberOfPosts;
    }
}