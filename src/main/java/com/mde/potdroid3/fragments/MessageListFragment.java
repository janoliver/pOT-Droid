package com.mde.potdroid3.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.mde.potdroid3.MessageActivity;
import com.mde.potdroid3.R;
import com.mde.potdroid3.helpers.AsyncHttpLoader;
import com.mde.potdroid3.models.Message;
import com.mde.potdroid3.models.MessageList;
import com.mde.potdroid3.parsers.MessageListParser;

import java.text.SimpleDateFormat;

/**
 * Created by oli on 11/9/13.
 */
public class MessageListFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<MessageList> {

    private String mMode;
    private MessageList mMessageList = null;
    private MessageListAdapter mListAdapter = null;
    private ListView mListView = null;

    public static MessageListFragment newInstance(String mode) {
        MessageListFragment f = new MessageListFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("mode", mode);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mMode = args.getString("mode");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = super.onCreateView(inflater, container, saved);

        mListAdapter = new MessageListAdapter();
        mListView = (ListView)v.findViewById(R.id.list_content);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getSupportActivity(), MessageActivity.class);
                intent.putExtra("message_id", mMessageList.getMessages().get(position).getId());
                startActivity(intent);
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        startLoader(this);
    }

    @Override
    public Loader<MessageList> onCreateLoader(int id, Bundle args) {
        int page = getArguments().getInt("page", 1);
        int bid = getArguments().getInt("board_id", 0);
        AsyncContentLoader l = new AsyncContentLoader(getSupportActivity(), mMode);
        showLoadingAnimation();
        return l;
    }

    @Override
    public void onLoadFinished(Loader<MessageList> loader, MessageList data) {
        hideLoadingAnimation();
        if(data != null) {
            mMessageList = data;
            mListAdapter.notifyDataSetChanged();

            getSupportActivity().supportInvalidateOptionsMenu();
            //getActionbar().setTitle(mMessageList.getName());
        } else {
            showError("Fehler beim Laden der Daten.");
        }
    }

    @Override
    public void onLoaderReset(Loader<MessageList> loader) {
        hideLoadingAnimation();
    }

    @Override
    protected int getLayout() {
        return R.layout.layout_message_list;
    }

    private class MessageListAdapter extends BaseAdapter {

        public int getCount() {
            if(mMessageList == null)
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
            View row = mInflater.inflate(R.layout.listitem_message, null);
            Message m = (Message)getItem(position);

            // set the name, striked if closed
            TextView title = (TextView) row.findViewById(R.id.title);
            title.setText(m.getTitle());

            // last post information
            String author = m.isSystem() ? "System" : m.getFrom().getNick();

            TextView description = (TextView) row.findViewById(R.id.description);
            Spanned content = Html.fromHtml("von <b>" + author
                    + "</b>, " + mMode == MessageList.TAG_INBOX ? "erhalten" : "gesendet"
                    + ": " + new SimpleDateFormat("HH:mm dd.MM.yyyy").format(m.getDate()));
            description.setText(content);

            if(m.isUnread()) {
                View v = row.findViewById(R.id.container);
                int bottom = v.getPaddingBottom();
                int top = v.getPaddingTop();
                int right = v.getPaddingRight();
                int left = v.getPaddingLeft();
                v.setBackgroundResource(R.drawable.sidebar_button_background);
                v.setPadding(left, top, right, bottom);
            }

            return row;
        }
    }

    static class AsyncContentLoader extends AsyncHttpLoader<MessageList> {
        AsyncContentLoader(Context cx, String mode) {
            super(cx, MessageList.Html.getUrl(mode), GET, null, "ISO-8859-15");
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
    }

}
