package com.example.roman.e2zaznamy.show;

import com.example.roman.e2zaznamy.record.RecordItem;

import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;

import jEvropa2.data.E2Data;
import jEvropa2.data.Show;

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
public boolean hasNextPageUrl(){return nextPageUrl != EMPTY_URL;}
  public void setNextPageUrl(URL nextPageUrl) {
    this.nextPageUrl = nextPageUrl;
  }
}
