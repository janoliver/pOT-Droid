package com.mde.potdroid.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.mde.potdroid.MessageActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.*;
import com.mde.potdroid.models.Message;
import com.mde.potdroid.models.MessageList;
import com.mde.potdroid.parsers.MessageListParser;
import org.apache.http.Header;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * This fragment displays a list of messages below a Tab bar for the inbox/outbox folders.
 */
public class MessageListFragment extends BaseFragment implements LoaderManager
        .LoaderCallbacks<MessageList>, OnRefreshListener
{

    // the tags of the fragment arguments
    public static final String ARG_MODE = "mode";
    public static final String MODE_INBOX = "inbox";
    public static final String MODE_OUTBOX = "inbox";

    // this variable also serves as fragmentmanager tag, so we have to use a String
    private String mMode;

    // message list and adapter
    private MessageList mMessageList;
    private MessageListAdapter mListAdapter;

    // the BenderHandler is used to display benders in front of the lines
    private BenderHandler mBenderHandler;
    private SettingsWrapper mSettings;

    /**
     * Returns an instance of the Fragment and sets required parameters as Arguments
     *
     * @param mode Which mode to use, INBOX or OUTBOX
     * @return MessageListFragment object
     */
    public static MessageListFragment newInstance(String mode) {
        MessageListFragment f = new MessageListFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString(ARG_MODE, mode);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mMode = args.getString(ARG_MODE);
        mSettings = new SettingsWrapper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_message_list, container, false);

        mListAdapter = new MessageListAdapter();
        ListView mListView = (ListView) v.findViewById(R.id.list_content);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getBaseActivity(), MessageActivity.class);
                intent.putExtra(MessageFragment.ARG_ID, mMessageList.getMessages().get(position)
                        .getId());
                startActivity(intent);
            }
        });

        getActionbar().setTitle(R.string.messages);
        getActionbar().setSubtitle(mMode.equals(MODE_INBOX) ? R.string.inbox : R.string.outbox);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getBaseActivity().enableLeftSidebar();
        getBaseActivity().enableRightSidebar();
        getBaseActivity().getRightSidebarFragment().setIsMessage(null);
        getBaseActivity().getRightSidebarFragment().setFormListener(new FormFragment.FormListener()
        {
            @Override
            public void onSuccess(Bundle result) {
                getBaseActivity().closeRightSidebar();
                Utils.toast(getBaseActivity(), getString(R.string.send_successful));
            }

            @Override
            public void onFailure(Bundle result) {
                Utils.toast(getBaseActivity(), getString(R.string.send_failure));
            }
        });

        mBenderHandler = new BenderHandler(getBaseActivity());

        if (mMessageList == null)
            startLoader(this);
    }

    @Override
    public Loader<MessageList> onCreateLoader(int id, Bundle args) {
        AsyncContentLoader l = new AsyncContentLoader(getBaseActivity(), mMode);
        showLoadingAnimation();
        return l;
    }

    @Override
    public void onLoadFinished(Loader<MessageList> loader, MessageList data) {
        hideLoadingAnimation();
        if (data != null) {
            mMessageList = data;
            mListAdapter.notifyDataSetChanged();

            getBaseActivity().supportInvalidateOptionsMenu();
        } else {
            showError(getString(R.string.loading_error));
        }
    }

    @Override
    public void onLoaderReset(Loader<MessageList> loader) {
        hideLoadingAnimation();
    }

    @Override
    public void onRefreshStarted(View view) {
        super.onRefreshStarted(view);
        restartLoader(this);
    }

    private class MessageListAdapter extends BaseAdapter
    {

        public int getCount() {
            if (mMessageList == null)
                return 0;
            return mMessageList.getMessages().size();
        }

        public Object getItem(int position) {
            return mMessageList.getMessages().get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = getInflater().inflate(R.layout.listitem_message, null);
            Message m = (Message) getItem(position);

            // set the name, striked if closed
            TextView title = (TextView) row.findViewById(R.id.title);
            title.setText(m.getTitle());

            // last post information
            String author = m.isSystem() ? "System" : m.getFrom().getNick();

            TextView description = (TextView) row.findViewById(R.id.pages);
            Spanned content = Html.fromHtml(getString(R.string.message_description,
                    author, mMode.equals(MessageList.TAG_INBOX) ? "erhalten" : "gesendet",
                    new SimpleDateFormat(getString(R.string.standard_time_format)).format(m
                            .getDate())));
            description.setText(content);

            if (m.isUnread()) {
                View v = row.findViewById(R.id.container);
                int padding_top = v.getPaddingTop();
                int padding_bottom = v.getPaddingBottom();
                int padding_right = v.getPaddingRight();
                int padding_left = v.getPaddingLeft();

                v.setBackgroundResource(R.drawable.sidebar_button_background);
                v.setPadding(padding_left, padding_top, padding_right, padding_bottom);
            }

            // bender. Show an alias as long as the real bender is not present. If the sender
            // is "System", hide the view.
            final ImageView bender_img = (ImageView) row.findViewById(R.id.bender);

            if (!m.isSystem() && mSettings.showBenders()) {

                try {
                    Drawable d = Utils.getDrawableFromAsset(getBaseActivity(),
                            "images/placeholder_bender.png");
                    bender_img.setImageDrawable(d);
                } catch (IOException e) {
                    bender_img.setVisibility(View.GONE);
                }

                mBenderHandler.getAvatar(m.getFrom(), new BenderHandler.BenderListener()
                {
                    @Override
                    public void onSuccess(String path) {
                        bender_img.setImageURI(Uri.parse(path));
                    }

                    @Override
                    public void onFailure() {
                    }
                });
            } else {
                bender_img.setVisibility(View.GONE);
            }

            return row;
        }
    }

    static class AsyncContentLoader extends AsyncHttpLoader<MessageList>
    {

        AsyncContentLoader(Context cx, String mode) {
            super(cx, MessageListParser.getUrl(mode), GET, null, Network.ENCODING_ISO);
        }

        @Override
        public MessageList processNetworkResponse(String response) {
            try {
                MessageListParser parser = new MessageListParser();
                return parser.parse(response);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onNetworkFailure(int statusCode, Header[] headers,
                                        String responseBody, Throwable error) {

            Utils.printException(error);
            deliverResult(null);
        }
    }

}
