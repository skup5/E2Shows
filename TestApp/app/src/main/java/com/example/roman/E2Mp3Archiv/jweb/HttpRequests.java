package com.example.roman.E2Mp3Archiv.jweb;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Static class for sending http request to the server.
 *
 * @author Roman Zelenik
 */
public class HttpRequests {

  public static Document httpGetSite(String url) throws IOException{
    return Jsoup.connect(url).get();
  }
  
  public static Document httpPostNextRecords(String url, String cat, String pg) throws IOException{
    String rate, tag;
    rate = tag = "";
    if(!url.endsWith("/")){
      url += "/";
    }
    url += "?";
    if(pg == null){
      pg = "1";
    }
    url += "pg="+pg;
    if(cat == null){
      cat = "";
    } else {
      url += "&cat="+cat;
    }
    return Jsoup.connect(url).data("rate", rate, "cat", cat, "tag", tag).post();
  }

  private HttpRequests() {}
}
