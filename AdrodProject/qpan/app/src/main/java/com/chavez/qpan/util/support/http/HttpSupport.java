package com.chavez.qpan.util.support.http;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.chavez.qpan.exception.HttpStopRequestException;
import com.chavez.qpan.model.UploadInfo;
import com.chavez.qpan.providers.UploadColumns;
import com.chavez.qpan.providers.UploadProvider;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpSupport {
    private final static String TAG = "HttpSupport";
    private static ExecutorService httpThread = Executors.newCachedThreadPool();
    private static Handler UiThreadHandler = new Handler(Looper.getMainLooper());
    private IHttpResponseHandle httpResponseHandle;

    public HttpSupport(IHttpResponseHandle handle) {
        this.httpResponseHandle = handle;
    }


    public void doGet(String webAddress, List<Map<String, String>>... params) {
        httpThread.execute(() -> {
            if (params.length == 1) {
                executeGet(webAddress, params[0], null);
            } else if (params.length == 2) {
                executeGet(webAddress, params[0], params[1]);
            }
        });
    }

    public void doGet(String webAddress,List<Map<String, String>> heads) {
        httpThread.execute(() -> {
            executeGet(webAddress, heads);
        });
    }

    public void doDelete(String webAddress,List<Map<String, String>> heads) {
        httpThread.execute(() -> {
            executeDelete(webAddress, heads);
        });
    }

    void executeGet (String webAddress, List<Map<String, String>> headers) {
        String finalWebAddress = webAddress;
        BufferedReader reader = null;
        HttpURLConnection connection = null;
        int statusCode = -1;
        String responseString = null;
        try {
            Log.i(TAG, "doDelete:" + finalWebAddress);
            URL url = new URL(finalWebAddress);
            connection = (HttpURLConnection) url.openConnection();
            // add headers
            if (headers != null && headers.size() != 0) {
                for (Map<String, String> head : headers) {
                    for (String key : head.keySet()) {
                        Log.i(TAG, "heads:" + key+":"+head.get(key));
                        connection.addRequestProperty(key, head.get(key));
                    }
                }
            }
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            statusCode = connection.getResponseCode();
            InputStream in = connection.getInputStream();
            reader = new BufferedReader((new InputStreamReader(in)));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            responseString = response.toString();
            responseHandle(statusCode, responseString);
        } catch (Exception e) {
            e.printStackTrace();
            responseHandle(statusCode, responseString);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    void executeDelete (String webAddress, List<Map<String, String>> headers) {
        String finalWebAddress = webAddress;
        BufferedReader reader = null;
        HttpURLConnection connection = null;
        int statusCode = -1;
        String responseString = null;
        try {
            Log.i(TAG, "doDelete:" + finalWebAddress);
            URL url = new URL(finalWebAddress);
            connection = (HttpURLConnection) url.openConnection();
            // add headers
            if (headers != null && headers.size() != 0) {
                for (Map<String, String> head : headers) {
                    for (String key : head.keySet()) {
                        Log.i(TAG, "heads:" + key+":"+head.get(key));
                        connection.addRequestProperty(key, head.get(key));
                    }
                }
            }
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("DELETE");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            statusCode = connection.getResponseCode();
            InputStream in = connection.getInputStream();
            reader = new BufferedReader((new InputStreamReader(in)));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            responseString = response.toString();
            responseHandle(statusCode, responseString);
        } catch (Exception e) {
            e.printStackTrace();
            responseHandle(statusCode, responseString);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    void executeGet(String webAddress, List<Map<String, String>> params, List<Map<String, String>> headers) {
        String finalWebAddress = webAddress;
        if (params != null && params.size() != 0) {
            StringBuilder paramsString = new StringBuilder();
            for (Map<String, String> param : params) {
                for (String key : param.keySet()) {
                    if (TextUtils.isEmpty(paramsString)) {
                        paramsString.append("?" + key + "=" + param.get(key));
                    } else {
                        paramsString.append("&" + key + "=" + param.get(key));
                    }
                }
            }
            finalWebAddress += paramsString;
        }
        BufferedReader reader = null;
        HttpURLConnection connection = null;
        int statusCode = -1;
        String responseString = null;
        try {
            Log.i(TAG, "doGet:" + finalWebAddress);
            URL url = new URL(finalWebAddress);
            connection = (HttpURLConnection) url.openConnection();
            // add headers
            if (headers != null && headers.size() != 0) {
                for (Map<String, String> head : headers) {
                    for (String key : head.keySet()) {
                        Log.i(TAG, "heads:" + key+":"+head.get(key));
                        connection.addRequestProperty(key, head.get(key));
                    }
                }
            }
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            statusCode = connection.getResponseCode();
            InputStream in = connection.getInputStream();
            reader = new BufferedReader((new InputStreamReader(in)));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            responseString = response.toString();
            responseHandle(statusCode, responseString);
        } catch (Exception e) {
            e.printStackTrace();
            responseHandle(statusCode, responseString);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void doPost(String webAddress, com.alibaba.fastjson.JSONObject params) {
        httpThread.execute(new Runnable() {
            @Override
            public void run() {
                executePost(webAddress, params);
            }
        });
    }

    public void doPost(String webAddress,  com.alibaba.fastjson.JSONObject params, List<Map<String, String>> headers) {
        httpThread.execute(new Runnable() {
            @Override
            public void run() {
                executePost(webAddress, params, headers);
            }
        });
    }

    void executePost(String webAddress, com.alibaba.fastjson.JSONObject params, List<Map<String, String>> headers) {
        BufferedReader reader = null;
        HttpURLConnection connection = null;
        int statusCode = -1;
        String responseString = null;
        try {
            URL url = new URL(webAddress);
            connection = (HttpURLConnection) url.openConnection();
            // add headers
            if (headers != null && headers.size() != 0) {
                for (Map<String, String> head : headers) {
                    for (String key : head.keySet()) {
                        Log.i(TAG, "heads:" + key+":"+head.get(key));
                        connection.addRequestProperty(key, head.get(key));
                    }
                }
            }
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");    // 默认是 GET方式
            connection.setUseCaches(false);         // Post 请求不能使用缓存
            connection.setInstanceFollowRedirects(true);   //设置本次连接是否自动重定向
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.addRequestProperty("Connection", "Keep-Alive");//设置与服务器保持连接
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;0.9");
            connection.connect();
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(params.toJSONString());
            out.flush();
            out.close();
            statusCode = connection.getResponseCode();
            StringBuilder response = new StringBuilder();
            if (statusCode == 200) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                HttpSupport.this.readLine(reader, response);
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                HttpSupport.this.readLine(reader, response);
            }
            responseString = response.toString();
            Log.i(TAG, "http post to :" + webAddress + "========>response: " + responseString);
            HttpSupport.this.responseHandle(statusCode, responseString);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            responseHandle(statusCode, responseString);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    void executePost(String webAddress, com.alibaba.fastjson.JSONObject params) {
        BufferedReader reader = null;
        HttpURLConnection connection = null;
        int statusCode = -1;
        String responseString = null;
        try {
            URL url = new URL(webAddress);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");    // 默认是 GET方式
            connection.setUseCaches(false);         // Post 请求不能使用缓存
            connection.setInstanceFollowRedirects(true);   //设置本次连接是否自动重定向
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.addRequestProperty("Connection", "Keep-Alive");//设置与服务器保持连接
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;0.9");
            connection.connect();
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(params.toJSONString());
            out.flush();
            out.close();
            statusCode = connection.getResponseCode();
            StringBuilder response = new StringBuilder();
            if (statusCode == 200) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                HttpSupport.this.readLine(reader, response);
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                HttpSupport.this.readLine(reader, response);
            }
            responseString = response.toString();
            Log.i(TAG, "http post to :" + webAddress + "========>response: " + responseString);
            HttpSupport.this.responseHandle(statusCode, responseString);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            responseHandle(statusCode, responseString);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void readLine(BufferedReader reader, StringBuilder result) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
    }

    private void responseHandle(int statusCode, String responseString) {
        JSONObject responseJson = null;
        if (httpResponseHandle != null) {
            if (statusCode == 200) {
                UiThreadHandler.post(() -> httpResponseHandle.success(new ResponseEntity(responseString, statusCode)));
                return;
            }
            try {
                responseJson = (JSONObject) JSONObject.parse(responseString);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
                UiThreadHandler.post(() -> httpResponseHandle.error(new ResponseEntity("网络异常", statusCode)));
            }
            if (responseJson != null) {
                JSONObject errorMsgJson = responseJson.getJSONObject("error");
                if (errorMsgJson != null) {
                    String errorMsg = errorMsgJson.getString("message");
                    UiThreadHandler.post(() -> httpResponseHandle.error(new ResponseEntity(errorMsg, statusCode)));
                }
            }
        }

    }

    public static List<Map<String, String>> obtainTokenHead(String accessToken) {
        System.out.println("access token: "+accessToken);
        List<Map<String, String>> heads = new ArrayList<>(1);
        Map<String, String> head = new HashMap<>(1);
        head.put("Access-Token", accessToken);
        heads.add(head);
        return heads;
    }

    public static List<Map<String, String>> obtainGetMethodParamList(Map<String, String>... params) {
        List<Map<String, String>> paramsList = new ArrayList<>(1);
        for (int i = 0; i < params.length; i++) {
            if (params[i].keySet() != null) {
                paramsList.add(params[i]);
            }
        }
        return paramsList;
    }

}
