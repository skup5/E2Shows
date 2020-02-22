package cz.skup5.e2shows.downloader;

import android.content.Context;
import androidx.annotation.NonNull;

import java.net.URI;

import cz.skup5.jEvropa2.Extractor;
import cz.skup5.jEvropa2.HttpRequest;

/**
 * This downloader allows to get {@link URI} of multimedia file from the specific Evropa2 HTML page.
 * The page is specified by {@link URI} execute parameter.
 * <p/>
 * Created on 5.1.2019
 *
 * @author Skup5
 */
public class MediaUrlDownloader extends AbstractDownloader<URI, Void, URI> {

    public MediaUrlDownloader() {
        super();
    }

    public MediaUrlDownloader(Context context, String dialogTitle) {
        super(context, dialogTitle);
    }

    @Override
    protected URI download(@NonNull URI... uris) {
        URI uri = null;
        try {
            uri = htmlParser.parseMp3Url(Extractor.getDataJSON(HttpRequest.httpGetSite(uris[0].toString())));
        } catch (Exception e) {
            e.printStackTrace();
            errors.add("Chyba při extrakci audio nebo video url adresy.");
        }
        return uri;
    }

}
