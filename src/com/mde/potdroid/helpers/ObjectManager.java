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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mde.potdroid.R;
import com.mde.potdroid.helpers.WebsiteInteraction.NoConnectionException;
import com.mde.potdroid.models.Board;
import com.mde.potdroid.models.Bookmark;
import com.mde.potdroid.models.Category;
import com.mde.potdroid.models.Forum;
import com.mde.potdroid.models.Post;
import com.mde.potdroid.models.Topic;
import com.mde.potdroid.models.User;

/**
 * This class is a container of all models that can be created. It builds these
 * models by parsing the documents of the xml api of the forum.
 * 
 * getCurrentUser() is not null if the current user is logged in. This should
 * be the primary method to check the login state of a user. 
 */
public class ObjectManager {
    
    // no models
    private WebsiteInteraction mWebsiteInteraction;
    
    // single valued storage
    private User    mCurrentUser     = null;
    private Forum   mForum           = null;
    private String  mLoginUsername   = "";
    private Integer mUnreadBookmarks = 0;
    private Context mContext         = null;
    
    // the objects that are known to the class are stored in a 
    // hashmap, using the object's id as identifier. 
    private Map<Integer, User>     mUsers      = new LinkedHashMap<Integer, User>();
    private Map<Integer, Board>    mBoards     = new LinkedHashMap<Integer, Board>();
    private Map<Integer, Category> mCategories = new LinkedHashMap<Integer, Category>();
    private Map<Integer, Post>     mPosts      = new LinkedHashMap<Integer, Post>();
    private Map<Integer, Topic>    mTopics     = new LinkedHashMap<Integer, Topic>();
    private Map<Integer, Bookmark> mBookmarks  = new LinkedHashMap<Integer, Bookmark>();
    
    public ObjectManager(Context cx) {
        mWebsiteInteraction = PotUtils.getWebsiteInteractionInstance(cx);
        mContext = cx;
        
        // check for login
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(cx);
        User u = getUser(settings.getInt("user_id", 0));
        u.setNick(settings.getString("user_name", ""));
        mLoginUsername = settings.getString("user_name", "");
        mCurrentUser = u;
    }
    
    public Integer getUnreadBookmarks() {
        return mUnreadBookmarks;
    }
    
    public Boolean isLoggedIn() {
        return (mCurrentUser != null && mCurrentUser.getId() > 0);
    }
    
    public User getCurrentUser() {
        return mCurrentUser;
    }
    
    public Topic getTopic(int id) {
        if(!mTopics.containsKey(id))
            mTopics.put(id, new Topic(id));
        return mTopics.get(id);
    }
    
    public Board getBoard(int id) {
        if(!mBoards.containsKey(id))
            mBoards.put(id, new Board(id));
        return mBoards.get(id);
    }
    
    public Post getPost(int id) {
        if(!mPosts.containsKey(id))
            mPosts.put(id, new Post(id));
        return mPosts.get(id);
    }
    
    public Bookmark[] getBookmarks() throws ParseErrorException, 
                                NoConnectionException, NotLoggedInException {
        _parseBookmarks();
        return mBookmarks.values().toArray(new Bookmark[0]);
    }
    
    public Bookmark getBookmark(int id) {
        if(!mBookmarks.containsKey(id))
            mBookmarks.put(id, new Bookmark(id));
        return mBookmarks.get(id);
    }
    
    public User getUser(int id) {
        if(!mUsers.containsKey(id))
            mUsers.put(id, new User(id));
        return mUsers.get(id);
    }
    
    public Category getCategory(int id) {
        if(!mCategories.containsKey(id))
            mCategories.put(id, new Category(id));
        return mCategories.get(id);
    }
    
    public Forum getForum() throws ParseErrorException, NoConnectionException, JDOMException, IOException {
        if(mForum == null) {
            mForum = new Forum();
            _parseForum();
        }
        return mForum;
    }
    
