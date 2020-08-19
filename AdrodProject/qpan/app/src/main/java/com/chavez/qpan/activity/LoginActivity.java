package com.chavez.qpan.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chavez.qpan.DBEngine;
import com.chavez.qpan.R;
import com.chavez.qpan.animation.AnimationHelper;
import com.chavez.qpan.providers.UserColumns;
import com.chavez.qpan.providers.UserInfoProvider;
import com.chavez.qpan.util.support.http.HttpSupport;
import com.chavez.qpan.util.support.http.IHttpResponseHandle;
import com.chavez.qpan.util.support.http.ResponseEntity;
import com.chavez.qpan.util.support.web.ServiceApi;

import org.w3c.dom.Text;

import java.util.Map;


public class LoginActivity extends AppCompatActivity {
    private final static String TAG = "LoginActivity";
    private static int mBackTime = 0;
    View loginLoadingView;
    ImageView loginLoadingIcon;
    Button loginBtn;
    EditText accountEditText;
    EditText pwdEditText;
    TextView registerText;
    Point windowPoint = new Point();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        //Get screen size
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(windowPoint);
        findView();
        int editWidth = (int) (windowPoint.x * 0.8f);
        int btnWidth = (int) (windowPoint.x * 0.81f);
        accountEditText.setWidth(editWidth);
        pwdEditText.setWidth(editWidth);
        loginBtn.setWidth(btnWidth);
        goToIndexIfDonotNeedLogin();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
    }

    void findView() {
        loginBtn = findViewById(R.id.loginBtn);
        accountEditText = findViewById(R.id.login_account);
        pwdEditText = findViewById(R.id.login_pwd);
        registerText = findViewById(R.id.login_register);
        loginLoadingView = findViewById(R.id.login_loading_view);
        loginLoadingIcon = findViewById(R.id.login_loading_view_icon);
    }

    void init() {
        loginBtn.setOnClickListener(v -> {
            loginActionHandle();
        });
        registerText.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            mBackTime = 0;
        });
        accountEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideSoftInput(v);
            }
        });
        pwdEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideSoftInput(v);
            }
        });
    }

    private void hideSoftInput(View view) {
        InputMethodManager manager = ((InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE));
        if (manager != null)
            manager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void showLoginIngView() {
        loginLoadingView.setVisibility(View.VISIBLE);
        AnimationHelper.translateAnimation(this, loginLoadingIcon, R.anim.load_ani, false, View.VISIBLE);
    }

    private void hideLoginIngView() {
        loginLoadingIcon.animate().cancel();
        loginLoadingView.setVisibility(View.GONE);
    }

    private void loginActionHandle() {
//        String account = "13143106151";
//        String pwd = "123456";
        String account = accountEditText.getText().toString();
        String pwd = pwdEditText.getText().toString();
        showLoginIngView();
        JSONObject requestJson = new JSONObject();
        requestJson.put("account", account);
        requestJson.put("password", pwd);
        new HttpSupport(new IHttpResponseHandle() {
            @Override
            public void success(ResponseEntity entity) {
                JSONObject responseStringJson = (JSONObject) JSONObject.parse(entity.getResponse());
                String accessToken = responseStringJson.getString("Access-Token");
                String expirationTime = responseStringJson.getString("expiration_time");
                Long freeBytes = responseStringJson.getLong("personal_free_byte");
                Long totalBytes = responseStringJson.getLong("total_bytes");
                DBEngine dbEngine = new DBEngine(LoginActivity.this);
                dbEngine.setDataBaseHandle(new DBEngine.DataBaseHandle<Object>() {
                    @Override
                    public Object doInBackground() {
                        if (!TextUtils.isEmpty(accessToken)) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(UserColumns.COLUMN_TOTAL_BYTES,totalBytes);
                            contentValues.put(UserColumns.COLUMN_USER_ACCOUNT, account);
                            contentValues.put(UserColumns.COLUMN_FREE_BYTES,freeBytes);
                            contentValues.put(UserColumns.COLUMN_ACCESS_TOKEN, accessToken);
                            contentValues.put(UserColumns.COLUMN_ACCESS_TOKEN_EXPIRATION_TIME, expirationTime);
                            try {
                                getContentResolver().insert(UserInfoProvider.CONTENT_URI, contentValues);
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                            }
                            return accessToken;
                        }
                        return null;
                    }

                    @Override
                    public void doInUiThread(Object result) {
                        hideLoginIngView();
                        goToIndexActivity(result.toString());
                    }
                });
                dbEngine.execute();
            }

            @Override
            public void error(ResponseEntity entity) {
                hideLoginIngView();
                System.out.println("code:" + entity.getResponseCode());
                System.out.println("response:" + entity.getResponse());
                if (entity.getResponseCode() == 404 || entity.getResponseCode() == 400) {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.account_or_pwd_is_error), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                }
            }
        }).
                doPost(ServiceApi.User.LOGIN_ACTION, requestJson);
    }

    @Override
    public void onBackPressed() {
        if (++mBackTime < 2) {
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_LONG).show();
        } else {
            finish();
        }
    }

    private void goToIndexIfDonotNeedLogin() {
        DBEngine engine = new DBEngine(LoginActivity.this);
        engine.setDataBaseHandle(new DBEngine.DataBaseHandle<Map<String, String>>() {
            @Override
            public Map<String, String> doInBackground() {
                Cursor cursor = null;
                Map<String, String> resultMap = new ArrayMap<>();
                cursor = getContentResolver().query(UserInfoProvider.CONTENT_URI, new String[]{
                        UserColumns.COLUMN_ACCESS_TOKEN, UserColumns.COLUMN_ACCESS_TOKEN_EXPIRATION_TIME
                }, null, null, null);
                if (cursor.moveToLast()) {
                    String accessToken = cursor.getString(cursor.getColumnIndex(UserColumns.COLUMN_ACCESS_TOKEN));
                    if (accessToken != null) {
                        resultMap.put(UserColumns.COLUMN_ACCESS_TOKEN, accessToken);
                        Log.v(TAG, "Access-Token:" + accessToken);
                    }
                    long tokenExpirationTime = cursor.getLong(cursor.getColumnIndex(UserColumns.COLUMN_ACCESS_TOKEN_EXPIRATION_TIME));
                    if (tokenExpirationTime > 0) {
                        resultMap.put(UserColumns.COLUMN_ACCESS_TOKEN_EXPIRATION_TIME, String.valueOf(tokenExpirationTime));
                    } else {
                        Log.v(TAG, accessToken + " token is expiry");
                    }
                }
                cursor.close();
                return resultMap;
            }

            @Override
            public void doInUiThread(Map<String, String> result) {
                String accessToken = result.get(UserColumns.COLUMN_ACCESS_TOKEN);
                if (accessToken != null) {
                    long tokenExpirationTime = Long.parseLong(result.get(UserColumns.COLUMN_ACCESS_TOKEN_EXPIRATION_TIME));
                    long now = System.currentTimeMillis();
                    if (tokenExpirationTime > now) {
                        showLoginIngView();
                        goToIndexActivity(accessToken);
                    } else {
                        Log.v(TAG, "token is expiry" + " token expiration time:" + tokenExpirationTime + " now time:" + System.currentTimeMillis());
                    }
                }
            }
        });
        engine.execute();
    }

    private void goToIndexActivity(String token) {
        Intent intent = new Intent();
        if (token!=null){
            intent.putExtra("token", token);
        }
        intent.setClass(LoginActivity.this, IndexActivity.class);
        startActivity(intent);
        mBackTime = 0;
        finish();
    }
}
