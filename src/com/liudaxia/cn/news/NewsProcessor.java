package com.liudaxia.cn.news;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * image图片爬虫
 *
 * @author liudaxia
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


         String productName = page.getHtml().xpath("//*[@id=\"J_DetailMeta\"]/div/div/div/div/h1/text()").get();
         String price = page.getHtml().xpath("//*[@id=\"J_StrPriceModBox\"]/dd/span/text()").get();
        System.out.println("卫生巾名称 = [" + productName + "]");
        System.out.println("价格 = [" + price + "]");

    }


    public static void main(String[] args) {
        long startTime, endTime;
        System.out.println("【爬虫开始】请耐心等待一大波数据到你碗里来...");
        startTime = System.currentTimeMillis();
        // 从用户博客首页开始抓，开启5个线程，启动爬虫
        Spider.create(new NewsProcessor()).addUrl("https://detail.tmall.com/item.htm?spm=a230r.1.14.20.67e519261rV04z&id=520169267371&ns=1&abbucket=5")
            .thread(2).run();
        endTime = System.currentTimeMillis();

        WebDriver driver = new ChromeDriver();
        driver.get("https://detail.tmall.com/item.htm?spm=a230r.1.14.20.67e519261rV04z&id=520169267371&ns=1&abbucket=5");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String price1 = driver.findElement(By.xpath("//*[@id=\"J_PromoPrice\"]/dd/div/span")).getText();
        System.out.println("args = [" + price1 + "]");
        driver.close();
        //System.out.println("【爬虫结束】共抓取" + size + "篇文章，耗时约" + ((endTime - startTime) / 1000) + "秒，已保存到数据库，请查收！");
    }
}
