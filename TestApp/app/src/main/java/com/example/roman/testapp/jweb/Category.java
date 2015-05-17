package com.example.roman.testapp.jweb;

import java.net.URL;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author jack
 */
public class Category extends E2Data {
  
  private static final int NO_ID = -1;
  private static final int NO_COUNT_RECORDS = -1;
  private static final URL NO_IMAGE = null;
  public static final URL NO_URL_SITE = null;

  private URL image;
  private URL webSite;
  private URL nextRecords;
  private final SortedSet<Record> records;
  private int totalRecordsCount;
  private int page;

  public Category(int id, String name, URL img, URL nextRecords) {
    this(id, name, NO_URL_SITE, NO_COUNT_RECORDS, img, nextRecords);
  }
  
  public Category(String name, URL webSite, int totalRecordsCount) {
    this(NO_ID, name, webSite, totalRecordsCount, NO_IMAGE, NO_URL_SITE);
  }
  
  public Category(int id, String name, URL webSite, int totalRecordsCount, URL image, URL nextRecords){
    super(id, name);
    this.webSite = webSite;
    this.totalRecordsCount = totalRecordsCount;
    this.image = image;
    this.nextRecords = nextRecords;
    this.records = new TreeSet<>();
    this.page = 1;
  }

    /**
     *
     * @param record
     * @return <code>true</code> if records is modified, <code>false</code> otherwise
     */
  public boolean addRecord(Record record){
    return this.records.add(record);
  }

    /**
     *
     * @param records
     * @return <code>true</code> if records is modified, <code>false</code> otherwise
     */
  public boolean addRecords(Collection<Record> records){
    return this.records.addAll(records);
  }
  
  public SortedSet<Record> getRecords() {
    return records;
  }

  public int getRecordsCount() { return records.size(); }

  public URL getNextRecords() {
    return nextRecords;
  }

  public int getPage() {
    return page;
  }

  /**
   *
   * @return total records count on web in this category
   */
  public int getTotalRecordsCount() {
      return totalRecordsCount;
  }

  public URL getWebSite() {
    return webSite;
  }

  public String getName() {
    return name;
  }

  public boolean hasNextRecords() {
    return nextRecords != NO_URL_SITE;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setImage(URL image) {
    this.image = image;
  }

  public void setNextRecords(URL nextRecords) {
    this.nextRecords = nextRecords;
  }
  
  public void setPage(int page) {
    this.page = page;
  }

  public String info(){
    return name + " (" + totalRecordsCount + ") \n" + webSite.toString();
  }
  
  @Override
  public String toString() {
    return name + " (" + totalRecordsCount + ")";
  }

  /**
   * 
   * @param category
   * @return true if update was successful
   */
  public boolean update(Category category) {
    if(this.name.compareTo(category.name) != 0){
      return false;
    }
    boolean success = true;
    if(this.id == NO_ID && category.id != NO_ID){
      this.id = category.id;
    } else {
      success = false;
    }
    if(this.totalRecordsCount == NO_COUNT_RECORDS && category.totalRecordsCount != NO_COUNT_RECORDS){
      this.totalRecordsCount = category.totalRecordsCount;
    } else {
      success = false;
    }
    if(this.image == NO_IMAGE && category.image != NO_IMAGE){
      this.image = category.image;
    } else {
      success = false;
    }
    if(this.webSite == NO_URL_SITE && category.webSite != NO_URL_SITE){
      this.webSite = category.webSite;
    } else {
      success = false;
    }
    if(this.nextRecords == NO_URL_SITE && category.nextRecords != NO_URL_SITE){
      this.nextRecords = category.nextRecords;
    }
    //return success;
    return true;
  }
  
}
