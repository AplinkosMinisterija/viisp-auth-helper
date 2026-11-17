package lt.biip.auth.generators;

public class UnknownAuthProviderException extends IllegalArgumentException {
    public UnknownAuthProviderException(String message) {
        super(message);
    }
}
