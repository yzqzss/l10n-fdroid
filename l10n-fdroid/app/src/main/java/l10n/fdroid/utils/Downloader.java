package l10n.fdroid.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;


public class Downloader {
    static Logger logger = Logger.getLogger(Downloader.class.getName());

    public static Integer getSize(String url) throws IOException {
        URL u = new URL(url);
        return u.openConnection().getContentLength();
    }
    public static void download(String url, File toFile) throws IOException {
        IOException exception = null;
        logger.info("Downloading..." + url + " to " + toFile);
        for (int i = 0; i < 3; i++) {
            try {
                // creat parent dir if not exist
                File fileParent = toFile.getParentFile();
                if (fileParent != null && !fileParent.exists()) {
                    fileParent.mkdirs();
                }

                downloadUsingNIO(new URL(url), toFile);
                return;
            } catch (IOException e) {
                exception = e;
                
                logger.warning("Failed to download " + url + " to " + toFile + " on attempt " + i);
            }
        }
        throw new IOException(exception);
    }
    private static void downloadUsingNIO(URL url, File file) throws IOException {
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

}
