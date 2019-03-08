package ir.am3n.craptionreporter.server;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import androidx.annotation.Nullable;
import android.util.Log;

public class ServerHandlerService extends Service {

    static public Handler handler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //Log.d("Meeeeeee", "ServerHandlerService() > onBind()");
        return null;
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate() {
        super.onCreate();
        //Log.d("Meeeeeee", "ServerHandlerService() > onCreate()");
        /*new Reporter()
                .listener(new Reporter.Listener() {
                    @Override
                    public void onStop() {
                        stopSelf();
                    }
                })
                .report();*/
        if (handler==null) {
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == 0) {
                        //Log.d("Meeeeeee", "ServerHandlerService() > stopSelf()");
                        stopSelf();
                    }
                }
            };
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.d("Meeeeeee", "ServerHandlerService() > onStartCommand()");
        return START_STICKY;
    }

}
