package com.mde.potdroid3.parsers;

import android.sax.*;
import android.util.Xml;
import com.mde.potdroid3.helpers.Utils;
import com.mde.potdroid3.models.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;

public class ForumParser extends DefaultHandler {

    private Category mCurrentCategory;
    private Board mCurrentBoard;
    private Post mCurrentPost;
    private User mCurrentUser;
    private Topic mCurrentThread;
    private Forum mForum;

    public ForumParser() {
        mForum = new Forum();
    }

    public Forum parse(InputStream instream) {
        RootElement categories = new RootElement(Forum.Xml.TAG);

        Element category = categories.getChild(Category.Xml.TAG);
        category.setElementListener(new ElementListener() {

            @Override
            public void end() {
                mForum.addCategory(mCurrentCategory);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentCategory = new Category(Integer.parseInt(attributes.getValue(Category.Xml.ID_ATTRIBUTE)));
            }
        });
        category.requireChild(Category.Xml.NAME_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mCurrentCategory.setName(body);
            }
        });
        category.requireChild(Category.Xml.DESCRIPTION_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mCurrentCategory.setDescription(body);
            }
        });

        Element board = category.getChild(Category.Xml.BOARDS_TAG).getChild(Board.Xml.TAG);
        board.setElementListener(new ElementListener() {

            @Override
            public void end() {
                mCurrentCategory.addBoard(mCurrentBoard);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentBoard = new Board(Integer.parseInt(attributes.getValue(Board.Xml.ID_ATTRIBUTE)));
                mCurrentBoard.setCategory(mCurrentCategory);
            }
        });
        board.requireChild(Board.Xml.NAME_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mCurrentBoard.setName(body);
            }
        });
        board.requireChild(Board.Xml.DESCRIPTION_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mCurrentBoard.setDescription(body);
            }
        });
        board.requireChild(Board.Xml.NUMBER_OF_THREADS_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentBoard.setNumberOfThreads(Integer.parseInt(attributes.getValue(Board.Xml.NUMBER_OF_THREADS_ATTRIBUTE)));
            }
        });
        board.requireChild(Board.Xml.NUMBER_OF_REPLIES_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentBoard.setNumberOfReplies(Integer.parseInt(attributes.getValue(Board.Xml.NUMBER_OF_REPLIES_ATTRIBUTE)));
            }
        });

        // the last post part
        Element last_post = board.getChild(Board.Xml.LASTPOST_TAG).getChild(Post.Xml.TAG);
        last_post.setElementListener(new ElementListener() {

            @Override
            public void end() {
                mCurrentBoard.setLastPost(mCurrentPost);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentPost = new Post();
                mCurrentPost.setBoard(mCurrentBoard);
            }
        });
        last_post.requireChild(User.Xml.TAG).setTextElementListener(new TextElementListener() {

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
        last_post.requireChild(Post.Xml.DATE_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setDateFromTimestamp(Integer.parseInt(attributes.getValue(Post.Xml.DATE_TIMESTAMP_ATTRIBUTE)));
            }
        });
        last_post.requireChild(Post.Xml.IN_THREAD_TAG).setTextElementListener(new TextElementListener() {

            @Override
            public void end(String body) {
                mCurrentThread.setTitle(body);
                mCurrentPost.setTopic(mCurrentThread);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentThread = new Topic(Integer.parseInt(attributes.getValue(Post.Xml.IN_THREAD_ID_ATTRIBUTE)));
            }
        });


        try {
            Xml.parse(instream, Xml.Encoding.UTF_8, categories.getContentHandler());
        } catch (IOException e) {
            Utils.log(e.getMessage());
            return null;
        } catch (SAXException e) {
            Utils.log(e.getMessage());
            return null;
        }
        return mForum;
    }
}
