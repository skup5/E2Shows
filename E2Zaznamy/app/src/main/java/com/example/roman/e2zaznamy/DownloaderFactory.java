package com.example.roman.e2zaznamy;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.example.roman.e2zaznamy.record.RecordItem;
import com.example.roman.e2zaznamy.record.RecordType;
import com.example.roman.e2zaznamy.show.ShowItem;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jEvropa2.Extractor;
import jEvropa2.HtmlParser;
import jEvropa2.HttpRequests;
import jEvropa2.data.Item;
import jEvropa2.data.Show;

/**
 * This factory class creates AsyncTasks for downloading data (Downloaders).
 *
 * @author Roman Zelenik
 */
public class DownloaderFactory {

  private static final String DOWNLOADING = "Stahuji...";

  public enum Type {Records, NextRecords, CoverImage, Shows, MediaUrl}

  private DownloaderFactory() {
  }

  public static AbstractDownloader getDownloader(Type type) {
    return getDownloader(type, null, null);
  }

  public static AbstractDownloader getDownloader(Type type, Context context, String dialogTitle) {
    switch (type) {
      case CoverImage:
        return new CoverImageDownloader(context, dialogTitle);
      case MediaUrl:
        return new MediaUrlDownloader(context, dialogTitle);
      case NextRecords:
        return new NextRecordsDownloader(context, dialogTitle);
      case Records:
        return new RecordsDownloader(context, dialogTitle);
      case Shows:
        return new ShowsDownloader(context, dialogTitle);
      default:
        return null;
    }
  }

  abstract static class AbstractDownloader<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected ProgressDialog mProgressDialog;
    protected Context context;
    protected HtmlParser htmlParser;
    protected boolean useProgressDialog = false;
    protected OnCompleteListener<Result> onCompleteListener;
    protected OnErrorListener onErrorListener;
    protected List<String> errors;

    public AbstractDownloader(Context context, String dialogTitle) {
      this.context = context;
      this.htmlParser = new HtmlParser();
      this.errors = new ArrayList();
      this.onCompleteListener = null;
      this.onErrorListener = null;
      if (context != null) {
        useProgressDialog = true;
        this.mProgressDialog = new ProgressDialog(context);
        this.mProgressDialog.setTitle(dialogTitle);
      }
    }

    public void setOnCompleteListener(OnCompleteListener<Result> onCompleteListener) {
      this.onCompleteListener = onCompleteListener;
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
      this.onErrorListener = onErrorListener;
    }

    protected abstract Result download(Params... params);

    @Override
    protected Result doInBackground(Params... params) {
      if (params != null && params.length > 0) {
        return download(params);
      }
      return null;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      if (useProgressDialog) {
        mProgressDialog.setMessage(DOWNLOADING);
        mProgressDialog.show();
      }
    }

