package com.qiniu.android.utils;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;
import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.qiniu.android.databinding.ActivityQiniuFullscreenBinding;
import com.qiniu.android.R;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class QiniuFullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler(Looper.myLooper());
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            if (Build.VERSION.SDK_INT >= 30) {
//                mContentView.getWindowInsetsController().hide(
//                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            } else {
                // Note that some of these constants are new as of API 16 (Jelly Bean)
                // and API 19 (KitKat). It is safe to use them, as they are inlined
                // at compile-time and do nothing on earlier devices.
                mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
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
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
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
    private ActivityQiniuFullscreenBinding binding;

    private WebView webView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityQiniuFullscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mVisible = true;
        mControlsView = binding.fullscreenContentControls;
        mContentView = binding.fullscreenContent;
        webView = binding.fullscreenWebview;

        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new JsInterface(this) , "jsBridge");

//        WebSettings settings = webView.getSettings();
//        settings.setJavaScriptEnabled(true);
//        settings.setDomStorageEnabled(true);
//        settings.setUseWideViewPort(true);
//        settings.setDatabaseEnabled(true);
//        // settings.setAppCacheEnabled(true);
//        settings.setAllowFileAccess(true);
//        settings.setSupportMultipleWindows(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        webView.getSettings().setJavaScriptEnabled(true);
//        Android. webview. "Uncaught (in promise) TypeErrior: Cannot read properties of null (reading 'removeItem')", 
        webView.getSettings().setDomStorageEnabled(true);

        String url = getIntent().getStringExtra("url");
        if (url != null) {
            url = url.replace("\\","");
            webView.loadUrl(url);

        }

/*
        //复写WebViewClient类的shouldOverrideUrlLoading方法
        webView.setWebViewClient(new WebViewClient() {

            //该重载方法不建议使用了，7.0系统以上已经摒弃了
            //shouldOverrideUrlLoading(WebView view, String url)此方法，
            //如果要拦截URL，需要做兼容性处理，重写
            //shouldOverrideUrlLoading(WebView view, WebResourceRequest request)方法，
            //获取得到的可正常使用的URL
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){

                // 步骤2：根据协议的参数，判断是否是所需要的url
                // 一般根据scheme（协议格式） & authority（协议名）判断（前两个参数）
                //假定传入进来的 url = "js://webview?arg1=111&arg2=222"（同时也是约定好的需要拦截的）
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    uri = request.getUrl();
                } else {
                    uri = Uri.parse(request.toString());
                }
                // 如果url的协议 = 预先约定的 js 协议,就解析往下解析参数
                //System.out.println("js调用了Android的方法");
                System.out.println("js调用了Android的方法"+uri.toString());
                if ( uri.getScheme().equals("js")) {
                    // 如果 authority = 预先约定协议里的webview，即代表都符合约定的协议
                    // 所以拦截url,下面JS开始调用Android需要的方法
//                    if (uri.getAuthority().equals("webview")) {
//
//                        // 步骤3：执行JS所需要调用的逻辑
//                        System.out.println("js调用了Android的方法");
//                        // 可以在协议上带有参数并传递到Android上
//                        HashMap<String, String> params = new HashMap<>();
//                        Set<String> collection = uri.getQueryParameterNames();
//
//                        String result = "Android回调给JS的数据为useid=123456";
//                        view.loadUrl("javascript:returnResult(\"" + result + "\")");
//
//                    }
                   // return true;
                }
                return super.shouldOverrideUrlLoading(view, request);
            }
        });
*/



        String aftoken = getIntent().getStringExtra("aftoken");

        Log.v("==========token", aftoken);
        if (aftoken != null) {
    //ADJUST
            String environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
            AdjustConfig config = new AdjustConfig(getApplicationContext(), aftoken, environment);
            Adjust.onCreate(config);
            Adjust.setEnabled(true);



    //appsflyer
            AfHelper af = new AfHelper();
            af.initAF(getApplicationContext(), aftoken, new AppsFlyerConversionListener() {
                @Override
                public void onConversionDataSuccess(Map<String, Object> conversionDataMap) {
                    Log.v("onConversionDataSuccess", conversionDataMap.toString());
                }

                @Override
                public void onConversionDataFail(String errorMessage) {
                    Log.v("onConversionDataFail", errorMessage);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //webView.loadUrl(p);
                        }
                    });
                }

                @Override
                public void onAppOpenAttribution(Map<String, String> attributionData) {
                    Log.v("onAppOpenAttribution", "error");
                }

                @Override
                public void onAttributionFailure(String errorMessage) {
                    Log.v("onAttributionFailure", errorMessage);
                }
            });
        }

        binding.dummyButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.goBack();
            }
        });
        binding.dummyButtonReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.reload();
            }
        });

        binding.dummyButtonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.goForward();
            }
        });

        binding.dummyButtonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = getIntent().getStringExtra("url");
                if (url != null) {
                    url = url.replace("\\","");
                    webView.loadUrl(url);
                }
            }
        });

        //fullscreen_webview

        // Set up the user interaction to manually show or hide the system UI.
