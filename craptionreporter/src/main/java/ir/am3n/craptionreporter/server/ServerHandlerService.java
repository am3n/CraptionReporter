package ir.am3n.craptionreporter.server;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import androidx.annotation.Nullable;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

public class ServerHandlerService extends Service {

    static public Handler handler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Meeeeeee", "ServerHandlerService() > onBind()");
        return null;
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Me", "ServerHandlerService() > onCreate()");
        if (handler == null) {
            handler = new Handler() {
                @Override
                public void handleMessage(@NotNull Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == 0) {
                        Log.d("Me", "ServerHandlerService() > stopSelf()");
                        stopSelf();
                        System.exit(0);
                    }
                }
            };
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Meeeeeee", "ServerHandlerService() > onStartCommand()");
        return START_STICKY;
    }

}
