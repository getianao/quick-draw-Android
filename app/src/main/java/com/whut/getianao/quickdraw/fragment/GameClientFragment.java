package com.whut.getianao.quickdraw.fragment;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.whut.getianao.quickdraw.R;
import com.whut.getianao.quickdraw.activity.ClientActivity;
import com.whut.getianao.quickdraw.entity.GameData;

import static android.content.Context.SENSOR_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;


public class GameClientFragment extends Fragment {

    private View view;
    private QMUITipDialog keepDownDialog;
    private QMUITipDialog keepSteadyDialog;
    private QMUITipDialog waitForFriendDialog;
    private QMUITipDialog readyDialog;
    private QMUITipDialog endDialog;
    private QMUITipDialog winDialog;
    private QMUITipDialog loseDialog;

    //todo：test
    private TextView startBtn;

    private ClientActivity parentActivity;
    private GameData myData = new GameData();//自己数据

    private SensorEventListener mySensorEventListener;
    private boolean isServerReady = false;
    private int flag1 = 0;
    private int flag2 = 0;
    private int flag3 = 0;

    public GameData getMyData() {
        return myData;
    }

    public boolean getServerReady() {
        return isServerReady;
    }

    public void setServerReady(boolean serverReady) {
        isServerReady = serverReady;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_game, container, false);
        init();

