package mycom.anystorage;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;

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
        String fileList[] = rootFile.list();
        for(int idx = 0; idx< fileList.length; idx++)
            Log.e("file : ", fileList[idx]);
    }
}
