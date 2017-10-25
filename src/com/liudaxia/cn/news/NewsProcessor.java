package com.liudaxia.cn.news;

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
public class NewsProcessor implements PageProcessor {

	private static int size = 0;// 共抓取到的文章数量

	private AtomicInteger processNumber = new AtomicInteger(0);

	// 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
	private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

	@Override
	public Site getSite() {
		return site;
	}

	@Override
	// process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
	public void process(Page page) {

	    if(page.getUrl().regex("https://github\\.com/liu923014086\\?*").match()){
	        page.addTargetRequests(page.getUrl().regex("https://github\\.com/liu923014086/*").all());
	        List list = page.getHtml().xpath("//*[@id=\"user-repositories-list\"]/div/div/a").links().all();
	        page.addTargetRequests(list);

            List projectName = page.getHtml().xpath("//*[@id=\"user-repositories-list\"]/ul/li/div/h3/a/text()").all();
            for(int i=0;i<projectName.size();i++){
                System.out.println("projectName = [" + projectName.get(i) + "]");
            }


        }




	}



	public static void main(String[] args) {
		long startTime, endTime;
		System.out.println("【爬虫开始】请耐心等待一大波数据到你碗里来...");
		startTime = System.currentTimeMillis();
		// 从用户博客首页开始抓，开启5个线程，启动爬虫
		Spider.create(new NewsProcessor()).addUrl("https://github.com/liu923014086?tab=repositories").thread(5).run();
		endTime = System.currentTimeMillis();
		System.out.println("【爬虫结束】共抓取" + size + "篇文章，耗时约" + ((endTime - startTime) / 1000) + "秒，已保存到数据库，请查收！");
	}
}
