package com.example.roman.testapp.jweb;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/*
 <li><a href="http://www.evropa2.cz/mp3-archiv/kategorie/hudebni-ceny-evropy-2-2014-5810">
 Hudební ceny Evropy 2 (2014) <span>(12)</span></a>
 </li>
 */
/**
 *
 * @author jack
 */
public class CategoryParser extends Parser {

  private static final int 
          CATEGORY_ID = 4,
          CATEGORY_NAME = 3,
          CATEGORY_IMG = 5;

  public Category parse(Element element, String host) {
    Element category;
    URL webSite = null, img = null;
    String name, recordsStr, imgStr;
    int id, records = 0;

    String[] jsParams = parseFun(element);
    id = Integer.parseInt(jsParams[CATEGORY_ID].trim());
    name = jsParams[CATEGORY_NAME].trim();
    
    imgStr = host + jsParams[CATEGORY_IMG].trim();
    try {
      //webSite = new URL(element.location());
      //System.out.println("imgStr:" + imgStr);
      img = new URL(imgStr);
    } catch (MalformedURLException ex) {
      Logger.getLogger(RecordParser.class.getName()).log(Level.SEVERE, null, ex);
    }
    return new Category(id, name, img);
  }

  public Category parse(Element element) throws MalformedURLException {
    URL url = new URL(element.absUrl("href"));
    String name = element.text();
    String recordsStr = element.getElementsByTag("span").text();
    name = name.substring(0, name.lastIndexOf(recordsStr)).trim();
    int records = records(recordsStr.toCharArray());
    return new Category(name, url, records);
  }

  private Element findCategory(Document document, String category) {
    Elements elements = Extractor.getCategoryList(document);
    for (Element element : elements) {
      if (element.text().contains(category)) {
        return element;
      }
    }
    return null;
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
