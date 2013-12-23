package com.mde.potdroid.parsers;

import android.net.Uri;
import android.sax.Element;
import android.sax.ElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.sax.TextElementListener;
import android.util.Xml;

import com.mde.potdroid.helpers.Utils;
import com.mde.potdroid.models.Board;
import com.mde.potdroid.models.Post;
import com.mde.potdroid.models.Topic;
import com.mde.potdroid.models.User;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * An XML Handler class to parse the API XML code of a Topic. A little bit messy,
 * but a long stare at the code should make it clear.
 */
public class TopicParser extends DefaultHandler
{

    public static String TAG = "thread";
    public static String SUBTITLE_TAG = "subtitle";
    public static String TITLE_TAG = "title";
    public static String FIRSTPOST_TAG = "firstpost";
    public static String ID_ATTRIBUTE = "id";
    public static String FLAGS_TAG = "flags";
    public static String POSTS_TAG = "posts";
    public static String POSTS_ATTRIBUTE_PAGE = "page";
    public static String POSTS_ATTRIBUTE_OFFSET = "offset";
    public static String IN_BOARD_TAG = "in-board";
    public static String IN_BOARD_ATTRIBUTE = "id";
    public static String NUMBER_OF_PAGES_TAG = "number-of-pages";
    public static String NUMBER_OF_PAGES_ATTRIBUTE = "value";
    public static String NUMBER_OF_HITS_TAG = "number-of-hits";
    public static String NUMBER_OF_HITS_ATTRIBUTE = "value";
    public static String NUMBER_OF_REPLIES_TAG = "number-of-replies";
    public static String NUMBER_OF_REPLIES_ATTRIBUTE = "value";
    public static String IS_CLOSED_TAG = "is-closed";
    public static String IS_CLOSED_ATTRIBUTE = "value";
    public static String IS_ANNOUNCEMENT_TAG = "is-announcement";
    public static String IS_ANNOUNCEMENT_ATTRIBUTE = "value";
    public static String IS_STICKY_TAG = "is-sticky";
    public static String IS_STICKY_ATTRIBUTE = "value";
    public static String IS_IMPORTANT_TAG = "is-important";
    public static String IS_IMPORTANT_ATTRIBUTE = "value";
    public static String IS_GLOBAL_TAG = "is-global";
    public static String IS_GLOBAL_ATTRIBUTE = "value";
    public static String TOKEN_NEWREPLY_TAG = "token-newreply";
    public static String TOKEN_NEWREPLY_ATTRIBUTE = "value";
    public static String POST_TAG = "post";
    public static String MESSAGE_TAG = "message";
    public static String ICON_TAG = "icon";
    public static String ICON_ATTRIBUTE = "id";
    public static String MESSAGE_TITLE_TAG = "title";
    public static String MESSAGE_CONTENT_TAG = "content";
    public static String MESSAGE_EDITED_TAG = "edited";
    public static String MESSAGE_EDITED_ATTRIBUTE = "count";
    public static String MESSAGE_LASTEDIT_TAG = "lastedit";
    public static String DATE_TAG = "date";
    public static String DATE_TIMESTAMP_ATTRIBUTE = "timestamp";
    public static String IN_THREAD_TAG = "in-thread";
    public static String IN_THREAD_ID_ATTRIBUTE = "id";
    public static String TOKEN_SETBOOKMARK_TAG = "token-setbookmark";
    public static String TOKEN_SETBOOKMARK_ATTRIBUTE = "value";
    public static String TOKEN_EDITREPLY_TAG = "token-editreply";
    public static String TOKEN_EDITREPLY_ATTRIBUTE = "value";
    public static String USER_TAG = "user";
    public static String AVATAR_TAG = "avatar";
    public static String AVATAR_ATTRIBUTE = "id";
    public static String URL = "xml/thread.php";
    private Post mCurrentPost;
    private User mCurrentUser;
    private User mCurrentLastEditUser;
    private Topic mThread;

    public TopicParser() {
        mThread = new Topic();
    }

