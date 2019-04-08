package com.whut.getianao.quickdraw.entity;

public class GameData {

    private boolean isReady = false;  // 是否在准备过程中
    private long startTime = 0;   // 开始游戏时间
    private float xPre = 0, yPre = 0, zPre = 0;   // 手机移动前的重量加速度
    private boolean isStart = false;//是否以及开始游戏
    private boolean fireBtnPressed; // 是否成功开枪
    private boolean isEnd; //本局游戏是否已经结束
    private long orderFireTime;//语音提示Fire的时间
    private boolean lowFireAngle;

    private long fireTime;//开火时间
    private long lastPutDownTime;       // 上一次手机放下的时间
    private long curPutDownTime;           // 当前手机放下的时间
    private boolean putDownByMistake = true;      // 不小心放下了手机
    private boolean moveWhenReady = false;


    public boolean isLowFireAngle() {
        return lowFireAngle;
    }

    public void setLowFireAngle(boolean lowFireAngle) {
        this.lowFireAngle = lowFireAngle;
    }

    public long getOrderFireTime() {
        return orderFireTime;
    }

    public void setOrderFireTime(long orderFireTime) {
        this.orderFireTime = orderFireTime;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public long getFireTime() {
        return fireTime;
    }

    public void setFireTime(long fireTime) {
        this.fireTime = fireTime;
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
    }


    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public float getxPre() {
        return xPre;
    }

    public void setxPre(float xPre) {
        this.xPre = xPre;
    }

    public float getyPre() {
        return yPre;
    }

    public void setyPre(float yPre) {
        this.yPre = yPre;
    }

    public float getzPre() {
        return zPre;
    }

    public void setzPre(float zPre) {
        this.zPre = zPre;
    }

    public boolean isFireBtnPressed() {
        return fireBtnPressed;
    }

    public void setFireBtnPressed(boolean fireBtnPressed) {
        this.fireBtnPressed = fireBtnPressed;
    }

    public long getLastPutDownTime() {
        return lastPutDownTime;
    }

    public void setLastPutDownTime(long lastPutDownTime) {
        this.lastPutDownTime = lastPutDownTime;
    }

    public long getCurPutDownTime() {
        return curPutDownTime;
    }

    public void setCurPutDownTime(long curPutDownTime) {
        this.curPutDownTime = curPutDownTime;
    }

    public boolean isPutDownByMistake() {
        return putDownByMistake;
    }

    public void setPutDownByMistake(boolean putDownByMistake) {
        this.putDownByMistake = putDownByMistake;
    }

    public boolean isMoveWhenReady() {
        return moveWhenReady;
    }

    public void setMoveWhenReady(boolean moveWhenReady) {
        this.moveWhenReady = moveWhenReady;
    }
}
