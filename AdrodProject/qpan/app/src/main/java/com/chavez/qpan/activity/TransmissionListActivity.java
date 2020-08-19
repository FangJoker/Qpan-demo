package com.chavez.qpan.activity;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;

import com.chavez.qpan.R;
import com.chavez.qpan.fragment.UploadCompletedListFragment;
import com.chavez.qpan.fragment.UploadListFragment;
import com.chavez.qpan.model.UploadInfo;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;


public class TransmissionListActivity extends AppCompatActivity {
    ViewPager viewPager;
    public static final int RESULT_UPLOAD_PAGE = 1;
    public static final int RESULT_DOWNLOAD_PAGE = 0;
    private TabLayout tabLayout = null;
    private ArrayList<Fragment> mFragmentArrays = null;
    private String[] mTabTitles = null;
    private List<UploadInfo> mUploadInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmission_list);
        // To get rid of the shadow
        //去掉阴影
        getSupportActionBar().setElevation(0);
        initActionBar();
        findView();
        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        System.out.println("activity requestCode:" + requestCode);
        if (requestCode == RESULT_DOWNLOAD_PAGE) {
            viewPager.setCurrentItem(RESULT_UPLOAD_PAGE);
        }
        if (requestCode == RESULT_DOWNLOAD_PAGE) {
            viewPager.setCurrentItem(RESULT_DOWNLOAD_PAGE);
        }
    }

    void findView() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.transmission_list_view_pager);
    }

    void initView() {
        mTabTitles = new String[]{getResources().getString(R.string.upload_completed_list),
                getResources().getString(R.string.upload_list)};
        mFragmentArrays = new ArrayList<>();
        mFragmentArrays.add(new UploadCompletedListFragment(this));
        mFragmentArrays.add(new UploadListFragment(this));

        PagerAdapter pagerAdapter = new MyViewPagerAdapter(getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(mFragmentArrays.size());
        viewPager.setAdapter(pagerAdapter);
        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().get("page")!=null) {
            viewPager.setCurrentItem(Integer.parseInt(intent.getExtras().get("page").toString()));
        }
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(viewPager);
    }

    void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }


    final class MyViewPagerAdapter extends FragmentPagerAdapter {


        public MyViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentArrays.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentArrays.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitles[position];
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
