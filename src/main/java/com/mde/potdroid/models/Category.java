package com.mde.potdroid.models;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Forum category model.
 */
public class Category implements Serializable {

    private static final long serialVersionUID = 5L;
    
    private Integer mId;
    private String  mName = "";
    private String  mDescription = "";
    private ArrayList<Board> mBoards = new ArrayList<Board>();

    public Category(Integer id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public ArrayList<Board> getBoards() {
        return mBoards;
    }

    public void setBoards(ArrayList<Board> boards) {
        mBoards = boards;
    }

    public void addBoard(Board board) {
        mBoards.add(board);
    }

    public Integer getId() {
        return mId;
    }

}
