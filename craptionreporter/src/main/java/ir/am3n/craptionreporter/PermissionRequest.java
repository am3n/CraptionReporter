package ir.am3n.craptionreporter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class PermissionRequest {

    public static final int RQST_STORAGE = 1000;
    public static final int RQST_LOCATION = 1001;
    public static final int RQST_TELEPHONE = 1002;
    public static final int RQST_WRITESETTINGS = 1003;
    public static final int RQST_RECORD_AUDIO = 1004;
    public static final int RQST_RECEIVE_SMS = 1005;
    public static final int RQST_CALLPHONE = 1006;
    public static final int RQST_CAMERA = 1007;


    public static boolean haveStorage(Context context) {
        return ContextCompat.checkSelfPermission(context
                , Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context
                        , Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;
    }
    public static void requestForStorage(Fragment context) {
        context.requestPermissions(
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                PermissionRequest.RQST_STORAGE);
    }
    public static void requestForStorage(Activity context) {
        ActivityCompat.requestPermissions(context
                , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}
                , PermissionRequest.RQST_STORAGE);
    }
    public static void requestForStorage(Activity context, int request) {
        ActivityCompat.requestPermissions(context
                , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, request);
    }




    public static boolean haveLocation(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                ==PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                ==PackageManager.PERMISSION_GRANTED;
    }
    public static void requestForLocation(Activity context) {
        ActivityCompat.requestPermissions(context, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, PermissionRequest.RQST_LOCATION);
    }




    public static boolean haveTelephone(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE)==PackageManager.PERMISSION_GRANTED;
    }
    public static void requestForTelephone(Activity context) {
        ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_PHONE_STATE}
                , PermissionRequest.RQST_TELEPHONE);
    }
    public static void requestForTelephone(Activity context, int request) {
        ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_PHONE_STATE}, request);
    }




    public static boolean haveWriteSettings(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(context);
        }
        return true;
    }
    public static void requestForWriteSettings(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(activity)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS
                        , Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, RQST_WRITESETTINGS);
            }
        }
    }





    public static boolean haveRecordAudio(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                ==PackageManager.PERMISSION_GRANTED;
    }
    public static void requestForRecordAudio(Activity context) {
        ActivityCompat.requestPermissions(context, new String[]{
                Manifest.permission.RECORD_AUDIO}, PermissionRequest.RQST_RECORD_AUDIO);
    }




    // -------------- RECEIVE_SMS ------------------------------------------------------------------
    public static boolean haveReceiveSms(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS)
                ==PackageManager.PERMISSION_GRANTED;
    }
    public static void requestForReceiveSms(Activity context) {
        ActivityCompat.requestPermissions(context, new String[]{
                        Manifest.permission.RECEIVE_SMS}
                , PermissionRequest.RQST_RECEIVE_SMS);
    }
    public static void requestForReceiveSms(Activity context, int request_code) {
        ActivityCompat.requestPermissions(context, new String[]{
                        Manifest.permission.RECEIVE_SMS}
                , request_code);
    }




    public static boolean haveCallPhone(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                ==PackageManager.PERMISSION_GRANTED;
    }
    public static void requestForCallPhone(Activity context) {
        ActivityCompat.requestPermissions(context, new String[]{
                Manifest.permission.CALL_PHONE}, PermissionRequest.RQST_CALLPHONE);
    }




    public static boolean haveCamera(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                ==PackageManager.PERMISSION_GRANTED;
    }
    public static void requestForCamera(Activity context) {
        ActivityCompat.requestPermissions(context, new String[]{
                Manifest.permission.CAMERA}, PermissionRequest.RQST_CAMERA);
    }



}