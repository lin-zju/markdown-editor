package lib.error;

public class ConnectionError extends MarkdownError {
    public ConnectionError(String msg) {
        super(msg);
    }

    @Override
    public String getMessage() {
        return "ConnectionError:" + super.getMessage();
    }
}
