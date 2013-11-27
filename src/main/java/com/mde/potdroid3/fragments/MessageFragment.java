package com.mde.potdroid3.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mde.potdroid3.BaseActivity;
import com.mde.potdroid3.R;
import com.mde.potdroid3.helpers.AsyncHTTPLoader;
import com.mde.potdroid3.models.Message;
import com.mde.potdroid3.parsers.MessageParser;

public class MessageFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<Message> {

    private Message mMessage;
    private BaseActivity mActivity;

    private TextView mFromView;
    private TextView mTitleView;
    private TextView mTextView;

    public static MessageFragment newInstance(int message_id) {
        MessageFragment f = new MessageFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("message_id", message_id);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (BaseActivity) getSupportActivity();
        startLoader(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = super.onCreateView(inflater, container, saved);

        mFromView = (TextView)v.findViewById(R.id.text_from);
        mTitleView = (TextView)v.findViewById(R.id.text_title);
        mTextView = (TextView)v.findViewById(R.id.text_text);

        return v;
    }

    @Override
    public Loader<Message> onCreateLoader(int id, Bundle args) {
        int mid = getArguments().getInt("message_id", 0);
        AsyncContentLoader l = new AsyncContentLoader(getSupportActivity(), mid);
        showLoadingAnimation();

        return l;
    }

    @Override
    public void onLoadFinished(Loader<Message> loader, Message data) {
        hideLoadingAnimation();

        if(data != null) {

            // update the topic data
            mMessage = data;

            mActivity.getActionBar().setTitle(mMessage.getTitle());
            //getSupportActivity().getActionBar().setSubtitle(subtitleText);

            mFromView.setText(mMessage.getFrom().getNick());
            mTitleView.setText(mMessage.getTitle());
            mTextView.setText(Html.fromHtml(mMessage.getText()));

            // populate right sidebar
            //mActivity.getRightSidebar().setIsNewMessage(mMessage.getId());

        } else {
            showError("Fehler beim Laden der Daten.");
        }
    }

    @Override
    public void onLoaderReset(Loader<Message> loader) {
        hideLoadingAnimation();
    }

    protected int getLayout() {
        return R.layout.layout_topic;
    }

    static class AsyncContentLoader extends AsyncHTTPLoader<Message> {
        private Integer mMessageId;

        AsyncContentLoader(Context cx, Integer message_id) {
            super(cx, Message.Html.getUrl(message_id), GET, null, "ISO-8859-15");

            mMessageId = message_id;
        }

        @Override
        public Message parseResponse(String response) {
            try {
                MessageParser parser = new MessageParser();
                return parser.parse(response, mMessageId);
            } catch (Exception e) {
                return null;
            }
        }
    }

}
