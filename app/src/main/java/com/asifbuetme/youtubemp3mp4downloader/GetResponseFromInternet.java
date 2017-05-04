package com.asifbuetme.youtubemp3mp4downloader;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

class GetResponseFromInternet extends AsyncTask<String, Integer, String> {

    public HttpsURLConnection client;
    InputStream inputStream;
    public Map<String, List<String>> responseHeaderList;
    public String response;
    private Context context;
    private String method;
    private int timeout;
    private HashMap<String, String> post_param;
    private String ActionName;


    GetResponseFromInternet(Context context, String method, int timeout) {
        this.method = method;
        this.timeout = timeout;
        this.context = context;
    }

    GetResponseFromInternet(Context context, String method, HashMap<String, String> data, int timeout) {

        this.method = method;
        this.timeout = timeout;
        this.post_param = data;
        this.context = context;

    }

    public void setActionName(String actionName) {
        this.ActionName = actionName;
    }

    @Override
    protected void onPreExecute() {// This method is also called in UI Thread so
        // Toast works fine
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... uri) {// This methos is called by
        // OS in another worker Thread so here context base work creats ANR msg dont use it
        try {
            // Dont use context to communicate with UI thread this cause ANR msg
            // or desire canot solve
            URL url = new URL(uri[0]);
            client = (HttpsURLConnection) url.openConnection();
            if (method.equals("GET")) {
                client.setRequestMethod(method);
                client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                client.setRequestProperty("accept-language", "en-US,en;q=0.8,bn;q=0.6,zh-CN;q=0.4,zh;q=0.2");
                client.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
                client.setConnectTimeout(timeout);
                client.setReadTimeout(timeout);
                client.connect();

                responseHeaderList = client.getHeaderFields();

                inputStream = client.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                inputStream.close();
                client.disconnect();
                response = stringBuilder.toString();
                return response;
            } else if (method.equals("POST")) {
                client.setConnectTimeout(timeout);
                client.setReadTimeout(timeout);
                client.setRequestMethod("POST");
                client.setDoOutput(true);
                client.setDoInput(true);
                client.setRequestProperty("Host", "www.saveitoffline.com");
                client.setRequestProperty("Content-Length", String.valueOf(getPostDataString(post_param).length()));
                client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                client.setRequestProperty("Origin", "http://www.saveitoffline.com");
                client.setRequestProperty("Referer", "http://www.saveitoffline.com/");
                client.setRequestProperty("accept-language", "en-US,en;q=0.8,bn;q=0.6,zh-CN;q=0.4,zh;q=0.2");
                client.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
                client.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                OutputStream outputStream = client.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                bufferedWriter.write(getPostDataString(post_param));
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                if (client.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = client.getInputStream();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    inputStream.close();
                    response = stringBuilder.toString();
                    client.disconnect();
                    return response;
                }
            } else if (method.equals("HEAD")) {
                client.setRequestMethod(method);
                client.setInstanceFollowRedirects(false);
                client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                client.setRequestProperty("accept-language", "en-US,en;q=0.8,bn;q=0.6,zh-CN;q=0.4,zh;q=0.2");
                client.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
                client.setConnectTimeout(timeout);
                client.setReadTimeout(timeout);
                client.connect();

                inputStream = client.getInputStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                inputStream.close();
                client.disconnect();
                response = stringBuilder.toString();
                return response;
            }

            return response;

        } catch (Exception e) {
            return response;
        }
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    @Override
    protected void onPostExecute(String result) {// This method is also called
        // in UI Thread so Toast works fine
        super.onPostExecute(result);

    }
}