package l10n.fdroid.worker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;

import l10n.fdroid.schema.FDroidPackage;
import l10n.fdroid.schema.Values;
import l10n.fdroid.utils.Downloader;
import l10n.fdroid.ValuesDecoder;
import l10n.fdroid.db.DB;

import org.bson.Document;

import java.util.logging.Logger;

public class ValuesWoker {
    static Logger logger = Logger.getLogger(ValuesWoker.class.getName());
    public static void main(String[] args) {
        DB db = new DB();

        // findOneAndUpdate {status: {$exists: false}} -> {status: "processing"}
        Document status_$EMPTY = new Document("status", new Document("$exists", false));
        Document status_DONE = new Document("$set", new Document("status", "DONE"));
        Document status_FAIL = new Document("$set", new Document("status", "FAIL"));
        Document status_PROCESSING = new Document("$set", new Document("status", "PROCESSING"));
        
        Document processing_doc = db.apps_col.findOneAndUpdate(status_$EMPTY, status_PROCESSING);
        while (processing_doc != null && !new File("stop").exists()) {
            logger.info("Processing " + processing_doc.getString("packageName"));

            FDroidPackage fdroidPackage = new FDroidPackage(processing_doc);
            
            // download apk
            String apk_url = "https://mirrors.tuna.tsinghua.edu.cn/fdroid/repo" + fdroidPackage.fileName;
            File apkFile = new File("data/f-droid/apk/" , fdroidPackage.packageName + "-" + fdroidPackage.versionCode + ".apk");
            File mainDirectory = new File("data/f-droid/values/" + fdroidPackage.packageName + "/" + fdroidPackage.versionCode);

            try {
                Downloader.download(apk_url, apkFile);
                logger.info("Downloaded " + apk_url + " to " + apkFile);

                // extract all values
                logger.info("Extracting " + apkFile + "'s values to " + mainDirectory);
                ValuesDecoder decoder = new ValuesDecoder();
                decoder.decodeValues(apkFile, mainDirectory);

                ArrayList<File> valuesDirs = ValuesDecoder.getValuesDirs(mainDirectory);
                ArrayList<File> languageValuesDirs = ValuesDecoder.filteValiedStringsValuesDirs(valuesDirs);
                logger.info("Extracted " + languageValuesDirs.size() + " valid language values dirs");

                // parse each values dir
                ArrayList<Document> string_docs_to_insert = new ArrayList<Document>();
                for (File valuesDir : languageValuesDirs) {
                    Values values = new Values(valuesDir);
                    values.loadStrings();
                    logger.info("Loaded " + values.strings.size() + " strings from " + valuesDir);
                    assert values.stringCount == values.strings.size();

                    // insert into db
                    string_docs_to_insert.add(
                        new Document("packageName", fdroidPackage.packageName)
                        .append("versionCode", fdroidPackage.versionCode)
                        .append("valuesName", values.name)
                        .append("stringCount", values.stringCount)
                        .append("strings", values.strings)
                    );
                }
                if (string_docs_to_insert.size() == 0) {
                    logger.warning("No strings to insert");
                } else {
                    db.values_col.insertMany(string_docs_to_insert);
                }
                db.apps_col.updateOne(new Document("_id", processing_doc.get("_id")), status_DONE);
            } catch (Exception e) {
                e.printStackTrace();
                logger.warning("Failed to process " + fdroidPackage.fileName);
                db.apps_col.updateOne(new Document("_id", processing_doc.get("_id")), status_FAIL);
            } finally {
                apkFile.delete();
                mainDirectory.delete();
                // delete mainDirectory
                Path pathToBeDeleted = mainDirectory.toPath();

                try {
                    Files.walk(pathToBeDeleted)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                }
                mainDirectory.delete();
            }

            processing_doc = db.apps_col.findOneAndUpdate(status_$EMPTY, status_PROCESSING); // -> {status: "PROCESSING"}
        }
        logger.info("Done");
    }
    
}
