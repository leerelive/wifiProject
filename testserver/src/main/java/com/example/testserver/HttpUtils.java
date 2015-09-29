package com.example.testserver;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by 李景晨 on 2015/9/25.
 */


public class HttpUtils {   //从服务器端下载到Json数据，也就是个字符串

    public static String getData(String url) throws Exception {

        StringBuilder sb = new StringBuilder();

        HttpClient httpClient = new DefaultHttpClient();

        HttpGet httpGet = new HttpGet(url);

        HttpResponse httpResponse = httpClient.execute(httpGet);

        HttpEntity httpEntity = httpResponse.getEntity();

        if (httpEntity != null) {

            InputStream instream = httpEntity.getContent();

            BufferedReader reader = new BufferedReader(new InputStreamReader(instream));

            String line = null;

            while ((line = reader.readLine()) != null) {

                sb.append(line);

            }

            return sb.toString();

        }

        return null;

    }

}