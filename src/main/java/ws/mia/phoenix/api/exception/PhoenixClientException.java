package ws.mia.phoenix.api.exception;

public class PhoenixClientException extends RuntimeException {
	public PhoenixClientException() {
	}

	public PhoenixClientException(String message) {
		super(message);
	}

	public PhoenixClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public PhoenixClientException(Throwable cause) {
		super(cause);
	}

	public PhoenixClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
