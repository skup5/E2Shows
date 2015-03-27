package com.example.roman.testapp.jweb;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Knihovni trida pro vyhledavani v html dokumentu.
 * 
 * @author jack
 */
public class Extractor {

  public static Elements getArchiveCategory(Document doc){
    return doc.select("li.archive a[href^=/mp3-archiv/kategorie]");
  }
   
  public static Elements getCategoryList(Document doc){
    return doc.select("li:not(.archive) a[href^=/mp3-archiv/kategorie/]");
  }

  public static Element getFirstRecord(Document doc){
    return getRecords(doc).first();
  }
  
  public static Elements getRecords(Document doc) {
    return doc.select("li a[onclick^=playMedia]");
  }
  
  
  private Extractor() {
  }
}
