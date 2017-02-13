package cz.skup5.e2shows.record;

import android.graphics.Bitmap;

import cz.skup5.e2shows.playlist.PlaylistItem;
import cz.skup5.jEvropa2.data.Item;

/**
 * Wrapper of {@link Item} record
 *
 * @author Skup
 */
public class RecordItem implements Comparable<RecordItem>, PlaylistItem {
  private Item record;
  private Bitmap cover = null;
  private RecordType type;

  public RecordItem(Item record, RecordType type) {
    this.record = record;
    this.type = type;
  }

  public Bitmap getCover() {
    return cover;
  }

  public Item getRecord() {
    return record;
  }

  public RecordType getType() {
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
//    System.out.println(this);
//    System.out.println("RecordItem.compareTo");
//    System.out.println(recordItem);
//    System.out.println("----------------------------");
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
      return (thisTime[thisTime.length - 1].contains("hod")) ? -1 : 1;
    }
    thisTimeHash = timeHash(thisTime[thisTime.length - 1]);
    itemTimeHash = timeHash(itemTime[itemTime.length - 1]);
    if (itemTimeHash == thisTimeHash) {
      if (thisTime.length == 3) {
        if (itemTime.length == 3) {
          int a = 0, b = 0;
          try {
            a = Integer.parseInt(thisTime[1]);
            b = Integer.parseInt(itemTime[1]);
          } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
          }
          return a - b;
        }
        return 1;
      }
      return -1;
    }
    return thisTimeHash - itemTimeHash;
  }

  private static int timeHash(String time) {
    time = time.trim();
    if (time.contains("hod")) return 1;
    if (time.contains("dny")) return 2;
    if (time.contains("týd")) return 3;
    if (time.contains("měs")) return 4;
    if (time.contains("rokem")) return 5;
    return 6;
  }

}
