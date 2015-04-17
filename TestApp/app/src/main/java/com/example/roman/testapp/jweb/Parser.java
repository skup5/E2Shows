package com.example.roman.testapp.jweb;

import org.jsoup.nodes.Element;

/**
 *
 * @author Roman Zelenik
 */
public abstract class Parser<E> {
  protected final String JS_PLAY_FUN = Extractor.JS_PLAY_FUN;
  protected final String JS_NEXT_PAGE_FUN = Extractor.JS_NEXT_PAGE_FUN;
  
  protected String[] parsePlayFun(Element element){
    String onclickAttr = element.attr("onclick");
    int mp3Index = onclickAttr.indexOf(".mp3");
    int funEndIndex = onclickAttr.indexOf(");", mp3Index);
    onclickAttr = onclickAttr.substring(onclickAttr.indexOf(JS_PLAY_FUN), funEndIndex);
    onclickAttr = onclickAttr.substring(onclickAttr.indexOf("'")+1, onclickAttr.lastIndexOf("'"));
    return onclickAttr.split("','");
  }
  
  /**
   * 
   * @param element
   * @return suburl for downloading next mp3 records
   */
  protected String parseNextPageFun(Element element) {
    String onclickAttr = element.attr("onclick");
    int begin = onclickAttr.indexOf(JS_NEXT_PAGE_FUN+"(");
    begin = onclickAttr.indexOf("'", begin);
    if(begin >= 0){
      begin++;
    }
    int end = onclickAttr.indexOf("'", begin);
    onclickAttr = onclickAttr.substring(begin, end);
    if(!onclickAttr.endsWith("/")){
      onclickAttr = onclickAttr.substring(0, onclickAttr.lastIndexOf("?"));
    }
    return onclickAttr;
  }
}
