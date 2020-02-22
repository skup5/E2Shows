package cz.skup5.e2shows.downloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * This downloader allows to download image in {@link Bitmap} format from the specific {@link URL}.
 * <p/>
 * Created on 5.1.2019
 *
 * @author Skup5
 */
public class CoverImageDownloader extends AbstractDownloader<URL, Void, Bitmap> {

    public CoverImageDownloader() {
        super();
    }

    public CoverImageDownloader(Context context, String dialogTitle) {
        super(context, dialogTitle);
    }

    @Override
    protected Bitmap download(@NonNull URL... urls) {
        Bitmap bitmap = null;
        URL site = urls[0];
        try (InputStream stream = site.openStream()) {
            bitmap = BitmapFactory.decodeStream(stream);
        } catch (IOException e) {
            e.printStackTrace();
            errors.add("Chyba při stahování obrázku kategorie.");
        }
        return bitmap;
    }
}
