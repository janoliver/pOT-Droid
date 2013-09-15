package com.mde.potdroid3.models;

import android.net.Uri;
import com.mde.potdroid3.helpers.Utils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Thread model.
 */
public class Topic implements Serializable{

    private static final long serialVersionUID = 2L;
    
    private Integer mId;
    private Integer mNumberOfPosts;
    private Integer mNumberOfHits;
    private Integer mNumberOfPages;
    private Integer mPage;
    private Integer mOffset;
    private Integer mPid;
    private Integer mPostsPerPage = 30;
    private String mTitle;
    private String mSubTitle;
    private Board mBoard;
    private User mAuthor;
    private Boolean mIsImportant;
    private Boolean mIsClosed;
    private Boolean mIsAnnouncement;
    private Boolean mIsGlobal;
    private Boolean mIsSticky;
    private String mNewreplytoken;
    private Integer mLastFetchedPage;
    private Post mFirstPost;
    private ArrayList<Post> mPosts = new ArrayList<Post>();
    private String mHtmlCache;

    // constructor for TopicActivity
    public Topic(Integer id) {
        mId = id;
    }

    public Topic() {}

    public void setId(int id) {
        mId = id;
    }

    public Integer getNumberOfPosts() {
        return mNumberOfPosts;
    }

    public void setNumberOfPosts(Integer numberOfPosts) {
        mNumberOfPosts = numberOfPosts;
    }

    public void setNumberOfPages(Integer numberOfPages) {
        mNumberOfPages = numberOfPages;
    }

    public Integer getNumberOfHits() {
        return mNumberOfHits;
    }

    public void setNumberOfHits(Integer numberOfHits) {
        mNumberOfHits = numberOfHits;
    }

    public Integer getNumberOfPages() {
        if(mNumberOfPages == null)
            mNumberOfPages = mNumberOfPosts / mPostsPerPage + 1;
        return mNumberOfPages;
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

    public Integer getPid() {
        return mPid;
    }

    public void setPid(Integer pid) {
        mPid = pid;
    }

    public Integer getPostsPerPage() {
        return mPostsPerPage;
    }

    public void setFirstPost(Post post) {
        mFirstPost = post;
    }

    public Post getFirstPost() {
        return mFirstPost;
    }

    public void setPostsPerPage(Integer postsPerPage) {
        mPostsPerPage = postsPerPage;
    }

    public Integer getLastFetchedPage() {
        return mLastFetchedPage;
    }

    public void setLastFetchedPage(Integer lastFetchedPage) {
        mLastFetchedPage = lastFetchedPage;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getSubTitle() {
        return mSubTitle;
    }

    public void setSubTitle(String subTitle) {
        mSubTitle = subTitle;
    }

    public Board getBoard() {
        return mBoard;
    }

    public void setBoard(Board board) {
        mBoard = board;
    }

    public User getAuthor() {
        return mAuthor;
    }

    public void setAuthor(User author) {
        mAuthor = author;
    }

    public Boolean isImportant() {
        return mIsImportant;
    }

    public void setIsImportant(Boolean isImportant) {
        mIsImportant = isImportant;
    }

    public Boolean isClosed() {
        return mIsClosed;
    }

    public void setIsClosed(Boolean isClosed) {
        mIsClosed = isClosed;
    }

    public Boolean isAnnouncement() {
        return mIsAnnouncement;
    }

    public void setIsAnnouncement(Boolean isAnnouncement) {
        mIsAnnouncement = isAnnouncement;
    }

    public Boolean isGlobal() {
        return mIsGlobal;
    }

    public void setIsGlobal(Boolean isGlobal) {
        mIsGlobal = isGlobal;
    }

    public Boolean isSticky() {
        return mIsSticky;
    }

    public void setIsSticky(Boolean isSticky) {
        mIsSticky = isSticky;
    }

    public String getNewreplytoken() {
        return mNewreplytoken;
    }

    public void setNewreplytoken(String newreplytoken) {
        mNewreplytoken = newreplytoken;
    }

    public ArrayList<Post> getPosts() {
        return mPosts;
    }

    public void addPost(Post post) {
        mPosts.add(post);
    }

    public void setPosts(ArrayList<Post> posts) {
        mPosts = posts;
    }

    public Integer getId() {
        return mId;
    }

    public void setHtmlCache(String cache) {
        mHtmlCache = cache;
    }

    public String getHtmlCache() {
        return mHtmlCache;
    }

    public Boolean isLastPage() {
        return mPage == getNumberOfPages();
    }

    public static class Xml {
        public static String TAG = "thread";
        public static String SUBTITLE_TAG = "subtitle";
        public static String TITLE_TAG = "title";
        public static String FIRSTPOST_TAG = "lastpost";
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

        public static String URL = "http://forum.mods.de/bb/xml/thread.php";

        public static String getUrl(int tid, int page, int pid) {
            Uri.Builder b = Uri.parse(URL).buildUpon();

            b.appendQueryParameter("TID", String.valueOf(tid));
            if(pid > 0) {
                b.appendQueryParameter("PID", String.valueOf(pid));
            } else {
                b.appendQueryParameter("page", String.valueOf(page));
            }
            Utils.log(b.build().toString());

            return b.build().toString();
        }
    }
}