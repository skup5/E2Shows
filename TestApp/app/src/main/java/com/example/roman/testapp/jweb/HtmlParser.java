
package com.example.roman.testapp.jweb;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.util.LinkedHashSet;
import java.util.Set;

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

  /**
   * Creates set of records.
   * @param elements
   * @param category
   * @return <code>Set&lt;Record&gt;</code> (empty if none not found)
   */
  public Set<Record> parseRecords(Elements elements, Category category) {
    Record newRecord;
    Set<Record> records = new LinkedHashSet<>();
    for (Element element : elements) {
      newRecord = recParser.parse(element);
      newRecord.setCategory(category);
      records.add(newRecord);
    }
    return records;
  }

  /**
   * Creates new <code>Category</code>.
   * @param record
   * @param nextRecord
   * @param urlHost
   * @return new Category with id, name, url of cover image and url of next records
   * @throws MalformedURLException 
   * @throws NullPointerException if some parameter is <code>null</code>
   */
  public Category parseCategory(Element record, Element nextRecord, String urlHost) throws MalformedURLException {
    if(record == null){
      throw new NullPointerException("none 'Element record' to parse");
    }
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
