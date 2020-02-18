package ir.am3n.craptionreporter.server;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.jaredrummler.android.device.DeviceName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Locale;

import ir.am3n.craptionreporter.CraptionReporter;
import ir.am3n.craptionreporter.PermissionRequest;
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

            Log.d("Meeeeeee", "Reporter > start()");

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

            Log.d("Meeeeeee", "Reporter > start() > startReport()");

            try {

                //Log.d("Meeeeeee", "startReport() > Thread() > start()");

                if (!crashDirIsEmpty() || !exceptionDirIsEmpty()) {
                    //Log.d("Meeeeeee", "startReport() > Thread() > 0");
                    Log.d("Meeeeeee", "Reporter > start() > startReport() > there is crash or excp.. sending...");

                    JSONObject crashesPack = getCrashExceptionsPack(CraptionReporter.getInstance().getUserIdentification());

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
                            Log.d("Meeeeeee", "Reporter > start() > startReport() > sent");
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
                    CraptionReporter.clearLogs();
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

    public static JSONObject getCrashExceptionsPack(String user_identification) {

        JSONArray crashes = getCrashes();

        JSONArray exceptions = getExceptions();

        JSONObject result = new JSONObject();
        try {
            result.put("crashes", crashes);
            result.put("exceptions", exceptions);
            result.put("user_identification", user_identification);
            result.put("app_version_code", CraptionReporter.getInstance().getAppVersionCode());
            result.put("os_version", Build.VERSION.SDK_INT+" ("+Build.VERSION.RELEASE+")");

            try {

                Context context = CraptionReporter.getInstance().getContext();

                String device_imei = "";
                try {
                    if (PermissionRequest.haveTelephone(context)) {
                        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        @SuppressLint({"MissingPermission", "HardwareIds"})
                        String imei = telephonyManager != null ? telephonyManager.getDeviceId() : "";
                        if (imei != null) {
                            device_imei = imei;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                result.put("device_imei", device_imei);


                String device_model = Build.BRAND;
                try {
                    device_model += " : " + DeviceName.getDeviceName();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                result.put("device_model", device_model);


                String device_screenclass = "Unknown";
                try {
                    if ((context.getResources().getConfiguration().screenLayout &
                            Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
                        device_screenclass = "Large";
                    } else if ((context.getResources().getConfiguration().screenLayout &
                            Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
                        device_screenclass = "Normal";
                    } else if ((context.getResources().getConfiguration().screenLayout &
                            Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
                        device_screenclass = "Small";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                result.put("device_screenclass", device_screenclass);


                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                float density = metrics.density;
                String device_dpiclass = "Unknown";
                try {
                    if (density <= 0.75f) {
                        device_dpiclass = "ldpi";
                    } else if (density <= 1.0f) {
                        device_dpiclass = "mdpi";
                    } else if (density <= 1.5f) {
                        device_dpiclass = "hdpi";
                    } else if (density <= 2.0f) {
                        device_dpiclass = "xhdpi";
                    } else if (density <= 3.0f) {
                        device_dpiclass = "xxhdpi";
                    } else if (density <= 4.0f) {
                        device_dpiclass = "xxxhdpi";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                result.put("device_dpiclass", device_dpiclass);


                String device_screensize = "";
                String device_screen_dimensions_dpis = "";
                String device_screen_dimensions_pixels = "";
                try {
                    int orientation = context.getResources().getConfiguration().orientation;
                    WindowManager wm = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
                    Display display = wm.getDefaultDisplay();
                    Point screenSize = new Point();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        display.getRealSize(screenSize);
                    } else {
                        display.getSize(screenSize);
                    }
                    int screenSize_x = screenSize.x;
                    int screenSize_y = screenSize.y;
                    int width = (orientation == Configuration.ORIENTATION_PORTRAIT ? screenSize_x : screenSize_y);
                    int height = (orientation == Configuration.ORIENTATION_PORTRAIT ? screenSize_y : screenSize_x);

                    double wi = (double) width / (double) metrics.xdpi;
                    double hi = (double) height / (double) metrics.ydpi;
                    double x = Math.pow(wi, 2);
                    double y = Math.pow(hi, 2);
                    double screenInches = Math.sqrt(x + y);
                    device_screensize = String.format(Locale.US, "%.2f", screenInches);


                    device_screen_dimensions_dpis = (int) (width / density) + " x " + (int) (height / density);


                    device_screen_dimensions_pixels = width + " x " + height;


                } catch (Exception e) {
                    e.printStackTrace();
                }
                result.put("device_screensize", device_screensize);
                result.put("device_screen_dimensions_dpis", device_screen_dimensions_dpis);
                result.put("device_screen_dimensions_pixels", device_screen_dimensions_pixels);

            } catch (Exception e) {
                e.printStackTrace();
            }

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

    public static JSONArray getCrashes() {

        File crashDir = new File(CraptionReporter.getInstance().getCrashReportPath());
        File logDir = new File(CraptionReporter.getInstance().getLogReportPath());

        JSONArray crashes = new JSONArray();
        String stackTrace;
        String logOfCrash = "";
        JSONObject crash;
        if (crashDir.listFiles() != null) {
            for (File crashFile : crashDir.listFiles()) {
                stackTrace = FileUtils.readFromFile(crashFile);
                File logFile = new File(logDir.getAbsolutePath()+File.separator+"log_"+crashFile.getName());
                if (logFile.exists())
                    logOfCrash = FileUtils.readFromFile(logFile);
                crash = new JSONObject();
                try {
                    crash.put("file_name", crashFile.getName());
                    crash.put("stack_trace", stackTrace);
                    crash.put("logs", logOfCrash);
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
