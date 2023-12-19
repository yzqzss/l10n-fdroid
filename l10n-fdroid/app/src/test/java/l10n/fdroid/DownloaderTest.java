package l10n.fdroid;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import l10n.fdroid.utils.Downloader;

public class DownloaderTest {
    @Test void testDownloader() throws IOException {
        String url = "https://f-droid.org/F-Droid.apk";
        File apkFile = new File("tmp/F-Droid.apk");
        apkFile.delete();
        Downloader.download(url, apkFile);
        assertTrue(apkFile.exists());
        apkFile.delete();
    }
}
