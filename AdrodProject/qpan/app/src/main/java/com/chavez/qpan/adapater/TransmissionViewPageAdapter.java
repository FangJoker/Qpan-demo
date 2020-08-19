package com.chavez.qpan.adapater;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class TransmissionViewPageAdapter extends PagerAdapter {
    private List<View> mViewPageData;

    public TransmissionViewPageAdapter(List<View> viewPagerData) {
        this.mViewPageData = viewPagerData;
    }

    @Override
    public int getCount() {
        return mViewPageData != null ? mViewPageData.size() : 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
       View item = mViewPageData.get(position);
       container.addView(item);
       return  item;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView(mViewPageData.get(position));
    }
}
