package com.whut.getianao.quickdraw.fragment;

import android.content.Context;
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

import com.whut.getianao.quickdraw.R;
import com.whut.getianao.quickdraw.entity.GameData;

import static android.content.Context.SENSOR_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;


public class GameFragment extends Fragment {

    private View view;
    GameData myData = new GameData();//自己数据
    GameData oppsiteData = new GameData();//对方数据
    private Button startBtn;
    private Button button2 ;
    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private TextView textView6;
    private TextView textView7;
    private TextView textView8;
    private TextView textView9;
    private TextView textView10;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //todo:界面替换
//        view = inflater.inflate(R.layout.fragment_game, container, false);
        view = inflater.inflate(R.layout.test, container, false);
        btnInit();
        init();
        return view;
    }

    private void btnInit() {
        startBtn =  view.findViewById(R.id.jbutton);
        button2 = (Button) view.findViewById(R.id.button2);
        textView1 = (TextView) view.findViewById(R.id.textView1);
        textView2 = (TextView) view.findViewById(R.id.textView2);
        textView3 = (TextView) view.findViewById(R.id.textView3);
        textView4 = (TextView) view.findViewById(R.id.textView4);
        textView5 = (TextView) view.findViewById(R.id.textView5);
        textView6 = (TextView) view.findViewById(R.id.textView6);
        textView7 = (TextView) view.findViewById(R.id.textView7);
        textView8 = (TextView) view.findViewById(R.id.textView8);
        textView9 = (TextView) view.findViewById(R.id.textView9);
        textView10 = (TextView) view.findViewById(R.id.textView10);

        //添加 重新开始按钮监听事件
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startBtn.getText().equals("游戏超时")) {
                    myData = new GameData();
                }
            }
        });
    }

    // 音量键监听
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (myData.isStart() && myData.getyPre() > -4) {
                startBtn.setText("开火成功");
                //成功抬起一定角度,且击发了按钮
                myData.setFireBtnPressed(true);
                myData.setFireTime(System.currentTimeMillis());
                myData.setEnd(true);
            } else if (myData.isStart()) {
                startBtn.setText("角度太低");
                myData.setFireBtnPressed(false);
                myData.setFireTime(System.currentTimeMillis());
                myData.setEnd(true);
            } else if (!myData.isStart() && myData.isReady()) {
                startBtn.setText("提前开火");
                myData.setFireBtnPressed(false);
                myData.setFireTime(System.currentTimeMillis());
                myData.setEnd(true);
            }

            button2.setText("开火");
            this.myData.setFireBtnPressed(true);
            return true;
        } else {
            return false;
        }
    }

    public void init() {

        // 得到传感器管理对象
        final SensorManager sensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);
        // 得到传感器对象
        Sensor gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // 初始化两个时间相等
        myData.setCurPutDownTime(System.currentTimeMillis());
        myData.setLastPutDownTime(myData.getCurPutDownTime());

        // 传感器监听事件类
        SensorEventListener mySensorEventListener = new SensorEventListener() {
            // 传感器数据变化响应时触发
            @Override
            public void onSensorChanged(SensorEvent event) {
                sensor();

                // 手机朝下放置
                if (event.values[1] < -9.5) {
                    myData.setCurPutDownTime(System.currentTimeMillis());
                    if (myData.getCurPutDownTime() - myData.getLastPutDownTime() > 2000) {
                        myData.setPutDownByMistake(false);//超过2s
                    }
                }
                startBtn.setText("1");
                //向下放置,即开始准备
                // 当手机朝下放置时（y轴加速度最小为-10）,且和上一次放下手机隔了2秒(isPutDownByMistake)，作出提示，3秒后开始游戏
                //event.values[1] < -9.5 && !myData.isEnd() && !myData.isStart() && !myData.isReady() && !myData.isPutDownByMistake()
                if (event.values[1] < -9.5 && !myData.isEnd() && !myData.isStart()
                        && !myData.isReady() && !myData.isPutDownByMistake()) {
                    // 播放系统提示音
//                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                    Ringtone r = RingtoneManager.getRingtone(getContext(), notification);
//                    r.play();
                    startBtn.setText("游戏将在3秒后开始...");
                    startBtn.refreshDrawableState();
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                        }
//                    });
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    startBtn.setText("2");

                    // 震动以提示开始 (张子煜的手机会闪退)
                    myData.setReady(true);
                    Vibrator vibrator = (Vibrator) getContext().getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(350);
                    // 设置开始游戏时间
                    myData.setStartTime(System.currentTimeMillis() + (int) (5 + Math.random() * (10)) * 1000);//5~14S的随机时间
                    myData.setStart(false);
                    myData.setReady(true);
                    startBtn.setText("");
                    return;
                }

                // 手机拿起时
                if (event.values[1] > 0) {
                    myData.setCurPutDownTime(System.currentTimeMillis());
                    myData.setLastPutDownTime(myData.getCurPutDownTime());

                    // 两次放下手机的时间
                    textView9.setText(String.valueOf(myData.getLastPutDownTime()));
                    textView10.setText(String.valueOf(myData.getCurPutDownTime()));
                    // 显示每个方向的加速度 （保留小数点后两位有效）

                    myData.setxPre(event.values[0]);
                    myData.setyPre(event.values[1]);
                    myData.setzPre(event.values[2]);



                }
                //游戏胜负判断
                if (myData.isEnd() || oppsiteData.isEnd()) {

                    //其中有一方没有成功开火
                    if (!myData.isFireBtnPressed() && oppsiteData.isFireBtnPressed()) {
                        //本用户输了
                    }
                    if (myData.isFireBtnPressed() && !oppsiteData.isFireBtnPressed()) {
                        //本用户赢了
                    }
                    //两方都没有成功开火
                    if (!myData.isFireBtnPressed() && !oppsiteData.isFireBtnPressed()) {
                        //我方更早提前开启,对方赢
                        if (myData.getFireTime() - myData.getStartTime() < oppsiteData.getFireTime() - oppsiteData.getStartTime()) {

                        }
                        //对方更早提前开枪,我方赢
                        else {

                        }
                    }
                    //两方都成功开火
                    //我方更早提前开枪,我方赢
                    if (myData.getFireTime() - myData.getStartTime() < oppsiteData.getFireTime() - oppsiteData.getStartTime()) {

                    }
                    //对方更早提前开枪,对方赢
                    else {

                    }

                    //复原
                    myData.setEnd(false);
                    myData.setReady(false);
                    myData.setStart(false);
                }


                //游戏开始,允许开始5S后没有人开火,游戏结束
                if (myData.isStart() && System.currentTimeMillis() - myData.getStartTime() > 5000) {
                    startBtn.setText("游戏超时");

                    myData.setReady(false);
                    myData.setStart(false);
                    myData.setEnd(true);
                }

                //游戏准备过程中,动了手机
                if (myData.isReady() && !myData.isStart() && myData.getyPre() > -7) {
                    myData.setReady(false);
                    myData.setMoveWhenReady(true);
                    startBtn.setText("准备过程中请保持手机朝下,请重新准备");
                    return;
                }

                //在准备过程中 且没有开始
                //提示游戏将在多久后开始
                else if (myData.isReady() && !myData.isStart()) {
                    long dTime = myData.getStartTime() - System.currentTimeMillis();
                    if (dTime < 0) {
                        startBtn.setText("准备就绪");
                        myData.setReady(false);
                        myData.setStart(true);
                        return;
                    }
                    startBtn.setText("游戏将在" + (int) dTime / 1000 + "秒后开始...");
                    return;
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

    private void sensor(){
        textView1.setText(String.valueOf("x轴加速度" + (float) (Math.round(myData.getxPre() * 100)) / 100));
        textView2.setText(String.valueOf("y轴加速度" + (float) (Math.round(myData.getyPre() * 100)) / 100));
        textView3.setText(String.valueOf("z轴加速度" + (float) (Math.round(myData.getzPre() * 100)) / 100));
    }
}
