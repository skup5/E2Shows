package cz.skup5.e2shows.dao;

import java.util.List;

import cz.skup5.e2shows.dto.ShowDto;
import cz.skup5.e2shows.exception.ShowLoadingException;

/**
 * Created by Skup on 16.2.2017.
 *
 * @author Skup5
 */
public interface ShowDao {

  /**
   * Loads and returns list of {@link ShowDto} instances.
   *
   * @return list of shows or empty list
   * @throws ShowLoadingException
   */
  List<ShowDto> loadAll() throws ShowLoadingException;
}
