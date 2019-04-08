package com.whut.getianao.quickdraw.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.whut.getianao.quickdraw.R;
import com.whut.getianao.quickdraw.activity.ServerActivity;

public class SummaryFragment extends Fragment {
    private ServerActivity parentActivity;
    private info.hoang8f.widget.FButton gameagain;
    private info.hoang8f.widget.FButton quit;
    private TextView myresult_text;
    private TextView eneryresylt_text;
    private int result;
    private String myresult;
    private String enemyresult;
    private View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.summary, container, false);
        parentActivity = (ServerActivity) getActivity();
        initView(view);
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }
    private void initView(View view){
        //button的初始化
        gameagain=view.findViewById(R.id.game_again);
        quit=view.findViewById(R.id.quit);
        gameagain.setButtonColor(getResources().getColor(R.color.red));
        quit.setButtonColor(getResources().getColor(R.color.red));
        myresult_text=view.findViewById(R.id.myresult);
        eneryresylt_text=view.findViewById(R.id.eneryresult);
        //获得结果并设置两条String

        myresult="你的开枪速度是：";
        enemyresult="对方的开枪速度是：";
        myresult_text.setText(myresult);
        eneryresylt_text.setText(enemyresult);
        //背景的设置
        if (result==1){
//            view.getWindow().setBackgroundDrawableResource(R.mipmap.win);
        }else{
//            getWindow().setBackgroundDrawableResource(R.mipmap.lose);
        }


    }

}
