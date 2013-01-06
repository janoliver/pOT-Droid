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


/**
 * Forum category model.
 */
public class Category implements Serializable {

    private static final long serialVersionUID = 5L;
    
    private Integer mId;
    private String  mName        = "";
    private String  mDescription = "";
    private Board[] mBoards;

    public Category(Integer id) {
        mId = id;
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
     * @return the boards
     */
    public Board[] getBoards() {
        return mBoards;
    }

    /**
     * @param boards the boards to set
     */
    public void setBoards(Board[] boards) {
        mBoards = boards;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return mId;
    }
}
