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

import org.jdom.Document;
import org.jdom.Element;

import com.janoliver.potdroid.baseclasses.ModelBase;
import com.janoliver.potdroid.helpers.PotUtils;

/**
 * The Board model.
 */
public class Board extends ModelBase {

    private Integer mId;
    private Integer mPage;
    private Integer mNumberOfThreads;
    private Integer mThreadsPerPage = 30;
    private String mName;
    private String mDescription;
    private Topic[] mThreadList;
    private Category mCategory;

    public Board(Integer id) {
        mId = id;
    }

    @Override
    public Boolean parse(Document doc) {
        Element root = doc.getRootElement();

        // some static information on the board
        mName = root.getChildText("name");
        mDescription = root.getChildText("description");

        String numberOfThreadsString = root.getChild("number-of-threads")
                .getAttributeValue("value");
        mNumberOfThreads = new Integer(numberOfThreadsString).intValue();

        // check if category is already set, if not, set it
        if (mCategory == null) {
            Integer catId = new Integer(root.getAttributeValue("in-category")).intValue();
            mCategory = new Category(catId);
        }

        @SuppressWarnings("unchecked")
        List<Element> threadElements = root.getChild("threads").getChildren();

        Topic[] threads = new Topic[threadElements.size()];
        int i = 0;
        for (Element el : threadElements) {
            Topic newThread = new Topic(new Integer(el.getAttributeValue("id")));
            newThread.setTitle(el.getChildText("title"));
            newThread.setSubTitle(el.getChildText("subtitle"));
            newThread.setBoard(this);
            newThread.setLastPage(new Integer(el.getChild("number-of-pages").getAttributeValue(
                    "value")));

            Element flags = el.getChild("flags");
            newThread.setIsClosed(flags.getChild("is-closed").getAttributeValue("value")
                    .equals("1"));
            newThread.setIsImportant(flags.getChild("is-important").getAttributeValue("value")
                    .equals("1"));
            newThread.setIsAnnouncement(flags.getChild("is-announcement")
                    .getAttributeValue("value").equals("1"));
            newThread.setIsGlobal(flags.getChild("is-global").getAttributeValue("value")
                    .equals("1"));

            threads[i++] = newThread;
        }
        mThreadList = threads;
        return true;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setCategory(Category category) {
        mCategory = category;
    }

    @Override
    public String getUrl() {
        return PotUtils.BOARD_URL_BASE + mId + "&page=" + mPage;
    }

    public void setPage(Integer page) {
        mPage = page;
    }

    public String getName() {
        return mName;
    }

    public Topic[] getThreadList() {
        return mThreadList;
    }

    public Integer getNumberOfThreads() {
        return mNumberOfThreads;
    }

    public Integer getThreadsPerPage() {
        return mThreadsPerPage;
    }

    public Integer getPage() {
        return mPage;
    }

    public Integer getId() {
        return mId;
    }

    public Category getCategory() {
        return mCategory;
    }

    public String getDescription() {
        return mDescription;
    }

}
