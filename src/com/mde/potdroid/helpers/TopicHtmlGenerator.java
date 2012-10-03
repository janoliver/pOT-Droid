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

package com.mde.potdroid.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.InputSource;

import ru.perm.kefir.bbcode.BBProcessorFactory;
import ru.perm.kefir.bbcode.TextProcessor;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import biz.source_code.miniTemplator.MiniTemplator;
import biz.source_code.miniTemplator.MiniTemplator.TemplateSpecification;

import com.mde.potdroid.R;
import com.mde.potdroid.models.Post;
import com.mde.potdroid.models.Topic;

/**
 * This class generates the html code for the topic activity. A topic (thread)
 * is a WebView with some locally generated html code. This, together with css
 * and javascript, turned out to be the best method when it comes to theming and
 * quickly adding new interaction features.
 */
public class TopicHtmlGenerator {
    private Topic     mTopic;
    private Resources mResources;
    private Activity  mActivity;
    private Integer   mPage;
    private SharedPreferences mSettings;
    
    // precompiled patterns
    private Pattern   mPatternList1 = Pattern.compile("<ul>(.*?)\\[\\*\\]", Pattern.DOTALL);
    private Pattern   mPatternList2 = Pattern.compile("\\[\\*\\]");
    private Pattern   mPatternList3 = Pattern.compile("</ul>");
    private Pattern   mPatternImage = Pattern.compile("<img src=\"([^#]*?)\" />");
    private Pattern   mPatternQuote = Pattern.compile("HEAD(.*?)CONTENT");
    private Pattern   mPatternCase  = Pattern.compile("\\[[/]?[A-Z]+(.*?)\\]");
    
    private HashMap<String, String> mSmileys = new HashMap<String, String>();

    public TopicHtmlGenerator(Topic topic, Integer page, Activity callingActivity) {
        
        mTopic     = topic;
        mResources = callingActivity.getResources();
        mActivity  = callingActivity;
        mPage      = page;
        mSettings  = PreferenceManager.getDefaultSharedPreferences(mActivity);

        // smileys
        mSmileys.put(":bang:", "banghead.gif");
        mSmileys.put(":D", "biggrin.gif");
        mSmileys.put(":confused:", "confused.gif");
        mSmileys.put(":huch:", "freaked.gif");
        mSmileys.put(":hm:", "hm.gif");
        mSmileys.put(":mata:", "mata.gif");
        mSmileys.put(":what:", "sceptic.gif");
        mSmileys.put(":moo:", "smiley-pillepalle.gif");
        mSmileys.put(":wurgs:", "urgs.gif");
        mSmileys.put(";)", "wink.gif");
        mSmileys.put(":zyklop:", "icon1.gif");
        mSmileys.put(":P", "icon2.gif");
        mSmileys.put("^^", "icon5.gif");
        mSmileys.put(":)", "icon7.gif");
        mSmileys.put(":|", "icon8.gif");
        mSmileys.put(":(", "icon12.gif");
        mSmileys.put(":mad:", "icon13.gif");
        mSmileys.put(":eek:", "icon15.gif");
        mSmileys.put(":o", "icon16.gif");
        mSmileys.put(":roll:", "icon18.gif");
    }

