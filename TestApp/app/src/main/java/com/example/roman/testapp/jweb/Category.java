package com.example.roman.testapp.jweb;

import java.net.URL;
import java.util.Collection;
import java.util.Set;
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
  private static final URL NO_WEB_SITE = null;

  private URL image;
  private int countRecords;
  private URL webSite;
  private final SortedSet<Record> records;

  public Category(int id, String name, URL img) {
    this(id, name, NO_WEB_SITE, NO_COUNT_RECORDS, img);
  }
  
  public Category(String name, URL webSite, int countRecords) {
    this(NO_ID, name, webSite, countRecords, NO_IMAGE);
  }
  
  public Category(int id, String name, URL webSite, int countRecords, URL image){
    super(id, name);
    this.webSite = webSite;
    this.countRecords = countRecords;
    this.image = image;
    this.records = new TreeSet<>();
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
  
  public int getCountRecords() {
    return countRecords;
  }

  public URL getWebSite() {
    return webSite;
  }

  public String getName() {
    return name;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setImage(URL image) {
    this.image = image;
  }

  public String info(){
    return name + " (" + countRecords + ") \n" + webSite.toString();
  }
  
  @Override
  public String toString() {
    return name + " (" + countRecords + ")";
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
    if(this.countRecords == NO_COUNT_RECORDS && category.countRecords != NO_COUNT_RECORDS){
      this.countRecords = category.countRecords;
    } else {
      success = false;
    }
    if(this.image == NO_IMAGE && category.image != NO_IMAGE){
      this.image = category.image;
    } else {
      success = false;
    }
    if(this.webSite == NO_WEB_SITE && category.webSite != NO_WEB_SITE){
      this.webSite = category.webSite;
    } else {
      success = false;
    }
    //return success;
    return true;
  }
  
  
}
