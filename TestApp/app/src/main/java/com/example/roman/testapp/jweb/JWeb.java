package com.example.roman.testapp.jweb;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Class of auxiliary functions for connecting to the server.
 *
 * @author Roman Zelenik
 */
public class JWeb {

  /**
   * @param args the command line arguments
   * @throws java.io.IOException
   */
  public static void mainn(String[] args) throws IOException {
    String url = "";
    String urlE2 = "http://evropa2.cz";
    String urlArchiv = "/mp3-archiv/";
    String urlNextMp3 = "/srv/www/content/pub/cs/tym-a-porady/mp3-archiv-list/?cat=100&pg=2";
    Document site;
    url = urlE2+urlArchiv;
    site = httpGetSite(url);
    
    System.out.println(url);
   
    printLinksAndCategory(site);
    
  }
  
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
  
  public static void printElements(Elements elements){
    for(Element e : elements){
        System.out.println(e);
    }
  }
  
  public static void printLinksAndCategory(Document doc){
    Elements links = doc.select("li a");
    Elements category = Extractor.getCategoryList(doc);
    Elements archive = Extractor.getArchiveCategory(doc);
    System.out.println("links: (" + links.size() + ")\n");
    System.out.println("kategorie: (" + category.size() + ")");
    printElements(category);
    System.out.println("archive: ("+ archive.size() + ")");
    printElements(archive);
  }
}
