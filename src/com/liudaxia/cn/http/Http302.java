package com.liudaxia.cn.http;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class Http302 {
  
    public static void main(String[] args) {
        getRedirectUrl();
    }

    public static String getRedirectUrl() {
        try {
            Properties p = new Properties();
            p.load(Http302.class.getResourceAsStream("/Address.properties"));
            String url = p.getProperty("url");
            System.out.println("访问地址:" + url);
            URL serverUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) serverUrl
                    .openConnection();
            conn.setRequestMethod("GET");
            // 必须设置false，否则会自动redirect到Location的地址
            conn.setInstanceFollowRedirects(false);

            conn.addRequestProperty("Accept-Charset", "UTF-8;");
            conn.addRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2.8) Firefox/3.6.8");
           // conn.addRequestProperty("Referer", "http://zuidaima.com/");
            conn.connect();
            String location = conn.getHeaderField("Location");

            if(location!=null&&!url.equals(location)){
                System.out.println("跳转地址:" + location);
                p.setProperty("url",location);
                OutputStream out =  new FileOutputStream(Http302.class.getClassLoader().getResource("Address.properties").getPath());
                p.store(out,"新地址");
                out.close();
                return location;
            }
            System.out.println(url+"地址没有更新");
            return url;

        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new RuntimeException("url为空");
    }

}  