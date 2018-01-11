package com.liudaxia.cn.picture;

import java.util.Calendar;
import java.util.TimeZone;

public class FileUtils {
    public static String getFileName(String path){
        if(path.contains("/")){
            return path.substring(path.lastIndexOf("/")+1);
        }
        return path;
    }

    public static String getTitleName(String titleName){
        if(titleName.contains("[")){
            return titleName.substring(0,titleName.indexOf("["));
        }else if(!titleName.contains("[")){
            titleName= titleName.substring(0,4);
        }
        return titleName;
    }

    public static void main(String[] args) {
        String path = "https://img.581gg.com/picdata-watermark/a1/766/76659-1.jpg";
        System.out.println(getFileName(path));
        String titleName = "68[]";
        System.out.println(getTitleName(titleName));
String pictureUrl = "https://img.581gg.com/picdata-watermark/a1/349/34920-6.jpg";
        System.out.println("FileUtils.getTitleName(pictureUrl)="+FileUtils.getTitleName(pictureUrl));
        System.out.println("FileUtils.getFileName(pictureUrl)="+FileUtils.getFileName(pictureUrl));

        titleName = "石原佑里子い";
        System.out.println(getTitleName(titleName));

        /*Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
        System.out.println(c.getTime());*/
        /*try {
            DownloadImage.download(pictureUrl,FileUtils.getFileName(pictureUrl),"F:\\image\\"+FileUtils.getTitleName(pictureUrl));
        } catch (Exception e) {
            System.out.println("下载失败");
            e.printStackTrace();
        }*/
    }
}
