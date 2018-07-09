package net.nipa0711.photosns;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hyunmin on 2015-03-30.
 * Start refactoring 2018-07-06.
 */
public class ServerPostComm extends Thread {
    private int command;
    private String url, values;
    private Handler handle;

    public ServerPostComm(String url, int command, String values,
                          Handler handle) {
        this.url = url;
        this.command = command;

        this.values = values;
        this.handle = handle;
    }


    public void run() {
        Log.d("=============here?","is this execute?");
        // 메시지 객체 생성
        Message message = handle.obtainMessage();

        try {
            // HttpClient 객체 생성
            HttpClient httpclient = new DefaultHttpClient();

            // HttpPost 객체 생성 및 파라메터 설정
            String addMaybe = "/hostserver/photoSNS";

            HttpPost httppost = new HttpPost(url + addMaybe + "?command=" + command);
            List<NameValuePair> paramList = new ArrayList<NameValuePair>();
            paramList.add(new BasicNameValuePair("params", values));
            httppost.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
            httppost.addHeader("text", "plain");

            // HTTP 호출 실행
            HttpResponse response = httpclient.execute(httppost);

            // 호출결과를 문자열로 변환
            String str = EntityUtils.toString(response.getEntity(), "UTF-8");

            // 호출 성공 후 핸들러 호출
            message.what = 0; // 성공
            message.arg1 = command;
            message.arg2 = 0; // POST 호출
            message.obj = str;
            handle.sendMessage(message);
        } catch (Exception e) {
            // 호출 실패 후 핸들러 호출
            message.what = 1; // 실패
            message.arg1 = command;
            message.arg2 = 0; // POST 호출
            handle.sendMessage(message);
        }
    }
}