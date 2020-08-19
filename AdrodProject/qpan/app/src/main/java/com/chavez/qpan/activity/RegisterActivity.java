package com.chavez.qpan.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.chavez.qpan.R;
import com.chavez.qpan.animation.AnimationHelper;
import com.chavez.qpan.util.support.web.ServiceApi;
import com.chavez.qpan.util.support.http.HttpSupport;
import com.chavez.qpan.util.support.http.IHttpResponseHandle;
import com.chavez.qpan.util.support.http.ResponseEntity;
import com.chavez.qpan.util.support.matcher.PhoneMatcherSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    static final String regExp = "^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$";
    Point windowPoint = new Point();
    TextView phoneNumberText;
    TextView verificationCodeText;
    TextView getVerificationBtn;
    Button registerActionBtn;
    View loadingView;
    ImageView loadingIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        // 获取屏幕尺寸
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(windowPoint);
        findView();
        phoneNumberText.setWidth((int) (windowPoint.x * 0.8f));
        verificationCodeText.setWidth((int) (windowPoint.x * 0.8f));
        registerActionBtn.setWidth((int) (windowPoint.x * 0.8f));
        init();
    }

    private void findView() {
        phoneNumberText = findViewById(R.id.register_account);
        verificationCodeText = findViewById(R.id.register_verification_code);
        registerActionBtn = findViewById(R.id.register_action_btn);
        getVerificationBtn = findViewById(R.id.register_get_verification_code_btn);
        loadingView = findViewById(R.id.loading_view);
        loadingIcon = findViewById(R.id.loading_image);
    }

    private void init() {
        getVerificationBtn.setOnClickListener(v -> {
            if (getVerificationBtn.isSelected()) return;
            CountDownTimer timer = new CountDownTimer(10000, 1000) {
                public void onTick(long millisUntilFinished) {
                    String phoneNumber = phoneNumberText.getText().toString();
                    if (!PhoneMatcherSupport.isPhoneLegal(phoneNumber)) {
                        Toast.makeText(RegisterActivity.this, getResources().getString(R.string.invalid_phone_number), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    getVerificationBtn.setEnabled(false);
                    getVerificationBtn.setSelected(true);
                    getVerificationBtn.setTextColor(getResources().getColor(R.color.menuText));
                    getVerificationBtn.setText(millisUntilFinished / 1000 + "s");
                    HttpSupport httpSupport = new HttpSupport(new IHttpResponseHandle() {
                        @Override
                        public void success(ResponseEntity entity) {
                            Toast.makeText(RegisterActivity.this, getResources().getString(R.string.send_verify_code_success), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void error(ResponseEntity entity) {
                            Toast.makeText(RegisterActivity.this, getResources().getString(R.string.send_verify_code_failed), Toast.LENGTH_SHORT).show();
                        }
                    });
                    List<Map<String, String>> paramList = new ArrayList<>();
                    Map<String, String> param = new HashMap<>();
                    param.put("phoneNumber", phoneNumber);
                    paramList.add(param);
                    httpSupport.doGet(ServiceApi.User.REGISTER_GET_VERIFY_CODE, paramList);
                }

                public void onFinish() {
                    getVerificationBtn.setSelected(false);
                    getVerificationBtn.setEnabled(true);
                    getVerificationBtn.setTextColor(getResources().getColor(R.color.buttonColor));
                    getVerificationBtn.setText(getResources().getString(R.string.RegetVerificationCode));
                }
            };
            // The start() method of the CountDownTimer object is called to start the countdown, and there is no thread handling involved
            //调用 CountDownTimer 对象的 start() 方法开始倒计时，也不涉及到线程处理
            timer.start();
        });

        registerActionBtn.setOnClickListener(v -> {
            if (verificationCodeText != null && phoneNumberText != null) {
                String verifyCode = verificationCodeText.getText().toString();
                String phoneNumber = phoneNumberText.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.invalid_phone_number), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (verifyCode != null && verifyCode.length() == 4) {
                    HttpSupport httpSupport = new HttpSupport(new IHttpResponseHandle() {
                        @Override
                        public void success(ResponseEntity entity) {
                            hideLoading();
                            Intent intent = new Intent();
                            intent.setClass(RegisterActivity.this, IndexActivity.class);
                            startActivity(intent);
                        }

                        @Override
                        public void error(ResponseEntity entity) {
                            hideLoading();
                            Toast.makeText(RegisterActivity.this, entity.getResponse(), Toast.LENGTH_LONG).show();
                        }
                    });
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("account", phoneNumber);
                    jsonParam.put("verificationCode", verifyCode);
                    System.out.println("post:"+jsonParam.toJSONString());
                    httpSupport.doPost(ServiceApi.User.REGISTER_ACTION, jsonParam);
                    showLoading();
                } else {
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.invalid_verify_code), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    void showLoading() {
        if (loadingView != null) {
            loadingView.setVisibility(View.VISIBLE);
            AnimationHelper.translateAnimation(RegisterActivity.this, loadingIcon, R.anim.load_ani, false, View.VISIBLE);
        }
    }

    void hideLoading() {
        if (loadingIcon != null) loadingIcon.clearAnimation();
        if (loadingView != null)  loadingView.setVisibility(View.GONE);
    }

}
