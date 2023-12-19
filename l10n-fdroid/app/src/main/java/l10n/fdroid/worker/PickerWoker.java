package l10n.fdroid.worker;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.ArrayList;

import l10n.fdroid.index.ParseIndexv2Json;
import l10n.fdroid.index.RepoIndex;
import l10n.fdroid.schema.FDroidPackage;
import l10n.fdroid.db.DB;

import org.bson.Document;

import java.util.logging.Logger;

public class PickerWoker extends Thread {
    static Logger logger = Logger.getLogger(PickerWoker.class.getName());

    static DB db;
    static ArrayList<Document> docs_to_insert;
    static HashMap<String, FDroidPackage> fdroidPackages_to_process;

    public PickerWoker() {
        super();
    }

    public void run() {
        FDroidPackage fdroidPackage = null;
        while (true) {
            synchronized (fdroidPackages_to_process) {
                if (fdroidPackages_to_process.size() == 0) {
                    logger.info("No more packages to process");
                    return;
                }
                fdroidPackage = fdroidPackages_to_process.remove(fdroidPackages_to_process.keySet().iterator().next());
            }
            logger.info("(" + fdroidPackages_to_process.size() + " left) Processing " + fdroidPackage.packageName + " v" + fdroidPackage.getLatestVersionCode());
            processIt(db, fdroidPackage);
        }
    }

    static public void processIt(DB db, FDroidPackage fdroidPackage){
        if (db.apps_col.find(
            new Document("packageName", fdroidPackage.packageName)
                .append("versionCode", fdroidPackage.getLatestVersionCode()))
                .first() != null
            ) {
            logger.info("Skipping " + fdroidPackage.packageName);
            return;
        }
        synchronized (docs_to_insert) {
            docs_to_insert.add(fdroidPackage.toDocument());
        }
    }

    /*
    * args[0]: number of threads
    */
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length == 0 || args[0].equals("--help")) {
            System.out.println("Usage: num_threads [0 for single thread mod] (100 is recommended)");
            return;
        }
        Integer num_threads = 100;
        num_threads = Integer.parseInt(args[0]);
        db = new DB();

        if (num_threads == 0) {
            logger.info("Single thread");
            main_sync(args);
            return;
        }
        if (num_threads < 1) { num_threads = 1; }

        logger.info("Multi thread");

        File indexFile = RepoIndex.initFDroidIndex();
        fdroidPackages_to_process = ParseIndexv2Json.parse(indexFile);
        docs_to_insert = new ArrayList<Document>();

        // start threads
        ArrayList<PickerWoker> workers = new ArrayList<PickerWoker>();

        for (int i = 0; i < num_threads; i++) {
            PickerWoker worker = new PickerWoker();
            logger.info("Starting " + worker);
            workers.add(worker);
            worker.start();
        }

        // wait for threads to finish
        for (PickerWoker worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        // insert
        if (docs_to_insert.size() == 0) {
            logger.info("No new documents to insert");
            return;
        }

        logger.info("Inserting " + docs_to_insert.size() + " documents");
        db.apps_col.insertMany(docs_to_insert);
        logger.info("Inserted");

    }

    public static void main_sync(String[] args) throws FileNotFoundException {
        File indexFile = RepoIndex.initFDroidIndex();

        HashMap<String, FDroidPackage> fdroidPackages = ParseIndexv2Json.parse(indexFile);

        ArrayList<Document> docs_to_insert = new ArrayList<Document>();

        for (String packageName : fdroidPackages.keySet()) {
            FDroidPackage fdroidPackage = fdroidPackages.get(packageName);
            // if {packageName and versionCode} already exists, skip
            if (db.apps_col.find(
                new Document("packageName", packageName)
                .append("versionCode", fdroidPackage.getLatestVersionCode()))
                .first() != null
                ) {
                logger.info("Skipping " + packageName);
                continue;
            }
            
            docs_to_insert.add(fdroidPackage.toDocument());
            logger.info("Queued " + packageName);
        }
        if (docs_to_insert.size() == 0) {
            logger.info("No new documents to insert");
            return;
        }

        logger.info("Inserting " + docs_to_insert.size() + " documents");
        db.apps_col.insertMany(docs_to_insert);
        logger.info("Inserted");
        logger.info(db.apps_col.countDocuments() + " documents in the collection");
    }
}
