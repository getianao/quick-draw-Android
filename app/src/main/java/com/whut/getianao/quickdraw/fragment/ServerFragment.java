package com.whut.getianao.quickdraw.fragment;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.whut.getianao.quickdraw.R;
import com.whut.getianao.quickdraw.activity.ServerActivity;
import com.whut.getianao.quickdraw.view.CustomVideoView;

public class ServerFragment extends Fragment {
    private ServerActivity parentActivity;
    private TextView text_state;
    private info.hoang8f.widget.FButton btn_closeWifi;
    private info.hoang8f.widget.FButton btn_createWifi;
    private info.hoang8f.widget.FButton btn_send;
    private info.hoang8f.widget.FButton btn_startGame;
    private View view;
    private CustomVideoView videoView;
    private QMUITipDialog tipDialog;
    private QMUITipDialog acceptDialog;

    public QMUITipDialog getTipDialog() {
        return tipDialog;
    }

    public QMUITipDialog getAcceptDialog() {
        return acceptDialog;
    }

    public TextView getText_state() {
        return text_state;
    }

    public Button getBtn_closeWifi() {
        return btn_closeWifi;
    }

    public Button getBtn_createWifi() {
        return btn_createWifi;
    }

    public Button getBtn_send() {
        return btn_send;
    }

    public Button getBtn_startGame() {
        return btn_startGame;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_server, container, false);
        parentActivity = (ServerActivity) getActivity();
        init(view);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopplayVideo();
    }

    @Override
    public void onResume() {
        super.onResume();
        playVedio();

    }

    private void stopplayVideo() {
        videoView.stopPlayback();
    }

    private void playVedio() {
        videoView.start();
    }

    private void init(View view) {
        //button
        btn_createWifi = view.findViewById(R.id.create_wifi);
        btn_closeWifi = view.findViewById(R.id.close_wifi);
        btn_send = view.findViewById(R.id.send);
        btn_startGame = view.findViewById(R.id.start_game);
        btn_createWifi.setButtonColor(getResources().getColor(R.color.red));
        btn_closeWifi.setButtonColor(getResources().getColor(R.color.red));
        btn_send.setButtonColor(getResources().getColor(R.color.red));
        btn_startGame.setButtonColor(getResources().getColor(R.color.red));
        btn_startGame.setEnabled(false);//disable 开始游戏直到匹配到客户端
        btn_startGame.setVisibility(View.GONE);
        btn_closeWifi.setVisibility(View.GONE);
        btn_send.setVisibility(View.GONE);

        //textview
        text_state = view.findViewById(R.id.receive);

        //背景视频
        videoView = view.findViewById(R.id.Server_videoView);
        videoView.setVideoURI(Uri.parse("android.resource://" + getContext().getPackageName() + "/" + R.raw.client_background));
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

        //Dialog
        tipDialog = new QMUITipDialog.Builder(getContext())
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在搜索")
                .create();
        acceptDialog = new QMUITipDialog.Builder(getContext())
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                .setTipWord("连接成功")
                .create();

        bindBtnOnClickListener();

    }

    //按钮绑定
    private void bindBtnOnClickListener() {
        //开启热点，等待客户端连接、确认
        btn_createWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipDialog.show();
                //热点原本未开启，提示开启
                if (parentActivity.isWifiApOpen(getContext()) == false) {
                    parentActivity.createWifiHotspot();
                    tipDialog.dismiss();
                    return;
                }
                btn_createWifi.setEnabled(false);
                btn_createWifi.setText("等待连接");
            }
        });

        //关闭热点
        btn_closeWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.closeWifiHotspot();
            }
        });

        //发送数据
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (parentActivity.getConnectThread() != null) {
                    parentActivity.getConnectThread().sendData("这是来自Wifi热点的消息");
                } else {
                    Log.w("AAA", "connectThread == null");
                }
            }
        });
    }



}
