package ir.am3n.craptionreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

import ir.am3n.craptionreporter.server.Reporter;
import ir.am3n.craptionreporter.server.ServerHandlerService;
import ir.am3n.craptionreporter.ui.CrashReporterActivity;
import ir.am3n.craptionreporter.utils.Constants;
import ir.am3n.craptionreporter.utils.CrashReporterExceptionHandler;
import ir.am3n.craptionreporter.utils.CrashReporterNotInitializedException;
import ir.am3n.craptionreporter.utils.CrashUtil;

public class CrashReporter {

    private static CrashReporter instance;

    private static Context applicationContext;

    private static String crashReportPath;
    private static String exceptionReportPath;
    private static String logReportPath;
    private static String retraceMappingFilePath;

    private static boolean isNotificationEnabled = true;

    private static String serverIP = "";
    private static Integer appVersionCode;
    private static boolean isServerReportEnabled = false;
    private static RetraceOn retraceOn = RetraceOn.NONE;
    private static boolean retraceVerbose = true;

    private static String userIdentification;


    public static CrashReporter with(Context context) {
        applicationContext = context;
        if (instance==null)
            instance = new CrashReporter();
        return instance;
    }
    public static CrashReporter getInstance() {
        if (instance==null)
            instance = new CrashReporter();
        return instance;
    }

    public CrashReporter disableNotification() {
        isNotificationEnabled = false;
        return instance;
    }
    public CrashReporter appVersionCode(int versionCode) {
        appVersionCode = versionCode;
        return instance;
    }
    public CrashReporter enableServer(String ip, int versionCode, RetraceOn retrace) {
        serverIP = ip;
        appVersionCode = versionCode;
        isServerReportEnabled = true;

        retraceOn = retrace;
        retraceVerbose = true;

        return instance;
    }
    public CrashReporter build() {
        //Log.d("Meeeeeee", "CrashReporter() > build()");
        setUpExceptionHandler();
        if (isServerReportEnabled) {
            //Log.d("Meeeeeee", "CrashReporter() > build() > report()");
            new Reporter()
                    .listener(() -> {
                        if (ServerHandlerService.handler!=null) {
                            Message message = new Message();
                            message.what = 0;
                            ServerHandlerService.handler.sendMessage(message);
                        }
                    }).report();
        }
        return instance;
    }

    private static void setUpExceptionHandler() {
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CrashReporterExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CrashReporterExceptionHandler());
        }
    }

    public Context getContext() {
        if (applicationContext == null) {
            try {
                throw new CrashReporterNotInitializedException("Initialize CrashReporter : call CrashReporter.initialize(context, crashReportPath)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return applicationContext;
    }

    public String getCrashReportPath() {
        if (TextUtils.isEmpty(crashReportPath)) {
            return CrashUtil.getDefaultCrashPath();
        }
        return crashReportPath;
    }
    public String getExceptionReportPath() {
        if (TextUtils.isEmpty(exceptionReportPath)) {
            return CrashUtil.getDefaultExceptionPath();
        }
        return exceptionReportPath;
    }
    public String getLogReportPath() {
        return logReportPath;
    }

    public boolean isNotificationEnabled() {
        return isNotificationEnabled;
    }
    public boolean isServerReportEnabled() {
        return isServerReportEnabled;
    }

    public RetraceOn getRetraceOn() {
        return retraceOn;
    }
    public String getRetraceMappingFilePath() {
        if (TextUtils.isEmpty(retraceMappingFilePath)) {
            return CrashUtil.getDefaultRetraceMappingFilePath();
        }
        return retraceMappingFilePath;
    }
    public boolean hasRetraceMappingFile() {
        return new File(getRetraceMappingFilePath()).exists();
    }
    public boolean getRetraceVerbose() {
        return retraceVerbose;
    }

    public String getServerIP() {
        return serverIP;
    }
    public int getAppVersionCode() {
        return appVersionCode;
    }
    public String getMappingFileUrl() {
        return Constants.SERVER_MAPPINGS_FILES_DIR+"mapping-"+appVersionCode+".txt";
    }
    public String getRepoterUrl() {
        return Constants.SERVER_REPORTER;
    }


    public void setUserIdentification(String identification) {
        userIdentification = identification;
        SharedPreferences sh = applicationContext.getSharedPreferences("crashreporte", Context.MODE_PRIVATE);
        sh.edit().putString("user_identification", identification).apply();
    }
    public String getUserIdentification() {
        if (TextUtils.isEmpty(userIdentification)) {
            SharedPreferences sh = applicationContext.getSharedPreferences("crashreporte", Context.MODE_PRIVATE);
            return sh.getString("user_identification", "");
        }
        return userIdentification;
    }

    public Intent getLaunchIntent() {
        return new Intent(applicationContext, CrashReporterActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }


    public static void exception(Exception exception) {
        CrashUtil.exception(exception);
    }
    public static void exception(Throwable exception, String eventLocation) {
        CrashUtil.exception(exception, eventLocation);
    }
    public static void log(String log) {
        CrashUtil.log(log);
    }

}
