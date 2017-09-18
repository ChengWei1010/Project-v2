package com.project.chengwei.project_v2;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

public class VideoPopUpActivity extends AppCompatActivity {

    private VideoView videoView;
    private MediaController vidControl;
    private Uri uri;
    private ProgressDialog progressDialog;
    private FrameLayout frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_pop_up);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.9),(int)(height*.75));

        //接收uri
        final String uriString = getIntent().getStringExtra("URI");
        uri = Uri.parse(uriString);

        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setZOrderOnTop(true);
        vidControl = new android.widget.MediaController(VideoPopUpActivity.this);
        videoView.setVideoURI(uri);

        progressDialog = ProgressDialog.show(VideoPopUpActivity.this, "請稍待", "影片讀取中 ...", true);

        //Interface definition for a callback to be invoked when the media source is ready for playback.
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                progressDialog.dismiss();
                vidControl.setAnchorView(videoView);
                videoView.setMediaController(vidControl);

                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp.gravity = Gravity.BOTTOM;

                vidControl.setLayoutParams(lp);

                ((ViewGroup) vidControl.getParent()).removeView(vidControl);

                ((FrameLayout) findViewById(R.id.videoViewWrapper)).addView(vidControl);

                videoView.start();
                videoView.requestFocus();
            }
        });

    }
}
