package com.adadjkwa.invitation;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



public class MainActivity extends AppCompatActivity {

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


    class AfHelper {


        public void initAF(Context ctx, String AF_KEY, AppsFlyerConversionListener conversionListener) {
            ctx = ctx;
            try {
                AppsFlyerLib.getInstance().init(AF_KEY, conversionListener, ctx);
                AppsFlyerLib.getInstance().start(ctx);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            }).start();
        }




    }

    static class Http {
        public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        static final OkHttpClient client = new OkHttpClient();

        public static String post(String url, String json,String appid,String key) throws IOException {
            RequestBody body = RequestBody.create(json, JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("appid",appid)
                    .addHeader("key",key)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                return response.body().string();
            }
        }
    }
    final String TAG = "MainActivity";

    final static String p = "file:///android_asset/p/07/index.html";
    private String URL = "https://server.theupermarket.vip/api/app/strategy";
    private String appid = "8";
    private String key = "uo32412q1";
    WebView webView;
    private String deviceId;
    private String versionName;

    ValueCallback<Uri[]> filePathProcess;
    ActivityResultLauncher<Intent> chooseFileResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                try {
                    if (filePathProcess == null)
                        return;
                    Uri[] uris = {};
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            uris = new Uri[] { data.getData() };
                        }
                    }
                    filePathProcess.onReceiveValue(uris);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

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

    @Keep
    @JavascriptInterface
    public String onAppEvent(String eventName, String json) {
        try {
            Map<String, Object> params = jsonToMap(json);
            switch (eventName) {
                case "openWB": {
                    Object url = params.get("url");
                    if (url != null) {
                        // 在mainActive 的webView 打开url
                        startActivity(Intent.parseUri(url.toString(), Intent.URI_INTENT_SCHEME));
                    }
                }
                break;
                case "openPG": {
                    Object url = params.get("url");
                    if (url != null) {
                        // 在mainActive 的webView 打开url
                        startActivity(new Intent(MainActivity.this, MainActivity.class)
                                .putExtra(Intent.ACTION_ATTACH_DATA, url.toString()));
                    }
                }
                break;
                case "aid": {
                    return Helper.getSharedPreferences(this, "adid");
                }
                case "referrer": {
                    return Helper.getSharedPreferences(this, "ref");
                }
            }
            AppsFlyerLib.getInstance().logEvent(this, eventName, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "null";
    }

    @Keep
    @JavascriptInterface
    public String onAppEvent(String eventName) {
        return onAppEvent(eventName, "{}");
    }

    @Keep
    @JavascriptInterface
    public String eventTracker(String eventName, String json) {
        return onAppEvent(eventName, json);
    }

    @Keep
    @JavascriptInterface
    public String eventTracker(String eventName) {
        return onAppEvent(eventName, "{}");
    }

    @Keep
    @JavascriptInterface
    public String onEvent(String eventName) {
        return onAppEvent(eventName, "{}");
    }

    @Keep
    @JavascriptInterface
    public String onEvent(String eventName, String json) {
        return onAppEvent(eventName, json);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView);

        deviceId = Settings.Secure.getString(MainActivity.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        PackageManager packageManager = MainActivity.this.getPackageManager();

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(MainActivity.this.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setDatabaseEnabled(true);
        // settings.setAppCacheEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        // Hgvdywgfsak webAppInterface = new
        // Hgvdywgfsak(MainActivity.this,webView);
        // webView.addJavascriptInterface(webAppInterface,"Android");
        // webView.addJavascriptInterface(webAppInterface,"i");
        // webView.addJavascriptInterface(webAppInterface,"AnalyticsWebInterface");

        webView.addJavascriptInterface(MainActivity.this, "Android");
        webView.addJavascriptInterface(MainActivity.this, "i");
        webView.addJavascriptInterface(MainActivity.this, "AnalyticsWebInterface");

        webView.getSettings().setUserAgentString(getUseragent(MainActivity.this, versionName, deviceId));

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                try {
                    if (request != null) {
                        String uri = request.getUrl().toString();
                        if (!uri.startsWith("http")) {
                            startActivity(Intent.parseUri(uri, Intent.URI_INTENT_SCHEME));
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return super.shouldOverrideUrlLoading(view, request);
            }

        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsAlert(WebView webView, String url, String message, JsResult jsResult) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainActivity.this);
                builder.setMessage(message)
                        .setPositiveButton("OK", (arg0, arg1) -> arg0.dismiss()).show();
                jsResult.cancel();
                return true;
            }

            @Override
            public void onCloseWindow(WebView window) {
                super.onCloseWindow(window);
                finish();
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                if (webView.getHitTestResult().getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE
                        || webView.getHitTestResult().getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    String url = webView.getHitTestResult().getExtra();
                    startActivity(
                            new Intent(MainActivity.this, MainActivity.class).putExtra(Intent.ACTION_ATTACH_DATA, url));
                } else {

                    WebView tempWebView = new WebView(MainActivity.this);
                    tempWebView.setWebViewClient(new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                            return super.shouldOverrideUrlLoading(view, request);
                        }

                        @Override
                        public boolean shouldOverrideUrlLoading(WebView w, String s) {
                            webView.loadUrl(s);
                            return super.shouldOverrideUrlLoading(w, s);
                        }

//                        @Override
//                        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler,
//                                                       SslError sslError) {
//                            if (sslErrorHandler != null) {
//                                sslErrorHandler.proceed();
//                            }
//                        }
                        //删除避免gp 审核提示
                    });
                    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                    transport.setWebView(tempWebView);
                    resultMsg.sendToTarget();
                    return true;
                }
                return true;
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                MainActivity.this.filePathProcess = filePathCallback;
                try {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    // intent.setType("image/*");
                    intent.setType("*/*");
                    chooseFileResultLauncher.launch(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        webView.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                    webView.goBack();
                    return true;
                }
            }
            return false;
        });
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            try {
                startActivity(Intent.parseUri(url, Intent.URI_INTENT_SCHEME));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        });

        String url = getIntent().getStringExtra(Intent.ACTION_ATTACH_DATA);
        if (url == null || url.equals("")) {
            new AsyncTask<Void, Void, JSONObject>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected JSONObject doInBackground(Void... voids) {

                    JSONObject result = new JSONObject();

                    // 在网络服务器中获取数据
                    OkHttpClient client = new OkHttpClient();
                    MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");
                    JSONObject body = new JSONObject();
                    JSONObject device = new JSONObject();
                    device.put("serial", Build.SERIAL);
                    device.put("deviceModel", Build.MODEL);
                    device.put("id", Build.ID);
                    device.put("manufacturer", Build.MANUFACTURER);
                    device.put("deviceBrand", Build.BRAND);
                    device.put("type", Build.TYPE);
                    device.put("user", Build.USER);
                    device.put("base", Build.VERSION_CODES.BASE);
                    device.put("incremental", Build.VERSION.INCREMENTAL);
                    device.put("board", Build.BOARD);
                    device.put("host", Build.HOST);
                    device.put("fingerprint", Build.FINGERPRINT);
                    device.put("versioncode", Build.VERSION.RELEASE);

                    // PackageManager packageManager = MainActivity.this.getPackageManager();
                    //
                    // try {
                    // PackageInfo packageInfo =
                    // packageManager.getPackageInfo(MainActivity.this.getPackageName(), 0);
                    // String versionName = packageInfo.versionName;
                    // device.put("appVersion", versionName);
                    // } catch (PackageManager.NameNotFoundException e) {
                    // e.printStackTrace();
                    // }

                    device.put("appVersion", versionName);

                    // String deviceId =
                    // Settings.Secure.getString(MainActivity.this.getContentResolver(),
                    // Settings.Secure.ANDROID_ID);
                    device.put("deviceId", deviceId);
                    device.put("platform", "android");
                    body.put("device", device);

                    String resp = null;
                    try {
                        resp = Http.post(URL, body.toString(), appid, key);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                        result.put("status", false);
                        return result;
                    }
                    JSONObject jsonObject = JSON.parseObject(resp);
                    int code = jsonObject.getInteger("code");
                    if (code == 0) {
                        JSONObject data = jsonObject.getJSONObject("data");

                        String effective_traffic = data.getString("effective_traffic");
                        Log.i(TAG, "effective_traffic:" + effective_traffic);
                        if (effective_traffic.equals("organic")) {
                            // 需要判断 af 归因 是否是不是自然流量，如果是需要过滤掉
                            result.put("is_af", true);
                        }else {
                            result.put("is_af", false);
                        }

                        String clickid = data.getString("clickid");
                        // String adjust_key = data.getString("adjust_key");
                        // String environment = data.getString("adjust_env");
                        // String adjust_click_event_token = data.getString("adjust_click_event_token");
                        // String adjust_open_event_token = data.getString("adjust_open_event_token");
                        //
                        // String adjust_click_event_name = data.getString("adjust_click_event_name");
                        // String adjust_open_event_name = data.getString("adjust_open_event_name");
                        //
                        String account = data.getString("account");
                        boolean seen = data.getBoolean("seen");
                        String jump_url = data.getString("jump_url");
                        String af_key = data.getString("af_key");

                        result.put("status", seen);
                        result.put("clickid", clickid);
                        result.put("jump_url", jump_url);
                        result.put("account", account);
                        result.put("af_key", af_key);
                        return result;
                    } else {
                        result.put("status", false);
                    }
                    return result;
                }

                @Override
                protected void onPostExecute(JSONObject result) {
                    if (result.getBoolean("status")) {
                        AfHelper af = new AfHelper();
                        af.initAF(MainActivity.this, result.getString("af_key"), new AppsFlyerConversionListener() {
                            @Override
                            public void onConversionDataSuccess(Map<String, Object> conversionDataMap) {
                                String status = Objects.requireNonNull(conversionDataMap.get("af_status")).toString();
                                if(status.equals("Non-organic")){
                                    String jump_url = result.getString("jump_url");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            webView.loadUrl(jump_url);
                                        }
                                    });
                                } else {
                                    if(result.getBoolean("is_af")){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                webView.loadUrl(p);
                                            }
                                        });
                                    }else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                String jump_url = result.getString("jump_url");
                                                webView.loadUrl(jump_url);
                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onConversionDataFail(String errorMessage) {
                                Log.v("mshh", errorMessage);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        webView.loadUrl(p);
                                    }
                                });
                            }

                            @Override
                            public void onAppOpenAttribution(Map<String, String> attributionData) {
                                Log.v("mshh", "error");
                            }

                            @Override
                            public void onAttributionFailure(String errorMessage) {
                                Log.v("mshh", errorMessage);
                            }
                        });
                    } else {
                        //a
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                webView.loadUrl(p);
                            }
                        });
                    }
                    super.onPostExecute(result);
                }
            }.execute();
        } else {
            webView.loadUrl(url);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destoryWebView();
    }

    private final void destoryWebView() {
        try {
            webView.stopLoading();
            webView.removeAllViews();
            webView.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getUseragent(final Context context, String version, String uuid) {
        String userAgent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                userAgent = WebSettings.getDefaultUserAgent(context);
            } catch (Exception e) {
                userAgent = System.getProperty("http.agent");
            }
        } else {
            userAgent = System.getProperty("http.agent");
        }
        final StringBuilder sb = new StringBuilder();
        assert userAgent != null;
        for (int i = 0, length = userAgent.length(); i < length; i++) {
            char c = userAgent.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        String replace = sb.toString().replace("; wv", "; xx-xx");
        return String.format("%s/%s AppShellVer:%s UUID/%s", replace, android.os.Build.BRAND, version, uuid);
    }

    private void openSystemBrowser(Uri uri) {
        Intent intent;
        try {
            intent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setComponent(null);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("openSystemBrowser failure", e.toString());
        }
    }
}