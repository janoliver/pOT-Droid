package com.mde.potdroid.models;

import android.util.SparseArray;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Forum model.
 */
public class Forum implements Serializable {

    private static final long serialVersionUID = 4L;
    ArrayList<Category> mCategories = new ArrayList<Category>();

    public ArrayList<Category> getCategories() {
        return mCategories;
    }

    public SparseArray<Board> getBoards() {
        SparseArray<Board> b = new SparseArray<Board>();
        for(Category c : mCategories)
            if(c.getBoards() != null)
                for(Board bo : c.getBoards())
                    b.put(bo.getId(), bo);
        return b;
    }

    public void setCategories(ArrayList<Category> categories) {
        mCategories = categories;
    }

    public void addCategory(Category cat) {
        mCategories.add(cat);
    }

}
