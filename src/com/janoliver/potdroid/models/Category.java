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

import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;

import com.janoliver.potdroid.baseclasses.ModelBase;
import com.janoliver.potdroid.helpers.PotUtils;

/**
 * Forum category model.
 */
public class Category extends ModelBase {

    private Integer mId;
    private String mName;
    private String mDescription;
    private Board[] mBoardList;

    public Category(Integer id) {
        mId = id;
    }

    @Override
    public Boolean parse(Document doc) {
        Element cat = getCategoryElement(doc);
        mName = cat.getChildText("name");
        mDescription = cat.getChildText("description");

        @SuppressWarnings("unchecked")
        List<Element> boardElements = cat.getChild("boards").getChildren();
        Board[] boards = new Board[boardElements.size()];
        int i = 0;
        for (Element el : boardElements) {
            try {
                int elementId = el.getAttribute("id").getIntValue();
                Board newBoard = new Board(elementId);
                newBoard.setName(el.getChildText("name"));
                newBoard.setDescription(el.getChildText("description"));
                newBoard.setCategory(this);
                boards[i++] = newBoard;

            } catch (DataConversionException e) {
                // do nothing here.
            }

        }
        mBoardList = boards;
        return true;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    /**
     * Takes the Document doc of the forum start page as argument and extracts
     * the part of the document which refers to this category (this.id)
     */
    public Element getCategoryElement(Document doc) {
        Element root = doc.getRootElement();

        @SuppressWarnings("unchecked")
        List<Element> cats = root.getChildren();

        for (Element el : cats) {
            try {
                int elementId = el.getAttribute("id").getIntValue();
                if (elementId == mId) {
                    return el;
                }
            } catch (DataConversionException e) {
                // hmmm...
            }

        }

        return null;
    }

    @Override
    public String getUrl() {
        return PotUtils.FORUM_URL;
    }

    public Integer getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public Board[] getBoardList() {
        return mBoardList;
    }

}
