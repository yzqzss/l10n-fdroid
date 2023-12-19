package l10n.fdroid.index;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import l10n.fdroid.schema.FDroidPackage;
import l10n.fdroid.schema.Version;

import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Set;

import java.util.HashMap;

import java.util.logging.Logger;
import java.util.logging.Level;

public class ParseIndexv2Json {
    static Logger logger = Logger.getLogger(ParseIndexv2Json.class.getName());
    public static HashMap<String, FDroidPackage> parse(File indexJsonFile) throws FileNotFoundException  {
        HashMap<String, FDroidPackage> fdroidPackages = new HashMap<String, FDroidPackage>();

        // Read the JSON file
        JsonElement fileElement = JsonParser.parseReader(new FileReader(indexJsonFile));
        JsonObject packagesObject = fileElement.getAsJsonObject().getAsJsonObject("packages");

        // Loop through each package
        Set<Map.Entry<String, JsonElement>> packageEntries = packagesObject.entrySet();
        logger.info("Number of packages: " + packageEntries.size());
        for (Map.Entry<String, JsonElement> packageEntry : packageEntries) {
            String packageName = packageEntry.getKey();
            JsonObject packageDetails = packageEntry.getValue().getAsJsonObject();

            // Get metadata
            JsonObject metadata = packageDetails.getAsJsonObject("metadata");
            String sourceCode = null;
            try {
                sourceCode = metadata.get("sourceCode").getAsString();
            } catch (NullPointerException e) {
                logger.log(Level.FINE, "No sourceCode for " + packageName);
            }

            logger.log(Level.FINEST,"Package Name: " + packageName);
            logger.log(Level.FINEST,"Source Code: " + sourceCode);

            fdroidPackages.put(packageName, new FDroidPackage(packageName, sourceCode));

            // Get versions
            JsonObject versions = packageDetails.getAsJsonObject("versions");
            Set<Map.Entry<String, JsonElement>> versionEntries = versions.entrySet();
            for (Map.Entry<String, JsonElement> versionEntry : versionEntries) {
                String versionId = versionEntry.getKey();
                JsonObject versionDetails = versionEntry.getValue().getAsJsonObject();

                String fileName = versionDetails.getAsJsonObject("file").get("name").getAsString();
                JsonObject manifest = versionDetails.getAsJsonObject("manifest");
                String versionName = manifest.get("versionName").getAsString();
                Long versionCode = manifest.get("versionCode").getAsLong();

                logger.log(Level.FINEST,"    Version ID: " + versionId);
                logger.log(Level.FINEST,"    File Name: " + fileName);
                logger.log(Level.FINEST,"    Version Name: " + versionName);
                logger.log(Level.FINEST,"    Version Code: " + versionCode);

                fdroidPackages.get(packageName).addVersion(versionCode, new Version(versionCode, versionName, fileName));
                break; // only get the latest version
            }
        }

        return fdroidPackages;
    }
}
