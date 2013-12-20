package com.mde.potdroid.parsers;

import android.sax.*;
import android.util.Xml;
import com.mde.potdroid.models.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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

    public Forum parse(String input) {
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
                //mCurrentBoard.setLastPost(mCurrentPost);
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
                mCurrentBoard.setLastPost(mCurrentPost);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentThread = new Topic(Integer.parseInt(attributes.getValue(Post.Xml.IN_THREAD_ID_ATTRIBUTE)));
            }
        });


        try {
            Xml.parse(input, categories.getContentHandler());
        } catch (SAXException e) {
            return null;
        }
        return mForum;
    }
}
