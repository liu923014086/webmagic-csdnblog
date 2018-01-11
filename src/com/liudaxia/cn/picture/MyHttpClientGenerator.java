package com.liudaxia.cn.picture;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.CookieStore;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HttpContext;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.downloader.HttpClientGenerator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class MyHttpClientGenerator {
    private PoolingHttpClientConnectionManager connectionManager;

    public MyHttpClientGenerator() {
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE).register("https",  new SSLConnectionSocketFactory(SSLContexts.createDefault(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)).build();
        this.connectionManager = new PoolingHttpClientConnectionManager(reg);
        this.connectionManager.setDefaultMaxPerRoute(100);
    }

    public MyHttpClientGenerator setPoolSize(int poolSize) {
        this.connectionManager.setMaxTotal(poolSize);
        return this;
    }

    public CloseableHttpClient getClient(Site site) {
        return this.generateClient(site);
    }

    private CloseableHttpClient generateClient(Site site) {
        HttpClientBuilder httpClientBuilder = HttpClients.custom().setConnectionManager(this.connectionManager);
        if (site != null && site.getUserAgent() != null) {
            httpClientBuilder.setUserAgent(site.getUserAgent());
        } else {
            httpClientBuilder.setUserAgent("");
        }

        if (site == null || site.isUseGzip()) {
            httpClientBuilder.addInterceptorFirst(new HttpRequestInterceptor() {
                public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
                    if (!request.containsHeader("Accept-Encoding")) {
                        request.addHeader("Accept-Encoding", "gzip");
                    }

                }
            });
        }

        SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setTcpNoDelay(true).build();
        httpClientBuilder.setDefaultSocketConfig(socketConfig);
        if (site != null) {
            httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(site.getRetryTimes(), true));
        }

        this.generateCookie(httpClientBuilder, site);
        return httpClientBuilder.build();
    }

    private void generateCookie(HttpClientBuilder httpClientBuilder, Site site) {
        CookieStore cookieStore = new BasicCookieStore();
        Iterator i$ = site.getCookies().entrySet().iterator();

        Map.Entry domainEntry;
        while(i$.hasNext()) {
            domainEntry = (Map.Entry)i$.next();
            BasicClientCookie cookie = new BasicClientCookie((String)domainEntry.getKey(), (String)domainEntry.getValue());
            cookie.setDomain(site.getDomain());
            cookieStore.addCookie(cookie);
        }

        i$ = site.getAllCookies().entrySet().iterator();

        while(i$.hasNext()) {
            domainEntry = (Map.Entry)i$.next();
            Iterator i$$ = ((Map)domainEntry.getValue()).entrySet().iterator();

            while(i$$.hasNext()) {
                Map.Entry<String, String> cookieEntry = (Map.Entry)i$$.next();
                BasicClientCookie cookie = new BasicClientCookie((String)cookieEntry.getKey(), (String)cookieEntry.getValue());
                cookie.setDomain((String)domainEntry.getKey());
                cookieStore.addCookie(cookie);
            }
        }

        httpClientBuilder.setDefaultCookieStore(cookieStore);
    }
}
