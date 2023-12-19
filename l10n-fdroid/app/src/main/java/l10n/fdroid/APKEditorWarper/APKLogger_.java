package l10n.fdroid.APKEditorWarper;

public class APKLogger_ implements com.reandroid.apk.APKLogger {

        @Override
        public void logError(String arg0, Throwable arg1) {
            System.err.println(arg0);
            arg1.printStackTrace();
        }

        @Override
        public void logMessage(String arg0) {
            System.out.println(arg0);
            if (arg0.contains("package_1")){
                // breakpoint
                System.out.println("breakpoint");
            }
        }

        @Override
        public void logVerbose(String arg0) {
            System.out.println(arg0);
        }
        
}