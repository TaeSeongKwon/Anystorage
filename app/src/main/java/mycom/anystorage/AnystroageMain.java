package mycom.anystorage;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.File;

public class AnystroageMain extends AppCompatActivity {
    private String rootPath;
    private String dataPath;
    private ClientWebSocket socket;
    private boolean isLogout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        init();
        // gkdl hhghgby
    }

    @Override
    protected void onDestroy() {
        Log.e("logout", "123");
        if(isLogout)
            socket.logout();
        else
            socket.powerOff();

        super.onDestroy();

    }

    public void init(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        File dataFile = Environment.getDataDirectory();
        File rootFile = Environment.getExternalStorageDirectory();
        rootPath = rootFile.getAbsolutePath();
        dataPath = dataFile.getAbsolutePath();
        socket = ClientWebSocket.getInstance();
        isLogout = false;
//        Log.e("root Path : ", rootPath);
//        Log.e("data Path : ", dataPath);
//        File fileList[] = rootFile.listFiles();
//        File file = new File(rootPath+"/"+fileList[0]);
//        Log.e("file Path : ", file.getAbsolutePath());
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd h:mm a");
//        for(int idx = 0; idx< fileList.length; idx++) {
////            Calendar calendar = Calendar.getInstance();
////            calendar.setTime();
//            Log.e("file : ", format.format(new Date(fileList[idx].lastModified())));
//        }
    }
    public void logout(View v){
        Intent intent = new Intent(this, LoginActivity.class);
        isLogout = true;
        startActivity(intent);
        this.finish();
    }
}
