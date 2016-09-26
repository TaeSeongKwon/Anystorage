package mycom.anystorage;


import android.app.Activity;
import android.content.Intent;
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

    public boolean isConnect(){ return device.connected(); }
    public boolean connect(Activity activity, String url, String userId, String userPwd){
        this.activity = activity;
        this.userId = userId;
        this.userPwd = userPwd;

        try{
            device = IO.socket(url);
            device.connect();
            init();
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

//                if(manager.setString("userPwd", this.userPwd))  Log.e("Save Pwd : ", "true");
//                else                                            Log.e("Save Pwd : ", "false");

                return true;
            }else return false;
        } catch (JSONException e) {
            Log.e("Login Error : ", e.toString());
            return false;
        }
    }
    private void registerEvent(){
        device.on("type", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = new JSONObject();
                device.emit("device", obj.toString());
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


    private void init(){
        device.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent intent;

                if ((userId != null && userPwd != null)) {
                    Log.e("userID : ", userId);
                    Log.e("userPASS : ", userPwd);
                    intent = new Intent(activity, AnystroageMain.class);
                } else {
                    intent = new Intent(activity, LoginActivity.class);
                }

                registerEvent();
                activity.startActivity(intent);
                activity.finish();

            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        });
    }
}
