package com.example.roman.testapp.jweb;
 
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;


/**
 * Creates <code>Record</code> from html code.
 * 
 * @author Roman Zelenik
 */
public class RecordParser extends Parser{

  private static final int
          RECORD_ID = 0,
          RECORD_NAME = 2,
          RECORD_MP3 = 6,
          RECORD_SITE = 7,
          DATE = 8;

  /**
   * Parse from record &lt;li&gt;
   *
   * @param element
   * @return new <code>Record</code> with id, name, mp3 and date
   * @throws IndexOutOfBoundsException
   * @throws MalformedURLException
   * @throws java.text.ParseException
   */
  public Record parse(Element element) throws IndexOutOfBoundsException, MalformedURLException, java.text.ParseException {
    Date date;
    URL mp3;
    String dateStr,
           name,
           mp3Str;
    int id;

    String[] jsParams = parsePlayFun(element);
    name = jsParams[RECORD_NAME].trim();
    id = Integer.parseInt(jsParams[RECORD_ID].trim());
    mp3Str = jsParams[RECORD_MP3].trim();
    mp3 = new URL(mp3Str);
    dateStr = jsParams[DATE].trim();
    date = Record.dateFormat.parse(dateStr);
    return new Record(id, name, mp3, date);
  }

}
