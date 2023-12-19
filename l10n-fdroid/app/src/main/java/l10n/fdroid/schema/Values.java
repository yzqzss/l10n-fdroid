package l10n.fdroid.schema;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.HashMap;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class Values {
    
    public String name; // "values" or "values-xx"
    public Integer stringCount = -1; // -1 means not initialized

    public HashMap<String, String> strings = new HashMap<String, String>();

    private File valuesDir = null;

    static Logger logger = Logger.getLogger(Values.class.getName());

    public Values(String name) {
        assert name.startsWith("values");
        assert !name.contains("/");
        logger.info("Values: " + name);
        this.name = name;
    }
    public Values(File valuesDir) throws IOException, XmlPullParserException, NotDirectoryException {
        if (!valuesDir.isDirectory()) {
            throw new NotDirectoryException(valuesDir.getAbsolutePath());
        }
        if (!valuesDir.getName().startsWith("values") || valuesDir.getName().contains("/")) {
            throw new IllegalArgumentException("valuesDir name must start with \"values\"");
        }
        logger.info("Values: " + valuesDir.getName());
        this.name = valuesDir.getName();
        this.valuesDir = valuesDir;
    }

    public void putString(String name, String value) {
        logger.log(Level.FINE, "putString: " + name + " = " + value);
        strings.put(name, value);
    }

    public void loadStrings() throws IOException, XmlPullParserException, NotDirectoryException {
        if (valuesDir == null) {
            throw new NullPointerException("valuesDir is null");
        }
        loadStrings(valuesDir);
    }

    public void loadStrings(File valuesDirORStringXml) throws IOException, XmlPullParserException, FileNotFoundException  {
        File stringXmlFile = null;
        if (valuesDirORStringXml.isDirectory()) { // for valuesDir
            valuesDir = valuesDirORStringXml;
            stringXmlFile = new File(valuesDir, "strings.xml");
            if (!stringXmlFile.exists()) { // not found
                throw new FileNotFoundException(stringXmlFile.getAbsolutePath());
            }
        } else { // strings.xml
            stringXmlFile = valuesDirORStringXml;
        }
        assert stringXmlFile.getName().equals("strings.xml");

        InputStream inputStream = new FileInputStream(stringXmlFile);
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setInput(inputStream, "UTF-8");
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.getName().equals("string")) {
                String name = parser.getAttributeValue(null, "name");
                String value = parser.nextText();
                putString(name, value);
            }
            eventType = parser.next();
        }
        logger.log(Level.INFO, "loadStrings: " + stringXmlFile.getAbsolutePath() + " " + strings.size());
        this.stringCount = strings.size();
    }

    public static void main(String[] args) {
        File valuesDir = new File("/home/yzqzss/git/l10n-fdroid/l10n-fdroid/app/src/test/resources/values");
        File stringXml = new File(valuesDir, "strings.xml");
        Values values = new Values(valuesDir.getName());
        try {
            values.loadStrings(stringXml);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String key : values.strings.keySet()) {
            System.out.println(key + " = " + values.strings.get(key));
        }
        System.out.println(values.stringCount);
    }
}
