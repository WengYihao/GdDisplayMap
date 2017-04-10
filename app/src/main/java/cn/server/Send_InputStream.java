package cn.server;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;

import cn.ui.MainActivity;

/**
 * Created by Shinelon on 2017/4/8.
 */

public class Send_InputStream implements Runnable {

    private InputStream inputStream;
    private MainActivity mainActivity;
    private String text = "null";

    public Send_InputStream(){}
    public Send_InputStream(InputStream inputStream){
        this.inputStream = inputStream;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
         int len = 0;
        byte[] buff = new byte[1024];
        try{
            while ((len = inputStream.read(buff)) != -1){
                text = new String(buff,0,len);
                Log.i("129",text+"返回值");
                JSONObject jsonObject = new JSONObject(text);
                int errorCode = jsonObject.getInt("errorCode");
                if (errorCode == 200){
                    JSONArray jsonArray = jsonObject.getJSONArray("date");
                    mainActivity.allLatLng(jsonArray);
                }
            }
        }catch (Exception e){
            Log.i("129",e.getMessage()+"接收返回值报错");
        }
    }
}
