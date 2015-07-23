package com.example.roman.testapp.jweb;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Static class with functions for searching in hmtl document.
 * 
 * @author Roman Zelenik
 */
public class Extractor {
  
  public static final String
          JS_PLAY_FUN = "playMedia",
          JS_NEXT_PAGE_FUN = "infinitePagination";

  /**
   * Finds elements containing archive categories
   *
   * @param doc
   * @return specific <code>Elements</code> (empty if not found)
   */
  public static Elements getArchiveCategory(Document doc) {
    return doc.select("li.archive a[href^=/mp3-archiv/kategorie]");
  }

  /**
   * Finds elements containing actual categories
   *
   * @param doc
   * @return specific <code>Elements</code> (empty if not found)
   */
  public static Elements getCategoryList(Document doc) {
    return doc.select("li:not(.archive) a[href^=/mp3-archiv/kategorie/]");
  }

  /**
   * Finds elements containing records
   *
   * @param doc
   * @return specific <code>Elements</code> (empty if not found)
   */
  public static Elements getRecords(Document doc) {
    return doc.select("li a[onclick^="+JS_PLAY_FUN+"]");
  }
  
  /**
   * Finds element containing url for next records
   *
   * @param doc
   * @return specific <code>Element</code> or <code>null</code> if was not found
   */
  public static Element getNextRecord(Document doc) {
    return doc.select("li a[onclick^="+JS_NEXT_PAGE_FUN+"]").first();
  }
  
  private Extractor() {}
}
