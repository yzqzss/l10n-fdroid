package l10n.fdroid.db;

import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.logging.Logger;

public class DB {
    MongoClient client;
    MongoDatabase database;
    public MongoCollection<Document> apps_col;
    public MongoCollection<Document> values_col;

    static Logger logger = Logger.getLogger(DB.class.getName());
    public DB() {
        logger.info("Getting MONDODB_URI from env");
        String uri = System.getenv("MONGODB_URI");
        if (uri == null || uri.isEmpty()) {
            logger.warning("MONGODB_URI is not set");
            System.exit(10);
        }
        logger.info("Connecting to MongoDB ... ");
        MongoClient mongoClient = MongoClients.create(uri);
        this.client = mongoClient;
        this.database = mongoClient.getDatabase("fdroidl10n");
        this.apps_col = database.getCollection("apps");
        this.values_col = database.getCollection("values");

        logger.info(apps_col.countDocuments() + " documents in the collection");
        logger.info("Connected to MongoDB :) ");
    }
    public void close() {
        logger.info("Closing MongoDB connection");
        this.client.close();
    }
}