package com.example.roman.testapp.jweb;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Knihovni trida pro vyhledavani v html dokumentu.
 * 
 * @author Roman Zelenik
 */
public class Extractor {
  
  public static final String
          JS_PLAY_FUN = "playMedia",
          JS_NEXT_PAGE_FUN = "infinitePagination";

  /**
   *
   * @param doc
   * @return specific <code>Elements</code> (empty if not found)
   */
  public static Elements getArchiveCategory(Document doc) {
    return doc.select("li.archive a[href^=/mp3-archiv/kategorie]");
  }

  /**
   *
   * @param doc
   * @return specific <code>Elements</code> (empty if not found)
   */
  public static Elements getCategoryList(Document doc) {
    return doc.select("li:not(.archive) a[href^=/mp3-archiv/kategorie/]");
  }

  public static Element getFirstRecord(Document doc) {
    return getRecords(doc).first();
  }

  /**
   *
   * @param doc
   * @return specific <code>Elements</code> (empty if not found)
   */
  public static Elements getRecords(Document doc) {
    return doc.select("li a[onclick^="+JS_PLAY_FUN+"]");
  }
  
  /**
   * 
   * @param doc
   * @return specific <code>Element</code> or <code>null</code> if was not found
   */
  public static Element getNextRecord(Document doc) {
    return doc.select("li a[onclick^="+JS_NEXT_PAGE_FUN+"]").first();
  }
  
  private Extractor() {}
}