    /**
     * This function gets the xml document from the api and
     * parses it. 
     * @throws ParseErrorException 
     * @throws NoConnectionException 
     * @throws NotLoggedInException 
     */
    private Boolean _parseBookmarks() throws ParseErrorException, NoConnectionException, NotLoggedInException {
        
        // fetch the xml file and return false if there was an error.
        String url   = PotUtils.BOOKMARK_URL;
        Document doc = mWebsiteInteraction.getDocument(url);
        if (doc == null) {
            throw new ParseErrorException();
        } else if(doc.getRootElement().getName().equals("not-logged-in")) {
            throw new NotLoggedInException();
        }
        
        // predefine some elements
        Element root        = doc.getRootElement();
        @SuppressWarnings("unchecked")
        List<Element> bookmarks = root.getChildren();
        
        // update the user object
        User u = getUser(_intAttr(root, "current-user-id"));
        u.setNick(mLoginUsername);
        mCurrentUser = u;
        
        // keep track of unread bookmarks
        mUnreadBookmarks = 0;
        
        // we clean the bookmarklist here to account for removed ones.
        mBookmarks.clear();
        
        // parse all the bookmarks
        for (Element el : bookmarks) {
            
            // create the category objects
            Bookmark b = getBookmark(_intAttr(el, "BMID"));
            b.setNumberOfNewPosts(_intAttr(el, "newposts"));
            mUnreadBookmarks += b.getNumberOfNewPosts();
            Post lastPost = getPost(_intAttr(el, "PID"));
            b.setLastPost(lastPost);
            b.setRemovetoken(_strAttr(el, "token-removebookmark", "value", ""));
            
            // the thread
            Topic t = getTopic(_intAttr(el, "thread", "TID", 0));
            t.setTitle(_strVal(el, "thread", ""));
            t.setIsClosed(_flagAttr(el, "thread", "closed", "1"));
            t.setLastPage(_intAttr(el, "thread", "pages", 1));
            
            // the board
            Board x = getBoard(_intAttr(el, "board", "BID", 0));
            x.setName(_strVal(el, "board", ""));
            t.setBoard(x);
            
            b.setThread(t);
                
        }

        return true;
    }

    /**
     * This function gets the xml document from the api and
     * parses it. 
     * @throws ParseErrorException 
     * @throws NoConnectionException 
     * @throws IOException 
     * @throws JDOMException 
     */
    private Boolean _parseForum() throws ParseErrorException, NoConnectionException, JDOMException, IOException {
        
        // fetch xml file
        SAXBuilder parser = new SAXBuilder();
        InputStream raw = mContext.getResources().openRawResource(R.raw.boards);
        Document doc = parser.build(raw);
        
        // predefine some elements
        Element root        = doc.getRootElement();
        @SuppressWarnings("unchecked")
        List<Element> categories = root.getChildren();
        
        // update the user object
        User u = getUser(_intAttr(root, "current-user-id"));
        u.setNick(mLoginUsername);
        mCurrentUser = u;
        
        // create the board
        Forum f = mForum;

        // parse all the topics of this page
        Category[] catTmp = new Category[categories.size()];
        int i = 0;
        for (Element el : categories) {
            // element shortcuts
            Element boards = el.getChild("boards");
            
            // create the category objects
            Category cat = getCategory(_intAttr(el, "id"));
            cat.setName(_strVal(el, "name", ""));
            cat.setDescription(_strVal(el, "description", ""));
            
            // find the boards of this category
            if(boards != null) {
                @SuppressWarnings("unchecked")
                List<Element> boardsList = boards.getChildren();
                
                Board[] boardTmp = new Board[boardsList.size()];
                int k = 0;
                for (Element bel : boardsList) {
                    Board b = getBoard(_intAttr(bel, "id"));
                    b.setName(_strVal(bel, "name", ""));
                    b.setDescription(_strVal(bel, "description", ""));
                    b.setCategory(getCategory(_intAttr(bel, "in-category", "id", 0)));
                    
                    // skipping moderators...
                    
                    // save
                    boardTmp[k++] = b;
                }
                
                cat.setBoards(boardTmp);
            }
            
            // write to object storage
            catTmp[i++] = cat;
        }
        f.setCategories(catTmp);
        return true;
    }
    
    /**
     * Get the board by specifying the page and the id.
     * It always refreshes the board before it is returned.
     * @throws ParseErrorException 
     * @throws NoConnectionException 
     */
    public Board getBoardByPage(int id, int page) throws ParseErrorException, NoConnectionException {
        _parseBoard(id, page);
        return getBoard(id);
    }
    
