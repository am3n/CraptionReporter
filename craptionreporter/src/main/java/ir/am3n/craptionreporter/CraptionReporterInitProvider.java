package ir.am3n.craptionreporter;

/**
 * Created by bali on 02/08/17.
 */

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;

/**
 * Created by amitshekhar on 16/11/16.
 */

public class CraptionReporterInitProvider extends ContentProvider {


    public CraptionReporterInitProvider() {
    }

    @Override
    public boolean onCreate() {
        //Log.d("Meeeeeee", "CraptionReporterInitProvider() > onCreate() > build...");
        CraptionReporter.with(getContext()).build();
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public void attachInfo(Context context, ProviderInfo providerInfo) {
        if (providerInfo == null) {
            throw new NullPointerException("CraptionReporterInitProvider ProviderInfo cannot be null.");
        }
        // So if the authorities equal the library internal ones, the developer forgot to set his applicationId
        if ("ir.am3n.craptionreporter.CraptionReporterInitProvider".equals(providerInfo.authority)) {
            throw new IllegalStateException("Incorrect provider authority in manifest. Most likely due to a "
                    + "missing applicationId variable in application\'s build.gradle.");
        }
        super.attachInfo(context, providerInfo);
    }

}