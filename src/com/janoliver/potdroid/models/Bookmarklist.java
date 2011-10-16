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
 * Bookmark List model.
 */
public class Bookmarklist extends ModelBase {

    private Integer mNumberOfThreads;
    private Integer mUnread;
    private Bookmark[] mBookmarks;

    @Override
    public Boolean parse(Document doc) {
        Element root = doc.getRootElement();

        if (root.getName() == "not-logged-in") {
            return false;
        }

        mNumberOfThreads = new Integer(root.getAttributeValue("count"));
        mUnread = new Integer(root.getAttributeValue("newposts"));

        @SuppressWarnings("unchecked")
        List<Element> bmElements = root.getChildren();
        Bookmark[] bookmarks = new Bookmark[bmElements.size()];
        int i = 0;
        for (Element el : bmElements) {
            Integer threadId = new Integer(el.getChild("thread").getAttributeValue("TID"));
            String elementTitle = el.getChildText("thread");
            Integer unread = new Integer(el.getAttributeValue("newposts"));
            Integer lastPost = new Integer(el.getAttributeValue("PID"));
            Integer lastPage = new Integer(el.getChild("thread").getAttributeValue("pages"));
            Integer elementId = new Integer(el.getAttributeValue("BMID")).intValue();

            Bookmark newBookmark = new Bookmark(elementId);
            Topic thread = new Topic(threadId);

            thread.setPid(lastPost);
            thread.setTitle(elementTitle);
            thread.setLastPage(lastPage);
            newBookmark.setThread(thread);
            newBookmark.setNumberOfNewPosts(unread);
            newBookmark.setRemovetoken(el.getChild("token-removebookmark").getAttributeValue(
                    "value"));
            newBookmark.setLastPost(lastPost);

            bookmarks[i++] = newBookmark;
        }
        mBookmarks = bookmarks;
        return true;
    }

    @Override
    public String getUrl() {
        return PotUtils.BOOKMARK_URL;
    }

    public Integer getUnread() {
        return mUnread;
    }

    public Bookmark[] getBookmarks() {
        return mBookmarks;
    }

    public Integer getNumberOfThreads() {
        return mNumberOfThreads;
    }

}