    /**
     * This function gets the xml document from the api and
     * parses it. 
     * @throws ParseErrorException 
     * @throws NoConnectionException 
     */
    private Boolean _parseBoard(int id, int page) throws ParseErrorException, NoConnectionException {
        
        // fetch the xml file and return false if there was an error.
        String url   = PotUtils.BOARD_URL_BASE + id + "&page=" + page;
        Document doc = mWebsiteInteraction.getDocument(url);
        if (doc == null || doc.getRootElement().getName().equals("invalid-board")) {
            throw new ParseErrorException();
        }
        
        // predefine some elements
        Element root        = doc.getRootElement();
        @SuppressWarnings("unchecked")
        List<Element> topics = root.getChild("threads").getChildren();
        
        // update the user object
        User u = getUser(_intAttr(root, "current-user-id"));
        u.setNick(mLoginUsername);
        mCurrentUser = u;
        
        // create the board
        Board b = getBoard(id);
        b.setName(_strVal(root, "name", ""));
        b.setDescription(_strVal(root, "description", ""));
        b.setNumberOfReplies(_intAttr(root, "number-of-replies", "value", 0));
        b.setNumberOfThreads(_intAttr(root, "number-of-threads", "value", 0));
        b.setCategory(getCategory(_intAttr(root, "in-category", "id", 0)));
        
        // clear the topic list of that page to account for removed ones.
        b.getTopics().remove(page);

        // parse all the topics of this page
        Topic[] topicTmp = new Topic[topics.size()];
        int i = 0;
        for (Element el : topics) {
            // element shortcuts
            Element post  = el.getChild("firstpost").getChild("post");
            Element flags = el.getChild("flags");
            
            // create the user object of the author
            User author = getUser(_intAttr(post, "user", "id", 0));
            author.setNick(_strVal(post, "user", ""));
            
            // create the post object
            Topic newTopic = getTopic(_intAttr(el,"id"));
            newTopic.setAuthor(author);
            newTopic.setIsClosed(_flagAttr(flags , "is-closed", "value", "1"));
            newTopic.setIsSticky(_flagAttr(flags, "is-sticky", "value", "1"));
            newTopic.setIsImportant(_flagAttr(flags, "is-important", "value", "1"));
            newTopic.setIsAnnouncement(_flagAttr(flags, "is-announcement", "value", "1"));
            newTopic.setIsGlobal(_flagAttr(flags, "is-global", "value", "1"));
            newTopic.setTitle(_strVal(el, "title", ""));
            newTopic.setSubTitle(_strVal(el, "subtitle", ""));
            newTopic.setNumberOfPosts(_intAttr(el, "number-of-replies", "value", 0) + 1);
            newTopic.setNumberOfHits(_intAttr(el, "number-of-hits", "value", 0));
            newTopic.setLastPage(_intAttr(el, "number-of-pages", "value", 0));

            // write to object storage
            topicTmp[i++] = newTopic;
        }
        b.setTopics(page, topicTmp);
        return true;
    }
    
    /**
     * Returns some topic with the posts on page "page". When refresh is
     * true, the topic is always fetched from the internet first.
     * @throws ParseErrorException 
     * @throws NoConnectionException 
     */
    public Topic getTopicByPage(int id, int page, Boolean refresh) throws ParseErrorException, NoConnectionException {
        Topic t = mTopics.get(id);
        if(t == null || !t.getPosts().containsKey(page) || refresh) {
            _parseTopic(id, page, 0);
        }
        
        return getTopic(id);
    }
    
    /**
     * Get the topic by specifying the pid and the id.
     * Since this is used only by the bookmarklist atm., it
     * always refreshes the topic before it is returned.
     * @throws ParseErrorException 
     * @throws NoConnectionException 
     */
    public Topic getTopicByPid(int id, int pid) throws ParseErrorException, NoConnectionException {
        if(!_parseTopic(id, 0, pid))
            return null;
        return getTopic(id);
    }
    
