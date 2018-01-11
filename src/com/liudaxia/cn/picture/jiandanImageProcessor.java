package com.liudaxia.cn.picture;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * image图片爬虫
 * @author  liudaxia
 */
public class jiandanImageProcessor implements PageProcessor {


	private static AtomicInteger processNumber = new AtomicInteger(0);

	// 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
	private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

	@Override
	public Site getSite() {


        site.addCookie("_ga","GA1.2.1508532935.1515670879");
        site.addCookie("_gid","GA1.2.1472433383.1515670879");
        site.addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36");
	    return site;
	}

	@Override
	// process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
	public void process(Page page) {

	    if(page.getUrl().regex("http://jandan.net/ooxx").match()) {
            String maxpageNum = page.getHtml().xpath("//*[@class=\"comments\"]/div/span/text()").get();
            int num = Integer.parseInt(maxpageNum.substring(maxpageNum.indexOf("[")+1, maxpageNum.lastIndexOf("]")));
            for (int i = num; i > 0; i--) {
                page.addTargetRequest("http://jandan.net/ooxx/page-" + i + "#comments");
            }
        }

        if(page.getUrl().regex("http://jandan.net/ooxx/page-\\d+#comments").match()){
            String url = page.getHtml().xpath("//*[@class='text']/p/img/@src").get();
            System.out.println("url="+url);
        }



        //详情页
        if(page.getUrl().regex("https://www.eee276.com/htm/pic\\d+/\\d+.htm").match()){
           //https://img.581gg.com/picdata-watermark/a1/275/27510-1.jpg
            List<String> all = page.getHtml().regex("https://img.581gg.com/picdata-watermark/a1/\\d+/\\d+-\\d+\\.jpg").all();
            String title = page.getHtml().xpath("//title/text()").get();
            System.out.println("title=============>"+title);

            for(String pictureUrl:all){
                System.out.println("pictureUrl = [" + pictureUrl + "]");
                processNumber.incrementAndGet();
                try {
                    System.out.println("开始下载。。。。。。。。。。。。。。。");
                 //   DownloadImage.download(pictureUrl,FileUtils.getFileName(pictureUrl),"F:\\liudaxia\\image\\"+FileUtils.getTitleName(title));
                } catch (Exception e) {
                    System.out.println("下载出错============>"+pictureUrl);
                    e.printStackTrace();
                }
            }
        }






	}


	public static void main(String[] args) {
		long startTime, endTime;
		System.out.println("【爬虫开始】请耐心等待一大波数据到你碗里来...");
		startTime = System.currentTimeMillis();
		// 从用户博客首页开始抓，开启5个线程，启动爬虫
		Spider.create(new jiandanImageProcessor()).addUrl("http://jandan.net/ooxx").thread(5).run();
		endTime = System.currentTimeMillis();
		System.out.println("【爬虫结束】共抓取" + processNumber + "篇文章，耗时约" + ((endTime - startTime) / 1000) + "秒");
	}
}
