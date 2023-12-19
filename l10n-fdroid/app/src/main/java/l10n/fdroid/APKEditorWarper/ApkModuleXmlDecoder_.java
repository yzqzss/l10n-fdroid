package l10n.fdroid.APKEditorWarper;

import java.io.File;
import java.io.IOException;

import com.reandroid.apk.ApkModule;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.coder.xml.XmlCoder;

public class ApkModuleXmlDecoder_ extends com.reandroid.apk.ApkModuleXmlDecoder {

    public ApkModuleXmlDecoder_(ApkModule apkModule) {
        super(apkModule);
    }
    
    public void decodeValues(File mainDirectory, TableBlock tableBlock) throws IOException {
        File resourcesDir = new File(mainDirectory, "resources");
        XmlCoder xmlCoder = XmlCoder.getInstance();
        xmlCoder.VALUES_XML.decodeTable(resourcesDir, tableBlock, this);
   }
}
