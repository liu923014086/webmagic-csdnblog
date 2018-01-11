package com.liudaxia.cn.http;

import org.apache.http.client.HttpClient;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;  
//对接口进行测试  
public class TestMain {  
    private String url = "https://192.168.1.101/";  
    private String charset = "utf-8";  
    private HttpClientUtil httpClientUtil = null;  
      
    public TestMain(){  
        httpClientUtil = new HttpClientUtil();  
    }  
      
    public void test(){  

        String httpOrgCreateTestRtn = httpClientUtil.doPost("https://www.eee220.com/htm/pic1/187484.htm",null,charset);
        System.out.println("result:"+httpOrgCreateTestRtn);  
    }




    public static void main(String[] args){  
        TestMain main = new TestMain();  
        main.test();  
    }  
}  