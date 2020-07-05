package ir.am3n.craptionreporter.server;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import ir.am3n.craptionreporter.CraptionReporter;
import ir.am3n.craptionreporter.RetraceOn;
import ir.am3n.craptionreporter.utils.FileUtils;

import static ir.am3n.needtool.ContextHelperKt.isDebug;

public class Reporter {

    static private boolean reporting = false;

    public interface Listener {
        void onStop();
    }
    private Listener listener;

    public Reporter() {

    }

    public Reporter listener(Listener listener) {
        this.listener = listener;
        return this;
    }

    synchronized public void report() {
        try {

            if (reporting)
                return;
            reporting = true;

            Log.d("Meeeeeee", "Reporter > start()");

            // barrasi inke chekar koanm.. static ro bardarm ya ...
            if (!CraptionReporter.getInstance().hasRetraceMappingFile() && CraptionReporter.getInstance().getRetraceOn()==RetraceOn.DEVICE) {
                downloadRetraceMappingFile();
            }

            startReport();

        } catch (Exception e) {
            reporting = false;
            e.printStackTrace();
            new Thread(() -> {
                if (listener!=null)
                    listener.onStop();
            }).start();
        }
    }


    private void downloadRetraceMappingFile() {
        DownloadMappingFileAsyncTask task = new DownloadMappingFileAsyncTask(() -> {

        });
        task.execute();
    }

