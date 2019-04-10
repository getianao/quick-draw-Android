package com.whut.getianao.quickdraw.fragment;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.whut.getianao.quickdraw.R;
import com.whut.getianao.quickdraw.activity.ClientActivity;
import com.whut.getianao.quickdraw.activity.ServerActivity;
import com.whut.getianao.quickdraw.thread.ConnectThread;
import com.whut.getianao.quickdraw.utils.WifiAdmin;
import com.whut.getianao.quickdraw.view.CustomVideoView;

import java.net.Socket;

public class ClientFragment extends Fragment {
    private ClientActivity parentActivity;
    private TextView status_init;//连接信息
    private TextView text_state;//消息显示
    private info.hoang8f.widget.FButton btn_connectToServer;
    private info.hoang8f.widget.FButton btn_sendToServer;
    private View view;
    private CustomVideoView videoView;
    private QMUITipDialog tipDialog;
    private QMUITipDialog acceptDialog;

    public QMUITipDialog getAcceptDialog() {
        return acceptDialog;
    }

    public TextView getStatus_init() {
        return status_init;
    }

    public TextView getText_state() {
        return text_state;
    }

    public QMUITipDialog getTipDialog() {
        return tipDialog;
    }

    public Button getConnectToServer() {
        return btn_connectToServer;
    }

    public Button getSendToServer() {
        return btn_sendToServer;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_client, container, false);
        parentActivity = (ClientActivity) getActivity();
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        playVedio();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopplayVideo();
    }

    private void playVedio() {
        videoView.start();
    }

    private void stopplayVideo() {
        videoView.stopPlayback();
    }

    private void initView(View view) {
        btn_connectToServer = view.findViewById(R.id.btn_client_connect);
        btn_sendToServer = view.findViewById(R.id.btn_client_send);
        text_state = view.findViewById(R.id.text_client_status);
        //status_init = view.findViewById(R.id.text_client_init);

        btn_sendToServer.setVisibility(View.GONE);

        videoView = view.findViewById(R.id.Client_videoView);

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

        bindBtnOnClickListener();

        tipDialog = new QMUITipDialog.Builder(getContext())
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在搜索")
                .create();
        acceptDialog = new QMUITipDialog.Builder(getContext())
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                .setTipWord("连接成功")
                .create();
    }

    private void bindBtnOnClickListener() {
        //连接至服务端热点
        btn_connectToServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipDialog.show();
                if (parentActivity.connectToServer() == false) {
                    //连接失败
                    tipDialog.dismiss();
                    return;
                }
                btn_connectToServer.setEnabled(false);
                btn_connectToServer.setText("等待连接");
            }
        });

        //test:发送消息到服务器
        btn_sendToServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.sendToServer("test");
            }
        });
    }

}
