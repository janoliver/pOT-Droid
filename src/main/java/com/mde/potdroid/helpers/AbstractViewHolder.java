package com.mde.potdroid.helpers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by oli on 7/31/15.
 */
public abstract class AbstractViewHolder<T> extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    // each data item is just a string in this case
    protected View mView;
    protected T mModel;
    protected Context mContext;


    public AbstractViewHolder(View v, Context c) {
        super(v);
        mView = v;
        mContext = c;
    }

    public AbstractViewHolder(View v, Context c, boolean bindClickListener, boolean bindLongClickListener) {
        super(v);
        mView = v;
        mContext = c;

        if(bindClickListener)
            v.setOnClickListener(this);

        if(bindLongClickListener)
            v.setOnLongClickListener(this);
    }

    public void bindModel(T t) {
        mModel = t;
    }

    public Context getContext() {
        return mContext;
    }

    public T getModel() {
        return mModel;
    }

    public View getView() {
        return mView;
    }

    @Override
    public abstract void onClick(View v);

    @Override
    public abstract boolean onLongClick(View v);
}
