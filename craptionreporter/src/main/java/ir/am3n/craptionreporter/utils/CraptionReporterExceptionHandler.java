package ir.am3n.craptionreporter.utils;

import androidx.annotation.NonNull;

public class CraptionReporterExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler exceptionHandler;

    public CraptionReporterExceptionHandler() {
        this.exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {

        //Log.d("Meeeeeee", "CraptionReporterExceptionHandler() > uncaughtException()");

        CraptionUtil.crash(throwable);

        CraptionUtil.startReportingToServer();

        exceptionHandler.uncaughtException(thread, throwable);

    }

}
