package com.example.roman.e2zaznamy.show;

import com.example.roman.e2zaznamy.record.RecordItem;
import com.example.roman.e2zaznamy.record.RecordType;

import java.net.URL;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import cz.skup5.jEvropa2.data.E2Data;
import cz.skup5.jEvropa2.data.Show;

/**
 * Created by Roman on 16.6.2016.
 */
public class ShowItem {
  public static final URL EMPTY_URL = E2Data.EMPTY_URL;
  private final SortedSet<RecordItem>
          audioRecords = new TreeSet<>(),
          videoRecords = new TreeSet<>();
  private Show show;
  private int page = 1;
  private URL nextPageUrl = EMPTY_URL;

  public ShowItem(Show show) {
    this.show = show;
  }

  public void addRecordItems(Collection<RecordItem> newRecordItems) {
    for (RecordItem item : newRecordItems) {
      if (item.getType() == RecordType.Audio) {
        audioRecords.add(item);
      } else if (item.getType() == RecordType.Video) {
        videoRecords.add(item);
      }
    }
  }

  public SortedSet<RecordItem> getAudioRecords() {
    return audioRecords;
  }

  public SortedSet<RecordItem> getVideoRecords() {
    return videoRecords;
  }

  public URL getNextPageUrl() {
    return nextPageUrl;
  }

  public Show getShow() {
    return show;
  }

  public boolean hasNextPageUrl() {
    return nextPageUrl != EMPTY_URL;
  }

  public void setNextPageUrl(URL nextPageUrl) {
    this.nextPageUrl = nextPageUrl;
  }
}
