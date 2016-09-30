package mycom.anystorage;


import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by KTS on 2016-09-26.
 */
public class ClientWebSocket {
    private static ClientWebSocket ourInstance = new ClientWebSocket();
    private Socket device;
    public static ClientWebSocket getInstance() {
        return ourInstance;
    }

    private String userId;
    private String userPwd;
    private Activity activity;
    private ClientWebSocket() {}
    private boolean isSuccess;
    private boolean flag = true;
    private boolean device_ack = true;
    private long cnt;
    public boolean isConnect(){ return device.connected(); }
    public boolean connect(Activity activity, String url, String userId, String userPwd){
        this.activity = activity;
        this.userId = userId;
        this.userPwd = userPwd;

        try{
            device = IO.socket(url);
            device.connect();
            init();
            new AutoLogin().start();
            return true;
        } catch (URISyntaxException e) {
            Log.e("Web Socket Error : ", e.toString());
            return false;
        }
    }
    public boolean login(String userId, String userPwd){
        this.userId = userId;
        this.userPwd = userPwd;
        return this.login();
    }
    public boolean login(){
        JSONObject loginData = new JSONObject();

        isSuccess = false;
        try {

            loginData.put("userId", this.userId);
            loginData.put("userPwd", this.userPwd);

            device.emit("device_login", loginData);
            flag = true;

            // Receive Ready
            while(flag) {
                try{
                    Thread.sleep(500);
                }catch(Exception e){}
            }

            if(isSuccess){
                AccountManager manager = AccountManager.getInstance();
                Log.e("userID : ", userId);
                Log.e("userPASS : ", userPwd);
                if(manager.setString("userId", this.userId))    Log.e("Save ID : ", "true");
                else                                            Log.e("Save ID : ", "false");

                if(manager.setString("userPwd", this.userPwd))  Log.e("Save Pwd : ", "true");
                else                                            Log.e("Save Pwd : ", "false");

                try {
                    String device_serial = (String) Build.class.getField("SERIAL").get(null);
                    device.on(userId + device_serial, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            device.emit("ack_connect_device", args[0]);
                        }
                    });
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                // Send Power On Message to Web Browser
                sendPowerOnMsg();
                return true;
            }else return false;
        } catch (JSONException e) {
            Log.e("Login Error : ", e.toString());
            return false;
        }
    }


    // Send Power On Message to Web Browser Method
    private void sendPowerOnMsg(){
        JSONObject res = new JSONObject();
        try {
            res.put("device_name", Build.DEVICE);
            res.put("device_model", Build.MODEL);
            res.put("device_serial", (String)Build.class.getField("SERIAL").get(null));
            device.emit("res_on_device", res);
        } catch (JSONException e) {
            Log.e("req_on_device Error : ", e.toString());
        }
        catch (NoSuchFieldException e) {
            Log.e("req_on_device Error : ", e.toString());
//            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e("req_on_device Error : ", e.toString());
//            e.printStackTrace();
        }
    }

    // Send Power Off Message to Web Browser Method
    private void sendPowerOffMsg(){
        JSONObject res = new JSONObject();
        try {
            res.put("device_name", Build.DEVICE);
            res.put("device_model", Build.MODEL);
            device.emit("res_off_device", res);
        } catch (JSONException e) {
            Log.e("req_on_device Error : ", e.toString());
        }
    }

    private void init(){
        // Initialize Socket
        device.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                // call register event
                device.on("type", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        JSONObject obj = new JSONObject();
                        device.emit("device", obj.toString());
                    }
                });
                device.on("req_on_device", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        // Send Power On Message to Web Browser
                        sendPowerOnMsg();
                    }
                });
                device.on("login_response", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
//                    JSONObject obj = new JSONObject(args[0].toString());
                            JSONObject obj = (JSONObject) args[0];
                            Log.e("Data ::::", obj.toString());
                            isSuccess = obj.getBoolean("isSuccess");

                        } catch (JSONException e) {
                            Log.e("Login Error : ", e.toString());
                            isSuccess = false;
                        }
                        flag = false;
                    }
                });

            }
        });
        device.on("req_file_tree", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e("request_file tree", " ");
                File rootFile = Environment.getExternalStorageDirectory();
                JSONArray tree = getFileTree(rootFile);
                Log.e("create file tree", "   ");
                device.emit("res_file_tree", tree.toString());
                Log.e("send file tree", "   ");
            }
        });

        device.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                sendPowerOffMsg();
            }
        });
    }
    private JSONArray getFileTree(File root){
        this.cnt = 0;
        return createFileTree(root);
    }
    private JSONArray createFileTree(File root){
        File[] childList = root.listFiles();
        JSONArray list = new JSONArray();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd h:mm a");
        Calendar calendar = Calendar.getInstance();
        JSONArray data;
        JSONObject obj;
        File node;
        Long timelong, size;

        int lastPoint, nameLength;
        String executeType, type;
        for(int idx = 0; idx < childList.length; idx++){
            node = childList[idx];
            cnt++;
            try {
                calendar.setTime(new Date(node.lastModified()));

                data = new JSONArray();
                obj = new JSONObject();

                obj.put("id", "file_"+cnt);
//                Log.e("id : ", ""+node.lastModified());
                obj.put("value", node.getName());
                obj.put("open", false);
                obj.put("date",node.lastModified()/1000);

                if (node.isFile()) {
                    lastPoint = node.getName().lastIndexOf(".");
                    nameLength = node.getName().length();
                    size = node.length();
                    obj.put("size", size);

                    if(lastPoint != -1){
                        executeType = node.getName().substring(lastPoint+1, nameLength);
                        executeType = executeType.toLowerCase();
                        Log.e("executeType", executeType);
                        if(executeType.equals("doc") || executeType.equals("docx") || executeType.equals("docm")){
                            type = "Document";
                        }else if(executeType.equals("dot") || executeType.equals("dotx") || executeType.equals("dotm")){
                            type = "Document";
                        }else if(executeType.equals("ppt") || executeType.equals("pptx") || executeType.equals("pptm")){
                            type = "pp";
                        }else if(executeType.equals("pot") || executeType.equals("potx") || executeType.equals("potm")){
                            type = "pp";
                        }else if(executeType.equals("pps") || executeType.equals("ppsx") || executeType.equals("ppsm")){
                            type = "pp";
                        }else if(executeType.equals("xls") || executeType.equals("xlsx") || executeType.equals("xlsm")){
                            type = "excel";
                        }else if(executeType.equals("zip") || executeType.equals("tar") || executeType.equals("rar")){
                            type = "archive";
                        }else if(executeType.equals("jar") || executeType.equals("alz") || executeType.equals("xlsm")){
                            type = "archive";
                        }else if(executeType.equals("jpg") || executeType.equals("jpeg") || executeType.equals("gif")){
                            type = "image";
                        }else if(executeType.equals("png") || executeType.equals("psd") || executeType.equals("pdd")){
                            type = "image";
                        }else if(executeType.equals("tif") || executeType.equals("raw") || executeType.equals("svg")){
                            type = "image";
                        }else{
                            type = executeType;
                        }
                    }else {
                        type = "file";
                    }
                    obj.put("type", type);
//                id: "files", value: "Files", open: true,  type: "folder", date:  new Date(2014,2,10,16,10), data:
                } else if (node.isDirectory()) {
                    type = "forder";
                    data = createFileTree(node);
                    obj.put("type", type);
                    obj.put("data", data);
                }
                list.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    class AutoLogin extends Thread{
        public void run(){
            Intent intent;
            while(true) {
                if (device.connected()) {
                    // Check SharedPreference UserInfo
                    if ((userId != null && userPwd != null)) {
                        Log.e("Start Login!", "4");
                        // Auto Login
                        if (login()) intent = new Intent(activity, AnystroageMain.class);
                        else intent = new Intent(activity, LoginActivity.class);
//                        intent = new Intent(activity, LoginActivity.class);
                    } else {
                        intent = new Intent(activity, LoginActivity.class);
                    }
                    // Change Activity
                    activity.startActivity(intent);
                    activity.finish();
                    break;
                }
                try{
                    Thread.sleep(500);
                }catch(Exception e){}
            }

        }
    }
}