    private void startReport() {

        new Thread(() -> {

            Log.d("Meeeeeee", "Reporter > start() > startReport()");

            try {

                //Log.d("Meeeeeee", "startReport() > Thread() > start()");

                if (!crashDirIsEmpty() || !exceptionDirIsEmpty() || !logDirIsEmpty()) {
                    //Log.d("Meeeeeee", "startReport() > Thread() > 0");
                    Log.d("Meeeeeee", "Reporter > start() > startReport() > there is crash or excp.. sending...");

                    JSONObject crashesPack = getCrashExceptionLogsPack();
                    Log.d("Me-Reporter", crashesPack.toString());

                    ReportThread task = new ReportThread(crashesPack, new ReportThread.Listener() {
                        @Override
                        public void onUploaded(JSONArray response) {
                            //Log.d("Meeeeeee", "startReport() > Thread() > onUploaded");
                            for (int i=0; i<response.length(); i++) {
                                try {
                                    clear(response.get(i).toString());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            try { clearOldLogFiles(); } catch (Throwable t) {}
                            reporting = false;
                            Log.d("Meeeeeee", "Reporter > start() > startReport() > sent");
                            //doing = false;
                            //stopSelf();
                            //intent.putExtra("doing", false);

                            new Thread(() -> {
                                if (listener!=null)
                                    listener.onStop();
                            }).start();
                        }
                        @Override
                        public void onError() {
                            //Log.d("Meeeeeee", "startReport() > Thread() > onError");
                            //doing = false;
                            //stopSelf();
                            //intent.putExtra("doing", false);
                            reporting = false;
                            new Thread(() -> {
                                if (listener!=null)
                                    listener.onStop();
                            }).start();
                        }
                    });
                    task.start();

                    //return;

                } else {
                    reporting = false;
                    if (listener!=null)
                        listener.onStop();
                    CraptionReporter.clearLogs();
                }

            } catch (Throwable e) {
                reporting = false;
                e.printStackTrace();
                if (listener!=null)
                    listener.onStop();
            }

            //doing = false;
            //stopSelf();
            //intent.putExtra("doing", false);

        }).start();
    }

    public static boolean crashDirIsEmpty() {
        File crashDir = new File(CraptionReporter.getInstance().getCrashReportPath());
        if (crashDir.listFiles() != null)
            return crashDir.listFiles().length <= 0;
        return true;
    }

    public static boolean exceptionDirIsEmpty() {
        File exceptionDir = new File(CraptionReporter.getInstance().getExceptionReportPath());
        if (exceptionDir.listFiles() != null)
            return exceptionDir.listFiles().length<=0;
        return true;
    }

    public static boolean logDirIsEmpty() {
        File logDir = new File(CraptionReporter.getInstance().getLogReportPath());
        if (logDir.listFiles() != null) {
            File[] logFiles = logDir.listFiles();
            int count = logFiles.length;
            if (count == 0)
                return true;
            else if (count == 1)
                return FileUtils.readFromFile(logFiles[0]).length() <= 0;
            else
                return false;
        }
        return true;
    }

    public static JSONObject getCrashExceptionLogsPack() {
        JSONObject result = new JSONObject();
        try {
            result.put("uid", CraptionReporter.getInstance().getUid());
            result.put("debug", isDebug(CraptionReporter.getInstance().getContext()));
            result.put("crashes", getCrashes());
            result.put("exceptions", getExceptions());
            result.put("logs", getLogs());
            result.put("appVersionCode", CraptionReporter.getInstance().getAppVersionCode());
        } catch (Throwable e) {}
        return result;
    }

    public static JSONArray getCrashes() {

        File crashDir = new File(CraptionReporter.getInstance().getCrashReportPath());
        JSONArray crashes = new JSONArray();
        String stackTrace;
        JSONObject crash;
        if (crashDir.listFiles() != null) {
            for (File crashFile : crashDir.listFiles()) {
                stackTrace = FileUtils.readFromFile(crashFile);
                crash = new JSONObject();
                try {
                    crash.put("file_name", crashFile.getName());
                    crash.put("stack_trace", stackTrace);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                crashes.put(crash);
            }
        }
        return crashes;
    }

    public static JSONArray getExceptions() {
        File exceptionDir = new File(CraptionReporter.getInstance().getExceptionReportPath());
        JSONArray exceptions = new JSONArray();
        String stackTrace;
        JSONObject exception;
        if (exceptionDir.listFiles() != null) {
            for (File crashFile : exceptionDir.listFiles()) {
                stackTrace = FileUtils.readFromFile(crashFile);
                exception = new JSONObject();
                try {
                    exception.put("file_name", crashFile.getName());
                    exception.put("stack_trace", stackTrace);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                exceptions.put(exception);
            }
        }
        return exceptions;
    }

    public static String getLogs() {
        String logs = "";

        File logDir = new File(CraptionReporter.getInstance().getLogReportPath());
        File[] oldLogFiles = logDir.listFiles();

        File logFile = new File(CraptionReporter.getInstance().getLogReportPath()+File.separator+"logReports.txt");
        if (logFile.exists()) {
            logs += FileUtils.readFromFile(logFile);
            if (logs.length() > 0)
                logFile.renameTo(new File(CraptionReporter.getInstance().getLogReportPath()+File.separator+"logReports-"+System.currentTimeMillis()+".txt"));
        }

        if (oldLogFiles != null && oldLogFiles.length > 0)
            for (File oldLogFile : oldLogFiles) {
                if (oldLogFile.getName() != "logReports.txt" && oldLogFile.isFile())
                    logs += FileUtils.readFromFile(oldLogFile);
            }

        return logs;
    }


    public static void clearAll() {
        try {
            FileUtils.deleteFiles(CraptionReporter.getInstance().getCrashReportPath());
            FileUtils.deleteFiles(CraptionReporter.getInstance().getExceptionReportPath());
            FileUtils.deleteFiles(CraptionReporter.getInstance().getLogReportPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clear(String crashFileName) {
        try {
            FileUtils.delete(CraptionReporter.getInstance().getCrashReportPath()+File.separator+crashFileName);
            FileUtils.delete(CraptionReporter.getInstance().getExceptionReportPath()+File.separator+crashFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearOldLogFiles() {
        try {
            File logDir = new File(CraptionReporter.getInstance().getLogReportPath());
            File[] oldLogFiles = logDir.listFiles();
            if (oldLogFiles != null && oldLogFiles.length > 0)
                for (File oldLogFile : oldLogFiles) {
                    if (oldLogFile.getName() != "logReports.txt" && oldLogFile.isFile())
                        oldLogFile.delete();
                }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
