package com.example.roman.testapp.jweb;

import java.util.Collection;
import java.util.List;
import org.jsoup.nodes.Element;

/**
 *
 * @author Roman Zelenik
 */
public abstract class Parser<E> {
  protected final String JSFUN = "playMedia";
  
  protected String[] parseFun(Element element){
    String onclickAttr = element.attr("onclick");
    int mp3Index = onclickAttr.indexOf(".mp3");
    int funEndIndex = onclickAttr.indexOf(");", mp3Index);
    onclickAttr = onclickAttr.substring(onclickAttr.indexOf(JSFUN), funEndIndex);
    onclickAttr = onclickAttr.substring(onclickAttr.indexOf("'")+1, onclickAttr.lastIndexOf("'"));
    return onclickAttr.split("','");
  }
}