//        mContentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggle();
//            }
//        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        binding.dummyButton.setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    public void onBackPressed() {



        if (webView.canGoBack()) {
            webView.goBack();
        } else {
           // super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
//        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar
        if (Build.VERSION.SDK_INT >= 30) {
            mContentView.getWindowInsetsController().show(
                    WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        } else {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}


class JsInterface{
    public Context context = null;
    public JsInterface(Context cnx) {
        context = cnx;
    }

    private Map<String, Object> jsonToMap(String json) {
        Map<String, Object> map = new HashMap<>();
        try {
            if (json != null && json.length() > 0) {
                org.json.JSONObject data = new org.json.JSONObject(json);
                Iterator<String> keys = data.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    map.put(key, data.get(key));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;

    }


    public String findEventValue(String[] events, String eventName) {
        for (String event : events) {
            if (event.startsWith(eventName + "：")) {
                return event.substring(eventName.length() + 1);
            }
        }
        return null;
    }

    // Android 调用 Js 方法1 中的返回值
    @JavascriptInterface
    public void postMessage(String name,String data){
        Log.e("TAG", "postMessage  name=="+name+"  data=="+data);


        String chzname = "";
        if (name.equals("login")) {
            chzname = "登录";
        } else if (name.equals("logout")) {
            chzname = "登出";
        } else if (name.equals("registerClick")) {
            chzname = "注册点击";
        } else if (name.equals("register")) {
            chzname = "注册成功";
        } else if (name.equals("rechargeClick")) {
            chzname = "充值点击";
        } else if (name.equals("firstrecharge")) {
            chzname = "首充成功";
        } else if (name.equals("recharge")) {
            chzname = "复充成功";
        } else if (name.equals("withdrawClick")) {
            chzname = "提现点击";
        } else if (name.equals("withdrawOrderSuccess")) {
            chzname = "提现成功";
        } else if (name.equals("enterGame")) {
            chzname = "进入游戏";
        } else if (name.equals("vipReward")) {
            chzname = "贵宾奖励";
        } else if (name.equals("dailyReward")) {
            chzname = "每日奖励";
        }else{
            chzname = name;
        }


        SharedPreferences sharedPref = context.getSharedPreferences(
                "SINGLE_NAME_FILESTORE", Context.MODE_PRIVATE);

        String maidianrule = sharedPref.getString("maidianrule","");

       // System.out.println("maidianrule：" + maidianrule);
        String[] events = maidianrule.split("\n");

        String eventValue = findEventValue(events, chzname);
        System.out.println(chzname + " 事件值：" + eventValue);
        if (eventValue != null){
            eventValue = eventValue.trim();


            if(eventValue != null  && eventValue.length()>0){
                chzname = eventValue;
            }
        }
        try {
            AdjustEvent adjustEvent = new AdjustEvent(chzname);
            Adjust.trackEvent(adjustEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Map<String, Object> params = jsonToMap(data);
            AppsFlyerLib.getInstance().logEvent(context, chzname, params);

        } catch (Exception e) {
            e.printStackTrace();
        }


        /*
        * JsInterface我司定义的事件名称如下：

            登录：“login”

            登出：“logout”

            点击注册：“registerClick”

            *注册成功 ：“register”

            点击充值：“rechargeClick”

            *首充成功 ：“firstrecharge”

            *复充成功 ：“recharge”

            提现点击：“withdrawClick”

            *提现成功 ：“withdrawOrderSuccess”

            进入游戏(包含三方与自营)：“enterGame”

            领取vip奖励：“vipReward”

            领取每日奖励：“dailyReward”
        * */
    }
}


class AfHelper {


    public void initAF(Context ctx, String AF_KEY, AppsFlyerConversionListener conversionListener) {

        try {
            AppsFlyerLib.getInstance().init(AF_KEY, conversionListener, ctx);
            AppsFlyerLib.getInstance().start(ctx);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        try {
            InstallReferrerClient referrerClient;
            referrerClient = InstallReferrerClient.newBuilder(ctx).build();
            Context finalCtx = ctx;
            referrerClient.startConnection(new InstallReferrerStateListener() {
                @Override
                public void onInstallReferrerSetupFinished(int responseCode) {
                    try {
                        switch (responseCode) {
                            case InstallReferrerClient.InstallReferrerResponse.OK:
                                ReferrerDetails response = referrerClient.getInstallReferrer();
                                if(response==null)return;
                                Log.i("initReferrer", response.getInstallReferrer());
                                Helper.setSharedPreferences(finalCtx,"ref", response.getInstallReferrer());
                                break;
                        }
                        referrerClient.endConnection();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onInstallReferrerServiceDisconnected() {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


        Context finalCtx1 = ctx;
        new Thread(()->{
            try {
                AdvertisingIdClient client = new AdvertisingIdClient(finalCtx1);
                client.start();
                AdvertisingIdClient.Info info = client.getInfo();
                if (info == null) return;
                Log.i("initAID", info.getId());
                Helper.setSharedPreferences (finalCtx1, "adid", info.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();*/
    }


    static class Helper {
        public static final void setSharedPreferences(Context context, String key, String val) {
            try {
                if (context == null) return;
                SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
                sp.edit().putString(key, val).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public static final String getSharedPreferences(Context context, String key) {
            try {
                if (context == null) return null;
                SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
                return sp.getString(key, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "null";
        }
    }



}


