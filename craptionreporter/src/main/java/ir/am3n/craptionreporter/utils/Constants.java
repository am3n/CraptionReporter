package ir.am3n.craptionreporter.utils;

import ir.am3n.craptionreporter.CraptionReporter;

public class Constants {

    public static final String LOG_SUFFIX = "_log";
    public static final String EXCEPTION_SUFFIX = "_exception";
    public static final String CRASH_SUFFIX = "_crash";

    static final String FILE_EXTENSION = ".txt";

    static final String DEFAULT_RETRACE_MAPPING_FILE_NAME = "mapping.txt";

    static final String MAIN_DIR = "CraptionReporter";
    static final String CRASH_REPORT_DIR = "crashReports";
    static final String EXCEPTION_REPORT_DIR = "exceptionReports";
    static final String LOG_REPORT_DIR = "logReports";

    static final int NOTIFICATION_ID = 1;

    public static final String LANDING = "landing";


    public final static Integer SERVER_TIMEOUTS = 5*1000;
    private final static String SERVER_ROOT = CraptionReporter.getInstance().getServerHost()+"craptionreporter/";
    public final static String SERVER_MAPPINGS_FILES_DIR = SERVER_ROOT+"mappings/";
    public final static String SERVER_REPORTER = SERVER_ROOT+"Report.php";

}
