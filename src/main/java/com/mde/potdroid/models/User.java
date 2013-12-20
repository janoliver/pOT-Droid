package com.mde.potdroid.models;

import java.io.Serializable;

/**
 * User model.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 3L;

    private Integer mId;
    private String  mNick;
    private String  mAvatarFile;
    private Integer mAvatarId;
    private Integer mGroup;

    public User(Integer id) {
        mId = id; 
    }

    public String getNick() {
        return mNick;
    }

    public void setNick(String nick) {
        mNick = nick;
    }

    public String getAvatarFile() {
        return mAvatarFile;
    }

    public void setAvatarFile(String avatar) {
        mAvatarFile = avatar;
    }

    public Integer getAvatarId() {
        return mAvatarId;
    }

    public void setAvatarId(Integer id) {
        mAvatarId = id;
    }

    public Integer getGroup() {
        return mGroup;
    }

    public void setGroup(Integer group) {
        mGroup = group;
    }

    public Integer getId() {
        return mId;
    }

    public static class Xml {
        public static String TAG = "user";
        public static String ID_ATTRIBUTE = "id";
        public static String AVATAR_TAG = "avatar";
        public static String AVATAR_ATTRIBUTE = "id";

    }

}
