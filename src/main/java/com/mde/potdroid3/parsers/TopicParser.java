package com.mde.potdroid3.parsers;

import android.sax.*;
import android.util.Xml;
import com.mde.potdroid3.models.Board;
import com.mde.potdroid3.models.Post;
import com.mde.potdroid3.models.Topic;
import com.mde.potdroid3.models.User;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by oli on 6/2/13.
 */
public class TopicParser extends DefaultHandler {
    private Post mCurrentPost;
    private User mCurrentUser;
    private User mCurrentLastEditUser;
    private Topic mThread;

    public TopicParser() {
        mThread = new Topic();
    }

    public Topic parse(InputStream instream) {
        RootElement thread = new RootElement(Topic.Xml.TAG);

        // find the thread information
        thread.setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mThread.setId(Integer.parseInt(attributes.getValue(Topic.Xml.ID_ATTRIBUTE)));
            }
        });
        thread.requireChild(Topic.Xml.TITLE_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mThread.setTitle(body);
            }
        });
        thread.requireChild(Topic.Xml.SUBTITLE_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mThread.setSubTitle(body);
            }
        });
        thread.requireChild(Topic.Xml.NUMBER_OF_HITS_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mThread.setNumberOfHits(Integer.parseInt(attributes.getValue(Topic.Xml.NUMBER_OF_HITS_ATTRIBUTE)));
            }
        });
        thread.requireChild(Topic.Xml.NUMBER_OF_REPLIES_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mThread.setNumberOfPosts(Integer.parseInt(attributes.getValue(Topic.Xml.NUMBER_OF_REPLIES_ATTRIBUTE)));
            }
        });
        thread.getChild(Topic.Xml.TOKEN_NEWREPLY_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mThread.setNewreplytoken(attributes.getValue(Topic.Xml.TOKEN_NEWREPLY_ATTRIBUTE));
            }
        });
        thread.requireChild(Topic.Xml.IN_BOARD_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                Board b = new Board(Integer.parseInt(attributes.getValue(Topic.Xml.IN_BOARD_ATTRIBUTE)));
                mThread.setBoard(b);
            }
        });
        Element first_post = thread.getChild(Topic.Xml.FIRSTPOST_TAG).getChild(Post.Xml.TAG);
        first_post.setElementListener(new ElementListener() {

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
        first_post.requireChild(User.Xml.TAG).setTextElementListener(new TextElementListener() {

            @Override
            public void end(String body) {
                mCurrentUser.setNick(body);
                mCurrentPost.setAuthor(mCurrentUser);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentUser = new User(Integer.parseInt(attributes.getValue(User.Xml.ID_ATTRIBUTE)));
            }
        });
        first_post.requireChild(Post.Xml.DATE_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setDateFromTimestamp(Integer.parseInt(attributes.getValue(Post.Xml.DATE_TIMESTAMP_ATTRIBUTE)));
            }
        });

        Element flags = thread.getChild(Topic.Xml.FLAGS_TAG);
        flags.requireChild(Topic.Xml.IS_CLOSED_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mThread.setIsClosed(Boolean.parseBoolean(attributes.getValue(Topic.Xml.IS_CLOSED_ATTRIBUTE)));
            }
        });
        flags.requireChild(Topic.Xml.IS_STICKY_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mThread.setIsSticky(Boolean.parseBoolean(attributes.getValue(Topic.Xml.IS_STICKY_ATTRIBUTE)));
            }
        });
        flags.requireChild(Topic.Xml.IS_ANNOUNCEMENT_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mThread.setIsAnnouncement(Boolean.parseBoolean(attributes.getValue(Topic.Xml.IS_ANNOUNCEMENT_ATTRIBUTE)));
            }
        });
        flags.requireChild(Topic.Xml.IS_IMPORTANT_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mThread.setIsImportant(Boolean.parseBoolean(attributes.getValue(Topic.Xml.IS_IMPORTANT_ATTRIBUTE)));
            }
        });
        flags.requireChild(Topic.Xml.IS_GLOBAL_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mThread.setIsGlobal(Boolean.parseBoolean(attributes.getValue(Topic.Xml.IS_GLOBAL_ATTRIBUTE)));
            }
        });

        Element posts = thread.requireChild(Topic.Xml.POSTS_TAG);
        posts.setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mThread.setPage(Integer.parseInt(attributes.getValue(Topic.Xml.POSTS_ATTRIBUTE_PAGE)));
                mThread.setOffset(Integer.parseInt(attributes.getValue(Topic.Xml.POSTS_ATTRIBUTE_OFFSET)));
            }
        });

        Element post = posts.getChild(Post.Xml.TAG);
        post.setElementListener(new ElementListener() {

            @Override
            public void end() {
                mThread.addPost(mCurrentPost);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentPost = new Post(Integer.parseInt(attributes.getValue(Post.Xml.ID_ATTRIBUTE)));
                mCurrentPost.setBoard(mThread.getBoard());
                mCurrentPost.setTopic(mThread);
            }
        });
        post.requireChild(User.Xml.TAG).setTextElementListener(new TextElementListener() {

            @Override
            public void end(String body) {
                mCurrentUser.setNick(body);
                mCurrentPost.setAuthor(mCurrentUser);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentUser = new User(Integer.parseInt(attributes.getValue(User.Xml.ID_ATTRIBUTE)));
            }
        });
        post.requireChild(Post.Xml.DATE_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setDateFromTimestamp(Integer.parseInt(attributes.getValue(Post.Xml.DATE_TIMESTAMP_ATTRIBUTE)));
            }
        });
        post.getChild(Post.Xml.TOKEN_SETBOOKMARK_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setBookmarktoken(attributes.getValue(Post.Xml.TOKEN_SETBOOKMARK_ATTRIBUTE));
            }
        });
        post.requireChild(User.Xml.AVATAR_TAG).setTextElementListener(new TextElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentUser.setAvatarId(Integer.parseInt(attributes.getValue(User.Xml.AVATAR_ATTRIBUTE)));
            }

            @Override
            public void end(String body) {
                mCurrentUser.setAvatarFile(body);
            }
        });
        post.getChild(Post.Xml.ICON_TAG).setTextElementListener(new TextElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setIconId(Integer.parseInt(attributes.getValue(Post.Xml.ICON_ATTRIBUTE)));
            }

            @Override
            public void end(String body) {
                mCurrentPost.setIconFile(body);
            }
        });

        Element message = post.getChild(Post.Xml.MESSAGE_TAG);
        message.requireChild(Post.Xml.MESSAGE_CONTENT_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mCurrentPost.setText(body);
            }
        });
        message.requireChild(Post.Xml.MESSAGE_TITLE_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mCurrentPost.setTitle(body);
            }
        });
        message.requireChild(Post.Xml.MESSAGE_EDITED_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setEdited(Integer.parseInt(attributes.getValue(Post.Xml.MESSAGE_EDITED_ATTRIBUTE)));
            }
        });

        Element lastedit = message.getChild(Post.Xml.MESSAGE_EDITED_TAG).getChild(Post.Xml.MESSAGE_LASTEDIT_TAG);
        lastedit.getChild(User.Xml.TAG).setTextElementListener(new TextElementListener() {

            @Override
            public void end(String body) {
                mCurrentLastEditUser.setNick(body);
                mCurrentPost.setLastEditUser(mCurrentLastEditUser);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentLastEditUser = new User(Integer.parseInt(attributes.getValue(User.Xml.ID_ATTRIBUTE)));
            }
        });
        lastedit.getChild(Post.Xml.DATE_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setLastEditDateFromTimestamp(Integer.parseInt(attributes.getValue(Post.Xml.DATE_TIMESTAMP_ATTRIBUTE)));
            }
        });

        try {
            Xml.parse(instream, Xml.Encoding.UTF_8, thread.getContentHandler());
        } catch (IOException e) {
            return null;
        } catch (SAXException e) {
            return null;
        }

        return mThread;
    }
}