    public static String getUrl(int tid, int page, int pid) {
        Uri.Builder b = Uri.parse(URL).buildUpon();

        b.appendQueryParameter("update_bookmark", String.valueOf(1));
        b.appendQueryParameter("TID", String.valueOf(tid));

        if (pid > 0) {
            b.appendQueryParameter("PID", String.valueOf(pid));
        } else {
            b.appendQueryParameter("page", String.valueOf(page));
        }

        return b.build().toString();
    }

    public Topic parse(String input) {
        RootElement thread = new RootElement(TAG);

        // find the thread information
        thread.setStartElementListener(new StartElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                mThread.setId(Integer.parseInt(attributes.getValue(ID_ATTRIBUTE)));

                if(Integer.parseInt(attributes.getValue(BoardParser.CURRENT_USER_ID)) == 0) {
                    Utils.setNotLoggedIn();
                }
            }
        });
        thread.requireChild(TITLE_TAG).setEndTextElementListener(new EndTextElementListener()
        {

            @Override
            public void end(String body) {
                mThread.setTitle(body);
            }
        });
        thread.requireChild(SUBTITLE_TAG).setEndTextElementListener(new EndTextElementListener()
        {

            @Override
            public void end(String body) {
                mThread.setSubTitle(body);
            }
        });
        thread.requireChild(NUMBER_OF_HITS_TAG).setStartElementListener(new StartElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                mThread.setNumberOfHits(Integer.parseInt(attributes.getValue
                        (NUMBER_OF_HITS_ATTRIBUTE)));
            }
        });
        thread.requireChild(NUMBER_OF_REPLIES_TAG).setStartElementListener(new StartElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                mThread.setNumberOfPosts(Integer.parseInt(attributes.getValue
                        (NUMBER_OF_REPLIES_ATTRIBUTE)));
            }
        });
        thread.getChild(TOKEN_NEWREPLY_TAG).setStartElementListener(new StartElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                mThread.setNewreplytoken(attributes.getValue(TOKEN_NEWREPLY_ATTRIBUTE));
            }
        });
        thread.requireChild(IN_BOARD_TAG).setStartElementListener(new StartElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                Board b = new Board(Integer.parseInt(attributes.getValue(IN_BOARD_ATTRIBUTE)));
                mThread.setBoard(b);
            }
        });
        Element first_post = thread.getChild(FIRSTPOST_TAG).getChild(POST_TAG);
        first_post.setElementListener(new ElementListener()
        {

            @Override
            public void end() {
                mThread.setFirstPost(mCurrentPost);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentPost = new Post();
                mCurrentPost.setBoard(mThread.getBoard());
                mCurrentPost.setTopic(mThread);
            }
        });
        first_post.requireChild(USER_TAG).setTextElementListener(new TextElementListener()
        {

            @Override
            public void end(String body) {
                mCurrentUser.setNick(body);
                mCurrentPost.setAuthor(mCurrentUser);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentUser = new User(Integer.parseInt(attributes.getValue(ID_ATTRIBUTE)));
            }
        });
        first_post.requireChild(DATE_TAG).setStartElementListener(new StartElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setDateFromTimestamp(Integer.parseInt(attributes.getValue
                        (DATE_TIMESTAMP_ATTRIBUTE)));
            }
        });

        Element flags = thread.getChild(FLAGS_TAG);
        flags.requireChild(IS_CLOSED_TAG).setStartElementListener(new StartElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                mThread.setIsClosed(attributes.getValue(IS_CLOSED_ATTRIBUTE).equals("1"));
            }
        });
        flags.requireChild(IS_STICKY_TAG).setStartElementListener(new StartElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                mThread.setIsSticky(attributes.getValue(IS_STICKY_ATTRIBUTE).equals("1"));
            }
        });
        flags.requireChild(IS_ANNOUNCEMENT_TAG).setStartElementListener(new StartElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                mThread.setIsAnnouncement(attributes.getValue(IS_ANNOUNCEMENT_ATTRIBUTE).equals
                        ("1"));
            }
        });
        flags.requireChild(IS_IMPORTANT_TAG).setStartElementListener(new StartElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                mThread.setIsImportant(attributes.getValue(IS_IMPORTANT_ATTRIBUTE).equals("1"));
            }
        });
        flags.requireChild(IS_GLOBAL_TAG).setStartElementListener(new StartElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                mThread.setIsGlobal(attributes.getValue(IS_GLOBAL_ATTRIBUTE).equals("1"));
            }
        });

        Element posts = thread.requireChild(POSTS_TAG);
        posts.setStartElementListener(new StartElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                mThread.setPage(Integer.parseInt(attributes.getValue(POSTS_ATTRIBUTE_PAGE)));
                mThread.setOffset(Integer.parseInt(attributes.getValue(POSTS_ATTRIBUTE_OFFSET)));
            }
        });

        Element post = posts.getChild(POST_TAG);
        post.setElementListener(new ElementListener()
        {

            @Override
            public void end() {
                mThread.addPost(mCurrentPost);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentPost = new Post(Integer.parseInt(attributes.getValue(ID_ATTRIBUTE)));
                mCurrentPost.setBoard(mThread.getBoard());
                mCurrentPost.setTopic(mThread);
            }
        });
        post.requireChild(USER_TAG).setTextElementListener(new TextElementListener()
        {

            @Override
            public void end(String body) {
                mCurrentUser.setNick(body);
                mCurrentPost.setAuthor(mCurrentUser);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentUser = new User(Integer.parseInt(attributes.getValue(ID_ATTRIBUTE)));
            }
        });
        post.requireChild(DATE_TAG).setStartElementListener(new StartElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setDateFromTimestamp(Integer.parseInt(attributes.getValue
                        (DATE_TIMESTAMP_ATTRIBUTE)));
            }
        });
        post.getChild(TOKEN_SETBOOKMARK_TAG).setStartElementListener(new StartElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setBookmarktoken(attributes.getValue(TOKEN_SETBOOKMARK_ATTRIBUTE));
            }
        });
        post.getChild(TOKEN_EDITREPLY_TAG).setStartElementListener(new StartElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setEdittoken(attributes.getValue(TOKEN_EDITREPLY_ATTRIBUTE));
            }
        });
        post.requireChild(AVATAR_TAG).setTextElementListener(new TextElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                mCurrentUser.setAvatarId(Integer.parseInt(attributes.getValue(AVATAR_ATTRIBUTE)));
            }

            @Override
            public void end(String body) {
                mCurrentUser.setAvatarFile(body);
            }
        });
        post.getChild(ICON_TAG).setTextElementListener(new TextElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setIconId(Integer.parseInt(attributes.getValue(ICON_ATTRIBUTE)));
            }

            @Override
            public void end(String body) {
                mCurrentPost.setIconFile(body);
            }
        });

        Element message = post.getChild(MESSAGE_TAG);
        message.requireChild(MESSAGE_CONTENT_TAG).setEndTextElementListener(new EndTextElementListener()
        {

            @Override
            public void end(String body) {
                mCurrentPost.setText(body);
            }
        });
        message.requireChild(MESSAGE_TITLE_TAG).setEndTextElementListener(new EndTextElementListener()
        {

            @Override
            public void end(String body) {
                mCurrentPost.setTitle(body);
            }
        });
        message.requireChild(MESSAGE_EDITED_TAG).setStartElementListener(new StartElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setEdited(Integer.parseInt(attributes.getValue
                        (MESSAGE_EDITED_ATTRIBUTE)));
            }
        });

        Element lastedit = message.getChild(MESSAGE_EDITED_TAG).getChild(MESSAGE_LASTEDIT_TAG);
        lastedit.getChild(USER_TAG).setTextElementListener(new TextElementListener()
        {

            @Override
            public void end(String body) {
                mCurrentLastEditUser.setNick(body);
                mCurrentPost.setLastEditUser(mCurrentLastEditUser);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentLastEditUser = new User(Integer.parseInt(attributes.getValue
                        (ID_ATTRIBUTE)));
            }
        });
        lastedit.getChild(DATE_TAG).setStartElementListener(new StartElementListener()
        {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setLastEditDateFromTimestamp(Integer.parseInt(attributes.getValue
                        (DATE_TIMESTAMP_ATTRIBUTE)));
            }
        });

        try {
            Xml.parse(input, thread.getContentHandler());
        } catch (SAXException e) {
            return null;
        }

        return mThread;
    }
}
