package com.whut.getianao.quickdraw.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.whut.getianao.quickdraw.R;
import com.whut.getianao.quickdraw.base.BaseActivity;
import com.whut.getianao.quickdraw.view.CustomVideoView;

public class MainActivity extends BaseActivity {
    private info.hoang8f.widget.FButton startNewGame;
    private info.hoang8f.widget.FButton joinGame;
    private info.hoang8f.widget.FButton btnIntro;
    private MediaPlayer mp;
    private CustomVideoView videoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        playVedio();
        playBGM();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopplayBGM();
        stopplayVideo();
    }

    private void playBGM() {
        mp.start();
    }

    private void stopplayBGM() {
        mp.stop();
    }

    private void stopplayVideo() {
        videoView.stopPlayback();
    }

    //播放背景视频
    private void playVedio() {
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.main_background));
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView.start();
                mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        return false;
                    }
                });
            }
        });
    }

    //播放演示视频
    private void playVedioIntro() {
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.intro));
        videoView.start();
        //播放完恢复主页面
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                startNewGame.setVisibility(View.VISIBLE);
                joinGame.setVisibility(View.VISIBLE);
                btnIntro.setVisibility(View.VISIBLE);
                playVedio();
                playBGM();
                mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        return false;
                    }
                });
            }
        });
    }

    private void init() {
        //button
        startNewGame = findViewById(R.id.tv_startNewGame);
        joinGame = findViewById(R.id.tv_joinGame);
        btnIntro = findViewById(R.id.tv_btnIntro);
        startNewGame.setButtonColor(getResources().getColor(R.color.red));
        joinGame.setButtonColor(getResources().getColor(R.color.red));
        btnIntro.setButtonColor(getResources().getColor(R.color.red));

        //bgm
        mp = MediaPlayer.create(this, R.raw.bgm);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });

        //video
        videoView = findViewById(R.id.main_videoView);

        bindBtnOnClickListener();
    }

    //按钮绑定
    private void bindBtnOnClickListener() {
        //开始游戏
        startNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ServerActivity.class);
                startActivity(intent);
            }
        });
        //创建游戏
        joinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ClientActivity.class);
                startActivity(intent);
            }
        });
        //游戏说明
        btnIntro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewGame.setVisibility(View.INVISIBLE);
                joinGame.setVisibility(View.INVISIBLE);
                btnIntro.setVisibility(View.INVISIBLE);
                stopplayBGM();
                playVedioIntro();
            }
        });
    }
}
