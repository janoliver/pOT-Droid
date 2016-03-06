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
 * User model.
 */
public class User {

    private Integer mId;
    private String  mNick   = ""; 
    private String  mAvatar = "";
    private Integer mGroup  = 0;

    public User(Integer id) {
        mId = id; 
    }

    /**
     * @return the nick
     */
    public String getNick() {
        return mNick;
    }

    /**
     * @param nick the nick to set
     */
    public void setNick(String nick) {
        mNick = nick;
    }

    /**
     * @return the avatar
     */
    public String getAvatar() {
        return mAvatar;
    }

    /**
     * @param avatar the avatar to set
     */
    public void setAvatar(String avatar) {
        mAvatar = avatar;
    }

    /**
     * @return the group
     */
    public Integer getGroup() {
        return mGroup;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(Integer group) {
        mGroup = group;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return mId;
    }

}
