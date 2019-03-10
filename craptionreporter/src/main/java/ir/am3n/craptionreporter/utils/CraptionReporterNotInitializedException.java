package ir.am3n.craptionreporter.utils;

/**
 * Created by bali on 02/08/17.
 */

/**
 * An Exception indicating that the Crash Reporter has not been correctly initialized.
 */
public class CraptionReporterNotInitializedException extends CraptionReporterException {
    static final long serialVersionUID = 1;

    /**
     * Constructs a CraptionReporterNotInitializedException with no additional information.
     */
    public CraptionReporterNotInitializedException() {
        super();
    }

    /**
     * Constructs a CraptionReporterNotInitializedException with a message.
     *
     * @param message A String to be returned from getMessage.
     */
    public CraptionReporterNotInitializedException(String message) {
        super(message);
    }

    /**
     * Constructs a CraptionReporterNotInitializedException with a message and inner error.
     *
     * @param message   A String to be returned from getMessage.
     * @param throwable A Throwable to be returned from getCause.
     */
    public CraptionReporterNotInitializedException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Constructs a CraptionReporterNotInitializedException with an inner error.
     *
     * @param throwable A Throwable to be returned from getCause.
     */
    public CraptionReporterNotInitializedException(Throwable throwable) {
        super(throwable);
    }
}