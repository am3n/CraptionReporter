package ir.am3n.craptionreporter.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ir.am3n.craptionreporter.BuildConfig;
import ir.am3n.craptionreporter.CrashReporter;
import ir.am3n.craptionreporter.R;
import ir.am3n.craptionreporter.RetraceOn;
import ir.am3n.craptionreporter.proguard.retrace.Retrace;
import ir.am3n.craptionreporter.server.ServerHandlerService;

import static android.content.Context.NOTIFICATION_SERVICE;

public class CrashUtil {

    private CrashUtil() {
        //this class is not publicly instantiable
    }

    private static String getCrashLogTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        return dateFormat.format(new Date());
    }

    static void crash(Throwable throwable) {
        new Thread(() -> {
            String crashReportPath = CrashReporter.getInstance().getCrashReportPath();
            String filename = getCrashLogTime() + Constants.CRASH_SUFFIX + Constants.FILE_EXTENSION;
            writeReportToFile(crashReportPath, filename, getStackTrace(throwable));
            showNotification(throwable.getLocalizedMessage(), true);
        }).start();
    }

    public static void exception(Throwable exception) {
        new Thread(() -> {
            String exceptionReportPath = CrashReporter.getInstance().getExceptionReportPath();
            final String filename = getCrashLogTime() + Constants.EXCEPTION_SUFFIX + Constants.FILE_EXTENSION;
            writeReportToFile(exceptionReportPath, filename, getStackTrace(exception));
            showNotification(exception.getLocalizedMessage(), false);
        }).start();
    }
    public static void exception(Throwable exception, String eventLocation) {
        new Thread(() -> {
            String exceptionReportPath = CrashReporter.getInstance().getExceptionReportPath();
            final String filename = getCrashLogTime() + Constants.EXCEPTION_SUFFIX + Constants.FILE_EXTENSION;
            writeReportToFile(exceptionReportPath, filename, eventLocation+"\n"+getStackTrace(exception));
            showNotification(exception.getLocalizedMessage(), false);
        }).start();
    }

    public static void log(final String logReport) {
        new Thread(() -> {
            String logReportPath = CrashReporter.getInstance().getLogReportPath();
            String log = getCrashLogTime() + "  -  " + logReport + "\n";
            writeLogToFile(logReportPath, log);
        }).start();
    }

    private static void writeReportToFile(String reportPath, String filename, String stacktrace) {

        File crashDir = new File(reportPath);

        String[] list = crashDir.list();
        if (list.length>100) {
            for (int i=50; i<list.length; i++) {
                //new File(crashDir, list[i]).delete();
                FileUtils.delete(crashDir+list[i]);
            }
        }

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(reportPath + File.separator + filename));
            bufferedWriter.write(stacktrace);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void writeLogToFile(String logReportPath, String log) {

        if (TextUtils.isEmpty(logReportPath)) {
            logReportPath = getDefaultLogPath();
        }

        File logDir = new File(logReportPath);
        if (!logDir.exists() || !logDir.isDirectory()) {
            logReportPath = getDefaultLogPath();
        }

        String filename = "logReports.txt";
        BufferedWriter bufferedWriter;
        try {

            File file = new File(logReportPath + File.separator + filename);
            if (!file.exists())
                file.createNewFile();

            int file_size_kilobyte = Integer.parseInt(String.valueOf(file.length()/5120));
            if (file_size_kilobyte>5120) {
                file.delete();
                file.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(logReportPath + File.separator + filename, true);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(log);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static void startReportingToServer() {
        //Log.d("Meeeeeee", "CrashUtil() > startReportingToServer()");
        if (CrashReporter.getInstance().isServerReportEnabled()) {
            //Log.d("Meeeeeee", "CrashUtil() > startReportingToServer() > ServerReportEnabled");
            Context context = CrashReporter.getInstance().getContext();
            Intent intent = new Intent(context, ServerHandlerService.class);
            context.startService(intent);
        }
    }

    private static void showNotification(String localisedMsg, boolean isCrash) {

        if (CrashReporter.getInstance().isNotificationEnabled()) {
            Context context = CrashReporter.getInstance().getContext();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.ic_warning_black_24dp);

            Intent intent = CrashReporter.getInstance().getLaunchIntent();
            intent.putExtra(Constants.LANDING, isCrash);
            intent.setAction(Long.toString(System.currentTimeMillis()));

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            builder.setContentIntent(pendingIntent);

            builder.setContentTitle(context.getString(R.string.view_crash_report));

            if (TextUtils.isEmpty(localisedMsg)) {
                builder.setContentText(context.getString(R.string.check_your_message_here));
            } else {
                builder.setContentText(localisedMsg);
            }

            builder.setAutoCancel(true);
            builder.setColor(ContextCompat.getColor(context, R.color.colorAccent_CrashReporter));

            NotificationManager notificationManager = (NotificationManager) context.
                    getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(Constants.NOTIFICATION_ID, builder.build());
        }
    }

    private static String getStackTrace(Throwable e) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        e.printStackTrace(printWriter);
        String crashLog = result.toString();
        printWriter.close();

        if (!BuildConfig.DEBUG) {
            if (CrashReporter.getInstance().getRetraceOn()==RetraceOn.DEVICE) {
                String retraceMappingFilePath = CrashReporter.getInstance().getRetraceMappingFilePath();
                if (TextUtils.isEmpty(retraceMappingFilePath)) {
                    retraceMappingFilePath = getDefaultRetraceMappingFilePath();
                }
                File mappingFile = new File(retraceMappingFilePath);
                if (mappingFile.exists()) {
                    String regularExpression = Retrace.STACK_TRACE_EXPRESSION;
                    boolean verbose = CrashReporter.getInstance().getRetraceVerbose();
                    Retrace retrace = new Retrace(mappingFile, crashLog, regularExpression, verbose);
                    crashLog = retrace.retrace();
                }
            } else if (CrashReporter.getInstance().getRetraceOn()==RetraceOn.SERVER) {
                crashLog = "retrace:"+crashLog;
            }
        }

        return crashLog;
    }

    private static String getDefaultCrashReporterPath() {
        String defaultPath = CrashReporter.getInstance().getContext().getExternalFilesDir(null).getAbsolutePath()
                + File.separator + Constants.MAIN_DIR;
        File file = new File(defaultPath);
        file.mkdirs();
        return defaultPath;
    }
    public static String getDefaultCrashPath() {
        String defaultPath = getDefaultCrashReporterPath() + File.separator + Constants.CRASH_REPORT_DIR;
        File file = new File(defaultPath);
        file.mkdirs();
        return defaultPath;
    }
    public static String getDefaultExceptionPath() {
        String defaultPath = getDefaultCrashReporterPath() + File.separator + Constants.EXCEPTION_REPORT_DIR;
        File file = new File(defaultPath);
        file.mkdirs();
        return defaultPath;
    }
    public static String getDefaultLogPath() {
        String defaultPath = getDefaultCrashReporterPath() + File.separator + Constants.LOG_REPORT_DIR;
        File file = new File(defaultPath);
        file.mkdirs();
        return defaultPath;
    }
    public static String getDefaultRetraceMappingFilePath() {
        String defaultPath = getDefaultCrashReporterPath() + File.separator + Constants.DEFAULT_RETRACE_MAPPING_FILE_NAME;
        return defaultPath;
    }
}
