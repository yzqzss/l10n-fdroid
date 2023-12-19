package l10n.fdroid;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.xmlpull.v1.XmlPullParserException;

import l10n.fdroid.schema.Values;

import java.nio.file.NotDirectoryException;

import java.io.File;
import java.io.IOException;

public class ValuesTest {
    @Test void testValuesDir() throws IOException, XmlPullParserException, NotDirectoryException{
        File valuesDir = new File("src/test/resources/values");
        Values values = new Values(valuesDir);
        values.loadStrings();
        assertTrue(values.stringCount > 0);
        assertTrue(values.name.equals("values"));
    }
    @Test void testValuesStringXml() throws IOException, XmlPullParserException, NotDirectoryException{
        File valuesDir = new File("src/test/resources/values-zh-rCN");
        File stringXml = new File(valuesDir, "strings.xml");
        Values values = new Values(valuesDir.getName());
        values.loadStrings(stringXml);
        for (String key : values.strings.keySet()) {
            System.out.println(key + " = " + values.strings.get(key));
        }
        assertTrue(values.stringCount > 0);
    }
    @Test void testIllegalValuesDir() throws IOException, XmlPullParserException, NotDirectoryException{ 
        assertThrows(java.lang.AssertionError.class, () -> {
            new Values("fakevalues-zh-rCN");
        });
        assertThrows(java.nio.file.NotDirectoryException.class, () -> {
            new Values(new File("src/test/resources/values-zh-rCN/strings.xml"));
        });
        assertThrows(java.lang.NullPointerException.class, () -> {
            Values values = new Values("values-zh-rCN");
            values.loadStrings();
        });
        
    }
}
