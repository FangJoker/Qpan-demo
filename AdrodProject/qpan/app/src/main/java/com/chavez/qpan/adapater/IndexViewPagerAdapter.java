package com.chavez.qpan.adapater;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class IndexViewPagerAdapter extends PagerAdapter {
    private List<View> mViewList;

    public IndexViewPagerAdapter(List<View> mViewList) {
        this.mViewList = mViewList;
    }

    @Override
    public int getCount() {
        if (mViewList != null && mViewList.size() != 0) {
            return mViewList.size();
        } else {
            return 0;
        }
    }
    //Determine whether the Page View and the instantiateItem(ViewGroup, int) return the same key to be made available to other functions
    //判断是否page view与 instantiateItem(ViewGroup, int)返回的object的key 是否相同，以提供给其他的函数使用
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    //instantiateItem The function of this method is to create a view of a page in a specified location. Before finishUpdate(ViewGroup) returns, the page should be guaranteed to be constructed

    // Return value: Returns an object for the page. This does not have to be a View, but should be some other container for the page

    //instantiateItem该方法的功能是创建指定位置的页面视图。finishUpdate(ViewGroup)返回前，页面应该保证被构造好
    //返回值：返回一个对应该页面的object，这个不一定必须是View，但是应该是对应页面的一些其他容器
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View item = mViewList.get(position);
        container.addView(item);
        return item;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
       container.removeView(mViewList.get(position));
    }
}
