package com.mde.potdroid.fragments;

import android.os.Bundle;
import android.view.*;

import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.content.Intent;

import com.mde.potdroid.R;
import com.mde.potdroid.TopicActivity;
import com.mde.potdroid.helpers.PostStorageHandler;
import com.mde.potdroid.helpers.PostStorageHandler.StoredPostInfo;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;

import java.util.List;

import com.afollestad.materialdialogs.MaterialDialog;

public class StoredPostsFragment extends BaseFragment {
    private PostStorageHandler mPostStorage = null;
    private PostStorageAdapter mPostStorageAdapter = null;

    public static StoredPostsFragment newInstance() {
        return new StoredPostsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_storedposts, container, false);
        getActionbar().setTitle(R.string.title_storedposts);

        mPostStorage = new PostStorageHandler(getContext());

        mPostStorageAdapter = new PostStorageAdapter(mPostStorage.getPosts());
        RecyclerView listView = (RecyclerView) v.findViewById(R.id.storedposts_list_content);
        listView.setLayoutManager(new LinearLayoutManager(getBaseActivity()));
        listView.setAdapter(mPostStorageAdapter);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionmenu_storedposts, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                if (mPostStorage.clearStorage())
                    showSuccess(R.string.msg_storage_cleared);
                return true;
            case R.id.export:
                if (mPostStorage.export())
                    showSuccess(R.string.msg_storage_exported);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class PostStorageAdapter extends RecyclerView.Adapter<PostStorageAdapter.ViewHolder> {
        private List<StoredPostInfo> mDataset;

        public class ViewHolder extends RecyclerView.ViewHolder {

            public FrameLayout mRoot;
            public RelativeLayout mContainer;
            public TextView mTextPoster;
            public TextView mTextThread;
            public TextView mTextUrl;

            public ViewHolder(FrameLayout container) {
                super(container);
                mRoot = container;
                mContainer = (RelativeLayout) container.findViewById(R.id.container);
                mTextPoster = (TextView) mContainer.findViewById(R.id.poster);
                mTextThread = (TextView) mContainer.findViewById(R.id.thread);
                mTextUrl = (TextView) mContainer.findViewById(R.id.url);
            }
        }

        public PostStorageAdapter(List<StoredPostInfo> data) {
            mDataset = data;
        }

        public void setItems(List<StoredPostInfo> data) {
            mDataset = data;
            notifyDataSetChanged();
        }

        // Create new views (invoked by the layout manager)
        @Override
        public PostStorageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            FrameLayout v = (FrameLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listitem_storedpost, parent, false);

            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            final StoredPostInfo b = mDataset.get(position);

            // set the topic
            TextView title = holder.mTextThread;
            title.setText(b.topic);

            // set the name of the poster
            TextView poster = holder.mTextPoster;
            poster.setText(b.poster);

            holder.mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getBaseActivity(), TopicActivity.class);
                    intent.putExtra(TopicFragment.ARG_POST_ID, (Integer) b.id_post);
                    intent.putExtra(TopicFragment.ARG_TOPIC_ID, (Integer) b.id_topic);
                    startActivity(intent);
                }
            });

            holder.mContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new MaterialDialog.Builder(getActivity())
                            .content(R.string.action_remove_storedpost)
                            .positiveText("Ok")
                            .negativeText("Abbrechen")
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    if (mPostStorage.deletePost(b.id_post, b.id_topic)) {
                                        showSuccess(R.string.msg_storedpost_deleted);
                                    }
                                };
                            }).show();
                    return true;
                };
            });
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }
}
