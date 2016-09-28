package mycom.anystorage;


import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

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


        device.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                sendPowerOffMsg();
            }
        });
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
