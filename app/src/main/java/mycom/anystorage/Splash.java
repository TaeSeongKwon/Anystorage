package mycom.anystorage;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.nkzawa.engineio.client.Socket;

public class Splash extends Activity {
    private Socket device;
    private SharedPreferences userInfo;
    private String userId;
    private String userPwd;
    private boolean isSuccess;

    private void init(){
        userInfo = getSharedPreferences("setting",0 );
        userId = userInfo.getString("userID", null);
        userPwd = userInfo.getString("userPwd", null);
        isSuccess = userInfo.getBoolean("isSuccess", false);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);
        init();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent Intent;
                if((userId != null && userPwd != null )&& isSuccess){
                    Intent = new Intent(Splash.this, AnystroageMain.class);
                    Splash.this.startActivity(Intent);
                }else{

                }
                Splash.this.finish();
            }
        }, 3000);
    }
}
