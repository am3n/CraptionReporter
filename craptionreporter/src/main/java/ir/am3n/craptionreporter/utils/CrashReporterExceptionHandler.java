package ir.am3n.craptionreporter.utils;

import android.util.Log;

import ir.am3n.craptionreporter.CrashReporter;

public class CrashReporterExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler exceptionHandler;

    public CrashReporterExceptionHandler() {
        this.exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {

        //Log.d("Meeeeeee", "CrashReporterExceptionHandler() > uncaughtException()");

        CrashUtil.crash(throwable);

        CrashUtil.startReportingToServer();

        exceptionHandler.uncaughtException(thread, throwable);
    }
}
