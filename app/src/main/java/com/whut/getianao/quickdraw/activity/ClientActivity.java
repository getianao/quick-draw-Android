package com.whut.getianao.quickdraw.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.whut.getianao.quickdraw.R;
import com.whut.getianao.quickdraw.base.BaseActivity;
import com.whut.getianao.quickdraw.fragment.ClientFragment;
import com.whut.getianao.quickdraw.fragment.GameFragment;
import com.whut.getianao.quickdraw.fragment.ServerFragment;
import com.whut.getianao.quickdraw.thread.ConnectThread;
import com.whut.getianao.quickdraw.thread.ListenerThread;
import com.whut.getianao.quickdraw.utils.WifiAdmin;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

public class ClientActivity extends BaseActivity {
    public static final int DEVICE_CONNECTING = 1;//有设备正在连接热点
    public static final int DEVICE_CONNECTED = 2;//有设备连上热点
    public static final int SEND_MSG_SUCCSEE = 3;//发送消息成功
    public static final int SEND_MSG_ERROR = 4;//发送消息失败
    public static final int GET_MSG = 6;//获取新消息
    private static final String WIFI_HOTSPOT_SSID = "TEST"; // 热点名称
    private static final String WIFI_HOTSPOT_PSW = "12345678"; // 热点名称
    private static final int PORT = 1234; //端口号

    private ClientFragment mClientFragment;
    private GameFragment mGameFragment;
    private ConnectThread connectThread;// 连接线程
    private ListenerThread listenerThread;//监听线程
    private WifiManager wifiManager;

    public ConnectThread getConnectThread() {
        return connectThread;
    }

    public ListenerThread getListenerThread() {
        return listenerThread;
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DEVICE_CONNECTED:
                    mClientFragment.getText_state().setText("设备连接成功");
                    //结束TipDialog
                    mClientFragment.getTipDialog().dismiss();
                    mClientFragment.getText_state().setText("已连接");
                    //显示3s成功提示
                    mClientFragment.getAcceptDialog().show();
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                Thread.sleep(3000);//休眠3秒
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            mClientFragment.getAcceptDialog().dismiss();
                            //跳转GameFragment
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .addToBackStack(null)  //将当前fragment加入到返回栈中
                                    .replace(R.id.client_fragment_container, mGameFragment).commit();
                        }
                    }.start();
                    break;
                case SEND_MSG_SUCCSEE:
                    mClientFragment.getText_state().setText("发送消息成功:" + msg.getData().getString("MSG"));
                    break;
                case SEND_MSG_ERROR:
                    mClientFragment.getText_state().setText("发送消息失败:" + msg.getData().getString("MSG"));
                    break;
                case GET_MSG:
                    //收到服务器发来的开始游戏的指令
                    mClientFragment.getText_state().setText("收到消息:" + msg.getData().getString("MSG"));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        init();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.client_fragment_container, mClientFragment)
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
        if (connectThread != null) {
            connectThread.interrupt();
        }
    }

    private void init() {
        mClientFragment = new ClientFragment();
        mGameFragment = new GameFragment();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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
    public boolean connectToServer() {
        WifiAdmin wifi = new WifiAdmin(getApplicationContext());
        //必须关闭热点，重新搜索
        if (isWifiApOpen(ClientActivity.this) == true) {
            closeWifiHotspot();
            return false;
        }
        //打开wifi
        if (wifi.isWifiEnable() == false) {
            wifi.openWifi();
            return false;
        }
        //一直搜索直到连接热点
        while (wifi.addNetwork(wifi.CreateWifiInfo(WIFI_HOTSPOT_SSID, WIFI_HOTSPOT_PSW, 3)) == false) {
            //死循环
        }
        //开启连接线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(6000);
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
        return true;
    }

    //向服务器发送数据
    public void sendToServer() {
        if (connectThread != null) {
            connectThread.sendData("这是来自Wifi客户端的消息");
        } else {
            Log.w("AAA", "connectThread == null");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (getSupportFragmentManager().findFragmentById(R.id.client_fragment_container) instanceof GameFragment) {
            ((GameFragment) getSupportFragmentManager().findFragmentById(R.id.client_fragment_container))
                    .onKeyDown(keyCode, event);
            return true;
        }
        if (getSupportFragmentManager().findFragmentById(R.id.server_fragment_container) instanceof GameFragment) {
            ((GameFragment) getSupportFragmentManager().findFragmentById(R.id.server_fragment_container))
                    .onKeyDown(keyCode, event);
            return true;
        }
        return false;
    }

    //关闭WiFi热点
    public void closeWifiHotspot() {
        if (isWifiApOpen(ClientActivity.this)) {
            //Android7.1及以上版本,提示手动关闭
            if (Build.VERSION.SDK_INT >= 25) {
                showRequestApDialogClose();
            } else {
                setWifiApEnble(false);
            }
        } else {
            //todo:提示已关闭
        }
    }

    //判断ap状态
    public static boolean isWifiApOpen(Context context) {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            //通过放射获取 getWifiApState()方法
            Method method = manager.getClass().getDeclaredMethod("getWifiApState");
            //调用getWifiApState() ，获取返回值
            int state = (int) method.invoke(manager);
            //通过放射获取 WIFI_AP的开启状态属性
            Field field = manager.getClass().getDeclaredField("WIFI_AP_STATE_ENABLED");
            //获取属性值
            int value = (int) field.get(manager);
            //判断是否开启
            if (state == value) {
                return true;
            } else {
                return false;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return false;
    }

    //弹窗提示关闭ap
    public void showRequestApDialogClose() {
        final QMUIDialog.MessageDialogBuilder builder = new QMUIDialog.MessageDialogBuilder(ClientActivity.this);
        builder.setMessage("android7.1系统以上不支持自动关闭热点,需要手动自动热点")
                .addAction("去关闭", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        openAP(1001);
                    }
                })
                .addAction("退出", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                }).show();
    }

    //打开系统的便携式热点界面
    public void openAP(int requestCode) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        ComponentName com = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
        intent.setComponent(com);
        startActivityForResult(intent, requestCode);
    }

    //7.1以下版本打开热点方法
    public void setWifiApEnble(boolean value) {
        final WifiConfiguration config = new WifiConfiguration();
        config.SSID = WIFI_HOTSPOT_SSID;
        config.preSharedKey = WIFI_HOTSPOT_PSW;
        config.hiddenSSID = false;
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);//开放系统认证
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;
        try {
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            boolean enable = (Boolean) method.invoke(wifiManager, config, value);
            if (enable) {
                if (value == true) {
                    mClientFragment.getText_state().setText("热点已开启 SSID:" + WIFI_HOTSPOT_SSID + " password:" + WIFI_HOTSPOT_PSW);
                } else {
                    mClientFragment.getText_state().setText("热点已关闭");
                }
            } else {
                mClientFragment.getText_state().setText("创建热点失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            mClientFragment.getText_state().setText("创建热点失败");
        }
    }
}
