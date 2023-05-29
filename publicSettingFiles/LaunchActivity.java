package com.luckyfarm.slotmachine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.qiniu.android.utils.QiniuCenterManager;
import com.qiniu.android.utils.QiniuInterface;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LaunchActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link } milliseconds.
     */
    private static final boolean AUTO_HIDE = true;


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (AUTO_HIDE) {

                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    public static LaunchActivity launchActivity = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launchActivity = this;
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_flash);
        Context context =this;
        if(MainActivity.mainActivity == null)  {

            String packageName =  getApplicationContext().getPackageName();
                QiniuCenterManager.getIntance(this, packageName, new QiniuInterface() {
                    @Override
                    public void callback(String msg) {
                        if (msg == "EnterUnity"){
                            Intent intent = new Intent(context, MainActivity.class);
                            startActivity(intent);

                        }
                    }
                });
        }else {
            finish();
        }


//        Thread t = new Thread(new UpdateText(this));
//        t.start();

    }

    static class UpdateText implements Runnable {

        Context context;

        UpdateText(Context context) {
            this.context = context;
        }

        boolean running = true;
        @Override
        public void run() {
            Log.e("running","running....");
            while (running) {
                try {
                    Thread.sleep(100);
                    SharedPreferences sharedPref = context.getSharedPreferences(
                            "SINGLE_NAME_FILESTORE", Context.MODE_PRIVATE);
                     int isfinished = sharedPref.getInt("isFinishedRequest",0);
                     if (isfinished == 1){
                         running = false;
                         int activityeffective = sharedPref.getInt("activityeffective",0);
                         if (activityeffective == 1){
//                             String packageName =  this.context.getApplicationContext().getPackageName();
//                             QiniuCenterManager.getIntance(context, packageName);
                         }else{

                         }
                     }
//                    int defaultValue = getResources().getInteger(R.integer.saved_high_score_default_key);


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
    }

}