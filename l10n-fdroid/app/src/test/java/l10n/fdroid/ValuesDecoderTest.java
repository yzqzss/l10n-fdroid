package l10n.fdroid;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ValuesDecoderTest {
    @Test void testValuesDecoder() throws IOException {
        File apkFile = new File("src/test/resources/testValuesDecoder.apk");
        File mainDirectory = new File("tmp/apk_resources");
        mainDirectory.delete();
        ValuesDecoder decoder = new ValuesDecoder();
        decoder.decodeValues(apkFile, mainDirectory);
        assertTrue(mainDirectory.exists());
        assertTrue(new File("tmp/apk_resources/resources/package_1/res/values/strings.xml").exists());
    }
    @Test void testGetValuesDirs() throws IOException {
        File mainDirectory = new File("tmp/apk_resources");
        ArrayList<File> valuesDirs = ValuesDecoder.getValuesDirs(mainDirectory);
        assertTrue(valuesDirs.size() == 140);
    }
    @Test void testFilteValiedStringsValuesDirs() throws IOException {
        File mainDirectory = new File("tmp/apk_resources");
        ArrayList<File> valuesDirs = ValuesDecoder.getValuesDirs(mainDirectory);
        ArrayList<File> languageValuesDirs = ValuesDecoder.filteValiedStringsValuesDirs(valuesDirs);
        assertTrue(languageValuesDirs.size() == 101);
    }
}
