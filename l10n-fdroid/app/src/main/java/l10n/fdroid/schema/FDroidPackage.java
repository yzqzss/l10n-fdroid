package l10n.fdroid.schema;

import java.util.HashMap;

import org.bson.Document;

public class FDroidPackage {
    public String packageName;
    public String sourceCode; // url typically, may null

    HashMap<Long, Version> versions = new HashMap<Long, Version>();

    public Long versionCode;
    public String versionName;
    public String fileName;

    public FDroidPackage(String packageName, String sourceCode) {
        this.packageName = packageName;
        this.sourceCode = sourceCode;
    }
    public FDroidPackage(Document document) {
        this.packageName = document.getString("packageName");
        this.sourceCode = document.getString("sourceCode");
        this.versionCode = document.getLong("versionCode");
        this.versionName = document.getString("versionName");
        this.fileName = document.getString("fileName");
    }

    public void addVersion(Long versionCode, Version version) {
        versions.put(versionCode, version);
    }

    public Long getLatestVersionCode() {
        Long maxVersionCode = -9999L;
        assert versions.size() > 0;

        for (Long versionCode : versions.keySet()) {
            if (versionCode > maxVersionCode) {
                maxVersionCode = versionCode;
            }
        }
        return maxVersionCode;
    }

    public Document toDocument() {
        Document document = new Document();
        document.put("packageName", packageName);
        document.put("sourceCode", sourceCode);

        // "versions": {
        //     1: {
        //         "versionCode": 1,
        //         "versionName": "1.0",
        //         "fileName": "app-1.apk"
        //     },
        //     2: {fileParent
        //         ...
        //     }
        // }
        
        // get max versionCode
        
        Version latestVersion = versions.get(this.getLatestVersionCode());

        document.put("versionCode", latestVersion.versionCode);
        document.put("versionName", latestVersion.versionName);
        document.put("fileName", latestVersion.fileName);

        return document;
    }
}
