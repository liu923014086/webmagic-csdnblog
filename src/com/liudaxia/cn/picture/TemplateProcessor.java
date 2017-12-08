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
public class TemplateProcessor implements PageProcessor {


	private static AtomicInteger processNumber = new AtomicInteger(0);

	// 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
	private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

	@Override
	public Site getSite() {
		return site;
	}

	@Override
	// process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
	public void process(Page page) {



		//列表页
        if(page.getUrl().regex("http://www.mahua.com/tags/\\d+_\\d+.htm").match()){

            List<String> allPages = page.getHtml().xpath("//div[@class='page']/a/@href").all();
            page.addTargetRequests(allPages);
            //获取分页
            /*List<String> pageLinks = page.getHtml().xpath("//div[@class='pageList']/a/@href").all();
            page.addTargetRequests(pageLinks);//把每页的数据加入*/

            List<String> allTitleList = page.getHtml().xpath("//dl[@class='mahua']/dt/span/a/text()").all();
            List<String> allImageList = page.getHtml().xpath("//dd[@class='content']/img/@mahuagifimg").all();
            for(int i=0;i<allTitleList.size();i++){
                String title = allTitleList.get(i);
                String pictureUrl = allImageList.get(i);
                System.out.println("title="+title+","+"imgUrl="+pictureUrl);
                processNumber.incrementAndGet();
                try {
                    System.out.println("开始下载。。。。。。。。。。。。。。。");
                    DownloadImage.download(pictureUrl,title+".gif","F:\\liudaxia\\gaoxiao");
                } catch (Exception e) {
                    System.out.println("下载出错============>"+pictureUrl);
                    e.printStackTrace();
                }

            }


        }

        //详情页
        /*if(page.getUrl().regex("https://www.eee276.com/htm/pic\\d+/\\d+.htm").match()){
           //https://img.581gg.com/picdata-watermark/a1/275/27510-1.jpg
            List<String> all = page.getHtml().regex("https://img.581gg.com/picdata-watermark/a1/\\d+/\\d+-\\d+\\.jpg").all();
            String title = page.getHtml().xpath("//title/text()").get();
            System.out.println("title=============>"+title);

            for(String pictureUrl:all){
                System.out.println("pictureUrl = [" + pictureUrl + "]");
                processNumber.incrementAndGet();
                try {
                    System.out.println("开始下载。。。。。。。。。。。。。。。");
                    DownloadImage.download(pictureUrl,FileUtils.getFileName(pictureUrl),"F:\\liudaxia\\image\\"+FileUtils.getTitleName(title));
                } catch (Exception e) {
                    System.out.println("下载出错============>"+pictureUrl);
                    e.printStackTrace();
                }
            }
        }*/






	}


	public static void main(String[] args) {
		long startTime, endTime;
		System.out.println("【爬虫开始】请耐心等待一大波数据到你碗里来...");
		startTime = System.currentTimeMillis();
		// 从用户博客首页开始抓，开启5个线程，启动爬虫
		Spider.create(new TemplateProcessor()).addUrl("http://www.mahua.com/tags/6727_1.htm").thread(5).run();
		endTime = System.currentTimeMillis();
		System.out.println("【爬虫结束】共抓取" + processNumber + "篇文章，耗时约" + ((endTime - startTime) / 1000) + "秒");
	}
}
