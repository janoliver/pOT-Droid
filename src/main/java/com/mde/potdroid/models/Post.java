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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * Post model.
 */
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Integer mId;
    private Topic mTopic;
    private Board mBoard;
    private String mText;
    private Date mDate;
    private String mTitle;
    private User mAuthor;
    private Integer mEdited;
    private User mLastEditUser;
    private Date mLastEditDate;
    private Integer mIconId;
    private String mIconFile;
    public String mBookmarktoken;
    public String mEdittoken;

    public Date getLastEditDate() {
        return mLastEditDate;
    }

    public void setLastEditDate(Date mLastEditDate) {
        this.mLastEditDate = mLastEditDate;
    }

    public void setLastEditDateFromTimestamp(long timestamp) {
        this.mLastEditDate = new Date(timestamp * 1000);
    }

    public User getLastEditUser() {
        return mLastEditUser;
    }

    public void setLastEditUser(User mLastEditUser) {
        this.mLastEditUser = mLastEditUser;
    }

    public Post(Integer id) {
        mId = id;
    }

    public Post() {}

    public void setText(String text) {
        try {
            mText = new String(text.getBytes(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            mText = text;
        }
    }

    public void setId(Integer id) {
        mId = id;
    }

    public void setTopic(Topic topic) {
        mTopic = topic;
    }

    public void setBoard(Board board) {
        mBoard = board;
    }

    public void setAuthor(User author) {
        mAuthor = author;
    }

    public void setBookmarktoken(String bookmarktoken) {
        mBookmarktoken = bookmarktoken;
    }

    public void setEdittoken(String edittoken) {
        mEdittoken = edittoken;
    }

    public void setDate(Date d) {
        mDate = d;
    }

    public void setDateFromTimestamp(long timestamp) {
        mDate = new Date(timestamp * 1000);
    }

    public void setTitle(String title) {
        mTitle = title;
    }
    
    public void setIconId(Integer icon) {
        mIconId = icon;
    }

    public void setIconFile(String icon) {
        mIconFile = icon;
    }

    public Integer getId() {
        return mId;
    }

    public String getEdittoken() {
        return mEdittoken;
    }

    public Date getDate() {
        return mDate;
    }

    public String getTitle() {
        return mTitle;
    }

    public User getAuthor() {
        return mAuthor;
    }

    public String getBookmarktoken() {
        return mBookmarktoken;
    }

    public String getText() {
        return mText;
    }
    
    public Integer getIconId() {
        return mIconId;
    }

    public String getIconFile() {
        return mIconFile;
    }

    public Topic getTopic() {
        return mTopic;
    }

    public Board getBoard() {
        return mBoard;
    }
    
    public void setEdited(Integer ed) {
        mEdited = ed;
    }
    
    public Integer getEdited() {
        return mEdited;
    }

    public static class Xml {
        public static String ID_ATTRIBUTE = "id";
        public static String TAG = "post";
        public static String MESSAGE_TAG = "message";

        public static String ICON_TAG = "icon";
        public static String ICON_ATTRIBUTE = "id";

        public static String MESSAGE_TITLE_TAG = "title";
        public static String MESSAGE_CONTENT_TAG = "content";
        public static String MESSAGE_EDITED_TAG = "edited";
        public static String MESSAGE_EDITED_ATTRIBUTE = "count";
        public static String MESSAGE_LASTEDIT_TAG = "lastedit";

        public static String DATE_TAG = "date";
        public static String DATE_TIMESTAMP_ATTRIBUTE = "timestamp";

        public static String IN_THREAD_TAG = "in-thread";
        public static String IN_THREAD_ID_ATTRIBUTE = "id";

        public static String TOKEN_SETBOOKMARK_TAG = "token-setbookmark";
        public static String TOKEN_SETBOOKMARK_ATTRIBUTE = "value";

        public static String TOKEN_EDITREPLY_TAG = "token-editreply";
        public static String TOKEN_EDITREPLY_ATTRIBUTE = "value";


    }

}