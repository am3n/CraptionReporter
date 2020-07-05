package ir.am3n.craptionreporter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

import ir.am3n.craptionreporter.server.HelloThread;
import ir.am3n.craptionreporter.server.Reporter;
import ir.am3n.craptionreporter.server.ServerHandlerService;
import ir.am3n.craptionreporter.ui.CraptionReporterActivity;
import ir.am3n.craptionreporter.utils.Constants;
import ir.am3n.craptionreporter.utils.CraptionReporterExceptionHandler;
import ir.am3n.craptionreporter.utils.CraptionUtil;
import ir.am3n.craptionreporter.utils.CraptionReporterNotInitializedException;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import static ir.am3n.needtool.ExecHelperKt.onIO;
import static ir.am3n.needtool.SharedPrefHelperKt.sh;
import static java.lang.Thread.sleep;

public class CraptionReporter {

    private static CraptionReporter instance;

    private static Context applicationContext;

    private static String crashReportPath;
    private static String exceptionReportPath;
    private static String logReportPath;
    private static String retraceMappingFilePath;

    private static int logSize = 500; // killobyetes

    private static boolean isNotificationEnabled = true;

    private static Thread reporterThread;
    private static boolean reporterThreadStoped = true;
    private static int reporterCounter = 1;

    private static String serverHost = "";
    private static Map<String, String> serverHeaders;
    private static String appVersionName;
    private static long appVersionCode;
    private static boolean isServerReportEnabled = false;
    private static RetraceOn retraceOn = RetraceOn.NONE;
    private static boolean retraceVerbose = true;

    private static String uid = "";
    private static String userIdentification = "";
    private static String extraInfo = "";

    private static NetworkStateReceiver.State networkState;

