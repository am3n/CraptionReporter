package ir.am3n.craptionreporter.utils;

/**
 * Created by bali on 02/08/17.
 */

/**
 * Represents an error condition specific to the Crash Reporter for Android.
 */
public class CraptionReporterException extends RuntimeException {
    static final long serialVersionUID = 1;

    /**
     * Constructs a new CraptionReporterException.
     */
    public CraptionReporterException() {
        super();
    }

    /**
     * Constructs a new CraptionReporterException.
     *
     * @param message the detail message of this exception
     */
    public CraptionReporterException(String message) {
        super(message);
    }

    /**
     * Constructs a new CraptionReporterException.
     *
     * @param format the format string (see {@link java.util.Formatter#format})
     * @param args   the list of arguments passed to the formatter.
     */
    public CraptionReporterException(String format, Object... args) {
        this(String.format(format, args));
    }

    /**
     * Constructs a new CraptionReporterException.
     *
     * @param message   the detail message of this exception
     * @param throwable the cause of this exception
     */
    public CraptionReporterException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Constructs a new CraptionReporterException.
     *
     * @param throwable the cause of this exception
     */
    public CraptionReporterException(Throwable throwable) {
        super(throwable);
    }

    @Override
    public String toString() {
        // Throwable.toString() returns "CraptionReporterException:{message}". Returning just "{message}"
        // should be fine here.
        return getMessage();
    }
}
