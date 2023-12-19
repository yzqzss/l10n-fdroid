package l10n.fdroid.schema;

public class Version {
    public Long versionCode;
    public String versionName;
    public String fileName;

    public Version(Long versionCode, String versionName, String fileName) {
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.fileName = fileName;
    }
}
