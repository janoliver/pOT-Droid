package com.mde.potdroid.models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Thread model.
 */
public class Topic implements Serializable
{

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

    public Topic() {
    }

    public void setId(int id) {
        mId = id;
    }

    public Integer getNumberOfPosts() {
        return mNumberOfPosts;
    }

    public void setNumberOfPosts(Integer numberOfPosts) {
        mNumberOfPosts = numberOfPosts + 1;
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
        if (mNumberOfPages == null)
            mNumberOfPages = (mNumberOfPosts-1) / mPostsPerPage + 1;
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

    public Integer getIconId() {
        return mFirstPost.getIconId();
    }

    public String getIconFile() {
        return mFirstPost.getIconFile();
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
        return mPage.equals(getNumberOfPages());
    }

    public Post getPostById(int id) {
        for (Post p : mPosts)
            if (p.getId() == id)
                return p;

        return null;
    }

}