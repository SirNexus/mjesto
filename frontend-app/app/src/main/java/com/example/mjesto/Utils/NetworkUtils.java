package com.example.mjesto.Utils;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkUtils {

    private static final OkHttpClient mHTTPClient = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");


    public static String doHttpGet(String url) throws IOException {
        Request req = new Request.Builder().url(url).build();
        Response res = mHTTPClient.newCall(req).execute();
        try {
            return res.body().string();
        } finally {
            res.close();
        }
    }

    public static String doHttpPatch(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request req = new Request.Builder().url(url).patch(body).build();
        Response res = mHTTPClient.newCall(req).execute();
        try {
            return res.body().string();
        } finally {
            res.close();
        }
    }

    public static String doHttpPost(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request req = new Request.Builder().url(url).post(body).build();
        Response res = mHTTPClient.newCall(req).execute();
        try {
            return res.body().string();
        } finally {
            res.close();
        }
    }
}
