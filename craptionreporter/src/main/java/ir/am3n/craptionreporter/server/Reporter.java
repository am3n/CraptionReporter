package ir.am3n.craptionreporter.server;

import android.os.Build;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import ir.am3n.craptionreporter.CraptionReporter;
import ir.am3n.craptionreporter.RetraceOn;
import ir.am3n.craptionreporter.utils.FileUtils;

public class Reporter {

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

    public void report() {
        try {

            // barrasi inke chekar koanm.. static ro bardarm ya ...
            if (!CraptionReporter.getInstance().hasRetraceMappingFile() && CraptionReporter.getInstance().getRetraceOn()==RetraceOn.DEVICE) {
                downloadRetraceMappingFile();
            }

            startReport();

        } catch (Exception e) {
            e.printStackTrace();
            if (listener!=null)
                listener.onStop();
        }
    }


    private void downloadRetraceMappingFile() {
        DownloadMappingFileAsyncTask task = new DownloadMappingFileAsyncTask(() -> {

        });
        task.execute();
    }

    private void startReport() {

        new Thread(() -> {

            try {

                //Log.d("Meeeeeee", "startReport() > Thread() > start()");

                if (!crashDirIsEmpty()) {
                    //Log.d("Meeeeeee", "startReport() > Thread() > 0");

                    JSONObject crashesPack = getCrashesPack(CraptionReporter.getInstance().getUserIdentification());

                    UploadCrashesAsyncTask task = new UploadCrashesAsyncTask(crashesPack, new UploadCrashesAsyncTask.Listener() {
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
                            //doing = false;
                            //stopSelf();
                            //intent.putExtra("doing", false);

                            if (listener!=null)
                                listener.onStop();
                        }
                        @Override
                        public void onError() {
                            //Log.d("Meeeeeee", "startReport() > Thread() > onError");
                            //doing = false;
                            //stopSelf();
                            //intent.putExtra("doing", false);

                            if (listener!=null)
                                listener.onStop();
                        }
                    });
                    task.execute();

                    //return;

                } else {
                    if (listener!=null)
                        listener.onStop();
                }

            } catch (Exception e) {
                e.printStackTrace();

                if (listener!=null)
                    listener.onStop();
            }

            //doing = false;
            //stopSelf();
            //intent.putExtra("doing", false);

        }).start();
    }

    static public boolean crashDirIsEmpty() {
        File crashDir = new File(CraptionReporter.getInstance().getCrashReportPath());
        return crashDir.listFiles().length<=0;
    }

    static public JSONObject getCrashesPack(String user_identification) {

        JSONArray crashes = getCrashes();

        JSONObject result = new JSONObject();
        try {
            result.put("crashes", crashes);
            result.put("user_identification", user_identification);
            result.put("app_version_code", CraptionReporter.getInstance().getAppVersionCode());
            result.put("os_version", Build.VERSION.SDK_INT+" ("+Build.VERSION.RELEASE+")");
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                result.put("cpu", TextUtils.join(", ", Build.SUPPORTED_ABIS));
            } else {
                result.put("cpu", Build.CPU_ABI+", "+Build.CPU_ABI2);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }
    static private JSONArray getCrashes() {
        File crashDir = new File(CraptionReporter.getInstance().getCrashReportPath());
        JSONArray crashes = new JSONArray();
        String stackTrace;
        JSONObject crash;
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
        return crashes;
    }

    public static void clearAll() {
        try {
            FileUtils.deleteFiles(CraptionReporter.getInstance().getCrashReportPath());
            FileUtils.deleteFiles(CraptionReporter.getInstance().getExceptionReportPath());
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

}
