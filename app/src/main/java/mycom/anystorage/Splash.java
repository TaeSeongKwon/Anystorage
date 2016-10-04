package mycom.anystorage;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

public class Splash extends Activity {
    // Customer Singletone Class
    private ClientWebSocket device;
    private AccountManager userInfo;

    private String userId;
    private String userPwd;
    private boolean isSuccess;

    private void init(){
        // This is Singletone DesignPattern -> Reference to ClientWebSocket.java

        device = ClientWebSocket.getInstance();
        userInfo = AccountManager.getInstance();
        userInfo.init(getSharedPreferences("setting",0 ));

        //asdfasdf
        userId = userInfo.getString("userId");
        userPwd = userInfo.getString("userPwd");

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);
        init();
//        if(userId != null)  Log.e("user ID : ", userId);
//        else                Log.e("user ID : ", "NULL");
//        if(userPwd != null) Log.e("userPASS : ", userPwd);
//        else                Log.e("userPASS : ", "NULL");

        device.connect(Splash.this, "http://www.devkts.kro.kr:9900", userId, userPwd);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        }, 3000);
    }
}
