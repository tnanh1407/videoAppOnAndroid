package com.example.videoapponandroid;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class VideoPlayerActivity extends AppCompatActivity {

    public static final String VIDEO_URI_KEY = "video_uri";

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player);

        videoView = findViewById(R.id.videoView);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Uri videoUri = null;
        if (getIntent() != null && getIntent().getData() != null) {
            videoUri = getIntent().getData();
        } else if (getIntent() != null && getIntent().hasExtra(VIDEO_URI_KEY)) {
            videoUri = Uri.parse(getIntent().getStringExtra(VIDEO_URI_KEY));
        }

        if (videoUri != null) {
            videoView.setVideoURI(videoUri);

            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoView.start();
                    Toast.makeText(VideoPlayerActivity.this, "Đang phát video...", Toast.LENGTH_SHORT).show();
                }
            });

            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Toast.makeText(VideoPlayerActivity.this, "Video đã phát xong.", Toast.LENGTH_SHORT).show();
                }
            });

            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Toast.makeText(VideoPlayerActivity.this, "Lỗi khi phát video: " + what + ", " + extra, Toast.LENGTH_LONG).show();
                    return false;
                }
            });

        } else {
            Toast.makeText(this, "Không tìm thấy video để phát.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }
}