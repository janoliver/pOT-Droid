package com.mde.potdroid3.models;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The Board model.
 */
public class Board implements Serializable {

    private static final long serialVersionUID = 7L;
    
    private Integer mId;
    private Integer mNumberOfThreads;
    private Integer mNumberOfReplies;
    private Integer mThreadsPerPage = 30;
    private Integer mPage;
    private Integer mOffset;
    private String  mName;
    private String  mDescription;
    private ArrayList<Topic> mTopics = new ArrayList<Topic>();
    private Category mCategory;
    private Post mLastPost;

    public Board(Integer id) {
        mId = id;
    }

    public Board() {}

    public void setId(int id) {
        mId = id;
    }

    public Integer getPage() {
        return mPage;
    }

    public void setPage(Integer page) {
        mPage = page;
    }

    public Integer getOffset() {
        return mOffset;
    }

    public void setOffset(Integer offset) {
        mOffset = offset;
    }

    public Integer getNumberOfThreads() {
        return mNumberOfThreads;
    }

    public Integer getNumberOfPages() {
        return  mNumberOfThreads / mThreadsPerPage + 1;
    }

    public void setNumberOfThreads(Integer numberOfThreads) {
        mNumberOfThreads = numberOfThreads;
    }

    public Integer getNumberOfReplies() {
        return mNumberOfReplies;
    }

    public void setNumberOfReplies(Integer numberOfReplies) {
        mNumberOfReplies = numberOfReplies;
    }

    public Integer getThreadsPerPage() {
        return mThreadsPerPage;
    }

    public void setThreadsPerPage(Integer threadsPerPage) {
        mThreadsPerPage = threadsPerPage;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setLastPost(Post post) {
        mLastPost = post;
    }

    public Post getLastPost() {
        return mLastPost;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public ArrayList<Topic> getTopics() {
        return mTopics;
    }

    public void setTopics(ArrayList<Topic> topics) {
        mTopics = topics;
    }

    public void addTopic(Topic topic) {
        mTopics.add(topic);
    }

    public Category getCategory() {
        return mCategory;
    }

    public void setCategory(Category category) {
        mCategory = category;
    }

    public Integer getId() {
        return mId;
    }

    public Boolean isLastPage() {
        return mPage == getNumberOfPages();
    }

    public static class Xml {
        public static String TAG = "board";
        public static String DESCRIPTION_TAG = "description";
        public static String NAME_TAG = "name";
        public static String LASTPOST_TAG = "lastpost";
        public static String ID_ATTRIBUTE = "id";

        public static String THREADS_TAG = "threads";
        public static String THREADS_ATTRIBUTE_PAGE = "page";

        public static String IN_CATEGORY_TAG = "in-category";
        public static String IN_CATEGORY_ID_ATTRIBUTE = "id";

        public static String NUMBER_OF_THREADS_TAG = "number-of-threads";
        public static String NUMBER_OF_THREADS_ATTRIBUTE = "value";

        public static String NUMBER_OF_REPLIES_TAG = "number-of-replies";
        public static String NUMBER_OF_REPLIES_ATTRIBUTE = "value";

        public static String URL = "xml/board.php";

        public static String getUrl(int bid, int page) {
            Uri.Builder b = Uri.parse(URL).buildUpon();
            b.appendQueryParameter("page", String.valueOf(page));
            b.appendQueryParameter("BID", String.valueOf(bid));
            return b.build().toString();
        }
    }

}
