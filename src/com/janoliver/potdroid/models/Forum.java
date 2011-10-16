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
 * Forum model.
 */
public class Forum extends ModelBase {

    Category[] mCatList;

    @Override
    public String getUrl() {
        return PotUtils.FORUM_URL;
    }

    @Override
    public Boolean parse(Document doc) {
        Element root = doc.getRootElement();

        @SuppressWarnings("unchecked")
        List<Element> cats = root.getChildren();

        Category[] categories = new Category[cats.size()];
        int i = 0;
        for (Element el : cats) {
            Integer elementId = null;
            try {
                elementId = el.getAttribute("id").getIntValue();
                Category newCat = new Category(elementId);
                newCat.setName(el.getChildText("name"));
                newCat.setDescription(el.getChildText("description"));
                categories[i++] = newCat;
            } catch (DataConversionException e) {
                // this might be allowed, we just skip this board.
                // hopefully this point --> . <-- is never reached.
                // one could return false here, but should one?
            }

        }
        mCatList = categories;
        return true;
    }

    public Category[] getCatList() {
        return mCatList;
    }

}
