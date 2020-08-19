package cn.chavez.qpan;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/5/18 22:03
 */
public class ResponseEntitySupport {
    private static JSONObject responseJson;
    private static JSONObject responseErrorJson;

    static {
        responseJson = new JSONObject();
        responseErrorJson = new JSONObject();
    }

    public static ResponseEntity<Object> success() {
        return obtainResponseEntity(responseJson, HttpStatus.OK);
    }

    public static ResponseEntity<Object> success(String msg, Object reason) {
        responseJson.put("message", msg);
        responseJson.put("reason", reason);
        return obtainResponseEntity(responseJson, HttpStatus.OK);
    }

    public static ResponseEntity<Object> success(Object obj) {

        try {
            String objJsonString = JSONObject.toJSONString(obj);
            JSONObject response = JSONObject.parseObject(objJsonString);
            return obtainResponseEntity(response, HttpStatus.OK);
        } catch (JSONException exception) {
            String objJsonString = JSONObject.toJSONString(obj);
            Object response = JSONArray.parse(objJsonString);
            return obtainResponseEntity(response, HttpStatus.OK);
        }

    }

    public static ResponseEntity<Object> error(HttpStatus httpStatus, String msg, Object reason) {
        JSONObject tempJSON = new JSONObject();
        tempJSON.put("type", httpStatus.getReasonPhrase());
        tempJSON.put("message", msg);
        tempJSON.put("reason", reason);
        responseErrorJson.put("error", tempJSON);
        return obtainResponseEntity(responseErrorJson, httpStatus);
    }

    private static ResponseEntity<Object> obtainResponseEntity(Object response, HttpStatus httpStatus) {
        return new ResponseEntity<Object>(response, httpStatus);
    }
}
