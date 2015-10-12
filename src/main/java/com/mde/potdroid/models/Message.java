package com.mde.potdroid.models;

import java.util.Date;

/**
 * Created by oli on 11/9/13.
 */
public class Message {

    private static final long serialVersionUID = 9L;

    private Integer mId;
    private String mTitle;
    private String mText;
    private Date mDate;
    private User mFrom;
    private Boolean mOutgoing;
    private Boolean mUnread;
    private Boolean mSystem = false;
    private String mHtmlCache;
    private String mPostbox;

    public Integer getId() {
        return mId;
    }

    public void setId(Integer id) {
        this.mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public User getFrom() {
        return mFrom;
    }

    public void setFrom(User from) {
        this.mFrom = from;
    }

    public Boolean isUnread() {
        return mUnread;
    }

    public Boolean isOutgoing() {
        return mOutgoing;
    }

    public Boolean isSystem() {
        return mSystem;
    }

    public void setHtmlCache(String cache) {
        mHtmlCache = cache;
    }

    public String getHtmlCache() {
        return mHtmlCache;
    }

    public void setPostbox(String postbox) {
        mPostbox = postbox;
    }

    public String getPostbox() {
        return mPostbox;
    }


    public void setOutgoing(Boolean out) {
        mOutgoing = out;
    }

    public void setUnread(Boolean unread) {
        mUnread = unread;
    }

    public void setSystem(Boolean system) {
        mSystem = system;
    }

    public boolean isReply() {
        return mTitle.length() > 3 && mTitle.substring(0, 3).equals("Re:");
    }

}
