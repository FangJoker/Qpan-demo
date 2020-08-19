package com.chavez.qpan.view;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chavez.qpan.R;
import com.chavez.qpan.animation.AnimationHelper;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.ActionBarContextView;
import androidx.appcompat.widget.LinearLayoutCompat;

/**
 * @Author Chavez Qiu
 * @Date 20-1-3.
 * Email：qiuhao1@meizu.com
 * Description：多选页自定义标题视图
 */
public class MultiChoiceView extends LinearLayoutCompat {

    WindowManager wm;
    private View actionModeTitleBar;
    private TextView mCancelTextView;
    private TextView mSelectAllTextView;
    private TextView mSelectCountTextView;

    private SelectAllListener mSelectAllListener;

    private String mTitle;

    public interface SelectAllListener {
        void selectAll();

        void cancel();
    }


    public void setSelectAllListener(SelectAllListener s) {
        this.mSelectAllListener = s;
    }

    public void setTitle(String title) {
        mTitle = title;
        initTitle();
    }

    private void initTitle() {
        if (mSelectCountTextView != null && mTitle != null) {
            mSelectCountTextView.setText(mTitle);
        }
    }

    public MultiChoiceView(Context context) {
        this(context, null);
    }

    public MultiChoiceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 0);
        setLayoutParams(layoutParams);
        final LayoutInflater inflater = LayoutInflater.from(context);
        actionModeTitleBar = inflater.inflate(R.layout.file_actionmode_title_bar, null);
        addView(actionModeTitleBar);
        mSelectCountTextView = actionModeTitleBar.findViewById(R.id.action_mode_selectCount);
        mSelectAllTextView = actionModeTitleBar.findViewById(R.id.action_mode_selectAll);
        mCancelTextView = actionModeTitleBar.findViewById(R.id.action_mode_cancel);
        mSelectAllTextView.setOnClickListener(v -> {
            if (mSelectAllListener != null) {
                // actionbar 动画
                mSelectAllListener.selectAll();
            }
        });
        mCancelTextView.setOnClickListener(v -> {
            if (mSelectAllListener != null) {
                mSelectAllListener.cancel();
            }
        });
    }

    public MultiChoiceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int customWidthMeasureSpec = widthMeasureSpec;
        if (getParent() instanceof ActionBarContextView) {
            View closeLayout = ((ActionBarContextView) getParent()).findViewById(R.id.action_mode_close_button);
            if (null != closeLayout) {
                customWidthMeasureSpec = MeasureSpec.makeMeasureSpec(closeLayout.getMeasuredWidth() + specWidth, specMode);
            }
        }
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        int width = point.x;
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width - 30, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}

