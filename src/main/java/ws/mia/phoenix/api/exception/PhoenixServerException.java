package ws.mia.phoenix.api.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import ws.mia.phoenix.api.model.response.PhoenixErrorResponse;

public class PhoenixServerException extends RuntimeException {
	private final int status;
	private final String error;
	private final String message;
	private final String path;

	public PhoenixServerException(int status, String error, String message, String path) {
		super(String.format("Server returned %d %s: %s", status, error, message));
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
	}

	public static PhoenixServerException fromPhoenixError(String json) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			PhoenixErrorResponse resp = mapper.readValue(json, PhoenixErrorResponse.class);
			return new PhoenixServerException(resp.getStatus(), resp.getError(), resp.getMessage(), resp.getPath());
		} catch (Exception e) {
			return new PhoenixServerException(500, "UNKNOWN", "Failed to parse server error", "");
		}
	}

	public int getStatus() {
		return status;
	}

	public String getError() {
		return error;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public String getPath() {
		return path;
	}
}
