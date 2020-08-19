package com.chavez.qpan.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chavez.qpan.R;

/**
 * 提示框
 *
 * @author chavezQiu
 */
public class CustomDialog extends Dialog {
    /**
     * 显示的icon
     */
    private ImageView imageIcon;

    /**
     * 显示的标题
     */
    private TextView titleTv;

    /**
     * 显示的消息
     */
    private TextView messageTv;

    /**
     * 输入的内容
     */
    private EditText editText;


    private View selectView;

    private TextView selectViewEditTv;

    /**
     * 选择器
     */
    private CheckBox item1,item2;

    /**
     * 确认和取消按钮
     */
    private Button negativeBtn, positiveBtn;


    /**
     * 按钮之间的分割线
     */
    private View columnLineView;

    private AlertParams mAlertParams;

    public static final int MESSAGE_TYPE_NORMAL = 0;
    public static final int MESSAGE_TYPE_WARNING = 1;


    private static class AlertParams {
        private String mMessage;
        private String mTitle;
        private String mPositiveText, mNegativeText;
        private String checkItem1Text, checkItem2Text;
        private String selectViewEditHint;
        private boolean showEditText = false;
        private boolean showSelectView = false;
        private String editTextHint;
        private int mIconResId = -1;
        /**
         * 底部是否只有一个按钮
         */
        private boolean isSingle = false;
        private DialogOnClickListen onClickListener = null;
        private DialogOnCheckListen onCheckListen = null;
        private int mMessageType = MESSAGE_TYPE_NORMAL;

        public String getTitle() {
            return mTitle;
        }

        public String getMessage() {
            return mMessage;
        }

        public String getPositiveBtnText() {
            return mPositiveText;
        }

        public String getNegativeBtnText() {
            return mNegativeText;
        }

        public int getIconResId() {
            return mIconResId;
        }

        public int getMessageType() {
            return mMessageType;
        }

    }


    public static class Builder {
        private final AlertParams P;
        private final Context mContext;


        public Builder(@NonNull Context context, AlertParams p) {
            P = p;
            mContext = context;
        }

        public Builder(@NonNull Context context) {
            this(context, new AlertParams());
        }

        public Builder setIcon(int resId) {
            P.mIconResId = resId;
            return this;
        }

        public Builder setIsSingle(boolean flag){
            P.isSingle = flag;
            return this;
        }

        public Builder setPositiveBtnText(String text) {
            P.mPositiveText = text;
            return this;
        }

        public Builder setNegativeBtnText(String text) {
            P.mNegativeText = text;
            return this;
        }

        public Builder setAlertType(int type) {
            P.mMessageType = type;
            return this;
        }

        public Builder setBtnOnclickListener(DialogOnClickListen dialogOnClickListen) {
            P.onClickListener = dialogOnClickListen;
            return this;
        }

        public Builder setCheckBoyOnCheckListener(DialogOnCheckListen listener){
            P.onCheckListen = listener;
            return this;
        }

        public Builder setCheckItem1Text(String text){
            P.showSelectView = true;
            P.checkItem1Text = text;
            return  this;
        }

        public Builder setCheckItem2Text(String text){
            P.showSelectView = true;
            P.checkItem2Text = text;
            return  this;
        }

        public Builder setSelectViewEditHint(String text){
            P.selectViewEditHint = text;
            return this;
        }

        public Builder setTitle(String title) {
            P.mTitle = title;
            return this;
        }

        public Builder setMessage(String msg) {
            P.mMessage = msg;
            return this;
        }

        public Builder setEditText(String hint) {
            P.showEditText = true;
            P.editTextHint = hint;
            return this;
        }

        public CustomDialog create() {
            if (mContext != null) {
                return new CustomDialog(mContext, P);
            } else {
                return null;
            }
        }

    }

