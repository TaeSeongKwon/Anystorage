package mycom.anystorage;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AnystroageMain extends AppCompatActivity {
    private String rootPath;
    private String dataPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        init();
        // gkdl hhghgby
    }

    public void init(){
        File dataFile = Environment.getDataDirectory();
        File rootFile = Environment.getExternalStorageDirectory();
        rootPath = rootFile.getAbsolutePath();
        dataPath = dataFile.getAbsolutePath();
        Log.e("root Path : ", rootPath);
        Log.e("data Path : ", dataPath);
        File fileList[] = rootFile.listFiles();
        File file = new File(rootPath+"/"+fileList[0]);
        Log.e("file Path : ", file.getAbsolutePath());
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd h:mm a");
//        for(int idx = 0; idx< fileList.length; idx++) {
////            Calendar calendar = Calendar.getInstance();
////            calendar.setTime();
//            Log.e("file : ", format.format(new Date(fileList[idx].lastModified())));
//        }
    }
}
