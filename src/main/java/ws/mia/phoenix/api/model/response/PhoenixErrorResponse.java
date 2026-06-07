package ws.mia.phoenix.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PhoenixErrorResponse {

	@JsonProperty("_")
	private String phoenix;

	private String timestamp;
	private String path;
	private int status;
	private String error;
	private String message;

	public PhoenixErrorResponse() { }

	public String getPhoenix() {
		return phoenix;
	}

	public void setPhoenix(String phoenix) {
		this.phoenix = phoenix;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
