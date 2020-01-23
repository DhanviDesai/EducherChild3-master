package com.example.educher_child;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VersionChecker extends AsyncTask<String,String,String> {
    String newVersion=null;
    @Override
    protected String doInBackground(String... strings) {
        try{
            newVersion = Jsoup.connect(strings[0])
                    .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get()
                    .select("div.xSyT2c:nth-child(1) > img")
                    .attr("src");
        }catch (Exception e){
            e.printStackTrace();
        }
        return newVersion;
    }
}
