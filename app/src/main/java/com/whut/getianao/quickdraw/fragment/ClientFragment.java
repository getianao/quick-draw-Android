package com.whut.getianao.quickdraw.fragment;

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
import com.whut.getianao.quickdraw.activity.ClientActivity;
import com.whut.getianao.quickdraw.activity.ServerActivity;
import com.whut.getianao.quickdraw.thread.ConnectThread;
import com.whut.getianao.quickdraw.utils.WifiAdmin;

import java.net.Socket;

public class ClientFragment extends Fragment {
    private ClientActivity parentActivity;
    private TextView status_init;//连接信息
    private TextView text_state;//消息显示
    private Button btn_connectToServer;
    private Button  btn_sendToServer;

    public TextView getStatus_init() {
        return status_init;
    }

    public TextView getText_state() {
        return text_state;
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
        View view = inflater.inflate(R.layout.fragment_client, container, false);
        parentActivity= (ClientActivity) getActivity();
        initView(view);
        return view;
    }

    private void initView(View view) {
        btn_connectToServer = view.findViewById(R.id.btn_client_connect);
        btn_sendToServer = view.findViewById(R.id.btn_client_send);
        text_state = view.findViewById(R.id.text_client_status);
        status_init = view.findViewById(R.id.text_client_init);

        btn_sendToServer.setVisibility(View.GONE);

        //连接至服务端热点
        btn_connectToServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.connectToServer();
            }
        });
        //test:发送消息到服务器
        btn_sendToServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.sendToServer();
            }
        });
    }
}