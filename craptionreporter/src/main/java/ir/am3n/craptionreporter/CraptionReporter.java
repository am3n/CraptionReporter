package ir.am3n.craptionreporter;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Network;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;
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

    private static int logSize = 500; // killobyetes

    private static boolean isNotificationEnabled = true;

    private static String serverHost = "";
    private static Map<String, String> serverHeaders;
    private static Integer appVersionCode;
    private static boolean isServerReportEnabled = false;
    private static RetraceOn retraceOn = RetraceOn.NONE;
    private static boolean retraceVerbose = true;

    private static String userIdentification;
    private static String extraInfo;

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
    public CraptionReporter setLogSize(int size) {
        logSize = size;
        return this;
    }
    public CraptionReporter enableServer(String host, int versionCode, RetraceOn retrace) {
        return enableServer(host, null, versionCode, retrace);
    }
    public CraptionReporter enableServer(String host, Map<String, String> headers, int versionCode, RetraceOn retrace) {
        serverHost = host;
        serverHeaders = headers;
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
                // start a service restart when app ended, serive runs and reports all
                CraptionUtil.startReportingToServer();
            }
            setUpNetworkReceiver();
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

    private static void setUpNetworkReceiver() {
        try {
            new NetworkStateReceiver(applicationContext, null, new NetworkStateReceiver.Listener() {
                @Override
                public void onChanged(@NotNull NetworkStateReceiver.State state, @Nullable Network network) {
                    Log.d("Me-CraptionReporter", "NetworkReceiver() > state: "+state.name());
                    if (network!=null)
                        Log.d("Me-CraptionReporter", "NetworkReceiver() > network: "+network.toString());
                    if (state == NetworkStateReceiver.State.AVAILABLE) {
                        new Reporter().report();
                    }
                }
                @Override
                public void onChangedOnLowApi(@NotNull NetworkStateReceiver.State state) {
                    Log.d("Me-CraptionReporter", "NetworkReceiver() > state: "+state.name());
                    if (state == NetworkStateReceiver.State.AVAILABLE) {
                        new Reporter().report();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
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
        if (TextUtils.isEmpty(logReportPath)) {
            return CraptionUtil.getDefaultLogPath();
        }
        return logReportPath;
    }

    public int getLogSize() {
        return logSize;
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
    public Map<String, String> getServerHeaders() {
        return serverHeaders;
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


    public void setExtraInfo(String extra) {
        extraInfo = extra;
        SharedPreferences sh = applicationContext.getSharedPreferences("craptionreporter", Context.MODE_PRIVATE);
        sh.edit().putString("extra_info", extraInfo).apply();
    }
    public String getExtraInfo() {
        if (TextUtils.isEmpty(extraInfo)) {
            SharedPreferences sh = applicationContext.getSharedPreferences("craptionreporter", Context.MODE_PRIVATE);
            return sh.getString("extra_info", "");
        }
        return extraInfo;
    }


    public Intent getLaunchIntent() {
        return new Intent(applicationContext, CraptionReporterActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }


    public static void crash() {
        throw new RuntimeException("Craption Reporter Crash");
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

    public static void clearLogs() {
        CraptionUtil.clearLogs();
    }

}
