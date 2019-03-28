package com.whut.getianao.quickdraw.thread;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.whut.getianao.quickdraw.activity.ServerActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ConnectThread extends Thread {
    private final Socket socket;
    private Handler handler;
    private InputStream inputStream;
    private OutputStream outputStream;
    Context context;

    public ConnectThread(Context context, Socket socket, Handler handler) {
        setName("ConnectThread");//线程命名
        this.socket = socket;
        this.handler = handler;
        this.context = context;
    }

    @Override
    public void run() {
        if (socket == null) {
            return;
        }
        handler.sendEmptyMessage(ServerActivity.DEVICE_CONNECTED);//message只有what值
        try {
            //获取数据流
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                //读取数据
                bytes = inputStream.read(buffer);
                if (bytes > 0) {
                    final byte[] data = new byte[bytes];
                    System.arraycopy(buffer, 0, data, 0, bytes);//复制到data
                    Message message = Message.obtain();
                    message.what = ServerActivity.GET_MSG;
                    Bundle bundle = new Bundle();
                    bundle.putString("MSG", new String(data));
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送数据
     */
    public void sendData(final String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("ConnectThread", "发送数据:" + (outputStream == null));
                if (outputStream != null) {
                    try {
                        outputStream.write(msg.getBytes());
                        Log.i("ConnectThread", "发送消息：" + msg);
                        Message message = Message.obtain();
                        message.what = ServerActivity.SEND_MSG_SUCCSEE;
                        Bundle bundle = new Bundle();
                        bundle.putString("MSG", new String(msg));
                        message.setData(bundle);
                        handler.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Message message = Message.obtain();
                        message.what = ServerActivity.SEND_MSG_ERROR;
                        Bundle bundle = new Bundle();
                        bundle.putString("MSG", new String(msg));
                        message.setData(bundle);
                        handler.sendMessage(message);
                    }
                }
            }
        }).start();
    }
}
