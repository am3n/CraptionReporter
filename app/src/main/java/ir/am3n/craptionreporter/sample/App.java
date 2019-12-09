package ir.am3n.craptionreporter.sample;
import android.app.Application;

import ir.am3n.craptionreporter.CraptionReporter;
import ir.am3n.craptionreporter.RetraceOn;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CraptionReporter.with(this)
            .enableServer("https://exmaple.com/", BuildConfig.VERSION_CODE, RetraceOn.SERVER)
            .build();

        CraptionReporter.getInstance().setUserIdentification("useId");
        //CraptionReporter.getInstance().disableNotification();

    }
}