    public CustomDialog(@NonNull Context context, AlertParams alertParams) {
        super(context, R.style.CustomDialog);
        this.mAlertParams = alertParams;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qpan_dialog);
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);
        initView();
    }

    @Override
    public void show() {
        super.show();
        initDialogData();
    }

    private void initView() {
        System.out.println("===dialog initView");
        negativeBtn = findViewById(R.id.dialog_negative_btn);
        positiveBtn = findViewById(R.id.dialog_positive_btn);
        titleTv = findViewById(R.id.dialog_title);
        messageTv = findViewById(R.id.dialog_message);
        imageIcon = findViewById(R.id.dialog_icon);
        columnLineView = findViewById(R.id.column_line);
        editText = findViewById(R.id.dialog_et);
        selectView = findViewById(R.id.select_view);
        selectViewEditTv = findViewById(R.id.select_view_edit);
        item1 = findViewById(R.id.select_view_item1);
        item2 = findViewById(R.id.select_view_item2);
    }

    private void initDialogData() {
        if (mAlertParams.mMessageType == MESSAGE_TYPE_NORMAL) {
            if (messageTv == null)
                messageTv.setTextColor(getContext().getResources().getColor(R.color.menuText));
        } else {
            messageTv.setTextColor(getContext().getResources().getColor(R.color.warningColor));
        }

        if (!TextUtils.isEmpty(mAlertParams.mMessage)) {
            messageTv.setText(mAlertParams.mMessage);
            messageTv.setVisibility(View.VISIBLE);
        } else {
            messageTv.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(mAlertParams.mTitle)) {
            titleTv.setText(mAlertParams.mTitle);
            titleTv.setVisibility(View.VISIBLE);
        } else {
            titleTv.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(mAlertParams.mPositiveText)) {
            positiveBtn.setText(mAlertParams.mPositiveText);
            positiveBtn.setVisibility(View.VISIBLE);
        } else {
            positiveBtn.setVisibility(View.GONE);
        }

        if (!mAlertParams.isSingle&& !TextUtils.isEmpty(mAlertParams.mNegativeText)) {
            negativeBtn.setText(mAlertParams.mNegativeText);
            negativeBtn.setVisibility(View.VISIBLE);
        } else {
            negativeBtn.setVisibility(View.GONE);
        }

        if (mAlertParams.mIconResId != -1) {
            imageIcon.setImageResource(mAlertParams.mIconResId);
            imageIcon.setVisibility(View.VISIBLE);
        } else {
            imageIcon.setVisibility(View.GONE);
        }

        if (mAlertParams.onClickListener != null) {
            positiveBtn.setOnClickListener(v -> mAlertParams.onClickListener.onPositive(CustomDialog.this));
        }

        if (mAlertParams.onClickListener != null) {
            negativeBtn.setOnClickListener(v -> mAlertParams.onClickListener.onNegative(CustomDialog.this));
        }

        if (mAlertParams.onCheckListen!=null){
            item1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mAlertParams.onCheckListen.onItem1Check(buttonView,isChecked);
                    item1.setChecked(isChecked);
                    item2.setChecked(!isChecked);
                    if (isChecked){
                        selectViewEditTv.setVisibility(View.VISIBLE);
                    }else {
                        selectViewEditTv.setVisibility(View.GONE);
                    }
                }
            });
            item2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mAlertParams.onCheckListen.onItem2Check(buttonView,isChecked);
                    item2.setChecked(isChecked);
                    item1.setChecked(!isChecked);
                    if (isChecked){
                        selectViewEditTv.setVisibility(View.GONE);
                    }else {
                        selectViewEditTv.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        if (mAlertParams.showEditText) {
            editText.setVisibility(View.VISIBLE);
            editText.setHint(mAlertParams.editTextHint);
        }
        if (mAlertParams.showSelectView){
            item1.setText(mAlertParams.checkItem1Text);
            item2.setText(mAlertParams.checkItem2Text);
           selectView.setVisibility(View.VISIBLE);
           selectViewEditTv.setHint(mAlertParams.selectViewEditHint);
        }
    }

    public interface DialogOnClickListen {
        void onPositive(CustomDialog dialog);

        void onNegative(CustomDialog dialog);
    }

    public interface  DialogOnCheckListen{
        void onItem1Check(CompoundButton buttonView, boolean isChecked);
        void onItem2Check(CompoundButton buttonView, boolean isChecked);
    }

    public String getEditText() {
        return editText != null ? editText.getText().toString() : "";
    }

    public String getSelectViewEditText(){
        return  selectViewEditTv!=null?selectViewEditTv.getText().toString():"新建文件夹";
    }

}
