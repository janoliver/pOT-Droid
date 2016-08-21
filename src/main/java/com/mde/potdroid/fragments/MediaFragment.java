package com.mde.potdroid.fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.view.*;
import android.widget.FrameLayout;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.listener.VideoControlsVisibilityListener;
import com.devbrackets.android.exomedia.ui.widget.EMVideoView;
import com.felipecsl.gifimageview.library.GifImageView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.mde.potdroid.BaseActivity;
import com.mde.potdroid.BuildConfig;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.ImageHandler;
import com.mde.potdroid.helpers.Network;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fragment that shows some information of the app
 */
public class MediaFragment extends BaseFragment implements OnPreparedListener {

    private FullScreenListener mFullScreenListener;
    private EMVideoView mVideoView;
    private PhotoView mImageView;
    private GifImageView mGifImageView;
    private PhotoViewAttacher mAttacher;
    private FrameLayout mYTView;
    private boolean mVideoViewPausedInOnStop;
    public final static String ARG_URI = "uri";
    public final static String ARG_TYPE = "type";
    //private ShareActionProvider mShareActionProvider;
    private Intent mShareIntent;

    /**
     * Create a new instance of AboutFragment and set the arguments
     *
     * @return AboutFragment instance
     */
    public static MediaFragment newInstance(Bundle args) {
        MediaFragment f = new MediaFragment();
        f.setArguments(args);
        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        setRetainInstance(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mFullScreenListener = new FullScreenListener();
        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.actionmenu_media, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.share:
                shareMedia();
                return true;
            case R.id.save:
                getBaseActivity().verifyStoragePermissions(getActivity(), new BaseActivity.ExternalPermissionCallback() {
                    @Override
                    public void granted() {
                        saveMedia();
                    }

                    @Override
                    public void denied() {
                        showError(R.string.msg_permission_denied_error);
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void shareMedia() {
        if (mShareIntent != null)
            startActivity(Intent.createChooser(mShareIntent, "Share with"));
        else
            showError(R.string.msg_share_error);

    }

    public void saveMedia() {
        Uri uri = Uri.parse(getArguments().getString(ARG_URI));

        String type = getArguments().getString(ARG_TYPE);

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        if (type.compareTo("gif") == 0 || type.compareTo("image") == 0) {

            ImageHandler.downloadImage(getActivity(), dir, uri, new ImageHandler.ImageDownloadCallback() {
                @Override
                public void onSuccess(Uri uri, File f) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showSuccess(R.string.msg_img_download_success);
                        }
                    });
                }

                @Override
                public void onFailure(Uri uri, Exception e) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            showError(R.string.msg_img_download_error);
                        }
                    });
                }
            });
        } else if (type.compareTo("video") == 0) {
            Network.downloadFile(getActivity(), uri, dir, new Network.DownloadCallback() {
                @Override
                public void onSuccess(Uri uri, File download) {
                    getBaseActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showSuccess(R.string.msg_video_download_success);
                        }
                    });
                }

                @Override
                public void onFailure(Uri uri, Exception e) {
                    e.printStackTrace();
                    getBaseActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            showError(R.string.msg_video_download_error);
                        }
                    });
                }
            });

        } else if (type.compareTo("youtube") == 0) {
            showError(R.string.msg_youtube_download_error);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_media, container, false);
        mVideoView = (EMVideoView) v.findViewById(R.id.video);
        mGifImageView = (GifImageView) v.findViewById(R.id.gif);
        mImageView = (PhotoView) v.findViewById(R.id.image);
        mYTView = (FrameLayout) v.findViewById(R.id.youtube_layout);

        mVideoView.setOnPreparedListener(this);
        mAttacher = new PhotoViewAttacher(mImageView);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Uri uri = Uri.parse(getArguments().getString(ARG_URI));

        String type = getArguments().getString(ARG_TYPE);

        ImageHandler ih = ImageHandler.getPictureHandler(getActivity());

        if (type.compareTo("gif") == 0) {
            showLoadingAnimation();
            ih.retrieveImage(uri.toString(), new ImageHandler.ImageHandlerCallback() {
                @Override
                public void onSuccess(String url, final String path, boolean from_cache) {
                    getBaseActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            hideLoadingAnimation();
                            InputStream is;
                            try {
                                is = getActivity().getContentResolver().openInputStream(Uri.parse(path));
                                mGifImageView.setBytes(streamToBytes(is));
                                mGifImageView.setVisibility(View.VISIBLE);
                                mGifImageView.startAnimation();
                            } catch (IOException e) {
                                showError(R.string.msg_img_loading_error);
                                e.printStackTrace();
                            }
                        }
                    });

                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
                    shareIntent.setType("image/*");
                    /*if(mShareActionProvider != null) {
                        mShareActionProvider.setShareIntent(shareIntent);
                    }*/
                    mShareIntent = shareIntent;
                }

                @Override
                public void onFailure(String url) {
                    getBaseActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            hideLoadingAnimation();
                            showError(R.string.msg_img_loading_error);
                        }
                    });
                }
            });

        } else if (type.compareTo("image") == 0) {
            showLoadingAnimation();
            try {
                ih.retrieveImage(uri.toString(), new ImageHandler.ImageHandlerCallback() {
                    @Override
                    public void onSuccess(String url, final String path, boolean from_cache) {
                        getBaseActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                mImageView.setVisibility(View.VISIBLE);
                                mImageView.setImageURI(Uri.parse(path));
                                mAttacher.update();
                                hideLoadingAnimation();
                            }
                        });
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
                        shareIntent.setType("image/*");
                        mShareIntent = shareIntent;
                    }

                    @Override
                    public void onFailure(String url) {
                        getBaseActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                hideLoadingAnimation();
                                showError(R.string.msg_img_loading_error);
                            }
                        });
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                hideLoadingAnimation();
            }
        } else if (type.compareTo("video") == 0) {
            mVideoView.setVisibility(View.VISIBLE);
            mVideoView.setVideoURI(uri);


            /*goFullscreen();
            if (mVideoView.getVideoControls() != null) {
                mVideoView.getVideoControls().setVisibilityListener(new ControlsVisibilityListener());
            }*/

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, uri.toString());
            shareIntent.setType("text/plain");
            mShareIntent = shareIntent;
        } else if (type.compareTo("youtube") == 0) {
            showLoadingAnimation();
            mYTView.setVisibility(View.VISIBLE);

            YouTubePlayerSupportFragment youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();

            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.youtube_layout, youTubePlayerFragment).commit();

            youTubePlayerFragment.initialize(BuildConfig.YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {

                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
                    if (!wasRestored) {
                        player.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                        player.loadVideo(extractYTId(uri.toString()));
                        player.play();

                        getBaseActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                hideLoadingAnimation();
                            }
                        });
                    }
                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult error) {
                    // YouTube error
                    getBaseActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            showError(R.string.msg_img_loading_error);
                            hideLoadingAnimation();
                        }
                    });
                }
            });

        }

    }


    @Override
    public void onStop() {
        super.onStop();

        mGifImageView.stopAnimation();
        mVideoView.stopPlayback();

        if (mVideoView.isPlaying()) {
            mVideoViewPausedInOnStop = true;
            mVideoView.pause();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mVideoViewPausedInOnStop) {
            mVideoView.start();
            mVideoViewPausedInOnStop = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        exitFullscreen();
    }

    @Override
    public void onPrepared() {
        //Starts the video playback as soon as it is ready
        mVideoView.start();
    }

    static byte[] streamToBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }


    public static String extractYTId(@NonNull String videoUrl) {

        final String reg = "(?:youtube(?:-nocookie)?\\.com\\/(?:[^\\/\\n\\s]+\\/\\S+\\/|(?:v|e(?:mbed)?)\\/|\\S*?[?&]v=)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})";

        Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(videoUrl);

        if (matcher.find())
            return matcher.group(1);
        return null;
    }


    private void goFullscreen() {
        setUiFlags(true);
    }

    private void exitFullscreen() {
        setUiFlags(false);
    }

    /**
     * Applies the correct flags to the windows decor view to enter
     * or exit fullscreen mode
     *
     * @param fullscreen True if entering fullscreen mode
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setUiFlags(boolean fullscreen) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            View decorView = getActivity().getWindow().getDecorView();
            if (decorView != null) {
                decorView.setSystemUiVisibility(fullscreen ? getFullscreenUiFlags() : View.SYSTEM_UI_FLAG_VISIBLE);
                decorView.setOnSystemUiVisibilityChangeListener(mFullScreenListener);
            }
        }
    }

    /**
     * Determines the appropriate fullscreen flags based on the
     * systems API version.
     *
     * @return The appropriate decor view flags to enter fullscreen mode when supported
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private int getFullscreenUiFlags() {
        int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        return flags;
    }

    /**
     * Listens to the system to determine when to show the default controls
     * for the {@link EMVideoView}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class FullScreenListener implements View.OnSystemUiVisibilityChangeListener {
        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                mVideoView.showControls();
            }
        }
    }

    private class ControlsVisibilityListener implements VideoControlsVisibilityListener {
        @Override
        public void onControlsShown() {
            // No additional functionality performed
        }

        @Override
        public void onControlsHidden() {
            goFullscreen();
        }
    }
}
