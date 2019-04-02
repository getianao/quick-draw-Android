package com.whut.getianao.quickdraw.fragment;

import android.app.Notification;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 *  //如果正在游戏过程中
 *                 if (myData.isStart()) {
 *                     final Button startBtn = (Button) findViewById(R.id.button);
 *                     startBtn.setOnClickListener(new View.OnClickListener() {
 *                         @Override
 *                         public void onClick(View v) {
 *                             startBtn.setText("开火成功");
 *                             if (myData.getyPre() > -4) {
 *                                 //成功抬起一定角度,且击发了按钮
 *                                 myData.setFireBtnPressed(true);
 *                                 myData.setFireTime(System.currentTimeMillis());
 *                                 myData.setEnd(true);
 *                             } else {
 *                                 myData.setFireBtnPressed(false);
 *                                 myData.setFireTime(System.currentTimeMillis());
 *                                 myData.setEnd(true);
 *                             }
 *                         }
 *                     });
 *                     return;
 *                 }
 */

public class MainActivity extends AppCompatActivity {
    GameData myData = new GameData();

    // 对方的游戏数据
    GameData oppsiteData = new GameData();

    // 音量键监听
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Button startBtn = (Button) findViewById(R.id.button);


            if (myData.isStart()&&myData.getyPre() > -4) {
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




            Button button2 = (Button) findViewById(R.id.button2);
            button2.setText("开火");
            this.myData.setFireBtnPressed(true);
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // 得到传感器管理对象
        final SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // 得到传感器对象
        Sensor gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        setContentView(R.layout.activity_main);

        // 初始化两个时间相等
        myData.setCurPutDownTime(System.currentTimeMillis());
        myData.setLastPutDownTime(myData.getCurPutDownTime());


        // 新建传感器监听事件类
        SensorEventListener mySensorEventListener = new SensorEventListener() {
            // 传感器数据变化响应时触发
            @Override
            public void onSensorChanged(SensorEvent event) {

                // 手机朝下放置
                if (event.values[1] < -9.5) {
                    myData.setCurPutDownTime(System.currentTimeMillis());
                    if (myData.getCurPutDownTime() - myData.getLastPutDownTime() > 2000) {
                        myData.setPutDownByMistake(false);
                    }
                }
                // 手机重新拿起
                if (event.values[1] > 0) {
                    myData.setCurPutDownTime(System.currentTimeMillis());
                    myData.setLastPutDownTime(myData.getCurPutDownTime());


                }
                // 两次放下手机的时间
                TextView textView9 = (TextView) findViewById(R.id.textView9);
                TextView textView10 = (TextView) findViewById(R.id.textView10);
                textView9.setText(String.valueOf(myData.getLastPutDownTime()));
                textView10.setText(String.valueOf(myData.getCurPutDownTime()));
                // 显示每个方向的加速度 （保留小数点后两位有效）

                myData.setxPre(event.values[0]);

                myData.setyPre(event.values[1]);
                myData.setzPre(event.values[2]);

                TextView textView1 = (TextView) findViewById(R.id.textView1);
                textView1.setText(String.valueOf("x轴加速度" + (float) (Math.round(myData.getxPre() * 100)) / 100));

                TextView textView2 = (TextView) findViewById(R.id.textView2);
                textView2.setText(String.valueOf("y轴加速度" + (float) (Math.round(myData.getyPre() * 100)) / 100));
                TextView textView3 = (TextView) findViewById(R.id.textView3);
                textView3.setText(String.valueOf("z轴加速度" + (float) (Math.round(myData.getzPre() * 100)) / 100));

                //-------------------------------------------------------------------------------------------------------------------------
                //-------------------------------------------------------------------------------------------------------------------------
                //-------------------------------------------------------------------------------------------------------------------------
                //-------------------------------------------------------------------------------------------------------------------------
                //-------------------------------------------------------------------------------------------------------------------------
                //-------------------------------------------------------------------------------------------------------------------------
                //-------------------------------------------------------------------------------------------------------------------------
                //-------------------------------------------------------------------------------------------------------------------------


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
                    Button startBtn = (Button) findViewById(R.id.button);
                    startBtn.setText("游戏超时");


                    myData.setReady(false);
                    myData.setStart(false);
                    myData.setEnd(true);
                }

                //游戏准备过程中,动了手机
                if (myData.isReady() && !myData.isStart() && myData.getyPre() > -7 ) {
                    myData.setReady(false);

                    myData.setMoveWhenReady(true);
                    Button startBtn = (Button) findViewById(R.id.button);


                    startBtn.setText("准备过程中请保持手机朝下,请重新准备");
                    return;
                }
                //在准备过程中 且没有开始
                //提示游戏将在多久后开始
                else if (myData.isReady() && !myData.isStart()) {
                    long dTime =  myData.getStartTime()-System.currentTimeMillis();
                    if (dTime < 0) {
                        Button startBtn = (Button) findViewById(R.id.button);
                        startBtn.setText("准备就绪");
                        startBtn.refreshDrawableState();
                        myData.setReady(false);
                        myData.setStart(true);
                        return;
                    }
                    Button startBtn = (Button) findViewById(R.id.button);
                    startBtn.setText("游戏将在"+(int)dTime/1000+"秒后开始...");
                    startBtn.refreshDrawableState();
                    return;
                }





                //向下放置,即开始准备
                // 当手机朝下放置时（y轴加速度最小为-10）,且和上一次放下手机隔了3秒(防止误操作)，作出提示，3秒后开始游戏
                if (!myData.isEnd()&&!myData.isStart()&&!myData.isReady() && event.values[1] < -9.5 && !myData.isPutDownByMistake()) {


                    // 播放系统提示音
                    Button startBtn = (Button) findViewById(R.id.button);
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
//
//                    startBtn.setText("游戏将在3秒后开始...");
//                    startBtn.refreshDrawableState();
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e) {
                        ;
                    }
                    myData.setReady(true);            // 设置准备flag

                    // 震动提示开始 (张子煜的手机会闪退)
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(350);
                    // 设置开始游戏时间
                    myData.setStartTime(System.currentTimeMillis() + (int) (5 + Math.random() * (10)) * 1000);//5~14S的随机时间
                    myData.setStart(false);
                    myData.setReady(true);
                    return;
                }


//                // 游戏准备好了,且没有在准备状态中动了手机，并按下开火键，且将手机抬至水平状态
//                if (myData.isReady() && !myData.isMoveWhenReady() && myData.isFireBtnPressed() && event.values[0] > 9.5) {
//
//                    long timeDelta = System.currentTimeMillis() - myData.getStartTime();  // 间隔时间
//                    float xDelta = event.values[0] - myData.getxPre();
//                    float yDelta = event.values[1] - myData.getyPre();
//                    float zDelta = event.values[2] - myData.getzPre();
//                    // 显示每个方向的加速度变化率
//                    TextView textView4 = (TextView) findViewById(R.id.textView4);
//                    textView4.setText(String.valueOf("x轴加速度变化率" + xDelta / timeDelta / 1000 + "(per second)"));
//                    TextView textView5 = (TextView) findViewById(R.id.textView5);
//                    textView5.setText(String.valueOf("y轴加速度" + yDelta / timeDelta / 1000 + "(per second)"));
//                    TextView textView6 = (TextView) findViewById(R.id.textView6);
//                    textView6.setText(String.valueOf("z轴加速度" + zDelta / timeDelta / 1000 + "(per second)"));
//                    TextView textView7 = (TextView) findViewById(R.id.textView7);
//                    textView7.setText(String.valueOf("间隔时间" + timeDelta / 1000.0 + "(s)"));
//
//                    // 重新开始游戏
////                    isReady = !isReady;
//
//                }


            }

            // 传感器精度响应时触发
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };















        //添加 重新开始按钮监听事件
        final Button startBtn = (Button) findViewById(R.id.button);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startBtn.getText().equals("游戏超时")) {
                    myData = new GameData();


                }
            }
        });

        //注册listener，第三个参数是检测的精确度
        sensorManager.registerListener(mySensorEventListener,
                gravitySensor, SensorManager.SENSOR_DELAY_GAME);


        // 游戏的胜负判断

    }


}
