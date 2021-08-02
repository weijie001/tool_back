package com.tool.http;


import okhttp3.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OkhttpUtils {
    public static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(30L, TimeUnit.SECONDS)
            .writeTimeout(30L, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .build();



    private OkhttpUtils() {
    }

    public static byte[] sendData(String url, byte[] input) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(outputStream);
        if (input != null) {
            dos.write(input);
        }
        return post(url,outputStream);
    }
    public static byte[] post(String url,ByteArrayOutputStream outputStream) throws IOException {
        byte[] result = null;
        RequestBody requestBody2 = RequestBody.create(MediaType.parse("application/x-protobuf"),outputStream.toByteArray());
        Request request2 = null;
        if (url.endsWith("loginC/login")) {
            request2 = new Request.Builder().url(url).addHeader("Accept","application/x-protobuf").post(requestBody2).build();
        } else {
            request2 = new Request.Builder().url(url).addHeader("Accept","application/x-protobuf")
                    .addHeader("cookie",threadLocal.get()).post(requestBody2).build();
        }
        Response response2 = okHttpClient.newCall(request2).execute();
        if (response2.isSuccessful()) {
            result= response2.body().bytes();
        }
        if(url.endsWith("loginC/login")){
            Headers headers = response2.headers();
            List<String> cookies = headers.values("Set-Cookie");
            String s = cookies.get(0);
            String session = s.substring(0, s.indexOf(";"));
            threadLocal.set(session);
        }
        return result;
    }

    public static byte[] post2(String url,byte[] input) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(outputStream);
        if (input != null) {
            dos.write(input);
        }
        byte[] result = null;
        RequestBody requestBody2 = RequestBody.create(MediaType.parse("application/x-protobuf"),outputStream.toByteArray());
        Request request2 = new Request.Builder().url(url).addHeader("Accept","application/x-protobuf").post(requestBody2).build();
        Response response2 = okHttpClient.newCall(request2).execute();
        if (response2.isSuccessful()) {
            result= response2.body().bytes();
        }
        return result;
    }
}
