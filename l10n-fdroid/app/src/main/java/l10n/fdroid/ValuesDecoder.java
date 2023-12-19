package l10n.fdroid;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.reandroid.apk.ApkModule;
import com.reandroid.arsc.chunk.TableBlock;

import l10n.fdroid.APKEditorWarper.APKLogger_;
import l10n.fdroid.APKEditorWarper.ApkModuleXmlDecoder_;


public class ValuesDecoder {
    static Logger logger = Logger.getLogger(ValuesDecoder.class.getName());
    
    public void decodeValues(File apkFile, File mainDirectory) throws IOException {
        logger.log(Level.INFO, "decode" + apkFile + " to " + mainDirectory);
        ApkModule apkModule=ApkModule.loadApkFile(new APKLogger_(), apkFile);
        ApkModuleXmlDecoder_ decoder = new ApkModuleXmlDecoder_(apkModule);
        TableBlock tableBlock = apkModule.getTableBlock();
        float start = System.currentTimeMillis();
        decoder.decodeValues(mainDirectory, tableBlock);
        float end = System.currentTimeMillis();
        logger.log(Level.INFO, "Done! " + (end - start)/1000 + "s");
    }

    public static ArrayList<File> getValuesDirs(File mainDirectory) {
        // mainDir = tmp/apk_resources
        // values -> tmp/apk_resources/resources/package_1/res/values

        // for each package dir
        //   for each res dir
        //    add values dir to list
        File resourcesDir = new File(mainDirectory, "resources");

        ArrayList<File> valuesDirs = new ArrayList<File>();

        for (File packageDir : resourcesDir.listFiles()) {
            File[] resDirs = packageDir.listFiles();
            for (File resDir : resDirs) {
                if (resDir.getName().equals("res")) {
                    File[] valuesDirs_ = resDir.listFiles();
                    for (File valuesDir : valuesDirs_) {
                        if (valuesDir.getName().startsWith("values")) {
                            valuesDirs.add(valuesDir);
                        }
                    }
                }
            }
        }
        return valuesDirs;
    }
    public static ArrayList<File> filteValiedStringsValuesDirs(ArrayList<File> valuesDirs) {
        ArrayList<File> languageValuesDirs = new ArrayList<File>();
        for (File valuesDir : valuesDirs) {
            if (new File(valuesDir, "strings.xml").exists()) {
                languageValuesDirs.add(valuesDir);
            }
        }
        return languageValuesDirs;
    }
    public static void main(String[] args) {
        // for (File valuesDir : getValuesDirs(new File("./l10n-fdroid/app/tmp/apk_resources/"))) {
        //     System.out.println(valuesDir);
        // }
        System.out.println(getValuesDirs(new File("./l10n-fdroid/app/tmp/apk_resources/")).size());
        System.out.println(filteValiedStringsValuesDirs(getValuesDirs(new File("./l10n-fdroid/app/tmp/apk_resources/"))).size());
        // filterLanguageValuesDirs(getValuesDirs(new File("./l10n-fdroid/app/tmp/apk_resources/"))).forEach(System.out::println);
    }
}
