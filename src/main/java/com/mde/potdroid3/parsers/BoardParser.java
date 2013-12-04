package com.mde.potdroid3.parsers;

import android.sax.*;
import android.util.Xml;
import com.mde.potdroid3.models.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BoardParser extends DefaultHandler {

    private Post mCurrentPost;
    private User mCurrentUser;
    private Topic mCurrentThread;
    private Board mBoard;

    public BoardParser() {
        mBoard = new Board();
    }

    public Board parse(String input) {
        RootElement board = new RootElement(Board.Xml.TAG);

        // find the board information
        board.setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mBoard.setId(Integer.parseInt(attributes.getValue(Board.Xml.ID_ATTRIBUTE)));
            }
        });
        board.requireChild(Board.Xml.NAME_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mBoard.setName(body);
            }
        });
        board.requireChild(Board.Xml.DESCRIPTION_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mBoard.setDescription(body);
            }
        });
        board.requireChild(Board.Xml.NUMBER_OF_THREADS_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mBoard.setNumberOfThreads(Integer.parseInt(attributes.getValue(Board.Xml.NUMBER_OF_THREADS_ATTRIBUTE)));
            }
        });
        board.requireChild(Board.Xml.NUMBER_OF_REPLIES_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mBoard.setNumberOfReplies(Integer.parseInt(attributes.getValue(Board.Xml.NUMBER_OF_REPLIES_ATTRIBUTE)));
            }
        });
        board.requireChild(Board.Xml.IN_CATEGORY_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                Category c = new Category(Integer.parseInt(attributes.getValue(Board.Xml.IN_CATEGORY_ID_ATTRIBUTE)));
                mBoard.setCategory(c);
            }
        });

        Element threads = board.getChild(Board.Xml.THREADS_TAG);
        threads.setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mBoard.setPage(Integer.parseInt(attributes.getValue(Board.Xml.THREADS_ATTRIBUTE_PAGE)));
            }
        });

        Element thread = threads.getChild(Topic.Xml.TAG);
        thread.setElementListener(new ElementListener() {

            @Override
            public void end() {
                mBoard.addTopic(mCurrentThread);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentThread = new Topic(Integer.parseInt(attributes.getValue(Topic.Xml.ID_ATTRIBUTE)));
                mCurrentThread.setBoard(mBoard);
            }
        });
        thread.requireChild(Topic.Xml.TITLE_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mCurrentThread.setTitle(body);
            }
        });
        thread.requireChild(Topic.Xml.SUBTITLE_TAG).setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String body) {
                mCurrentThread.setSubTitle(body);
            }
        });
        thread.requireChild(Topic.Xml.NUMBER_OF_HITS_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setNumberOfHits(Integer.parseInt(attributes.getValue(Topic.Xml.NUMBER_OF_HITS_ATTRIBUTE)));
            }
        });
        thread.requireChild(Topic.Xml.NUMBER_OF_REPLIES_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setNumberOfPosts(Integer.parseInt(attributes.getValue(Topic.Xml.NUMBER_OF_REPLIES_ATTRIBUTE)));
            }
        });
        thread.requireChild(Topic.Xml.NUMBER_OF_PAGES_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setNumberOfPages(Integer.parseInt(attributes.getValue(Topic.Xml.NUMBER_OF_PAGES_ATTRIBUTE)));
            }
        });
        Element first_post = thread.getChild(Topic.Xml.FIRSTPOST_TAG).getChild(Post.Xml.TAG);
        first_post.setElementListener(new ElementListener() {

            @Override
            public void end() {
                mCurrentThread.setFirstPost(mCurrentPost);
            }

            @Override
            public void start(Attributes attributes) {
                mCurrentPost = new Post();
                mCurrentPost.setBoard(mBoard);
                mCurrentPost.setTopic(mCurrentThread);
            }
        });
        first_post.requireChild(Post.Xml.DATE_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentPost.setDateFromTimestamp(Integer.parseInt(attributes.getValue(Post.Xml.DATE_TIMESTAMP_ATTRIBUTE)));
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

        Element flags = thread.getChild(Topic.Xml.FLAGS_TAG);

        flags.requireChild(Topic.Xml.IS_CLOSED_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setIsClosed(attributes.getValue(Topic.Xml.IS_CLOSED_ATTRIBUTE).equals("1"));
            }
        });
        flags.requireChild(Topic.Xml.IS_STICKY_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setIsSticky(attributes.getValue(Topic.Xml.IS_STICKY_ATTRIBUTE).equals("1"));
            }
        });
        flags.requireChild(Topic.Xml.IS_ANNOUNCEMENT_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setIsAnnouncement(attributes.getValue(Topic.Xml.IS_ANNOUNCEMENT_ATTRIBUTE).equals("1"));
            }
        });
        flags.requireChild(Topic.Xml.IS_IMPORTANT_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setIsImportant(attributes.getValue(Topic.Xml.IS_IMPORTANT_ATTRIBUTE).equals("1"));
            }
        });
        flags.requireChild(Topic.Xml.IS_GLOBAL_TAG).setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                mCurrentThread.setIsGlobal(attributes.getValue(Topic.Xml.IS_GLOBAL_ATTRIBUTE).equals("1"));
            }
        });

        try {
            Xml.parse(input, board.getContentHandler());
        } catch (SAXException e) {
            return null;
        }

        return mBoard;
    }
}