    /**
     * Returns html code of the topic.
     */
    public String buildTopic() throws IOException {

        Post[] posts = mTopic.getPosts().get(mPage);

        // Check for preferred style and set it accordingly
        String cssFile = mSettings.getString("style", "threadcss_bbstyle");

        // compatibility hack
        if (cssFile.equals("1") || cssFile.equals("2") || cssFile.equals("3")) {
            cssFile = "threadcss_bbcode";
        }
        
        // generate template specs
        TemplateSpecification tplSpecs = new TemplateSpecification();

        // get post html template
        InputStream template     = mResources.getAssets().open("thread.html");
        tplSpecs.templateText    = PotUtils.inputStreamToString(template);
        MiniTemplator t          = new MiniTemplator(tplSpecs);
        
        // build the topic
        t.setVariable("css", cssFile);
        
        for (int i = 0; i < posts.length; i++) {
            t.setVariable("number", i);
            t.setVariable("postId", posts[i].getId());
            t.setVariable("author", posts[i].getAuthor().getNick());
            t.setVariable("date", posts[i].getDate());
            t.setVariable("postText", postToHtml(posts[i]));
            t.setVariable("postTitle", posts[i].getTitle());
            
            if (posts[i].getAuthor().getId() == PotUtils.getObjectManagerInstance(mActivity)
                    .getCurrentUser().getId())
                t.setVariableOpt("currentUser", "curruser");
            
            if ((posts[i].getId() > mTopic.getPid()) && mSettings.getBoolean("marknewposts", false))
                t.setVariableOpt("newPost", "newpost");
            
            t.addBlock("post");
        }
        
        String htmlCode = t.generateOutput();
        
        htmlCode = parseQuotes(htmlCode);
        htmlCode = parseImages(htmlCode);
        htmlCode = parseLists(htmlCode);
        htmlCode = parseSmileys(htmlCode);
        
        return htmlCode;
    }

    /**
     * Replaces all emoticon codes with the smiley images.
     */
    private String parseSmileys(String code) {
        Iterator<Map.Entry<String, String>> i = mSmileys.entrySet().iterator();

        while (i.hasNext()) {
            Map.Entry<String, String> me = i.next();
            code = code.replace(me.getKey(),
                    "<img src=\"file:///android_asset/smileys/" + me.getValue() + "\" />");
        }
        return code;
    }

    /**
     * Returns parsed lists.
     */
    private String parseLists(String code) {
        code = mPatternList1.matcher(code).replaceAll("<ul><li>");
        code = mPatternList2.matcher(code).replaceAll("</li><li>");
        code = mPatternList3.matcher(code).replaceAll("</li></ul>");
        
        return code;
    }

    /**
     * replaces, if the setting is right, all images with buttons to show it
     */
    private String parseImages(String code) {
        int loadImages = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(mActivity)
                .getString("loadImages", "0")); 

        if ((loadImages == 0)
                || ((WebsiteInteraction.getConnectionType(mActivity) == 2) && (loadImages == 1))) {
            
            final Matcher matcher = mPatternImage.matcher(code);
            while (matcher.find()) {
                final MatchResult matchResult = matcher.toMatchResult();
                final String replacement = "<input type=\"button\" value=\"Bild anzeigen.\" "
                        + "class=\"loadimage\" alt=\"" + matchResult.group(1) + "\" />";
                code = code.substring(0, matchResult.start()) + replacement
                        + code.substring(matchResult.end());
                matcher.reset(code);
            }
        }
        return code;
    }

    /**
     * Parses quotes.
     */
    private String parseQuotes(String code) {
        final Matcher matcher = mPatternQuote.matcher(code);
        while (matcher.find()) {
            final MatchResult matchResult = matcher.toMatchResult();
            final String replacement = parseQuoteHead(matchResult.group(1));
            code = code.substring(0, matchResult.start()) + replacement
                    + code.substring(matchResult.end());
            matcher.reset(code);
        }
        return code;
    }

    /**
     * Takes care of the case, where no author is present in the quote header.
     */
    private String parseQuoteHead(String head) {
        String[] attrs = head.split(",", 3);

        if (attrs.length < 3) {
            return head;
        }
        attrs[2] = attrs[2].replaceAll("\"", "");

        return "<div class=\"author\">" + attrs[2] + "</div>";
    }

    /**
     * Parses the post content itself. Uses KefirBB.
     */
    private String postToHtml(Post post) {
        String text = post.getText();
        
        // convert tags to lower case
        final Matcher matcher = mPatternCase.matcher(text);
        while (matcher.find()) {
            final MatchResult matchResult = matcher.toMatchResult();
            final String replacement = matchResult.group(0).toLowerCase();
            text = text.substring(0, matchResult.start()) + replacement
                    + text.substring(matchResult.end());
            matcher.reset(text);
        }

        InputSource bbcodeConf = new InputSource(mResources.openRawResource(R.raw.bbcode));
        TextProcessor proc = BBProcessorFactory.getInstance().create(bbcodeConf.getByteStream());
        return proc.process(text.replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br />"));
    }

}