    @Override
    protected void onPostExecute(Result result) {
      super.onPostExecute(result);
      if (onErrorListener != null && !errors.isEmpty()) {
        onErrorListener.onError(errors);
      }
      if (onCompleteListener != null) {
        onCompleteListener.onComplete(result);
      }

      if (useProgressDialog && mProgressDialog.isShowing()) {
        mProgressDialog.cancel();
      }
    }
  }

  static class RecordsDownloader extends AbstractDownloader<URL, Void, Map<String, Object>> {

    public RecordsDownloader(Context context, String dialogTitle) {
      super(context, dialogTitle);
    }

    @Override
    protected Map<String, Object> download(URL... urls) {
      Set<RecordItem> records = new HashSet<>();
      Set<Item> itemSet;
      Map<String, Object> result = new HashMap<>(2);
      Item activeItem = null;
      URL nextPageUrl = null;
      URL site = urls[0];
      try {
        Document doc = HttpRequests.httpGetSite(site.toString());
        Element active = Extractor.getActiveItem(doc);
        activeItem = htmlParser.parseActiveAudioShowItem(active);
        if (activeItem != null) records.add(new RecordItem(activeItem, RecordType.Audio));
        Elements recordsElements = Extractor.getAudioItems(doc);
        if (!recordsElements.isEmpty()) {
          itemSet = htmlParser.parseAudioShowItems(recordsElements);
          for (Item i : itemSet) {
            records.add(new RecordItem(i, RecordType.Audio));
          }
        }

        activeItem = null;
        activeItem = htmlParser.parseActiveVideoShowItem(active);
        if (activeItem != null) records.add(new RecordItem(activeItem, RecordType.Video));
        recordsElements = Extractor.getVideoItems(doc);
        if (!recordsElements.isEmpty()) {
          itemSet = htmlParser.parseVideoShowItems(recordsElements);
          for (Item i : itemSet) {
            records.add(new RecordItem(i, RecordType.Video));
          }
        }

        Element next = Extractor.getNextShowItems(doc);
        if (next != null) nextPageUrl = htmlParser.parseNextPageUrl(next);
      } catch (IOException e) {
        e.printStackTrace();
        errors.add("Chyba při stahování seznamu záznamů.");
      }

      result.put("records", records);
      result.put("nextPage", nextPageUrl);
      return result;
    }
  }

  static class NextRecordsDownloader extends AbstractDownloader<ShowItem, Void, Set<RecordItem>> {

    public NextRecordsDownloader(Context context, String dialogTitle) {
      super(context, dialogTitle);
    }

    @Override
    protected Set<RecordItem> download(ShowItem... shows) {
     /* ShowItem show = shows[0];
      URL site = show.getShow().getWebSiteUrl();
      site.
      int page = category.getPage();
      if (site != null && category.getRecordsCount() < category.getTotalRecordsCount()) {
        try {
          Set<Record> set;
          page++;
          Document doc = HttpRequests.httpPostNextRecords(site.toString(), category.getId() + "", page + "");
          Elements records = Extractor.getRecords(doc);
          if (!records.isEmpty()) {
            set = this.htmlParser.parseRecords(records, category);
            category.addRecords(set);
          }
        } catch (IOException e) {
          e.printStackTrace();
          page--;
          errors.add("Chyba při stahování dalších záznamů.");
        }
      }
      category.setPage(page);
      return category;*/
      return new HashSet<>();
    }
  }

  static class CoverImageDownloader extends AbstractDownloader<URL, Void, Bitmap> {

    public CoverImageDownloader(Context context, String dialogTitle) {
      super(context, dialogTitle);
    }

    @Override
    protected Bitmap download(URL... urls) {
      Bitmap bitmap = null;
      URL site = urls[0];
      if (site != null) {
        try {
          InputStream stream = null;
          stream = site.openStream();
          bitmap = BitmapFactory.decodeStream(stream);
          stream.close();
        } catch (IOException e) {
          e.printStackTrace();
          errors.add("Chyba při stahování obrázku kategorie.");
        }
      }
      return bitmap;
    }
  }

  static class ShowsDownloader extends AbstractDownloader<URL, Void, Set<Show>> {

    public ShowsDownloader(Context context, String dialogTitle) {
      super(context, dialogTitle);
    }

    @Override
    protected Set<Show> download(URL... urls) {
      Set<Show> showsSet = new HashSet<>();
      Document site;
      try {
        site = HttpRequests.httpGetSite(urls[0].toString());
        Elements shows = Extractor.getShowsList(site);
        if (!shows.isEmpty()) {
          showsSet = htmlParser.parseShows(shows);
        }
      } catch (IOException e) {
        e.printStackTrace();
        errors.add("Chyba při stahování Shows seznamu.");
      }
      return showsSet;
    }
  }

  static class MediaUrlDownloader extends AbstractDownloader<MediaUrlDownloader.Params, Void, URL> {
    public MediaUrlDownloader(Context context, String dialogTitle) {
      super(context, dialogTitle);
    }

    @Override
    protected URL download(Params... params) {
      URL url = null;
      Document doc;
      Params param;
      try {
        param = params[0];
        doc = HttpRequests.httpGetSite(param.url.toString());
        Element element = Extractor.getPlayerScript(doc);
        if (element != null) {
          switch (param.type) {
            case Params.TYPE_AUDIO:
              url = htmlParser.parseMp3Url(element);
              break;
            case Params.TYPE_VIDEO:
              url = htmlParser.parseMp4Url(element);
              break;
            default:
              break;
          }
        } else {
          errors.add("Script s obsahující url adresu nenalezen");
        }
      } catch (Exception e) {
        e.printStackTrace();
        errors.add("Chyba při extrakci audio nebo video url adresy.");
      }
      return url;
    }

    public static class Params {
      public static final int TYPE_AUDIO = 1, TYPE_VIDEO = 2;
      public int type;
      public URL url;

      public Params(int type, URL url) {
        this.type = type;
        this.url = url;
      }
    }
  }

  public interface OnCompleteListener<Result> {
    void onComplete(Result result);
  }

  public interface OnErrorListener {
    void onError(List<String> errors);
  }
}
