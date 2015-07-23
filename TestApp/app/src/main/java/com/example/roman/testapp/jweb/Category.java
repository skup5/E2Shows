package com.example.roman.testapp.jweb;

import android.graphics.Bitmap;

import java.net.URL;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class represent one specific category of records.
 *
 * @author Roman Zelenik
 */
public class Category extends E2Data {
  
  private static final int NO_ID = -1;
  private static final int NO_COUNT_RECORDS = -1;
  public static final Bitmap NO_IMAGE = null;
  public static final URL NO_URL = null;

  private Bitmap cover;
  private URL imageUrl;
  private URL webSite;
  private URL nextRecords;
  private final SortedSet<Record> records;
  private int totalRecordsCount;
  private int page;

  public Category(int id, String name, URL img, URL nextRecords) {
    this(id, name, NO_URL, NO_COUNT_RECORDS, img, nextRecords);
  }
  
  public Category(String name, URL webSite, int totalRecordsCount) {
    this(NO_ID, name, webSite, totalRecordsCount, NO_URL, NO_URL);
  }

  public Category(int id, String name, URL webSite, int totalRecordsCount, URL image, URL nextRecords) {
    this(id, name, webSite, totalRecordsCount, image, nextRecords, NO_IMAGE);
  }

  public Category(int id, String name, URL webSite, int totalRecordsCount, URL image, URL nextRecords, Bitmap cover) {
    super(id, name);
    this.webSite = webSite;
    this.totalRecordsCount = totalRecordsCount;
    this.imageUrl = image;
    this.nextRecords = nextRecords;
    this.cover = cover;
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

  public Bitmap getCover() {
    return cover;
  }

  public URL getImageUrl() {
    return imageUrl;
  }

  public URL getNextRecords() {
    return nextRecords;
  }

  public int getPage() {
    return page;
  }

  public SortedSet<Record> getRecords() {
    return records;
  }

  public int getRecordsCount() { return records.size(); }

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

  public boolean hasCover() { return cover != NO_IMAGE; }

  public boolean hasNextRecords() {
    return nextRecords != NO_URL;
  }

  public void setCover(Bitmap cover) {
    this.cover = cover;
  }

  public void setId(int id) {
    this.id = id;
  }
  
  public void setPage(int page) {
    this.page = page;
  }

  @Override
  public String toString() {
    return name + " (" + getRecordsCount()+ "/" + totalRecordsCount + ")";
  }

  /**
   * Update actual category with attributes from specific <code>category</code>.
   *
   * @param category
   * @return <code>true</code> if and only if category is complete,
   * <code>false</code> otherwise
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
    if(this.imageUrl == NO_URL && category.imageUrl != NO_URL){
      this.imageUrl = category.imageUrl;
    } else {
      success = false;
    }
    if(this.webSite == NO_URL && category.webSite != NO_URL){
      this.webSite = category.webSite;
    } else {
      success = false;
    }
    if(this.nextRecords == NO_URL && category.nextRecords != NO_URL){
      this.nextRecords = category.nextRecords;
    }
    if(this.cover == NO_IMAGE && category.cover != NO_IMAGE){
      this.cover = category.cover;
    }
    //return success;
    return true;
  }
  
}
