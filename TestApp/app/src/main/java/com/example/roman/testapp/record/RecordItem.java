package com.example.roman.testapp.record;

import android.graphics.Bitmap;

import jEvropa2.data.Item;

/**
 * Created by Roman on 16.6.2016.
 */
public class RecordItem implements Comparable<RecordItem> {
  private Item record;
  private Bitmap cover = null;
  private Type type;

  public RecordItem(Item record, Type type) {
    this.record = record;
    this.type = type;
  }

  public Bitmap getCover() {
    return cover;
  }

  public Item getRecord() {
    return record;
  }

  public Type getType() {
    return type;
  }

  public boolean hasCover() {
    return cover != null;
  }

  public void setCover(Bitmap cover) {
    this.cover = cover;
  }

  @Override
  public int compareTo(RecordItem recordItem) {
    int timeCmp = compareTimes(recordItem);
    if (timeCmp == 0)
      return getRecord().getName().compareTo(recordItem.getRecord().getName());
    else return timeCmp;
  }

  @Override
  public String toString() {
    return getRecord().getName();
  }

  private int compareTimes(RecordItem recordItem) {
    int thisTimeHash, itemTimeHash;
    if (getRecord().getTime().compareTo(recordItem.getRecord().getTime()) == 0) return 0;
    String[] thisTime = getRecord().getTime().split(" ");
    String[] itemTime = recordItem.getRecord().getTime().split(" ");
    if (thisTime[0].contains("včera")) {
      return (itemTime[itemTime.length - 1].contains("hod")) ? 1 : -1;
    }
    if (itemTime[0].contains("včera")) {
      return (thisTime[thisTime.length - 1].contains("hod")) ? 1 : -1;
    }
    thisTimeHash = timeHash(thisTime[thisTime.length - 1]);
    itemTimeHash = timeHash(itemTime[itemTime.length - 1]);
    if(itemTimeHash == thisTimeHash){
      if(thisTime.length == 3){
        if(itemTime.length==3) return thisTime[1].compareTo(itemTime[1]);
        return 1;
      }
      return -1;
    }
    return thisTimeHash-itemTimeHash;
  }

  private static int timeHash(String time) {
    time = time.trim();
    if (time.contains("hod")) return 1;
    if (time.contains("dny")) return 2;
    if (time.contains("tyd")) return 3;
    if (time.contains("mes")) return 4;

    return 5;
  }

  public enum Type {Audio, Video}
}
