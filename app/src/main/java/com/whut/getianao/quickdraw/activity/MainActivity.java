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
    private CustomVideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();


    }

    @Override
    protected void onResume() {
        super.onResume();
        playVedio();
        playBGM();
    }

    @Override
    protected void onPause() {
        stopplayBGM();
        super.onPause();
    }

    //播放bgm
    private  void  playBGM()
    {
        MediaPlayer mp =MediaPlayer.create(this, R.raw.bgm);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });
    }
    private  void  stopplayBGM()
    {
        MediaPlayer mp =MediaPlayer.create(this, R.raw.bgm);
        mp.stop();
    }
    private void playVedio(){
        videoView =  findViewById(R.id.main_videoView);
        videoView.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.main_background));
        //播放
        videoView.start();
        //循环播放
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
    /**
     * 初始化
     */
    private void initView() {
        startNewGame = findViewById(R.id.tv_startNewGame);
        joinGame = findViewById(R.id.tv_joinGame);

        startNewGame.setButtonColor(getResources().getColor(R.color.red));
        joinGame.setButtonColor(getResources().getColor(R.color.red));

        String  font="fonts/BigMountain.ttf";
        Typeface typeface=Typeface.createFromAsset(getAssets(),font);
        startNewGame.setTypeface(typeface);
        joinGame.setTypeface(typeface);

        //加入游戏
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



    }

}
