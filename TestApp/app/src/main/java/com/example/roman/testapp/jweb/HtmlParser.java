
package com.example.roman.testapp.jweb;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Zpracuje html kod a vytvori seznam zaznamu.
 * 
 * @author Roman Zelenik
 */
public class HtmlParser {
  RecordParser recParser;
  CategoryParser catParser;

  public HtmlParser() {
    this.recParser = new RecordParser();
    this.catParser = new CategoryParser();
  }
  
  public Set<Record> parseRecords(Elements elements, String host, Category category){
    Record newRecord;
    Set<Record> records = new LinkedHashSet<>();
    for (Element element : elements) {
      newRecord = recParser.parse(element, host);
      newRecord.setCategory(category);
      records.add(newRecord);
    }
    return records;
  }

  /**
   * 
   * @param record
   * @param nextRecord
   * @param urlHost
   * @return
   * @throws MalformedURLException 
   * @throws NullPointerException if some parameter is <code>null</code>
   */
  public Category parseCategory(Element record, Element nextRecord, String urlHost) throws MalformedURLException {
//    Set<Category> category = new LinkedHashSet<>();
//    for (Element element : elements) {
//      category.add(catParser.parse(element, urlHost));
//    }
    
    if(record == null){
      throw new NullPointerException("none 'Element record' to parse");
    }
//    if(nextRecord == null){
//      throw new NullPointerException("none 'Element nextRecord' to parse");
//    }
    if(urlHost == null){
      throw new NullPointerException("none 'String urlHost' to use");
    }
    return catParser.parse(record, nextRecord, urlHost);
  }
  
  public Set<Category> parseCategoryItems(Elements elements) throws MalformedURLException {
    Set<Category> categoryItems = new LinkedHashSet<>();
    for (Element element : elements) {
      categoryItems.add(catParser.parse(element));
}
    return categoryItems;
  }
}
