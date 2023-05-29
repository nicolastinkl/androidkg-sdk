package com.qiniu.android.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import java.util.concurrent.CountDownLatch;

public class Wait {

    final CountDownLatch completeSingle = new CountDownLatch(1);

    public void startWait(){
        while (completeSingle.getCount() > 0) {
            try {
                completeSingle.await();
                break;
            } catch (InterruptedException e) {
            }
        }
    }

    public void stopWait(){
        completeSingle.countDown();
    }

}
