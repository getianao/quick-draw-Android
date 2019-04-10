package com.whut.getianao.quickdraw.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.whut.getianao.quickdraw.R;
import com.whut.getianao.quickdraw.base.BaseActivity;
import com.whut.getianao.quickdraw.entity.GameData;
import com.whut.getianao.quickdraw.fragment.GameClientFragment;
import com.whut.getianao.quickdraw.fragment.GameServerFragment;
import com.whut.getianao.quickdraw.fragment.ServerFragment;
import com.whut.getianao.quickdraw.thread.ConnectThread;
import com.whut.getianao.quickdraw.thread.ListenerThread;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class ServerActivity extends BaseActivity {
    public static final int DEVICE_CONNECTING = 1;//有设备正在连接热点
    public static final int DEVICE_CONNECTED = 2;//有设备连上热点
    public static final int SEND_MSG_SUCCSEE = 3;//发送消息成功
    public static final int SEND_MSG_ERROR = 4;//发送消息失败
    public static final int GET_MSG = 6;//获取新消息
    private static final String WIFI_HOTSPOT_SSID = "TEST";// 热点名称
    private static final String WIFI_HOTSPOT_PSW = "12345678";  // 热点名称
    private static final int PORT = 1234; //端口号

    private ServerFragment mServerFragment;
    private GameServerFragment mGameFragment;
    private WifiManager wifiManager;
    private Boolean isReady;
    private ConnectThread connectThread; // 连接线程
    private ListenerThread listenerThread; //监听线程

    public Boolean getReady() {
        return isReady;
    }

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
                case DEVICE_CONNECTING:
                    connectThread = new ConnectThread(ServerActivity.this, listenerThread.getSocket(), handler);
                    connectThread.start();
                case DEVICE_CONNECTED:
                    //隐藏搜索提示
                    mServerFragment.getTipDialog().dismiss();
                    //提示可以开始
                    mServerFragment.getText_state().setText("已连接");
                    //显示3s成功提示
                    mServerFragment.getAcceptDialog().show();
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                Thread.sleep(3000);//休眠3秒
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            mServerFragment.getAcceptDialog().dismiss();
                            //跳转游戏界面
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .addToBackStack(null)  //将当前fragment加入到返回栈中
                                    .replace(R.id.server_fragment_container, mGameFragment).commit();
                        }
                    }.start();
                    isReady = true;
                    break;
                case SEND_MSG_SUCCSEE:
                    mServerFragment.getText_state().setText("发送消息成功:" + msg.getData().getString("MSG"));
                    break;
                case SEND_MSG_ERROR:
                    mServerFragment.getText_state().setText("发送消息失败:" + msg.getData().getString("MSG"));
                    break;
                case GET_MSG://展示收到的信息
                    mServerFragment.getText_state().setText("收到消息:" + msg.getData().getString("MSG"));
                    if (msg.getData().getString("MSG").equals("ready")) {
                        //得知客户端准备完毕
                        mGameFragment.setClientReady(true);
                    } else if (msg.getData().getString("MSG").equals("win")) {
                        //客户端通知你赢了
                        mGameFragment.getMyData().setEnd(true);
                        mGameFragment.getMyData().setWin(true);
                    } else if (msg.getData().getString("MSG").equals("lose")) {
                        //客户端通知你输了
                        mGameFragment.getMyData().setEnd(true);
                        mGameFragment.getMyData().setWin(false);
                    } else if (msg.getData().getString("MSG").equals("clear")) {
                        //清除isClientReady
                        mGameFragment.setClientReady(false);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        init();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.server_fragment_container, mServerFragment)
                    .commit();
        }
    }

    private void init() {
        mServerFragment = new ServerFragment();
        mGameFragment = new GameServerFragment();

        isReady = false;

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        //开启监听线程
        listenerThread = new ListenerThread(PORT, handler);
        listenerThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    //获取热点本机ip
    public String getWifiApIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().contains("ap") || intf.getName().contains("wlan")) {
                    //todo:wlan
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && (inetAddress.getAddress().length == 4)) {
                            Log.d("Main", inetAddress.getHostAddress());
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("Main", ex.toString());
        }
        return null;
    }

    //创建Wifi热点
    public void createWifiHotspot() {
        if (!isWifiApOpen(ServerActivity.this)) {
            if (wifiManager.isWifiEnabled()) {
                //如果wifi处于打开状态，则关闭wifi,
                wifiManager.setWifiEnabled(false);
            }
            //Android7.1及以上版本,提示手动开启
            if (Build.VERSION.SDK_INT >= 25) {
                showRequestApDialog();
            } else {
                setWifiApEnble(true);
            }
        } else {
            //todo:提示已开启
        }
    }

    //关闭WiFi热点
    public void closeWifiHotspot() {
        if (isWifiApOpen(ServerActivity.this)) {
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

    //弹窗提示开启ap
    public void showRequestApDialog() {
        final QMUIDialog.MessageDialogBuilder builder = new QMUIDialog.MessageDialogBuilder(ServerActivity.this);
        builder.setMessage("android7.1系统以上不支持自动开启热点,需要手动开启热点")
                .addAction("去开启", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        openAP();
                    }
                })
                .addAction("退出", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                }).show();
    }

    //弹窗提示关闭ap
    public void showRequestApDialogClose() {
        final QMUIDialog.MessageDialogBuilder builder = new QMUIDialog.MessageDialogBuilder(ServerActivity.this);
        builder.setMessage("android7.1系统以上不支持自动关闭热点,需要手动自动热点")
                .addAction("去关闭", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        openAP();
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
    public void openAP() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        ComponentName com = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
        intent.setComponent(com);
        startActivity(intent);
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
                    mServerFragment.getText_state().setText("热点已开启 SSID:" + WIFI_HOTSPOT_SSID + " password:" + WIFI_HOTSPOT_PSW);
                } else {
                    mServerFragment.getText_state().setText("热点已关闭");
                }
            } else {
                mServerFragment.getText_state().setText("创建热点失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            mServerFragment.getText_state().setText("创建热点失败");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 获取当前回退栈中的Fragment个数
            int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
            // 回退栈中至少有多个fragment,栈底部是首页
            if (backStackEntryCount > 1) {
                // 回退一步
                getSupportFragmentManager().popBackStackImmediate();
            } else {
                try {
                    if (listenerThread != null) {
                        listenerThread.closeServerSocket();
                        listenerThread.interrupt();
                        listenerThread.stop();
                    }
                    if (connectThread != null) {
                        connectThread.stop();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finish();
            }
            return true;
        } else if (getSupportFragmentManager().findFragmentById(R.id.server_fragment_container) instanceof GameServerFragment) {
            ((GameServerFragment) getSupportFragmentManager().findFragmentById(R.id.server_fragment_container))
                    .onKeyDown(keyCode, event);
            return true;
        }
        return false;
    }

    //向客户端发送数据
    public void sendToClient(String msg) {
        if (connectThread != null) {
            connectThread.sendData(msg);
        } else {
            Log.w("AAA", "connectThread == null");
        }
    }

    public void tellClientToStart() {
        sendToClient("start");
    }

    public void tellClientStartTime(String time) {
        sendToClient(time);
    }

    //弃用
    //将字符串解析为GameData
    private GameData resolveGameData(String data) {
        String[] list = new String[15];
        for (int i = 0; i < 15; i++) {
            int start = data.indexOf('{');
            int end = data.indexOf('}');
            String tmp = data.substring(start, end + 1);
            list[i] = tmp.substring(tmp.indexOf(':') + 1, tmp.indexOf('}'));
            data = data.substring(end + 1);
        }
        GameData res = new GameData();

        res.setReady(Boolean.valueOf(list[0]));
        res.setStart(Boolean.valueOf(list[1]));
        res.setEnd(Boolean.valueOf(list[2]));
        res.setPutDownByMistake(Boolean.valueOf(list[3]));
        res.setStartTime(Long.valueOf(list[4]));
        res.setLastPutDownTime(Long.valueOf(list[5]));
        res.setCurPutDownTime(Long.valueOf(list[6]));
        res.setxPre(Float.valueOf(list[7]));
        res.setyPre(Float.valueOf(list[8]));
        res.setzPre(Float.valueOf(list[9]));
        res.setFireBtnPressed(Boolean.valueOf(list[10]));
        res.setOrderFireTime(Long.valueOf(list[11]));
        res.setLowFireAngle(Boolean.valueOf(list[12]));
        res.setFireTime(Long.valueOf(list[13]));
        res.setMoveWhenReady(Boolean.valueOf(list[14]));

        return res;
    }

    //告知客户端你输了
    public void tellClientLose() {
        sendToClient("lose");
    }

    //告知客户端你赢了
    public void tellClientWin() {
        sendToClient("win");
    }

    //告知客户端清除服务端稳定状态
    public void tellClientClear() {
        sendToClient("clear");
    }


}

