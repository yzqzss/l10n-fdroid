package l10n.fdroid;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import l10n.fdroid.index.ParseIndexv2Json;
import l10n.fdroid.schema.FDroidPackage;

public class ParseIndexv2JsonTest {
    @Test void testParseIndexv2Json() throws FileNotFoundException {
        File indexv2file = new File("src/test/resources/index-v2.json");
        assertTrue(indexv2file.exists());
        HashMap<String, FDroidPackage> fdroidPackage = ParseIndexv2Json.parse(indexv2file);
        assertTrue(fdroidPackage.containsKey("org.fdroid.fdroid"));
        assertTrue(fdroidPackage.size() > 4000);
    }
}
