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
 * Forum model.
 */
public class Forum {

    Category[] mCategories;

    /**
     * @return the categories
     */
    public Category[] getCategories() {
        return mCategories;
    }

    /**
     * @param categories the categories to set
     */
    public void setCategories(Category[] categories) {
        mCategories = categories;
    }

    
}