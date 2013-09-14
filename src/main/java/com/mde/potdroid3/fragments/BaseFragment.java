package com.mde.potdroid3.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mde.potdroid3.R;
import com.mde.potdroid3.helpers.Network;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class provides some methods to show Notifications on top of the fragment.
 */
public abstract class BaseFragment extends Fragment {

    private Map<String, View> mNotifications = new HashMap<String, View>();
    private LinearLayout mNotificationContainer;
    protected LayoutInflater mInflater;

    protected Network mNetwork = null;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNetwork = new Network(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        return inflater.inflate(getLayout(), container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // our layout _must_ contain this LinearLayout! Use
        // <include layout="@layout/notification_container"/> to include it!
        // It must be included below the contents of the fragment
        mNotificationContainer = (LinearLayout)getView().findViewById(R.id.notifications);
    }

    abstract protected int getLayout();

    /**
     * Start the content loader
     */
    public void startLoader(LoaderManager.LoaderCallbacks l) {
        getLoaderManager().initLoader(0, null, l).forceLoad();
    }

    /**
     * Restart the content loader
     */
    public void restartLoader(LoaderManager.LoaderCallbacks l) {
        getLoaderManager().restartLoader(0, null, l).forceLoad();
    }

    /**
     * Shows a success message.
     */
    public void showSuccess(String text) {
        showMessage(0xaa00bb00, "Success", text, "success", false);
    }

    /**
     * Shows an error message
     */
    public void showError(String text) {
        showMessage(0xaaff0000, "Error", text, "error", false);
    }

    /**
     * Shows a temporary success message
     */
    public void showTimedSuccess(String text) {
        showMessage(0xaa00bb00, "Success", text, "success", true);
    }

    /**
     * Shows a temporary error message
     */
    public void showTimedError(String text) {
        showMessage(0xaaff0000, "Error", text, "error", true);
    }

    /**
     * Only for internal use. Shows a notification message.
     */
    private void showMessage(int color, String title, String text, String key, Boolean timed) {
        View layout = mInflater.inflate(R.layout.notification_message, null);

        // find and modify layout
        layout.findViewById(R.id.container).setBackground(new ColorDrawable(color));
        ((TextView)layout.findViewById(R.id.msg_title)).setText(title);
        ((TextView)layout.findViewById(R.id.msg_text)).setText(text);

        // add dismiss ontouchlistener
        final String f_key = key;
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideNotification(f_key);
                return true;
            }
        });

        if(timed)
            timedNotification(key, layout);
        else
            showNotification(key, layout);

    }

    /**
     * Shows a "loading" message with a small loading animation
     */
    public void showLoader() {
        View layout = mInflater.inflate(R.layout.notification_loader, null);
        showNotification("loader", layout);
    }

    /**
     * Hides the loading message
     */
    public void hideLoader() {
        hideNotification("loader");
    }

    /**
     * Shows a notification and starts a timer to dismiss it.
     */
    private void timedNotification(String key, View layout) {

        showNotification(key, layout);

        // our runnable that dismisses the notification
        class Timer_Tick implements Runnable {
            String mKey;

            Timer_Tick(String key) {
                mKey = key;
            }

            public void run() {
                hideNotification(mKey);
            }
        };

        // start the 3 second timer
        Timer hideTimer = new Timer();
        final String t_key = key;
        hideTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Timer_Tick(t_key));
            }
        }, 3000);
    }

    /**
     * Hide/remove a notification
     */
    private void hideNotification(String key) {
        if(!mNotifications.containsKey(key))
            return;

        View to_remove = mNotifications.remove(key);

        Animation animFadeOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
        if(mNotifications.isEmpty()) {
            // remove the whole container using an animation
            animFadeOut.setAnimationListener(new FadeOutAnimationListener(to_remove, true));
            mNotificationContainer.startAnimation(animFadeOut);
        } else {
            // remove only the one notification
            animFadeOut.setAnimationListener(new FadeOutAnimationListener(to_remove, false));
            to_remove.startAnimation(animFadeOut);
        }
    }

    /**
     * hide/remove all notifications
     */
    public void hideAllNotifications() {
        mNotifications.clear();
        Animation animFadeOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
        animFadeOut.setAnimationListener(new FadeOutAnimationListener(null, true));
        mNotificationContainer.startAnimation(animFadeOut);
    }

    /**
     * Show a notification
     */
    private void showNotification(String key, View layout) {

        // does it exist already?
        if(mNotifications.containsKey(key))
            return;

        mNotifications.put(key, layout);
        mNotificationContainer.addView(layout);

        Animation animFadeIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        if(mNotifications.size() == 1) {
            animFadeIn.setAnimationListener(new FadeInAnimationListener(layout, true));
            mNotificationContainer.startAnimation(animFadeIn);
        } else {
            animFadeIn.setAnimationListener(new FadeInAnimationListener(layout, false));
            layout.startAnimation(animFadeIn);
        }
    }

    /**
     * The animator for the fade-in
     */
    private class FadeInAnimationListener implements Animation.AnimationListener {
        private View mView;
        private Boolean mShowContainer;

        public FadeInAnimationListener(View layout, Boolean showContainer) {
            mView = layout;
            mShowContainer = showContainer;
        }

        @Override
        public void onAnimationEnd(Animation animation) {}

        @Override
        public void onAnimationRepeat(Animation animation) {}

        @Override
        public void onAnimationStart(Animation animation) {
            if(mShowContainer)
                mNotificationContainer.setVisibility(View.VISIBLE);
        }

    }

    /**
     * The animator for the fade-out
     */
    private class FadeOutAnimationListener implements Animation.AnimationListener {
        private View mView;
        private Boolean mRemoveContainer;

        public FadeOutAnimationListener(View layout, Boolean removeContainer) {
            mView = layout;
            mRemoveContainer = removeContainer;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if(mView != null)
                mNotificationContainer.removeView(mView);
            else
                mNotificationContainer.removeAllViews();

            if(mRemoveContainer)
                mNotificationContainer.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}

        @Override
        public void onAnimationStart(Animation animation) {}

    }

}