        return view;
    }

    private void initView() {
        startBtn = view.findViewById(R.id.textView);

        keepDownDialog = new QMUITipDialog.Builder(getContext())
                .setTipWord("请保持手机竖直向下")
                .create();
        keepSteadyDialog = new QMUITipDialog.Builder(getContext())
                .setTipWord("请稳定手机竖直向下3秒，以进入准备状态")
                .create();
        waitForFriendDialog = new QMUITipDialog.Builder(getContext())
                .setTipWord("准备完毕，正在等待好友就绪")
                .create();
        readyDialog = new QMUITipDialog.Builder(getContext())
                .setTipWord("游戏即将开始，请聆听指令")
                .create();
        winDialog = new QMUITipDialog.Builder(getContext())
                .setTipWord("你赢了")
                .create();
        loseDialog = new QMUITipDialog.Builder(getContext())
                .setTipWord("你输了")
                .create();
    }

    private void onlyShowKeepDownDialog() {
        keepDownDialog.show();
        keepSteadyDialog.dismiss();
        waitForFriendDialog.dismiss();
        readyDialog.dismiss();
    }

    private void onlyShowKeepSteadyDialog() {
        keepDownDialog.dismiss();
        keepSteadyDialog.show();
        waitForFriendDialog.dismiss();
        readyDialog.dismiss();
    }

    private void onlyShowWaitForFriendDialog() {
        keepDownDialog.dismiss();
        keepSteadyDialog.dismiss();
        waitForFriendDialog.show();
        readyDialog.dismiss();
    }


    private void dismissAllDialog() {
        keepDownDialog.dismiss();
        keepSteadyDialog.dismiss();
        waitForFriendDialog.dismiss();
        readyDialog.dismiss();
    }


    // 音量键监听
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (!myData.isEnd()) {
                //游戏没有结束
                //todo:播放gun
                if (!myData.isPutDownByMistake() && myData.isReady() && !myData.isStart()) {
                    //已经稳定，已经准备好，但没有开始
                    startBtn.setText("提前开火");
                    myData.setResult(3);
                    myData.setFireBtnPressed(false);
                    myData.setFireTime(System.currentTimeMillis());
                    myData.setEnd(true);
                } else if (!myData.isPutDownByMistake() && myData.isReady() && myData.isStart() && myData.getyPre() < -4) {
                    //已经稳定，已经准备好，已经开始，但美哟欧陆抬起一定角度
                    startBtn.setText("角度太低");
                    myData.setResult(2);
                    myData.setFireBtnPressed(false);
                    myData.setFireTime(System.currentTimeMillis());
                    myData.setEnd(true);
                } else if (!myData.isPutDownByMistake() && myData.isReady() && myData.isStart() && myData.getyPre() > -4) {
                    //已经稳定，已经准备好，已经开始，成功抬起一定角度
                    startBtn.setText("开火成功");
                    myData.setResult(1);
                    myData.setFireBtnPressed(true);
                    myData.setFireTime(System.currentTimeMillis());
                    myData.setEnd(true);
                }
                if (flag2 == 0) {
                    flag2 = 1;
                    parentActivity.sentClientDataToServer(myData);
                }
                myData.setFireBtnPressed(true);
                return true;
            }
        }
        return false;
    }

    public void init() {
        initView();
        parentActivity = (ClientActivity) getActivity();
        // 得到传感器管理对象
        final SensorManager sensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);
        // 得到传感器对象
        Sensor gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // 初始化两个时间相等
        myData.setCurPutDownTime(System.currentTimeMillis());
        myData.setLastPutDownTime(myData.getCurPutDownTime());

        // 传感器监听事件类
        mySensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                sensor(event);
                //游戏还没结束
                if (!myData.isEnd()) {
                    // 手机朝下放置，但还没稳定，游戏未准备，游戏未开始
                    if (event.values[1] < -8 && myData.isPutDownByMistake() && !myData.isReady() && !myData.isStart()) {
                        long diffValue = myData.getCurPutDownTime() - myData.getLastPutDownTime();
                        //astPutDownTime是保持刚放下一瞬间的时刻，currentTimeMillis保持刷新
                        myData.setCurPutDownTime(System.currentTimeMillis());
                        //超过2s时，标志稳定
                        if (diffValue > 3000) {
                            //震动，并播放提示音，提示已稳定
                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            Ringtone r = RingtoneManager.getRingtone(getContext(), notification);
                            r.play();
                            Vibrator vibrator = (Vibrator) getContext().getSystemService(VIBRATOR_SERVICE);
                            vibrator.vibrate(350);
                            myData.setPutDownByMistake(false);
                        } else {
                            onlyShowKeepSteadyDialog();
                        }
                    }

                    // 当手机朝下放置,且已稳定，且游戏未准备，且游戏未开始
                    if (event.values[1] < -8 && !myData.isPutDownByMistake() && !myData.isReady() && !myData.isStart()) {
                        try {
                            onlyShowWaitForFriendDialog();
                            //保持CurPutDownTime刷新
                            myData.setCurPutDownTime(System.currentTimeMillis());
                            //给服务器发送就绪消息,等待服务器就绪
                            if (flag1 == 0) {
                                flag1 = 1;
                                parentActivity.tellServerIMReady();
                            }
                            if (isServerReady) {
                                //服务器已就绪
                                myData.setReady(true);
                            }
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // 手机拿起时,且游戏还没开始（可能已稳定，可能已准备）
                    if (event.values[1] > -8 && !myData.isStart()) {
                        if(myData.isPutDownByMistake()==false){//原本已稳定
                            //清除服务器中的客户端已稳定状态
                            parentActivity.tellServerClear();
                        }
                        flag1=0;
                        flag2=0;
                        //保持CurPutDownTime和LastPutDownTime是相同的
                        myData.setCurPutDownTime(System.currentTimeMillis());
                        myData.setLastPutDownTime(myData.getCurPutDownTime());
                        //重置稳定状态、游戏准备状态
                        myData.setPutDownByMistake(true);
                        myData.setReady(false);
                        onlyShowKeepDownDialog();

                    }

                    //已稳定，游戏已准备，但没有开始
                    if (!myData.isPutDownByMistake() && myData.isReady() && !myData.isStart()) {
                        //client does nothing
                        dismissAllDialog();
                        return;
                    }


                    //已稳定，游戏已准备，游戏已开始,且5s后游戏没有结束
                    if (!myData.isPutDownByMistake() && myData.isReady() && myData.isStart() && System.currentTimeMillis() - myData.getStartTime() > 5000) {
                        startBtn.setText("游戏超时");
                        //重置状态，强行结束
                        myData.setPutDownByMistake(true);
                        myData.setReady(false);
                        myData.setStart(false);
                        myData.setEnd(true);
                    }
                } else {
                    //游戏结束
                    if (myData.isEnd()) {
                        if (flag3 == 0) {
                            flag3 = 1;
                            switch (myData.getResult()) {
                                //由对方结束游戏
                                case 0:
                                    if (myData.getWin() == true) {
                                        winDialog.show();
                                    } else {
                                        loseDialog.show();
                                    }
                                    break;
                                //由自己结束游戏
                                case 1:
                                    //正常开枪，赢
                                    myData.setWin(true);
                                    parentActivity.tellServerLose();
                                    winDialog.show();
                                    //todo:显示详细数据
                                    showData();
                                    break;
                                case 2:
                                    //低角度开枪,输
                                    myData.setWin(false);
                                    parentActivity.tellServerWin();
                                    loseDialog.show();
                                    showData();
                                    break;
                                case 3:
                                    //提前开枪，输
                                    myData.setWin(false);
                                    parentActivity.tellServerWin();
                                    loseDialog.show();
                                    showData();
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }

            // 传感器精度响应时触发
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };


        //注册listener，第三个参数是检测的精确度
        sensorManager.registerListener(mySensorEventListener,
                gravitySensor, SensorManager.SENSOR_DELAY_GAME);

        // 游戏的胜负判断

    }

    private void sensor(SensorEvent event) {
        //加速度
        myData.setxPre(event.values[0]);
        myData.setyPre(event.values[1]);
        myData.setzPre(event.values[2]);
    }

    public void setStartTime(int time) {
        myData.setStartTime(System.currentTimeMillis() + time);
    }
    public void youwin() {
        endDialog = new QMUITipDialog.Builder(getContext())
                .setTipWord("你赢了")
                .create();
        endDialog.show();
    }

    public void youlose() {
        endDialog = new QMUITipDialog.Builder(getContext())
                .setTipWord("你输了")
                .create();
        endDialog.show();
    }

    private void showData(){
        String str;
        long duringTime=myData.getFireTime()-myData.getStartTime();
        str="开枪花费时间："+(duringTime)+"毫秒\n开枪速度："+(1000.0/duringTime)+"m/s";
        startBtn.setText(str);
    }
}
