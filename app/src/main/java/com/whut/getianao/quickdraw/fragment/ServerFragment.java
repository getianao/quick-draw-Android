package com.whut.getianao.quickdraw.fragment;

import android.content.Intent;
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

import com.whut.getianao.quickdraw.R;
import com.whut.getianao.quickdraw.activity.GameActivity;
import com.whut.getianao.quickdraw.activity.ServerActivity;

public class ServerFragment extends Fragment {
    private ServerActivity parentActivity;
    private TextView text_state;
    private Button btn_closeWifi;
    private Button btn_createWifi;
    private Button btn_send;
    private Button btn_startGame;

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
        View view = inflater.inflate(R.layout.fragment_server, container, false);
        parentActivity = (ServerActivity) getActivity();
        initView(view);
        return view;
    }

    //view设置
    private void initView(View view) {
        btn_createWifi = view.findViewById(R.id.create_wifi);
        btn_closeWifi = view.findViewById(R.id.close_wifi);
        btn_send = view.findViewById(R.id.send);
        btn_startGame=view.findViewById(R.id.start_game);

        text_state = view.findViewById(R.id.receive);
        btn_startGame.setEnabled(false);//disable 开始游戏直到匹配到客户端
        btn_closeWifi.setVisibility(View.GONE);
        btn_send.setVisibility(View.GONE);

        bindBtnOnClickListener();
    }

    //按钮绑定
    private void bindBtnOnClickListener() {
        //开启热点，等待客户端连接、确认
        btn_createWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.createWifiHotspot();
                //todo：热点开启后进行广播，界面显示ip
                text_state.setText(parentActivity.getWifiApIpAddress());
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
                if (parentActivity.getConnectThread()!= null) {
                    parentActivity.getConnectThread().sendData("这是来自Wifi热点的消息");
                } else {
                    Log.w("AAA", "connectThread == null");
                }
            }
        });

        //客户端已上线，可以开始游戏,并通知客户端进入游戏
        btn_startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(parentActivity.getReady()==true){
                    parentActivity.gotoGame();
                }
            }
        });
    }

}
