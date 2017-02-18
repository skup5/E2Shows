package cz.skup5.e2shows.manager;


import java.util.List;

import cz.skup5.e2shows.dao.OnlineShowDao;
import cz.skup5.e2shows.dao.ShowDao;
import cz.skup5.e2shows.dto.ShowDto;
import cz.skup5.e2shows.exception.ShowLoadingException;

/**
 * Basic implementation of {@link ShowManager} using Singleton pattern.
 * <p>
 * Created by Skup on 16.2.2017.
 *
 * @author Skup5
 */
public class BasicShowManager implements ShowManager {

  private static BasicShowManager ourInstance = new BasicShowManager();
  private static ShowDao showDao = new OnlineShowDao();

  public static BasicShowManager getInstance() {
    return ourInstance;
  }

  private BasicShowManager() {
  }

  @Override
  public List<ShowDto> loadAllShows() throws ShowLoadingException {
    return showDao.loadAll();
  }
}
