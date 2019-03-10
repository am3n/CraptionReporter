package ir.am3n.craptionreporter.utils;

public class CraptionReporterExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler exceptionHandler;

    public CraptionReporterExceptionHandler() {
        this.exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {

        //Log.d("Meeeeeee", "CraptionReporterExceptionHandler() > uncaughtException()");

        CraptionUtil.crash(throwable);

        CraptionUtil.startReportingToServer();

        exceptionHandler.uncaughtException(thread, throwable);
    }
}