    /**
     * This function gets the xml document from the api and
     * parses it. 
     * @throws ParseErrorException 
     * @throws NoConnectionException 
     */
    private Boolean _parseTopic(int id, int page, int pid) throws ParseErrorException, NoConnectionException {
        
        // fetch the xml file and return false if there was an error.
        String url   = PotUtils.THREAD_URL_BASE + id + ((pid > 0) ? "&PID=" + pid : "&page=" + page);
        Document doc = mWebsiteInteraction.getDocument(url);
        if (doc == null || doc.getRootElement().getName().equals("invalid-thread")) {
            throw new ParseErrorException();
        }
        
        // predefine some elements
        Element root        = doc.getRootElement();
        Element flags       = root.getChild("flags");
        Element op          = root.getChild("firstpost").getChild("post");
        @SuppressWarnings("unchecked")
        List<Element> posts = root.getChild("posts").getChildren();
        
        // update the user object
        User u = getUser(_intAttr(root, "current-user-id"));
        u.setNick(mLoginUsername);
        mCurrentUser = u;
        
        // create the topic
        Topic t = getTopic(id);
        t.setTitle(_strVal(root, "title", ""));
        t.setSubTitle(_strVal(root, "subtitle", ""));
        t.setNumberOfPosts(_intAttr(root, "number-of-replies", "value", 0)+1);
        t.setNumberOfHits(_intAttr(root, "number-of-hits", "value", 0));
        t.setIsClosed(_flagAttr(flags, "is-closed", "value", "1"));
        t.setIsSticky(_flagAttr(flags, "is-sticky", "value", "1"));
        t.setIsImportant(_flagAttr(flags, "is-important", "value", "1"));
        t.setIsAnnouncement(_flagAttr(flags, "is-announcement", "value", "1"));
        t.setIsGlobal(_flagAttr(flags, "is-global", "value", "1"));
        t.setLastPage((int) Math.ceil((double) t.getNumberOfPosts() / t.getPostsPerPage()));
        t.setNewreplytoken(_strAttr(root, "token-newreply", "value", ""));
        t.setBoard(new Board(_intAttr(root, "in-board", "id", 0)));
        
        // set the pid to be able to scroll to the correct post
        t.setPid(pid);
        
        // we update the page variable in case the thread was fetched via pid
        t.setLastFetchedPage(_intAttr(root, "posts", "page", 1));
        
        // parse the op
        User topicAuthor = getUser(_intAttr(op, "user", "id", 0));
        topicAuthor.setNick(_strVal(op, "user", ""));
        t.setAuthor(topicAuthor);
        
        // clear the post list of that page to account for removed ones.
        t.getPosts().remove(page);

        // parse all the posts of this page
        Post[] postsTmp = new Post[posts.size()];
        int i = 0;
        for (Element el : posts) {
            // create the user object of the author
            User author = getUser(_intAttr(el, "user", "id", 0));
            author.setNick(_strVal(el, "user", ""));
            author.setGroup(_intAttr(el, "user", "group-id", 0));
            author.setAvatar(_strVal(el, "avatar", ""));
            
            // create the post object
            Post newPost = getPost(_intAttr(el,"id"));
            newPost.setAuthor(author);
            newPost.setDate(_strVal(el, "date", ""));
            newPost.setText(_strVal(el.getChild("message"), "content", ""));
            newPost.setTitle(_strVal(el.getChild("message"), "title", ""));
            newPost.setEdited(_intAttr(el.getChild("message"), "edited" ,"count", 0));
            newPost.setBookmarktoken(_strAttr(el, "token-setbookmark", "value", ""));
            newPost.setEdittoken(_strAttr(el, "token-editreply", "value", ""));
            newPost.setIcon(_intAttr(el, "icon", "id", 0));
            newPost.setTopic(t);

            // write to object storage
            postsTmp[i++] = newPost;
        }
        t.setPosts(t.getLastFetchedPage(), postsTmp);
        return true;
    }
    
    private int _intAttr(Element el, String attribute) {
        String tmp = el.getAttributeValue(attribute);
        return Integer.valueOf(tmp).intValue();
    }
    
    private int _intAttr(Element el, String child, String attribute, int altValue) {
        Element ch = el.getChild(child);
        if(ch == null)
            return altValue;
        String tmp = ch.getAttributeValue(attribute);
        return Integer.valueOf(tmp).intValue();
    }
    
    private Boolean _flagAttr(Element el, String child, String attribute, String eq) {
        Element ch = el.getChild(child);
        if(ch == null)
            return false;
        return ch.getAttributeValue(attribute).equals(eq);
    }
    
    private String _strVal(Element el, String child, String altValue) {
        Element ch = el.getChild(child);
        if(ch == null)
            return altValue;
        return el.getChildText(child);
    }
    
    private String _strAttr(Element el, String child, String attr, String altValue) {
        Element ch = el.getChild(child);
        if(ch == null)
            return altValue;
        String tmp = ch.getAttributeValue(attr);
        return tmp;
    }
    
    /**
     * This is the exception that is thrown when parsing of some xml
     * file failed, e.g. the url was wrong or access was denied.
     */
    public class ParseErrorException extends Exception {
        private static final long serialVersionUID = 1L;

        public ParseErrorException() {
            super("Error fetching the xml of the request.");
        }
    }
    
    /**
     * user is not logged in
     */
    public class NotLoggedInException extends Exception {
        private static final long serialVersionUID = 2L;

        public NotLoggedInException() {
            super("You are not logged in!");
        }
    }
}
