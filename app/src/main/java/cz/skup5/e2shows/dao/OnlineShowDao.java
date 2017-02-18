package cz.skup5.e2shows.dao;


import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.skup5.e2shows.R;
import cz.skup5.e2shows.dto.ShowDto;
import cz.skup5.e2shows.exception.ShowLoadingException;
import cz.skup5.e2shows.utils.ResourcesUtils;
import cz.skup5.jEvropa2.Extractor;
import cz.skup5.jEvropa2.HtmlParser;
import cz.skup5.jEvropa2.HttpRequests;
import cz.skup5.jEvropa2.data.Show;

/**
 * Implementation of {@link ShowDao} which downloading {@link Show} from Evropa2 server.
 * <p>
 * Created by Skup on 16.2.2017.
 *
 * @author Skup5
 */
public class OnlineShowDao implements ShowDao {

  private HtmlParser htmlParser;

  private String showsUrl;

  public OnlineShowDao() {
    this.htmlParser = new HtmlParser();
  }

  private String getShowsUrl() {
    if (showsUrl == null) {
      showsUrl = ResourcesUtils.getString(R.string.url_evropa2_domain)
              + ResourcesUtils.getString(R.string.url_evropa2_shows_subdomain);
    }
    return showsUrl;
  }

  @Override
  public List<ShowDto> loadAll() throws ShowLoadingException {
    List<ShowDto> showList = new ArrayList<>();
    Document site;
    String url = getShowsUrl();
    try {
      site = HttpRequests.httpGetSite(url);
      Elements shows = Extractor.getShowsList(site);
      if (!shows.isEmpty()) {
        for (Show show : htmlParser.parseShows(shows)) {
          showList.add(new ShowDto(show));
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new ShowLoadingException(ResourcesUtils.getString(R.string.error_on_show_list_loading));
    }

    return showList;
  }

}
