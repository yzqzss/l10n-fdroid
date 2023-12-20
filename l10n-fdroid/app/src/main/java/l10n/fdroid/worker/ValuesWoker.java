package l10n.fdroid.worker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import l10n.fdroid.schema.FDroidPackage;
import l10n.fdroid.schema.Values;
import l10n.fdroid.utils.Downloader;
import l10n.fdroid.ValuesDecoder;
import l10n.fdroid.db.DB;

import org.bson.Document;

import java.util.logging.Logger;

public class ValuesWoker {
    // query docs:
    static Document status_not_EXISTS = new Document("status", new Document("$exists", false));
    static Document status_DONE = new Document("status", "DONE");
    static Document status_FAIL = new Document("status", "FAIL");
    static Document status_PROCESSING = new Document("status", "PROCESSING");

    // update docs:
    static Document status_$DONE = new Document("$set", new Document("status", "DONE"));
    static Document status_$FAIL = new Document("$set", new Document("status", "FAIL"));
    static Document status_$PROCESSING = new Document("$set", new Document("status", "PROCESSING"));

    static Logger logger = Logger.getLogger(ValuesWoker.class.getName());
    public static void main(String[] args) {
        HashMap<String, Document> claimable_statuses = new HashMap<String, Document>();
        claimable_statuses.put("NOT_EXISTS", status_not_EXISTS);
        claimable_statuses.put("FAIL", status_FAIL);
        claimable_statuses.put("PROCESSING", status_PROCESSING);
        // claimable_statuses.put("DONE", status_DONE);

        if (args.length == 0 || args[0] == "--help") {
            System.out.println("Usage: [task_status_to_claim]");
            System.out.println("task_status_to_claim: " + claimable_statuses.keySet());
            System.exit(0);
        }
        if (!claimable_statuses.containsKey(args[0])) {
            System.out.println("task_status_to_claim: " + claimable_statuses.keySet());
            System.exit(1);
        }

        // task to claim:
        Document doc_to_claim = claimable_statuses.get(args[0]);

        DB db = new DB();

        
        Document inprocessing_doc = db.apps_col.findOneAndUpdate(doc_to_claim, status_$PROCESSING);
        while (inprocessing_doc != null && !new File("stop").exists()) {
            logger.info("Processing " + inprocessing_doc.getString("packageName"));

            FDroidPackage fdroidPackage = new FDroidPackage(inprocessing_doc);
            
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
                db.apps_col.updateOne(new Document("_id", inprocessing_doc.get("_id")), status_$DONE);
            } catch (Exception e) {
                e.printStackTrace();
                logger.warning("Failed to process " + fdroidPackage.fileName);
                db.apps_col.updateOne(new Document("_id", inprocessing_doc.get("_id")), status_$FAIL);
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

            inprocessing_doc = db.apps_col.findOneAndUpdate(doc_to_claim, status_$PROCESSING); // -> {status: "PROCESSING"}
        }
        logger.info("Done");
    }
    
}
