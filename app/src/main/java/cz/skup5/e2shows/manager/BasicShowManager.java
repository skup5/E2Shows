package cz.skup5.e2shows.manager;


import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.skup5.e2shows.dao.OnlineShowDao;
import cz.skup5.e2shows.dao.ShowDao;
import cz.skup5.e2shows.dto.ShowDto;
import cz.skup5.e2shows.exception.ShowLoadingException;
import cz.skup5.e2shows.listener.OnCompleteListener;
import cz.skup5.e2shows.listener.OnErrorListener;

/**
 * Basic implementation of {@link ShowManager} using Singleton pattern.
 * <p>
 * Created on 16.2.2017.
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

    @Override
    public void loadAllShowsAsync(OnCompleteListener<List<ShowDto>> completeListener, OnErrorListener errorListener) {
        new Loader(completeListener, errorListener).execute();
    }

    private static class Loader extends AsyncTask<Void, Void, List<ShowDto>> {
        protected final OnCompleteListener<List<ShowDto>> onCompleteListener;
        protected final OnErrorListener onErrorListener;
        protected final List<String> errors;

        private Loader(OnCompleteListener<List<ShowDto>> onCompleteListener, OnErrorListener onErrorListener) {
            this.onCompleteListener = onCompleteListener;
            this.onErrorListener = onErrorListener;
            errors = new ArrayList<>();
        }

        @Override
        protected List<ShowDto> doInBackground(Void... voids) {
            try {
                return showDao.loadAll();
            } catch (ShowLoadingException e) {
                errors.add(e.getLocalizedMessage());
            }
            return Collections.emptyList();
        }

        @Override
        protected void onPostExecute(List<ShowDto> showDtos) {
            super.onPostExecute(showDtos);
            if (onErrorListener != null && !errors.isEmpty()) {
                onErrorListener.onError(errors);
            }
            if (onCompleteListener != null) {
                onCompleteListener.onComplete(showDtos);
            }
        }
    }
}
