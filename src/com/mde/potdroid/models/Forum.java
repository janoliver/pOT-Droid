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

import android.util.SparseArray;


/**
 * Forum model.
 */
public class Forum implements Serializable {

    private static final long serialVersionUID = 4L;
    Category[] mCategories;

    /**
     * @return the categories
     */
    public Category[] getCategories() {
        return mCategories;
    }
    
    /**
     * @return the categories
     */
    public SparseArray<Board> getBoards() {
        SparseArray<Board> b = new SparseArray<Board>();
        for(Category c : mCategories)
            if(c.getBoards() != null)
                for(Board bo : c.getBoards())
                    b.put(bo.getId(), bo);
        return b;
    }

    /**
     * @param categories the categories to set
     */
    public void setCategories(Category[] categories) {
        mCategories = categories;
    }

    
}
