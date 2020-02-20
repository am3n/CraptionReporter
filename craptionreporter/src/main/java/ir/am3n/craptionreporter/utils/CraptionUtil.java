package ir.am3n.craptionreporter.utils;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ir.am3n.craptionreporter.BuildConfig;
import ir.am3n.craptionreporter.CraptionReporter;
import ir.am3n.craptionreporter.R;
import ir.am3n.craptionreporter.RetraceOn;
import ir.am3n.craptionreporter.proguard.retrace.Retrace;
import ir.am3n.craptionreporter.server.ServerHandlerService;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;

public class CraptionUtil {

    private CraptionUtil() {
        //this class is not publicly instantiable
    }

    private static String getCrashLogTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.ENGLISH);
        return dateFormat.format(new Date());
    }

    static void crash(Throwable throwable) {
        String crashReportPath = CraptionReporter.getInstance().getCrashReportPath();
        String filename = getCrashLogTime() + Constants.CRASH_SUFFIX + Constants.FILE_EXTENSION;
        writeReportToFile(crashReportPath, filename, getStackTrace(throwable, null));
        showNotification(throwable.getLocalizedMessage(), true);
    }

    public static void exception(Throwable exception) {
        exception(exception, null);
    }
    synchronized public static void exception(Throwable exception, String eventLocation) {
        new Thread(() -> {
            Log.d("CraptionReporter", "exception() > el: "+eventLocation);
            String exceptionReportPath = CraptionReporter.getInstance().getExceptionReportPath();
            final String filename = getCrashLogTime() + Constants.EXCEPTION_SUFFIX + Constants.FILE_EXTENSION;
            Log.d("CraptionReporter", "exception() > filename: "+filename);
            writeReportToFile(exceptionReportPath, filename, getStackTrace(exception, eventLocation));
            Log.d("CraptionReporter", "exception() > writed");
            showNotification(exception.getLocalizedMessage(), false);
        }).start();
    }

    // todo add a parametr (APPEND:'append main file' or NEW:'create file same crashes and exceptiones')
    public static void log(final String log) {
        new Thread(() -> {
            String logReportPath = CraptionReporter.getInstance().getLogReportPath();
            writeLogToFile(logReportPath, getCrashLogTime()+"  -  "+log+"\n");
        }).start();
    }

    private static void writeReportToFile(String reportPath, String filename, String stacktrace) {

        File crashDir = new File(reportPath);

        String[] list = crashDir.list();
        // todo enable user to set max crash or exception or log files
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


        String logReportPath = CraptionReporter.getInstance().getLogReportPath();
        if (TextUtils.isEmpty(logReportPath)) {
            logReportPath = getDefaultLogPath();
        }
        File logDir = new File(logReportPath);
        if (!logDir.exists() || !logDir.isDirectory()) {
            logReportPath = getDefaultLogPath();
        }
        File logFile = new File(logReportPath + File.separator + "logReports.txt");
        logFile.renameTo(new File(logReportPath + File.separator + "log_" + filename));

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

            long file_size_kilobyte = file.length() / 1024; // kilobytes
            if (file_size_kilobyte > CraptionReporter.getInstance().getLogSize()) {
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

    public static void clearLogs() {
        String logReportPath = CraptionReporter.getInstance().getLogReportPath();
        if (TextUtils.isEmpty(logReportPath)) {
            logReportPath = getDefaultLogPath();
        }
        File logDir = new File(logReportPath);
        if (!logDir.exists() || !logDir.isDirectory()) {
            logReportPath = getDefaultLogPath();
        }
        File logFile = new File(logReportPath + File.separator + "logReports.txt");
        try {
            logFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startReportingToServer() {
        //Log.d("Meeeeeee", "CraptionUtil() > startReportingToServer()");
        if (CraptionReporter.getInstance().isServerReportEnabled()) {
            //Log.d("Meeeeeee", "CraptionUtil() > startReportingToServer() > ServerReportEnabled");
            Context context = CraptionReporter.getInstance().getContext();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                AlarmManager alarmMgr = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                if (alarmMgr == null) return;
                Intent i = new Intent(context, ServerHandlerService.class);
                PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, 0);
                alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10, pendingIntent);
            } else  {
                Intent intent = new Intent(context, ServerHandlerService.class);
                context.startService(intent);
            }
        }
    }

    private static void showNotification(String localisedMsg, boolean isCrash) {

        // todo need channel for new android versions

        if (CraptionReporter.getInstance().isNotificationEnabled()) {
            Context context = CraptionReporter.getInstance().getContext();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.ic_warning_black_24dp);

            Intent intent = CraptionReporter.getInstance().getLaunchIntent();
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
            builder.setColor(ContextCompat.getColor(context, R.color.colorAccent_CraptionReporter));

            NotificationManager notificationManager = (NotificationManager) context.
                    getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(Constants.NOTIFICATION_ID, builder.build());
        }
    }

    private static String getStackTrace(Throwable e, String eventLocation) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        e.printStackTrace(printWriter);
        String crashLog = result.toString();
        printWriter.close();

        if (!BuildConfig.DEBUG) {
            if (CraptionReporter.getInstance().getRetraceOn()==RetraceOn.DEVICE) {
                String retraceMappingFilePath = CraptionReporter.getInstance().getRetraceMappingFilePath();
                if (TextUtils.isEmpty(retraceMappingFilePath)) {
                    retraceMappingFilePath = getDefaultRetraceMappingFilePath();
                }
                File mappingFile = new File(retraceMappingFilePath);
                if (mappingFile.exists()) {
                    String regularExpression = Retrace.STACK_TRACE_EXPRESSION;
                    boolean verbose = CraptionReporter.getInstance().getRetraceVerbose();
                    Retrace retrace = new Retrace(mappingFile, crashLog, regularExpression, verbose);
                    crashLog = retrace.retrace();
                }
                if (eventLocation != null)
                    crashLog = "at: "+eventLocation+"\r\n"+crashLog;
            } else if (CraptionReporter.getInstance().getRetraceOn()==RetraceOn.SERVER) {
                if (eventLocation != null)
                    crashLog = "retrace:"+"\r\n"+"at: "+eventLocation+"\r\n"+crashLog;
                else
                    crashLog = "retrace:"+"\r\n"+crashLog;
            }
        } else
            if (eventLocation != null)
                crashLog = "at: "+eventLocation+"\r\n"+crashLog;

        return crashLog;
    }

    private static String getDefaultCraptionReporterPath() {
        File storage = CraptionReporter.getInstance().getContext().getFilesDir();
        if (storage == null)
            storage = CraptionReporter.getInstance().getContext().getExternalFilesDir(null);
        if (storage == null)
            storage = Environment.getExternalStorageDirectory();

        String defaultPath = storage.getAbsolutePath() + File.separator + Constants.MAIN_DIR;
        File file = new File(defaultPath);
        file.mkdirs();
        return defaultPath;
    }
    public static String getDefaultCrashPath() {
        String defaultPath = getDefaultCraptionReporterPath() + File.separator + Constants.CRASH_REPORT_DIR;
        File file = new File(defaultPath);
        file.mkdirs();
        return defaultPath;
    }
    public static String getDefaultExceptionPath() {
        String defaultPath = getDefaultCraptionReporterPath() + File.separator + Constants.EXCEPTION_REPORT_DIR;
        File file = new File(defaultPath);
        file.mkdirs();
        return defaultPath;
    }
    public static String getDefaultLogPath() {
        String defaultPath = getDefaultCraptionReporterPath() + File.separator + Constants.LOG_REPORT_DIR;
        File file = new File(defaultPath);
        file.mkdirs();
        return defaultPath;
    }
    public static String getDefaultRetraceMappingFilePath() {
        return getDefaultCraptionReporterPath() + File.separator + Constants.DEFAULT_RETRACE_MAPPING_FILE_NAME;
    }
}
