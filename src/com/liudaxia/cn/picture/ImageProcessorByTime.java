package com.liudaxia.cn.picture;

import com.liudaxia.cn.MyDownloader;
import com.liudaxia.cn.http.Http302;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * image图片爬虫
 * @author  liudaxia
 */
public class ImageProcessorByTime implements PageProcessor {


	private static AtomicInteger processNumber = new AtomicInteger(0);

	  Set<Integer> acceptCode = new HashSet<Integer>();

	// 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
	private Site site = Site.me().setUserAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36")
       .setRetryTimes(3).setSleepTime(1000);

	private static String yuming = "https://www.eee196.com";

	@Override
	public Site getSite() {
        acceptCode.add(Integer.valueOf(304));
        acceptCode.add(Integer.valueOf(200));
        site.setAcceptStatCode(acceptCode);
        site.setDomain(yuming);
        site.addCookie("__cfduid","de18be6d7cd5787b2bff1d515c5fa18f81513942248");
        site.addCookie("Hm_lvt_767e27c6fc5a7b6a90ba665ed5f7559b","1513942352,1513994364");
        site.addHeader(":authority","www.eee220.com");
        site.addHeader(":method","GET");
        site.addHeader(":path","/htm/index.htm");
        site.addHeader(":scheme","https");
        site.addHeader("accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        site.addHeader("accept-encoding","gzip, deflate, br");
	    return site;
	}

	@Override
	// process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
	public void process(Page page) {

        List list = page.getHtml().links().regex(yuming+"/htm/piclist\\d+/").all();
		page.addTargetRequests(list);

		//列表页
        if(page.getUrl().regex(yuming+"/htm/piclist\\d+/").match()){
            /*String title = page.getHtml().xpath("//div[@class='mainArea']/ul/li/a/text()").get();
            System.out.println("title===========>"+title);*/
            //获取分页
            List<String> pageLinks = page.getHtml().xpath("//div[@class='pageList']/a/@href").all();
            List<String> partLinks = new ArrayList<String>();
            for(String s:pageLinks){

                String pageNum = s.substring(s.lastIndexOf("/")+1,s.lastIndexOf("."));
                int i = 0;
                try {
                    i = Integer.parseInt(pageNum);
                    if(i<=3){
                        System.err.println("分页数据=================》"+s);
                        partLinks.add(s);
                    }
                }catch (Exception e){
                    System.err.println("页码解析失败！"+pageNum);
                }


            }
            if(partLinks!=null&&partLinks.size()>0) {
                page.addTargetRequests(partLinks);//把每页的数据加入
            }else{
                page.addTargetRequests(pageLinks);//把每页的数据加入
            }

            //获取列表url
            List<String> all = page.getHtml().links().regex(yuming+"/htm/pic\\d+/\\d+.htm").all();
            for(String s:all){

                System.out.println("url = [" + s + "]");
            }
            page.addTargetRequests(all);
        }

        //详情页
        if(page.getUrl().regex(yuming+"/htm/pic\\d+/\\d+.htm").match()){
           //https://img.581gg.com/picdata-watermark/a1/275/27510-1.jpg
            List<String> all = page.getHtml().regex("https://img.581gg.com/picdata-watermark/a1/\\d+/\\d+-\\d+\\.jpg").all();
            String parentFolder = page.getHtml().xpath("//div[@class='position']/a[@class='on']/text()").get();
            String title = page.getHtml().xpath("//title/text()").get();
            System.out.println("title=============>"+title);

            for(String pictureUrl:all){
                System.out.println("pictureUrl = [" + pictureUrl + "]");
                processNumber.incrementAndGet();
                try {

                    Calendar c = Calendar.getInstance();
                    String date = ""+c.get(Calendar.YEAR)+c.get(Calendar.MONTH)+c.get(Calendar.DAY_OF_MONTH);
                    String savePath = "G:\\im\\"+date+"\\"+parentFolder+"\\"+FileUtils.getTitleName(title);
                    System.out.println("开始下载==>"+savePath);
                    DownloadImage.download(pictureUrl,FileUtils.getFileName(pictureUrl),savePath);
                } catch (Exception e) {
                    System.out.println("下载出错============>"+pictureUrl);
                    e.printStackTrace();
                }
            }
        }






	}


	public static void main(String[] args) {
	    //先提取更新的地址
        yuming = Http302.getRedirectUrl();
		long startTime, endTime;
		System.out.println("【爬虫开始】请耐心等待一大波数据到你碗里来...");
		startTime = System.currentTimeMillis();
		// 从用户博客首页开始抓，开启5个线程，启动爬虫
		Spider.create(new ImageProcessorByTime()).setDownloader(new MyDownloader()).addUrl(yuming+"/htm/index.htm").thread(5).run();
		endTime = System.currentTimeMillis();
		System.out.println("【爬虫结束】共抓取" + processNumber + "篇文章，耗时约" + ((endTime - startTime) / 1000) + "秒");
	}
}
