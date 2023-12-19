package l10n.fdroid.index;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;


import l10n.fdroid.utils.Downloader;

public class RepoIndex {
    static Logger logger = Logger.getLogger(RepoIndex.class.getName());
    static String fdroid_url = "https://f-droid.org/repo/index-v2.json";
    public static File initFDroidIndex() {
        File indexv2file = new File("data/f-droid/index-v2.json");
        try {
            if (!indexv2file.exists()) {
                logger.log(Level.INFO, "index-v2.json does not exist, downloading...");
                Downloader.download(fdroid_url, indexv2file);
            } else {
                logger.log(Level.INFO, "index-v2.json exists, checking size...");
                if (indexv2file.length() != Downloader.getSize(fdroid_url)) {
                    logger.log(Level.INFO, "index-v2.json size mismatch, downloading...");
                    Downloader.download(fdroid_url, indexv2file);
                }
                logger.log(Level.INFO, "index-v2.json size match, no need to download");
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.warning("Failed to download " + fdroid_url);
        }

        logger.log(Level.INFO, "index-v2.json size: " + indexv2file.length() + " , path: " + indexv2file.getAbsolutePath());
        return indexv2file;
    }
}
