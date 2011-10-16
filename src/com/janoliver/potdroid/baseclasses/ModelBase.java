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

package com.janoliver.potdroid.baseclasses;

import org.jdom.Document;

import android.app.Activity;

import com.janoliver.potdroid.helpers.PotUtils;
import com.janoliver.potdroid.helpers.WebsiteInteraction;

/**
 * Base class for all models. It basically just defines the update method and
 * has some abstracts.
 */
public abstract class ModelBase {

    /**
     * parses the document
     */
    protected abstract Boolean parse(Document doc);

    /**
     * gets the url of the xml file
     */
    protected abstract String getUrl();

    /**
     * Updates the model's information.
     */
    public Boolean update(Activity activity) {
        WebsiteInteraction ws = PotUtils.getWebsiteInteractionInstance(activity);
        Document document = ws.getDocument(getUrl());
        if (document == null) {
            return false;
        }
        return parse(document);
    }
}
