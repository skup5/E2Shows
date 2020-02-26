package cz.skup5.e2shows.dto;

import java.net.URL;
import java.util.Collection;
import java.util.NavigableSet;
import java.util.TreeSet;

import cz.skup5.e2shows.record.RecordItem;
import cz.skup5.e2shows.record.RecordType;
import cz.skup5.jEvropa2.data.Show;

/**
 * Created by Roman on 16.6.2016.
 */
public class ShowDto {

  private static final URL EMPTY_URL = null;
  private final NavigableSet<RecordItem> recordItems = new TreeSet<>();
  //          audioRecords = new TreeSet<>(),
//          videoRecords = new TreeSet<>();
  private Show show;
  //  private int page = 1;
  private URL nextPageUrl = EMPTY_URL;

  public ShowDto(Show show) {
    if(show == null) throw new NullPointerException();
    this.show = show;
  }

  public void addRecordItems(Collection<RecordItem> newRecordItems) {
//    for (RecordItem item : newRecordItems) {
//      recordItems.add(item);
//    }
//    for (RecordItem r : newRecordItems)
//      System.out.println(r);
//    System.out.println("##################");

    recordItems.addAll(newRecordItems);
  }

  /**
   * Returns set of {@link RecordItem}s with the requested {@link RecordType}.
   *
   * @param type the requested {@link RecordType}. If is {@link RecordType#All}, returns all record items
   * @return set of record items with the given {@link RecordType} or empty set
   */
  public NavigableSet<RecordItem> getRecordItemsWithType(RecordType type) {
    NavigableSet<RecordItem> filteredSet = new TreeSet<>();
    if (type == RecordType.All) {
      filteredSet.addAll(recordItems);
    } else {
      for (RecordItem item : recordItems) {
        if (item.getType() == type) {
          filteredSet.add(item);
        }
      }
    }
    return filteredSet;
  }

//  public SortedSet<RecordItem> getAudioRecords() {
//    return audioRecords;
//  }

//  public SortedSet<RecordItem> getVideoRecords() {
//    return videoRecords;
//  }

  public String getInfo() {
    return show.info();
  }

  public String getName() {
    return show.getName();
  }

  public URL getNextPageUrl() {
    return nextPageUrl;
  }

  //  public Show getShow() {
//    return show;
//  }
  public URL getWebSiteUrl() {
    return show.getWebSiteUrl();
  }

  public boolean hasNextPageUrl() {
    return nextPageUrl != EMPTY_URL;
  }

  public boolean hasWebSiteUrl() {
    return show.hasWebSiteUrl();
  }

  /**
   * @return true if has none record items
   */
  public boolean isEmpty() {
    return recordItems.isEmpty();
  }

  public void setNextPageUrl(URL nextPageUrl) {
    this.nextPageUrl = nextPageUrl;
  }

  @Override
  public String toString() {
    return getName();
  }
}
