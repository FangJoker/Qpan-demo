package com.chavez.qpan.animation;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


/**
 * @Author Chavez Qiu
 * @Date 20-1-6.
 * Email：qiuhao1@meizu.com
 * Animation tool class (animation can be cached with the LUR algorithm)
 * Description： 动画工具类（可以用LUR算法缓存动画）
 */
public class AnimationHelper {

    /**
     * 转换动画
     *
     * @param context
     * @param view        需要作画的view  A view that needs to be painted
     * @param resourceId  动画资源Id Animation resource Id
     * @param isFillAfter 是否保持动画后的位置 Whether to maintain the position after the animation
     * @param visibility  view是否可见  Is the View visible
     */
   public static void translateAnimation(Context context, View view, int resourceId, boolean isFillAfter, int visibility) {
        Animation animation = AnimationUtils.loadAnimation(context, resourceId);
        animation.setFillAfter(isFillAfter);
        view.setVisibility(visibility);
        view.startAnimation(animation);
    }
}
