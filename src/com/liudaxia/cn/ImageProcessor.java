package com.liudaxia.cn;

import csdnblog.CsdnBlog;
import csdnblog.CsdnBlogDao;
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
public class ImageProcessor implements PageProcessor {

	private static String username = "qq598535550";// 设置csdn用户名
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

		/*if(page.getHtml().links().regex("http://www\\.zbjuran\\.com/mei/\\w+/\\d+/\\d+.html").match()){
			List list = page.getHtml().xpath("/html/body/div/div/ul/li/div/div/b/a").links().all();
			page.addTargetRequests(list);
			System.out.println("page = [" + list.size() + "]");
		}*/


		List list = page.getHtml().links().regex("http://www\\.zbjuran\\.com/mei/.*").all();
		page.addTargetRequests(list);

        if(page.getUrl().regex("http://www\\.zbjuran\\.com/mei/\\w+/\\d+/\\d+_?\\d+.html").match()){

            String currentUrl = page.getUrl().get();
            System.out.println("currentUrl = [" + currentUrl + "]");
            String lastNum = splitUrlAndGetLastNumber(currentUrl);
           /* page.getHtml().xpath("//div[@class='page']").links()// 限定其他列表页获取区域
                    .regex("\\d+_\\d+.html")
                    .replace("/" + username + "/", "http://blog.csdn.net/" + username + "/")// 巧用替换给把相对url转换成绝对url
                    .all()*/
            List list1 = page.getHtml().xpath("//*[@id='"+lastNum+"']/img/@src").all();///html/body/div[2]/div[4]/div[4]/div[4]/ul/li[1]/a/img
            for(int i=0;i<list1.size();i++){
                System.out.println("page = [" + list1.get(i) + "]");
            }
        }

	}

	public static String splitUrlAndGetLastNumber(String url){
        String[] strlist = url.split("/");
       // System.out.println("url = [" + strlist[strlist.length-1] + "]");
        String lastNum = strlist[strlist.length-1].split("\\.")[0];
        System.out.println("lastNum = [" + lastNum + "]");
        return lastNum;

    }

    public static void main1(String[] args) {
        splitUrlAndGetLastNumber("http://www.zbjuran.com/mei/xinggan/201710/86528.html");
    }

	// 把list转换为string，用,分割
	public static String listToString(List<String> stringList) {
		if (stringList == null) {
			return null;
		}
		StringBuilder result = new StringBuilder();
		boolean flag = false;
		for (String string : stringList) {
			if (flag) {
				result.append(",");
			} else {
				flag = true;
			}
			result.append(string);
		}
		return result.toString();
	}

	public static void main(String[] args) {
		long startTime, endTime;
		System.out.println("【爬虫开始】请耐心等待一大波数据到你碗里来...");
		startTime = System.currentTimeMillis();
		// 从用户博客首页开始抓，开启5个线程，启动爬虫
		Spider.create(new ImageProcessor()).addUrl("http://www.zbjuran.com/mei/").thread(5).run();
		endTime = System.currentTimeMillis();
		System.out.println("【爬虫结束】共抓取" + size + "篇文章，耗时约" + ((endTime - startTime) / 1000) + "秒，已保存到数据库，请查收！");
	}
}
