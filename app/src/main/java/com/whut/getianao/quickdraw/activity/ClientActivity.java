package com.whut.getianao.quickdraw.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.whut.getianao.quickdraw.R;
import com.whut.getianao.quickdraw.base.BaseActivity;
import com.whut.getianao.quickdraw.fragment.ClientFragment;
import com.whut.getianao.quickdraw.fragment.GameFragment;
import com.whut.getianao.quickdraw.fragment.ServerFragment;
import com.whut.getianao.quickdraw.thread.ConnectThread;
import com.whut.getianao.quickdraw.thread.ListenerThread;
import com.whut.getianao.quickdraw.utils.WifiAdmin;


import java.net.Socket;

public class ClientActivity extends BaseActivity {
    public static final int DEVICE_CONNECTING = 1;//有设备正在连接热点
    public static final int DEVICE_CONNECTED = 2;//有设备连上热点
    public static final int SEND_MSG_SUCCSEE = 3;//发送消息成功
    public static final int SEND_MSG_ERROR = 4;//发送消息失败
    public static final int GET_MSG = 6;//获取新消息

    private ClientFragment mClientFragment;
    private GameFragment mGameFragment;

    public ConnectThread getConnectThread() {
        return connectThread;
    }

    public ListenerThread getListenerThread() {
        return listenerThread;
    }

    // 连接线程
    private ConnectThread connectThread;
    //监听线程
    private ListenerThread listenerThread;
    // 热点名称
    private static final String WIFI_HOTSPOT_SSID = "TEST";
    // 热点名称
    private static final String WIFI_HOTSPOT_PSW = "12345678";
    //端口号
    private static final int PORT = 1234;
    private WifiManager wifiManager;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DEVICE_CONNECTED:
                    mClientFragment.getText_state().setText("设备连接成功");
                    break;
                case SEND_MSG_SUCCSEE:
                    mClientFragment.getText_state().setText("发送消息成功:" + msg.getData().getString("MSG"));
                    break;
                case SEND_MSG_ERROR:
                    mClientFragment.getText_state().setText("发送消息失败:" + msg.getData().getString("MSG"));
                    break;
                case GET_MSG:
                    //text_state.setText("收到消息:" + msg.getData().getString("MSG"));
                    //收到服务器发来的开始游戏的指令
                    if(msg.getData().getString("MSG").equals("start_game!")){
                        getSupportFragmentManager()
                                .beginTransaction()
                                .addToBackStack(null)  //将当前fragment加入到返回栈中
                                .replace(R.id.client_fragment_container, mGameFragment).commit();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        mClientFragment=new ClientFragment();
        mGameFragment=new GameFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.client_fragment_container, mClientFragment)
                    .commit();
        }
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listenerThread!=null){
            listenerThread.interrupt();
        }
        if(connectThread!=null){
            connectThread.interrupt();
        }
    }



    /**
     * 获取已连接的热点路由
     *
     * @return
     */
    private String getIp() {
        //检查Wifi状态
        if (!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);
        WifiInfo wi = wifiManager.getConnectionInfo();
        //获取32位整型IP地址
        int ipAdd = wi.getIpAddress();
        //把整型地址转换成“*.*.*.*”地址
        String ip = intToIp(ipAdd);
        return ip;
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    /**
     * wifi获取 已连接网络路由  路由ip地址---方法同上
     *
     * @param context
     * @return
     */
    private static String getWifiRouteIPAddress(Context context) {
        WifiManager wifi_service = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifi_service.getDhcpInfo();
        String routeIp = Formatter.formatIpAddress(dhcpInfo.gateway);
        Log.i("route ip", "wifi route ip：" + routeIp);
        return routeIp;
    }

    //连接服务器
    public void connectToServer(){
        WifiAdmin wifi = new WifiAdmin(getApplicationContext());

        wifi.openWifi();
        wifi.addNetwork(wifi.CreateWifiInfo(WIFI_HOTSPOT_SSID, WIFI_HOTSPOT_PSW, 3));

        //开启连接线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(6000);//todo:开启、扫描wifi需要时间，应该监听一个wifi的广播
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mClientFragment.getStatus_init().setText("已连接到："
                                    + wifiManager.getConnectionInfo().getSSID()
                                    + "\nIP:" + getIp()
                                    + "\n路由：" + getWifiRouteIPAddress(ClientActivity.this));
                        }
                    });
                    Socket socket = new Socket(getWifiRouteIPAddress(ClientActivity.this), PORT);
                    connectThread = new ConnectThread(ClientActivity.this, socket, handler);
                    connectThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mClientFragment.getText_state().setText("通信连接失败");
                        }
                    });
                }
            }
        }).start();
    }

    //向服务器发送数据
    public void sendToServer(){
        if (connectThread != null) {
            connectThread.sendData("这是来自Wifi客户端的消息");
        } else {
            Log.w("AAA", "connectThread == null");
        }
    }

}