    public static CraptionReporter with(Context context) {
        applicationContext = context;
        if (instance == null)
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
    public CraptionReporter setLogSize(int size) {
        logSize = size;
        return this;
    }
    public CraptionReporter enableServer(String host, RetraceOn retrace) {
        return enableServer(host, null, retrace);
    }
    public CraptionReporter enableServer(String host, Map<String, String> headers, RetraceOn retrace) {
        serverHost = host;
        serverHeaders = headers;
        isServerReportEnabled = true;

        retraceOn = retrace;
        retraceVerbose = true;

        return instance;
    }
    public CraptionReporter build() {
        //Log.d("Meeeeeee", "CraptionReporter() > build()");

        try {
            PackageInfo pInfo = applicationContext.getPackageManager().getPackageInfo(applicationContext.getPackageName(), 0);
            appVersionName = pInfo.versionName;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                appVersionCode = pInfo.getLongVersionCode();
            else
                appVersionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {}

        setUpExceptionHandler();

        if (isServerReportEnabled) {
            //Log.d("Meeeeeee", "CraptionReporter() > build() > report()");

            new Reporter().listener(() -> {
                Log.d("Meeee", "send message to ServerHandlerService");
                if (ServerHandlerService.handler != null) {
                    Message message = new Message();
                    message.what = 0;
                    ServerHandlerService.handler.sendMessage(message);
                    Log.d("Meeee", "sent message to ServerHandlerService");
                }
            }).report();

            setUpNetworkReceiver();

        }
        return instance;
    }

    private static long lastHello = 0L;
    private static void hello() {
        if (isServerReportEnabled && (lastHello == 0L || System.currentTimeMillis() - lastHello > 5000)) {
            lastHello = System.currentTimeMillis();
            new HelloThread(new HelloThread.Listener() {
                @Override
                public void onSuccess(String uid) {
                    lastHello = System.currentTimeMillis() + 5000;
                    CraptionReporter.getInstance().setUid(uid);
                    setUpReporterThread();
                }
                @Override
                public void onError() {
                    //onIO(CraptionReporter::hello, 3000);
                }
            }).start();
        }
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
                    networkState = state;
                    //Log.d("Me-CraptionReporter", "NetworkReceiver() > state: "+state.name());
                    if (network!=null) {
                        //Log.d("Me-CraptionReporter", "NetworkReceiver() > network: "+network.toString());
                        if (state == NetworkStateReceiver.State.AVAILABLE) {
                            reporterCounter = 1;
                            onIO(CraptionReporter::hello, 3000);
                        }
                    }
                }
                @Override
                public void onChangedOnLowApi(@NotNull NetworkStateReceiver.State state) {
                    //Log.d("Me-CraptionReporter", "NetworkReceiver() > state: "+state.name());
                    if (state == NetworkStateReceiver.State.AVAILABLE) {
                        reporterCounter = 1;
                        onIO(CraptionReporter::hello, 3000);
                    }
                }
                @Override
                public void onCapabilitiesChanged(@NotNull Network network, @NotNull NetworkCapabilities networkCapabilities) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized private static void setUpReporterThread() {
        try {
            if (isServerReportEnabled) {
                if (reporterThread == null || reporterThreadStoped) {
                    try {
                        if (reporterThread!=null)
                            reporterThread.stop();
                    } catch (Throwable ignore) {}
                    reporterThread = new Thread(() -> {
                        while (networkState == NetworkStateReceiver.State.AVAILABLE) {
                            reporterThreadStoped = false;
                            new Reporter().listener(null).report();
                            try {
                                sleep(((reporterCounter/5)+1) * 10 * 1000);
                                if (reporterCounter < 30)
                                    reporterCounter++;
                            } catch (Throwable ignore) {}
                        }
                        reporterThreadStoped = true;
                    });
                    reporterThread.start();
                } else {
                    reporterThread.interrupt();
                }
            } else {
                if (reporterThread!=null)
                    reporterThread.stop();
            }
        } catch (Throwable t) {
            t.printStackTrace();
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
    public long getAppVersionCode() {
        return appVersionCode;
    }
    public String getAppVersionName() {
        return appVersionName;
    }
    public String getAppVersion() {
        return appVersionCode+" ("+appVersionName+")";
    }
    public String getMappingFileUrl() {
        return Constants.SERVER_MAPPINGS_FILES_DIR+"mapping-"+appVersionCode+".txt";
    }
    public String getHelloUrl() {
        return Constants.SERVER_HELLO;
    }
    public String getRepoterUrl() {
        return Constants.SERVER_REPORTER;
    }


    public void setUid(String unique) {
        uid = unique;
        sh(applicationContext, "craptionreporter")
            .edit()
            .putString("uid", uid)
            .apply();
    }
    public String getUid() {
        if (TextUtils.isEmpty(uid))
            uid = sh(applicationContext, "craptionreporter").getString("uid", "");
        return uid;
    }

    public void setUserIdentification(String identification) {
        userIdentification = identification;
        sh(applicationContext, "craptionreporter")
            .edit()
            .putString("user_identification", identification)
            .apply();
    }
    public String getUserIdentification() {
        if (TextUtils.isEmpty(userIdentification))
            userIdentification = sh(applicationContext, "craptionreporter").getString("user_identification", "");
        return userIdentification;
    }

    public void setExtraInfo(String extra) {
        extraInfo = extra;
        sh(applicationContext, "craptionreporter")
            .edit()
            .putString("extra_info", extra)
            .apply();
    }
    public String getExtraInfo() {
        if (TextUtils.isEmpty(extraInfo))
            extraInfo = sh(applicationContext, "craptionreporter").getString("extra_info", "");
        return extraInfo;
    }


    public Intent getLaunchIntent() {
        return new Intent(applicationContext, CraptionReporterActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public static void crash() {
        throw new RuntimeException("Craption Reporter Crash");
    }
    public static void exception(Exception exception) {
        exception(exception, null);
    }
    public static void exception(Throwable exception, String eventLocation) {
        CraptionUtil.exception(exception, eventLocation);
        hello();
    }
    public static void log(String log) {
        CraptionUtil.log(log);
        hello();
    }

    public static void clearLogs() {
        CraptionUtil.clearLogs();
    }

}
