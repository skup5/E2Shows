package com.example.roman.testapp.jweb;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Predstavuje zaznam z vysilani.
 *
 * @author Roman Zelenik
 */
public class Record implements Comparable<Record>, E2Data{

  private final int id;
  private final String name;
  private Category category;
  private final URL mp3;
  private final Date date;
  public static final DateFormat dateFormat = new SimpleDateFormat("dd. M. yyyy");

  public Record(int id, String name, URL mp3, Date date) {
    this(id, name, null, mp3, date);
  }

  /**
   *
   * @param id
   * @param name
   * @param category
   * @param mp3
   * @param date
   */
  public Record(int id, String name, Category category, URL mp3, Date date) {
    this.id = id;
    this.name = name;
    this.category = category;
    this.mp3 = mp3;
    this.date = date;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  @Override
  public int compareTo(Record o) {
    int c = this.date.compareTo(o.date);
    return c == 0 ? c+1 : c;
    //return 1;
  }

  @Override
  public String toString() {
    return name;
  }

  public String info() {
    return id + "\n" + name + " (" + dateFormat.format(date) + ")\n" + category + "\n" + mp3;
  }

}
