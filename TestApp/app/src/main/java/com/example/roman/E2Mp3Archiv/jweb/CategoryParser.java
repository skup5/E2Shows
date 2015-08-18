package com.example.roman.E2Mp3Archiv.jweb;
 
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Creates <code>Category</code> from html code.
 *
 * @author Roman Zelenik
 */
public class CategoryParser extends Parser {

  private static final int 
          CATEGORY_ID = 4,
          CATEGORY_NAME = 3,
          CATEGORY_IMG = 5;

  /**
   * Parse from record &lt;li&gt;
   *
   * @param record html code of record
   * @param nextRecord html code with url for next records
   * @param host
   * @return new <code>Category</code> with id, name, url of cover image and url of next records
   */
  public Category parse(Element record, Element nextRecord, String host) throws MalformedURLException{
    URL img = Category.NO_URL, nextRecords = Category.NO_URL;
    String name, imgStr;
    int id;

    String[] jsParams = parsePlayFun(record);

    id = Integer.parseInt(jsParams[CATEGORY_ID].trim());
    name = jsParams[CATEGORY_NAME].trim();
    imgStr = jsParams[CATEGORY_IMG].trim();

    if(nextRecord != null) {
      String nextRecordsStr = host + parseNextPageFun(nextRecord);
      nextRecords = new URL(nextRecordsStr);
    }

    img = new URL(imgStr);

    return new Category(id, name, img, nextRecords);
  }

  /**
   * Parse from category list in this format: <br>
   *  &lt;li&gt;&lt;a href="http://www.evropa2.cz/mp3-archiv/kategorie/hudebni-ceny-evropy-2-2014-5810"&gt;
   *  Hudební ceny Evropy 2 (2014) &lt;span&gt;(12)&lt;/span&gt;&lt;/a&gt;&lt;/li&gt;
   *
   * @param element html code of category
   * @return new <code>Category</code> with name, url of website and total number of records
   */
  public Category parse(Element element) throws IndexOutOfBoundsException, MalformedURLException {
    URL url = new URL(element.absUrl("href"));
    String name = element.text();
    String recordsStr = element.getElementsByTag("span").text();
    name = name.substring(0, name.lastIndexOf(recordsStr)).trim();
    int records = records(recordsStr.toCharArray());
    return new Category(name, url, records);
  }

  private int records(char[] recordsStr) {
    String num = "";
    for (char s : recordsStr) {
      if (s >= 48 && s <= 57) {
        num += s;
      }
    }
    return Integer.parseInt(num);
  }

}
