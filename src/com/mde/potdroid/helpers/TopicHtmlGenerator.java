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

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import biz.source_code.miniTemplator.MiniTemplator;
import biz.source_code.miniTemplator.MiniTemplator.TemplateSpecification;
import biz.source_code.miniTemplator.MiniTemplator.VariableNotDefinedException;

import com.mde.potdroid.helpers.BBCodeParser.UnknownErrorException;
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
    private BBCodeParser mParser;
    private SharedPreferences mSettings;
    private Pattern   mPatternImage = Pattern.compile("<img src=\"([^#]*?)\" />");
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
        
        // BBCode Parser
        mParser = prepareBbCodeParser();
    }

    /**
     * Returns html code of the topic.
     * @throws UnknownErrorException 
     * @throws VariableNotDefinedException 
     */
    public String buildTopic() throws IOException, VariableNotDefinedException {

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
        
        // 
        
        for (int i = 0; i < posts.length; i++) {
            t.setVariable("number", i);
            t.setVariable("postId", posts[i].getId());
            t.setVariable("author", posts[i].getAuthor().getNick());
            t.setVariable("date", posts[i].getDate());
            t.setVariable("postText", parseBbCode(posts[i]));
            t.setVariable("postTitle", posts[i].getTitle());
            
            if (posts[i].getAuthor().getId() == PotUtils.getObjectManagerInstance(mActivity)
                    .getCurrentUser().getId())
                t.setVariableOpt("currentUser", "curruser");
            
            if ((posts[i].getId() > mTopic.getPid()) && mSettings.getBoolean("marknewposts", false))
                t.setVariableOpt("newPost", "newpost");
            
            t.addBlock("post");
        }
        
        String htmlCode = t.generateOutput();
        
        htmlCode = parseImages(htmlCode);
        htmlCode = parseSmileys(htmlCode);
        
        return htmlCode;
    }

    /**
     * Try parsing the bbcode. If it fails for any reason, at least return
     * the raw code.
     */
    private String parseBbCode(Post post) {
        
        try {
            return mParser.parse(post.getText());
        } catch (Exception e) {
            return post.getText();
        }
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
    
    private BBCodeParser prepareBbCodeParser() {
        BBCodeParser a = new BBCodeParser();

        
        String allNodes = "string, b, u, s, i, mod, spoiler, " +
                "code, img, quote, url, list, table";
        
        BBCodeParser.BBCodeTag b;
        
        b = new BBCodeParser.BBCodeTag();
        b.mTag = "b";
        b.mDescription = "Bold";
        b.allow(allNodes);
        b.html("<strong>{0}</strong>");
        b.mInvalidStartRecovery = BBCodeParser.BBCodeTag.RECOVERY_CLOSE;
        b.mInvalidEndRecovery = BBCodeParser.BBCodeTag.RECOVERY_REOPEN;
        a.registerTag(b);
        
        b = new BBCodeParser.BBCodeTag();
        b.mTag = "u";
        b.mDescription = "Underline";
        b.allow(allNodes);
        b.html("<ul>{0}</u>");
        b.mInvalidStartRecovery = BBCodeParser.BBCodeTag.RECOVERY_CLOSE;
        b.mInvalidEndRecovery = BBCodeParser.BBCodeTag.RECOVERY_REOPEN;
        a.registerTag(b);
        
        b = new BBCodeParser.BBCodeTag();
        b.mTag = "s";
        b.mDescription = "Strike";
        b.allow(allNodes);
        b.html("<strike>{0}</strike>");
        b.mInvalidStartRecovery = BBCodeParser.BBCodeTag.RECOVERY_CLOSE;
        b.mInvalidEndRecovery = BBCodeParser.BBCodeTag.RECOVERY_REOPEN;
        a.registerTag(b);
        
        b = new BBCodeParser.BBCodeTag();
        b.mTag = "i";
        b.mDescription = "Italic";
        b.allow(allNodes);
        b.html("<em>{0}</em>");
        b.mInvalidStartRecovery = BBCodeParser.BBCodeTag.RECOVERY_CLOSE;
        b.mInvalidEndRecovery = BBCodeParser.BBCodeTag.RECOVERY_REOPEN;
        a.registerTag(b);
        
        b = new BBCodeParser.BBCodeTag();
        b.mTag = "spoiler";
        b.mDescription = "Spoiler";
        b.allow(allNodes);
        b.html("<span class=\"spoiler\">{0}</span>");
        b.mInvalidStartRecovery = BBCodeParser.BBCodeTag.RECOVERY_CLOSE;
        b.mInvalidEndRecovery = BBCodeParser.BBCodeTag.RECOVERY_REOPEN;
        a.registerTag(b);
        
        b = new BBCodeParser.BBCodeTag();
        b.mTag = "code";
        b.mDescription = "Code";
        b.allow("string");
        b.html("<span class=\"code\">{0}</span>");
        b.mInvalidStartRecovery = BBCodeParser.BBCodeTag.RECOVERY_CLOSE;
        b.mInvalidEndRecovery = BBCodeParser.BBCodeTag.RECOVERY_REOPEN;
        a.registerTag(b);
        
        b = new BBCodeParser.BBCodeTag();
        b.mTag = "mod";
        b.mDescription = "Highlight";
        b.allow(allNodes);
        b.html("<span class=\"mod\">{0}</span>");
        b.mInvalidStartRecovery = BBCodeParser.BBCodeTag.RECOVERY_CLOSE;
        b.mInvalidEndRecovery = BBCodeParser.BBCodeTag.RECOVERY_REOPEN;
        a.registerTag(b);
        
        b = new BBCodeParser.BBCodeTag();
        b.mTag = "list";
        b.mDescription = "List";
        b.allow("*");
        b.html(0, "<ul>{0}</ul>");
        b.html(1, "<ol>{0}</ol>");
        b.mInvalidRecoveryAddTag = "*";
        b.mInvalidStartRecovery = BBCodeParser.BBCodeTag.RECOVERY_ADD;
        b.mInvalidStringRecovery = BBCodeParser.BBCodeTag.RECOVERY_ADD;
        b.mInvalidEndRecovery = BBCodeParser.BBCodeTag.RECOVERY_CLOSE;
        a.registerTag(b);
        
        b = new BBCodeParser.BBCodeTag();
        b.mTag = "quote";
        b.mDescription = "Zitat";
        b.allow(allNodes);
        b.html(0, "<blockquote>{0}</blockquote>");
        b.html(3, "<blockquote><div class=\"author\">{3}</div>{0}</blockquote>");
        b.mInvalidStartRecovery = BBCodeParser.BBCodeTag.RECOVERY_CLOSE;
        b.mInvalidEndRecovery = BBCodeParser.BBCodeTag.RECOVERY_REOPEN;
        a.registerTag(b);
        
        b = new BBCodeParser.BBCodeTag();
        b.mTag = "*";
        b.mDescription = "Listitem";
        b.allow(allNodes);
        b.html("<li>{0}</li>");
        b.mInvalidStartRecovery = BBCodeParser.BBCodeTag.RECOVERY_CLOSE;
        b.mInvalidEndRecovery = BBCodeParser.BBCodeTag.RECOVERY_CLOSE;
        a.registerTag(b);
        
        b = new BBCodeParser.BBCodeTag();
        b.mTag = "url";
        b.mDescription = "Link";
        b.allow("string");
        b.html(0, "<a href=\"{0}\">{0}</a>");
        b.html(1, "<a href=\"{1}\">{0}</a>");
        b.mInvalidStartRecovery = BBCodeParser.BBCodeTag.RECOVERY_STRING;
        b.mInvalidEndRecovery = BBCodeParser.BBCodeTag.RECOVERY_STRING;
        a.registerTag(b);
        
        b = new BBCodeParser.BBCodeTag();
        b.mTag = "img";
        b.mDescription = "Image";
        b.allow("string");
        b.html("<img src=\"{0}\" />");
        b.mInvalidStartRecovery = BBCodeParser.BBCodeTag.RECOVERY_CLOSE;
        b.mInvalidEndRecovery = BBCodeParser.BBCodeTag.RECOVERY_REOPEN;
        a.registerTag(b);

        b = new BBCodeParser.BBCodeTag();
        b.mTag = "table";
        b.mDescription = "Table";
        b.allow("--");
        b.html("<table>{0}</table>");
        b.mInvalidRecoveryAddTag = "--";
        b.mInvalidStartRecovery = BBCodeParser.BBCodeTag.RECOVERY_ADD;
        b.mInvalidStringRecovery = BBCodeParser.BBCodeTag.RECOVERY_ADD;
        b.mInvalidEndRecovery = BBCodeParser.BBCodeTag.RECOVERY_CLOSE;
        a.registerTag(b);
        
        b = new BBCodeParser.BBCodeTag();
        b.mTag = "--";
        b.mDescription = "TableRow";
        b.allow("||");
        b.html("<tr>{0}</tr>");
        b.mInvalidRecoveryAddTag = "||";
        b.mInvalidStartRecovery = BBCodeParser.BBCodeTag.RECOVERY_ADD;
        b.mInvalidStringRecovery = BBCodeParser.BBCodeTag.RECOVERY_ADD;
        b.mInvalidEndRecovery = BBCodeParser.BBCodeTag.RECOVERY_CLOSE;
        a.registerTag(b);
        
        b = new BBCodeParser.BBCodeTag();
        b.mTag = "||";
        b.mDescription = "TableCol";
        b.allow(allNodes);
        b.html("<td>{0}</td>");
        b.mInvalidStartRecovery = BBCodeParser.BBCodeTag.RECOVERY_CLOSE;
        b.mInvalidEndRecovery = BBCodeParser.BBCodeTag.RECOVERY_CLOSE;
        a.registerTag(b);
        
        return a;
    }

}
