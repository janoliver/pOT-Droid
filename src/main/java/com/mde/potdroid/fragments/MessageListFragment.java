package com.mde.potdroid.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.mde.potdroid.EditorActivity;
import com.mde.potdroid.MessageActivity;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.*;
import com.mde.potdroid.helpers.ptr.SwipyRefreshLayoutDirection;
import com.mde.potdroid.models.Message;
import com.mde.potdroid.models.MessageList;
import com.mde.potdroid.parsers.MessageListParser;
import org.apache.http.Header;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This fragment displays a list of messages below a Tab bar for the inbox/outbox folders.
 */
public class MessageListFragment extends BaseFragment implements LoaderManager
        .LoaderCallbacks<MessageList> {
    private static Drawable mBenderPlaceholder;

    // the tags of the fragment arguments
    public static final String ARG_MODE = "mode";
    public static final String MODE_INBOX = "inbox";
    public static final String MODE_OUTBOX = "outbox";

    // this variable also serves as fragmentmanager tag, so we have to use a String
    private String mMode;

    // message list and adapter
    private MessageList mMessageList;
    private MessageListAdapter mListAdapter;

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

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_message_list, container, false);

        mListAdapter = new MessageListAdapter(new ArrayList<Message>(), mMode);

        ObservableRecyclerView listView = (ObservableRecyclerView) v.findViewById(R.id.forum_list_content);
        listView.setAdapter(mListAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseActivity());
        listView.setLayoutManager(layoutManager);

        getActionbar().setTitle(R.string.title_messages);
        getActionbar().setSubtitle(mMode.equals(MODE_INBOX) ? R.string.tab_inbox : R.string.tab_outbox);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mMessageList == null)
            startLoader(this);
        else
            mListAdapter.setItems(mMessageList.getMessages());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_messagelist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {

            case R.id.refresh:
                // reload content
                restartLoader(this);
                return true;
            case R.id.new_message:

                Intent intent = new Intent(getBaseActivity(), EditorActivity.class);
                intent.putExtra(EditorFragment.ARG_MODE, EditorFragment.MODE_MESSAGE);
                startActivityForResult(intent, EditorFragment.MODE_MESSAGE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == EditorFragment.MODE_MESSAGE) {
            if (resultCode == Activity.RESULT_OK) {
                showSuccess(R.string.msg_send_successful);
            }
        }
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
            mListAdapter.setItems(mMessageList.getMessages());

            getBaseActivity().supportInvalidateOptionsMenu();
        } else {
            showError(getString(R.string.msg_loading_error));
        }
    }

    @Override
    public void onLoaderReset(Loader<MessageList> loader) {
        hideLoadingAnimation();
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {
        super.onRefresh(direction);
        restartLoader(this);
    }

    public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {
        private ArrayList<Message> mDataset;
        public String mMode;
        SettingsWrapper mSettings;
        BenderHandler mBenderHandler;


        public class ViewHolder extends RecyclerView.ViewHolder {

            public FrameLayout mRoot;
            public RelativeLayout mContainer;
            public TextView mTextTitle;
            public TextView mTextDescription;
            public ImageView mBender;

            public ViewHolder(FrameLayout container) {
                super(container);
                mRoot = container;
                mContainer = (RelativeLayout) container.findViewById(R.id.container);
                mTextTitle = (TextView)container.findViewById(R.id.title);
                mBender = (ImageView)container.findViewById(R.id.bender);
                mTextDescription = (TextView)container.findViewById(R.id.pages);
            }
        }

        public MessageListAdapter(ArrayList<Message> data, String mode) {
            mDataset = data;
            mMode = mode;

            mSettings = new SettingsWrapper(getContext());
            mBenderHandler = new BenderHandler(getContext());
        }

        public void setItems(ArrayList<Message> data) {
            mDataset = data;
            notifyDataSetChanged();
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MessageListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            FrameLayout v = (FrameLayout)LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listitem_message, parent, false);

            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            final Message m = mDataset.get(position);

            holder.mTextTitle.setText(Utils.fromHtml(m.getTitle()));

            // last post information
            String author = m.isSystem() ?
                    getContext().getString(R.string.pm_author_system) :
                    m.getFrom().getNick();

            String time = Utils.getFormattedTime(getString(R.string.default_time_format), m.getDate());

            Spanned content = Utils.fromHtml(getContext().getString(R.string.message_description,
                    mMode.equals(MessageList.TAG_INBOX) ? "von" : "an",
                    author, mMode.equals(MessageList.TAG_INBOX) ? "erhalten" : "gesendet",
                    time));
            holder.mTextDescription.setText(content);

            if (m.isUnread())
                holder.mRoot.setBackgroundColor(Utils.getColorByAttr(getContext(), R.attr.bbDarkerItemBackground));
            else
                holder.mRoot.setBackgroundColor(Utils.getColorByAttr(getContext(), R.attr.bbItemBackground));

            // bender. Show an alias as long as the real bender is not present. If the sender
            // is "System", hide the view.

            holder.mBender.setVisibility(View.INVISIBLE);
            if (!m.isSystem() && mSettings.showBenders()) {
                holder.mBender.setVisibility(View.VISIBLE);

                String bender_path = mBenderHandler.getAvatarFilePathIfExists(m.getFrom());
                if(bender_path != null) {
                    holder.mBender.setImageURI(Uri.parse(bender_path));
                } else {
                    // this functionality uses some primitive caching
                    if (MessageListFragment.mBenderPlaceholder == null) {
                        try {
                            MessageListFragment.mBenderPlaceholder =
                                    Utils.getDrawableFromAsset(getContext(),
                                            "images/placeholder_bender.png");

                        } catch (IOException e) {
                            Utils.printException(e);
                        }
                    }

                    if (MessageListFragment.mBenderPlaceholder == null) {
                        holder.mBender.setVisibility(View.INVISIBLE);
                    } else {
                        holder.mBender.setImageDrawable(MessageListFragment.mBenderPlaceholder);
                    }
                }
            }

            holder.mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getBaseActivity(), MessageActivity.class);
                    intent.putExtra(MessageFragment.ARG_ID, m.getId());
                    startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

    static class AsyncContentLoader extends AsyncHttpLoader<MessageList> {

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
