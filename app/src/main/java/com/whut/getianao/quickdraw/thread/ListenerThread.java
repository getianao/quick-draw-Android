package com.whut.getianao.quickdraw.thread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.whut.getianao.quickdraw.activity.ServerActivity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ListenerThread extends Thread {
    private ServerSocket serverSocket = null;
    private Handler handler;
    private int port;
    private Socket socket;
    private boolean flag;


    public ListenerThread(int port, Handler handler) {
        setName("ListenerThread");
        this.port = port;
        this.handler = handler;
        flag = true;
        try {
            serverSocket = new ServerSocket(port);//监听本机的端口
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (flag) {
            try {
                if (serverSocket != null)
                    socket = serverSocket.accept(); //阻塞，直到有客户端连接
                Message message = Message.obtain();
                message.what = ServerActivity.DEVICE_CONNECTING;
                handler.sendMessage(message);
            } catch (IOException e) {
                Log.i("ListennerThread", "error:" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }

    //停止阻塞状态
    public void closeServerSocket() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
