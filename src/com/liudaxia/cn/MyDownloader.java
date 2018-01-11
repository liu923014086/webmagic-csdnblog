package com.liudaxia.cn;

import com.google.common.collect.Sets;
import com.liudaxia.cn.picture.MyHttpClientGenerator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.downloader.HttpClientGenerator;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.UrlUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MyDownloader extends AbstractDownloader {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, CloseableHttpClient> httpClients = new HashMap();
    private MyHttpClientGenerator httpClientGenerator = new MyHttpClientGenerator();

    public MyDownloader() {
    }

    private CloseableHttpClient getHttpClient(Site site) {






        if (site == null) {
            return this.httpClientGenerator.getClient((Site)null);
        } else {
            String domain = site.getDomain();
            CloseableHttpClient httpClient = (CloseableHttpClient)this.httpClients.get(domain);
            if (httpClient == null) {
                synchronized(this) {
                    httpClient = (CloseableHttpClient)this.httpClients.get(domain);
                    if (httpClient == null) {
                        httpClient = this.httpClientGenerator.getClient(site);
                        this.httpClients.put(domain, httpClient);
                    }
                }
            }

            return httpClient;
        }
    }

    public Page download(Request request, Task task) {
        Site site = null;
        if (task != null) {
            site = task.getSite();
        }

        String charset = null;
        Map<String, String> headers = null;
        Object acceptStatCode;
        if (site != null) {
            acceptStatCode = site.getAcceptStatCode();
            charset = site.getCharset();
            headers = site.getHeaders();
        } else {
            acceptStatCode = Sets.newHashSet(new Integer[]{Integer.valueOf(200)});
        }

        this.logger.info("downloading page {}", request.getUrl());
        CloseableHttpResponse httpResponse = null;
        int statusCode = 0;

        Page page;
        try {

            HttpUriRequest httpUriRequest = this.getHttpUriRequest(request, site, headers);
            httpResponse = this.getHttpClient(site).execute(httpUriRequest);
            statusCode = httpResponse.getStatusLine().getStatusCode();
            request.putExtra("statusCode", statusCode);
            if (this.statusAccept((Set)acceptStatCode, statusCode)) {
                page = this.handleResponse(request, charset, httpResponse, task);
                this.onSuccess(request);
                Page var11 = page;
                return var11;
            }

            this.logger.warn("code error " + statusCode + "\t" + request.getUrl());
            page = null;
            return page;
        } catch (IOException var23) {
            this.logger.warn("download page " + request.getUrl() + " error", var23);
            if (site.getCycleRetryTimes() > 0) {
                page = this.addToCycleRetry(request, site);
                return page;
            }

            this.onError(request);
            page = null;
        } finally {
            request.putExtra("statusCode", statusCode);

            try {
                if (httpResponse != null) {
                    EntityUtils.consume(httpResponse.getEntity());
                }
            } catch (IOException var22) {
                this.logger.warn("close response fail", var22);
            }

        }

        return page;
    }

    public void setThread(int thread) {
        this.httpClientGenerator.setPoolSize(thread);
    }

    protected boolean statusAccept(Set<Integer> acceptStatCode, int statusCode) {
        return acceptStatCode.contains(statusCode);
    }

    protected HttpUriRequest getHttpUriRequest(Request request, Site site, Map<String, String> headers) {
        RequestBuilder requestBuilder = this.selectRequestMethod(request).setUri(request.getUrl());
        if (headers != null) {
            Iterator i$ = headers.entrySet().iterator();

            while(i$.hasNext()) {
                Map.Entry<String, String> headerEntry = (Map.Entry)i$.next();
                requestBuilder.addHeader((String)headerEntry.getKey(), (String)headerEntry.getValue());
            }
        }

        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom().setConnectionRequestTimeout(site.getTimeOut()).setSocketTimeout(site.getTimeOut()).setConnectTimeout(site.getTimeOut()).setCookieSpec("best-match");
        if (site.getHttpProxyPool().isEnable()) {
            HttpHost host = site.getHttpProxyFromPool();
            requestConfigBuilder.setProxy(host);
            request.putExtra("proxy", host);
        }

        requestBuilder.setConfig(requestConfigBuilder.build());
        return requestBuilder.build();
    }

    protected RequestBuilder selectRequestMethod(Request request) {
        String method = request.getMethod();
        if (method != null && !method.equalsIgnoreCase("GET")) {
            if (method.equalsIgnoreCase("POST")) {
                RequestBuilder requestBuilder = RequestBuilder.post();
                NameValuePair[] nameValuePair = (NameValuePair[])((NameValuePair[])request.getExtra("nameValuePair"));
                if (nameValuePair.length > 0) {
                    requestBuilder.addParameters(nameValuePair);
                }

                return requestBuilder;
            } else if (method.equalsIgnoreCase("HEAD")) {
                return RequestBuilder.head();
            } else if (method.equalsIgnoreCase("PUT")) {
                return RequestBuilder.put();
            } else if (method.equalsIgnoreCase("DELETE")) {
                return RequestBuilder.delete();
            } else if (method.equalsIgnoreCase("TRACE")) {
                return RequestBuilder.trace();
            } else {
                throw new IllegalArgumentException("Illegal HTTP Method " + method);
            }
        } else {
            return RequestBuilder.get();
        }
    }

    protected Page handleResponse(Request request, String charset, HttpResponse httpResponse, Task task) throws IOException {
        String content = this.getContent(charset, httpResponse);
        Page page = new Page();
        page.setRawText(content);
        page.setUrl(new PlainText(request.getUrl()));
        page.setRequest(request);
        page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
        return page;
    }

    protected String getContent(String charset, HttpResponse httpResponse) throws IOException {
        if (charset == null) {
            byte[] contentBytes = IOUtils.toByteArray(httpResponse.getEntity().getContent());
            String htmlCharset = this.getHtmlCharset(httpResponse, contentBytes);
            if (htmlCharset != null) {
                return new String(contentBytes, htmlCharset);
            } else {
                this.logger.warn("Charset autodetect failed, use {} as charset. Please specify charset in Site.setCharset()", Charset.defaultCharset());
                return new String(contentBytes);
            }
        } else {
            return IOUtils.toString(httpResponse.getEntity().getContent(), charset);
        }
    }

    protected String getHtmlCharset(HttpResponse httpResponse, byte[] contentBytes) throws IOException {
        String value = httpResponse.getEntity().getContentType().getValue();
        String charset = UrlUtils.getCharset(value);
        if (StringUtils.isNotBlank(charset)) {
            this.logger.debug("Auto get charset: {}", charset);
            return charset;
        } else {
            Charset defaultCharset = Charset.defaultCharset();
            String content = new String(contentBytes, defaultCharset.name());
            if (StringUtils.isNotEmpty(content)) {
                Document document = Jsoup.parse(content);
                Elements links = document.select("meta");
                Iterator i$ = links.iterator();

                while(i$.hasNext()) {
                    Element link = (Element)i$.next();
                    String metaContent = link.attr("content");
                    String metaCharset = link.attr("charset");
                    if (metaContent.indexOf("charset") != -1) {
                        metaContent = metaContent.substring(metaContent.indexOf("charset"), metaContent.length());
                        charset = metaContent.split("=")[1];
                        break;
                    }

                    if (StringUtils.isNotEmpty(metaCharset)) {
                        charset = metaCharset;
                        break;
                    }
                }
            }

            this.logger.debug("Auto get charset: {}", charset);
            return charset;
        }
    }
}
