package com.chavez.qpan.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.chavez.qpan.R;
import com.chavez.qpan.adapater.IndexViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

public class WelcomeActivity extends AppCompatActivity {
    ViewPager viewPager;
    CircleIndicator indicator;
    Button startAppBtn;
    private IndexViewPagerAdapter mViewPagerAdapter;
    private List<View> viewPagerData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        findView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initViewPager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        doTimeTask();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    void findView() {
        viewPager = findViewById(R.id.index_view_pager);
        indicator =  findViewById(R.id.indicator);
        startAppBtn = findViewById(R.id.index_start_btn);
    }

    void initViewPager() {
        ImageView item = new ImageView(this);
        item.setImageResource(R.drawable.index1);
        ImageView item2 = new ImageView(this);
        item2.setImageResource(R.drawable.index2);
        viewPagerData.add(item);
        viewPagerData.add(item2);
        mViewPagerAdapter = new IndexViewPagerAdapter(viewPagerData);
        viewPager.setAdapter(mViewPagerAdapter);
        indicator.setViewPager(viewPager);
        startAppBtn.setOnClickListener(v -> {
          cancelTimeTask();
        });
    }

    Timer timer = new Timer();
    void doTimeTask(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    int cur = viewPager.getCurrentItem();
                    if (cur == viewPagerData.size()-1) {
                        startAppBtn.setVisibility(View.VISIBLE);
                    } else {
                        cur = (cur + 1) % viewPagerData.size();
                        viewPager.setCurrentItem(cur);
                    }
                });
            }
        };
        timer.schedule(timerTask, 1000, 1000);
    }

    void cancelTimeTask(){
        timer.cancel();
        Intent intent = new Intent();
        intent.setClass(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
    }

}
