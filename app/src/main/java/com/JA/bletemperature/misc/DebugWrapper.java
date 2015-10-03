package com.JA.bletemperature.misc;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DebugWrapper {
    final static public boolean DISPLAY_DEBUG_MSGS = true;
    final static public boolean DISPLAY_INFO_MSGS = true;
    final static public boolean DISPLAY_ERROR_MSGS = true;
//    public static final String TAG = "BL600 OTA";
    
    
    public static void debugMsg(String msg, String tag){
        if(DISPLAY_DEBUG_MSGS == true){
            Log.d(tag, msg);
        }
    }
    
    public static void infoMsg(String msg, String tag){
        if(DISPLAY_INFO_MSGS == true){
            Log.i(tag, msg);
        }
    }
    
    public static void errorMsg(String msg, String tag){
        if(DISPLAY_ERROR_MSGS == true){
            Log.e(tag, msg);
        }
    }
    
    /**
     * displays the given message on the screen
     * @param activity
     * @param msg the text to display
     */
    public static void toastMsg(final Activity activity, final String msg){
    /**    activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                LinearLayout toastLayout = (LinearLayout) toast.getView();
                TextView toastTV = (TextView) toastLayout.getChildAt(0);
                toastTV.setTextSize(30);
                toast.show();
            }
        });
     **/
    }
}