package com.carnot.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.carnot.R;
import com.carnot.global.ConstantCode;
import com.carnot.libclasses.BaseFragment;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;

/**
 * Created by pankaj on 1/4/16.
 */
public class FragmentAddPlugInAdapter extends BaseFragment {

    ImageView img;
    ImageView icPlayerIcon;
    String imageUrl, videoUrl;

    public FragmentAddPlugInAdapter() {
        setContentView(R.layout.fragment_add_new_car_setup_plugin_adapter);
    }

    @Override
    public void initVariable() {

    }

    @Override
    public void initView() {
        img = (ImageView) links(R.id.img);
        icPlayerIcon = (ImageView) links(R.id.ic_player_icon);
        links(R.id.image_parent).setVisibility(View.INVISIBLE);
    }

    String youtubeUrl;
    YouTubePlayer youtubePlayer;
    YouTubePlayerSupportFragment youTubePlayerFragment;

    @Override
    public void addAdapter() {
        icPlayerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (youtubePlayer != null) {
                    icPlayerIcon.setVisibility(View.GONE);
                    youtubePlayer.play();
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl)));
                }
            }
        });

//        Initialize youtubeplayersupportfragment
        youTubePlayerFragment = (YouTubePlayerSupportFragment) getChildFragmentManager().findFragmentById(R.id.youtube_fragment);
        getFragmentManager().beginTransaction().hide(youTubePlayerFragment).commit();
        youTubePlayerFragment.initialize(getString(R.string.google_map_api_key), new YouTubePlayer.OnInitializedListener() { //"AIzaSyAtoUXCcrGTxELOHn5ynYsuSbwoD886nZQ"

            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
                if (!wasRestored) {
                    youtubePlayer = player;
                    youtubePlayer.setFullscreen(false);
                    youtubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS); //we used CHROMELESS Style so default controller is not visible because we use our own custom button to play the video
                    youtubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
                        @Override
                        public void onPlaying() {

                        }

                        @Override
                        public void onPaused() {

                        }

                        @Override
                        public void onStopped() {
                            icPlayerIcon.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onBuffering(boolean isBuffering) {

                            //Checking if video is buffering then we show progress bar
                            ViewGroup ytView = (ViewGroup) youTubePlayerFragment.getView(); // if you are using YouTubePlayerFragment
                            ProgressBar progressBar;
                            try {

                                ViewGroup child1 = (ViewGroup) ytView.getChildAt(0);
                                ViewGroup child2 = (ViewGroup) child1.getChildAt(3);
                                progressBar = (ProgressBar) child2.getChildAt(2);
                            } catch (Throwable t) {
                                // As its position may change, we fallback to looking for it
                                progressBar = findProgressBar(ytView);
                                // I recommend reporting this problem so that you can update the code in the try branch: direct access is more efficient than searching for it
                            }

                            int visibility = isBuffering ? View.VISIBLE : View.INVISIBLE;
                            if (progressBar != null) {
                                progressBar.setVisibility(visibility);
                                // Note that you could store the ProgressBar instance somewhere from here, and use that later instead of accessing it again.
                            }

                        }

                        @Override
                        public void onSeekTo(int i) {

                        }
                    });
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider arg0, YouTubeInitializationResult arg1) {
                // TODO Auto-generated method stub

            }
        });
    }

    //Finding progress bar from youtube view to hide and show
    private ProgressBar findProgressBar(View view) {
        if (view instanceof ProgressBar) {
            return (ProgressBar) view;
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                ProgressBar res = findProgressBar(viewGroup.getChildAt(i));
                if (res != null) return res;
            }
        }
        return null;
    }

    @Override
    public boolean sendData(Bundle bnd) {
        super.sendData(bnd);
        if (isAdded()) {
            //Checking if previous FragmentAddCarInfo Next button is clicked so we need to load url from server for specific Model and Brand.
            if (bnd.getString(ConstantCode.INTENT_ACTION).equalsIgnoreCase(ConstantCode.ACTION_UPDATE)) {
                loadUrlsFromServer(bnd.getString(ConstantCode.INTENT_BRAND), bnd.getString(ConstantCode.INTENT_MODEL));
                return true;
            }
        }
        return false;
    }

    @Override
    public void onFragmentResume() {
        super.onFragmentResume();
//        links(R.id.image_parent).setVisibility(View.GONE);
        if (youTubePlayerFragment != null) {
            getFragmentManager().beginTransaction().show(youTubePlayerFragment).commit();
        }
//        links(R.id.container_youtube).setVisibility(View.VISIBLE);
    }

    @Override
    public void onFragmentPause() {
        super.onFragmentPause();
        if (youTubePlayerFragment != null) {
            getFragmentManager().beginTransaction().hide(youTubePlayerFragment).commit();
        }
        //we are pausing youtube video when fragment pauses
        if (youtubePlayer != null) {
            youtubePlayer.pause();
//            icPlayerIcon.setVisibility(View.VISIBLE);
        }
//        links(R.id.container_youtube).setVisibility(View.INVISIBLE);
    }

    /**
     * Extract youtube Video Url from main url
     *
     * @param youtubeUrl
     * @return
     */
    private String getYoutubeUrlId(String youtubeUrl) {
        String id = null;
        try {
            String query = new URL(youtubeUrl).getQuery();
            if (query != null) {
                String[] param = query.split("&");
                for (String row : param) {
                    String[] param1 = row.split("=");
                    if (param1[0].equals("v")) {
                        id = param1[1];
                    }
                }
            } else {
                if (youtubeUrl.contains("embed")) {
                    id = youtubeUrl.substring(youtubeUrl.lastIndexOf("/") + 1);
                }
            }
        } catch (Exception ex) {
            Log.e("Exception", ex.toString());
        }
        return id;
    }

    /**
     * Loading Image or Video Urls from server.
     *
     * @param brand
     * @param model
     */
    private void loadUrlsFromServer(String brand, String model) {
        /*Bundle bnd = ((ActivityAddNewCarSetup) mActivity).bundle;
        if (!bnd.containsKey(ConstantCode.INTENT_BRAND))
            return;*/

        HashMap<String, Object> param = new HashMap<>();
        param.put(ConstantCode.brand, brand);
        param.put(ConstantCode.model, model);
        /*String brand = bnd.getString(ConstantCode.INTENT_BRAND);
        String model = bnd.getString(ConstantCode.INTENT_MODEL);*/
        WebUtils.call(WebServiceConfig.WebService.GET_INSTRUCTION_VIDEO_URL, null, param, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);

                try {
                    JSONObject json = (JSONObject) values;
                    JSONObject jsonData = json.optJSONObject(ConstantCode.data);
                    youtubeUrl = jsonData.optString(ConstantCode.videoUrl);

                    //Getting Youtube Id from video url
                    String youtubeId = getYoutubeUrlId(jsonData.optString(ConstantCode.videoUrl));

                    //Getting thumbnail Url for Youtube Video
                    String thumbnailUrl = String.format("http://img.youtube.com/vi/%s/0.jpg", youtubeId);
                    links(R.id.image_parent).setVisibility(View.VISIBLE);

                    //Checking if Youtube id is not Null then we need to show the load Youtube url in YoutubePlayer else simply put the image
                    if (!TextUtils.isEmpty(youtubeId)) {

                        icPlayerIcon.setVisibility(View.VISIBLE);
//                    //New to show youtube player api
//                    if (youTubePlayerFragment != null) {
//                        getFragmentManager().beginTransaction().show(youTubePlayerFragment).commit();
//                    }
//                        icPlayerIcon.setVisibility(View.GONE);
                        img.setVisibility(View.GONE);
                        if (youtubePlayer != null) {
                            try {
                                //Loading youtube url in youtubePlayer
                                youtubePlayer.loadVideo(youtubeId);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    } else {

                        //We need to load imageUrl
                        icPlayerIcon.setVisibility(View.GONE);

                        //hiding youtubeplayer fragment because we are showing image instead of youtube url
                        if (youTubePlayerFragment != null) {
                            getFragmentManager().beginTransaction().hide(youTubePlayerFragment).commit();
                        }


                        String imageUrl = jsonData.optString(ConstantCode.imageUrl);
//                        imageUrl = imageUrl.replaceAll("////", "");
                        Log.e("ws", "Loading Image " + imageUrl);
                        links(R.id.progress_image).setVisibility(View.VISIBLE);

                        //Loading image urln using glide
                        Glide.with(mActivity)
                                .load(imageUrl)
                                .error(R.drawable.img_no_image)
                                .crossFade()
                                .listener(new RequestListener<String, GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        links(R.id.image_parent).setVisibility(View.GONE);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        links(R.id.progress_image).setVisibility(View.GONE);
                                        links(R.id.image_parent).setVisibility(View.VISIBLE);
                                        return false;
                                    }
                                }).
                                into(img);
                        img.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
//                progressDialog.dismiss();
                showToast("failed to save " + values.toString());
            }

            @Override
            public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                super.failedForNetwork(values, webService);
//                progressDialog.dismiss();
            }
        });

    }

    @Override
    public void loadData() {

    }
}
