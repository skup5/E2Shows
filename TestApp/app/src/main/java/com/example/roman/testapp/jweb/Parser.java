package com.example.roman.testapp.jweb;
 
import org.jsoup.nodes.Element;

/**
 * Abstract class containing help parsing functions.
 *
 * @author Roman Zelenik
 */
public abstract class Parser {
  protected final String
          JS_PLAY_FUN = Extractor.JS_PLAY_FUN,
          JS_NEXT_PAGE_FUN = Extractor.JS_NEXT_PAGE_FUN;

  /**
   * Parse form records list in this format: <br>
   *
   * &lt;li class="clr" id="podcast-item-18077"&gt; <br>
   * &lt;a onclick="playMedia(&#39;18077&#39;,
   *         &#39;true&#39;,
   *         &#39;Páteèní houseparty&#39;,
   *         &#39;To nejlepší z Ranní show&#39;,
   *         &#39;100&#39;,
   *         &#39;/img/edee/tym-a-porady/mp3-archiv/leos_patrik-1.jpg&#39;,
   *         &#39;/file/edee/tym-a-porady/mp3-archiv/18077/zvuky-patecni-houseparty.mp3&#39;,
   *         &#39;/mp3-archiv/patecni-houseparty-18077&#39;,
   *         &#39;27. 2. 2015&#39;);
   * return false;"
   * href="http://www.evropa2.cz/mp3-archiv/patecni-houseparty-18077"&gt;
   * Páteèní houseparty&lt;br&gt;To nejlepší z Ranní show (27. 2. 2015)
   * &lt;/a&gt; <br>
   * &lt;p title="74" class="rating" id="rating-18077"&gt; <br>
   * &lt;img width="74%" height="15" alt="" src="./Páteèní houseparty · MP3 archiv To nejlepší z Ranní show · Evropa 2 · MaXXimum muziky_files/spacer.gif"&gt; <br>
   * &lt;/p&gt; <br>
   * &lt;/li&gt;
   *
   * @param element html code
   * @return array of arguments from javascript header of function
   */
  protected String[] parsePlayFun(Element element) throws IndexOutOfBoundsException {
//    Log.println(Log.ASSERT, "parsePlayFun", element.toString());
    String onclickAttr = element.attr("onclick");
    int mp3Index = onclickAttr.indexOf(".mp3");
    int funEndIndex = onclickAttr.indexOf(");", mp3Index);

      onclickAttr = onclickAttr.substring(onclickAttr.indexOf(JS_PLAY_FUN), funEndIndex);
      onclickAttr = onclickAttr.substring(onclickAttr.indexOf("'") + 1, onclickAttr.lastIndexOf("'"));

    return onclickAttr.split("','");
  }
  
  /**
   * 
   * @param element html code
   * @return suburl for downloading next mp3 records
   */
  protected String parseNextPageFun(Element element) throws IndexOutOfBoundsException {
    String onclickAttr = element.attr("onclick");
    int begin = onclickAttr.indexOf(JS_NEXT_PAGE_FUN+"(");
    begin = onclickAttr.indexOf("'", begin);
    if(begin >= 0){
      begin++;
    }
    int end = onclickAttr.indexOf("'", begin);
    onclickAttr = onclickAttr.substring(begin, end);
    if (!onclickAttr.endsWith("/")) {
      onclickAttr = onclickAttr.substring(0, onclickAttr.lastIndexOf("?"));
    }
    return onclickAttr;
  }
}
