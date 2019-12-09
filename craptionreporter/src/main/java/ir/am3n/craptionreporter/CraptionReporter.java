package ir.am3n.craptionreporter;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.text.TextUtils;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import ir.am3n.craptionreporter.server.Reporter;
import ir.am3n.craptionreporter.server.ServerHandlerService;
import ir.am3n.craptionreporter.ui.CraptionReporterActivity;
import ir.am3n.craptionreporter.utils.Constants;
import ir.am3n.craptionreporter.utils.CraptionReporterExceptionHandler;
import ir.am3n.craptionreporter.utils.CraptionUtil;
import ir.am3n.craptionreporter.utils.CraptionReporterNotInitializedException;

public class CraptionReporter {

    private static CraptionReporter instance;

    private static Context applicationContext;

    private static String crashReportPath;
    private static String exceptionReportPath;
    private static String logReportPath;
    private static String retraceMappingFilePath;

    private static boolean isNotificationEnabled = true;

    private static String serverHost = "";
    private static Integer appVersionCode;
    private static boolean isServerReportEnabled = false;
    private static RetraceOn retraceOn = RetraceOn.NONE;
    private static boolean retraceVerbose = true;

    private static String userIdentification;


    public static CraptionReporter with(Context context) {
        applicationContext = context;
        if (instance==null)
            instance = new CraptionReporter();
        return instance;
    }
    public static CraptionReporter getInstance() {
        if (instance==null)
            instance = new CraptionReporter();
        return instance;
    }

    public CraptionReporter disableNotification() {
        isNotificationEnabled = false;
        return instance;
    }
    public CraptionReporter appVersionCode(int versionCode) {
        appVersionCode = versionCode;
        return instance;
    }
    public CraptionReporter enableServer(String host, int versionCode, RetraceOn retrace) {
        serverHost = host;
        appVersionCode = versionCode;
        isServerReportEnabled = true;

        retraceOn = retrace;
        retraceVerbose = true;

        return instance;
    }
    public CraptionReporter build() {
        //Log.d("Meeeeeee", "CraptionReporter() > build()");
        setUpExceptionHandler();
        if (isServerReportEnabled) {
            //Log.d("Meeeeeee", "CraptionReporter() > build() > report()");
            new Reporter()
                    .listener(() -> {
                        if (ServerHandlerService.handler!=null) {
                            Message message = new Message();
                            message.what = 0;
                            ServerHandlerService.handler.sendMessage(message);
                        }
                    }).report();
            if (!isServiceRunning(getContext(), ServerHandlerService.class)) {
                CraptionUtil.startReportingToServer();
            }
        }
        return instance;
    }

    private static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        String className = serviceClass.getName();
        if (context!=null && context.getSystemService(Context.ACTIVITY_SERVICE) != null) {
            List<ActivityManager.RunningServiceInfo> list =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE);
            if (list != null) {
                for (ActivityManager.RunningServiceInfo serviceInfo : list) {
                    if (className.equals(serviceInfo.service.getClassName()))
                        return true;
                }
            }
        }
        return false;
    }

    private static void setUpExceptionHandler() {
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CraptionReporterExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CraptionReporterExceptionHandler());
        }
    }

    public Context getContext() {
        if (applicationContext == null) {
            try {
                throw new CraptionReporterNotInitializedException("Initialize CraptionReporter : call CraptionReporter.initialize(context, crashReportPath)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return applicationContext;
    }

    public String getCrashReportPath() {
        if (TextUtils.isEmpty(crashReportPath)) {
            return CraptionUtil.getDefaultCrashPath();
        }
        return crashReportPath;
    }
    public String getExceptionReportPath() {
        if (TextUtils.isEmpty(exceptionReportPath)) {
            return CraptionUtil.getDefaultExceptionPath();
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
            return CraptionUtil.getDefaultRetraceMappingFilePath();
        }
        return retraceMappingFilePath;
    }
    public boolean hasRetraceMappingFile() {
        return new File(getRetraceMappingFilePath()).exists();
    }
    public boolean getRetraceVerbose() {
        return retraceVerbose;
    }

    public String getServerHost() {
        return serverHost;
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
        SharedPreferences sh = applicationContext.getSharedPreferences("craptionreporter", Context.MODE_PRIVATE);
        sh.edit().putString("user_identification", identification).apply();
    }
    public String getUserIdentification() {
        if (TextUtils.isEmpty(userIdentification)) {
            SharedPreferences sh = applicationContext.getSharedPreferences("craptionreporter", Context.MODE_PRIVATE);
            return sh.getString("user_identification", "");
        }
        return userIdentification;
    }

    public Intent getLaunchIntent() {
        return new Intent(applicationContext, CraptionReporterActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }


    public static void exception(Exception exception) {
        CraptionUtil.exception(exception);
    }
    public static void exception(Throwable exception, String eventLocation) {
        CraptionUtil.exception(exception, eventLocation);
    }
    public static void log(String log) {
        CraptionUtil.log(log);
    }

}
