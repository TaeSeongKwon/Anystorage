package mycom.anystorage;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * Created by KTS on 2016-09-26.
 */
public class AccountManager {
    private static AccountManager ourInstance = new AccountManager();
    public static AccountManager getInstance() {
        return ourInstance;
    }

    private SharedPreferences userInfo;
    private Editor writer;

    private AccountManager() {
    }
    public void init(SharedPreferences reader){
        this.userInfo = reader;
        this.writer = reader.edit();
    }
    public String getString(String key){
        if(this.userInfo == null){
            Log.e("Account Manager Error ","userInfo is null!!");
            return null;
        }
        return this.userInfo.getString(key, null);
    }

    public boolean setString(String key, String val){
        if(this.writer == null){
            Log.e("Account Manager Error ","userInfo is null!!");
            return false;
        }
        this.writer.remove(key);
        this.writer.putString(key, val);
        return this.writer.commit();
    }
}
