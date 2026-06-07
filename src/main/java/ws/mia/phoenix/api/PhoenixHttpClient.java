package ws.mia.phoenix.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ws.mia.phoenix.api.exception.PhoenixClientException;
import ws.mia.phoenix.api.exception.PhoenixServerException;
import ws.mia.phoenix.api.model.Route;
import ws.mia.phoenix.api.model.response.*;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class PhoenixHttpClient implements PhoenixClient {

	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;
	private final String baseUrl;

	private final String authToken;

	public PhoenixHttpClient(String baseUrl) {
		this(baseUrl, (String) null);
	}

	public PhoenixHttpClient(String baseUrl, String authToken) {
		this(baseUrl, HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.build(), authToken);
	}

	public PhoenixHttpClient(String baseUrl, HttpClient httpClient) {
		this(baseUrl, httpClient, null);
	}

	public PhoenixHttpClient(String baseUrl, HttpClient httpClient, String authToken) {
		this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		this.httpClient = httpClient;

		this.objectMapper = initObjectMapper();

		this.authToken = authToken;
	}

	private ObjectMapper initObjectMapper() {
		return new ObjectMapper();
	}

	private HttpRequest.Builder getAuthorizedBuilder(String endpoint) {
		if (endpoint.startsWith("/")) endpoint = endpoint.substring(1);
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + "/api/" + endpoint));

		if (authToken != null) {
			builder.header("Authorization", "Bearer " + authToken);
		}

		return builder;
	}

	@Override
	public FlushRouteCacheResponse flushRouteCache(String source) {
		try {
			HttpRequest request = getAuthorizedBuilder("flush-route-cache?source=" + urlEncode(source))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.noBody())
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (isSuccess(response.statusCode())) {
				return objectMapper.readValue(response.body(), FlushRouteCacheResponse.class);
			}

			throw objectMapper.readValue(response.body(), PhoenixServerException.class);

		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new PhoenixClientException("Failed to flush route cache", e);
		}
	}

	@Override
	public FlushRouteCacheResponse flushRouteCache() {
		try {
			HttpRequest request = getAuthorizedBuilder("flush-route-cache")
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.noBody())
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (isSuccess(response.statusCode())) {
				return objectMapper.readValue(response.body(), FlushRouteCacheResponse.class);
			}

			throw objectMapper.readValue(response.body(), PhoenixServerException.class);

		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new PhoenixClientException("Failed to flush route cache", e);
		}
	}

	@Override
	public List<Route> getRoutes() {
		try {
			HttpRequest request = getAuthorizedBuilder("routes")
					.GET()
					.header("Accept", "application/json")
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() >= 200 && response.statusCode() < 300) {
				return objectMapper.readValue(response.body(), new TypeReference<List<Route>>() {
				});
			}

			throw objectMapper.readValue(response.body(), PhoenixServerException.class);

		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new PhoenixClientException("Failed to get routes", e);
		}
	}

	@Override
	public Optional<Route> getRoute(String source) {
		try {
			HttpRequest request = getAuthorizedBuilder("route?source=" + source)
					.GET()
					.header("Accept", "application/json")
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 404 || response.body().isEmpty()) {
				return Optional.empty();
			}

			if (isSuccess(response.statusCode())) {
				return Optional.of(objectMapper.readValue(response.body(), Route.class));
			}

			throw objectMapper.readValue(response.body(), PhoenixServerException.class);
		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new PhoenixClientException("Failed to get route with source: " + source, e);
		}
	}

	@Override
	public PushRouteResponse pushRoute(Route route) {
		try {
			String json = objectMapper.writeValueAsString(route);
			HttpRequest request = getAuthorizedBuilder("push-route")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.header("Content-Type", "application/json")
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (isSuccess(response.statusCode())) {
				return objectMapper.readValue(response.body(), PushRouteResponse.class);
			}

			throw objectMapper.readValue(response.body(), PhoenixServerException.class);
		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new PhoenixClientException("Failed to push route for source: " + route.getSource(), e);
		}
	}

	@Override
	public ModifyRouteResponse modifyRoute(String source, Route newRoute) {
		try {
			String json = objectMapper.writeValueAsString(newRoute);

			HttpRequest request = getAuthorizedBuilder("/modify-route?source=" + urlEncode(source))
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.header("Content-Type", "application/json")
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (isSuccess(response.statusCode())) {
				return objectMapper.readValue(response.body(), ModifyRouteResponse.class);
			}

			throw objectMapper.readValue(response.body(), PhoenixServerException.class);
		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new PhoenixClientException("Failed to modify route for original source: " + source, e);
		}
	}

	@Override
	public RemoveRouteResponse removeRoute(String source) {
		try {
			HttpRequest request = getAuthorizedBuilder("remove-route?source=" + urlEncode(source))
					.POST(HttpRequest.BodyPublishers.noBody())
					.header("Content-Type", "application/json")
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (isSuccess(response.statusCode())) {
				return objectMapper.readValue(response.body(), RemoveRouteResponse.class);
			}

			throw objectMapper.readValue(response.body(), PhoenixServerException.class);
		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new PhoenixClientException("Failed to remove route for source: " + source, e);
		}
	}

	@Override
	public PasswordProtectedResponse isPasswordProtected(String source) {
		try {
			HttpRequest request = getAuthorizedBuilder("requires-password?source=" + urlEncode(source))
					.GET()
					.header("Accept", "application/json")
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (isSuccess(response.statusCode())) {
				return objectMapper.readValue(response.body(), PasswordProtectedResponse.class);
			}

			throw objectMapper.readValue(response.body(), PhoenixServerException.class);
		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new PhoenixClientException("Failed to check password protection for route: " + source, e);
		}
	}

	@Override
	public boolean routeExists(String source) {
		try {
			HttpRequest request = getAuthorizedBuilder("route-exists?source=" + urlEncode(source))
					.GET()
					.header("Accept", "application/json")
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (isSuccess(response.statusCode())) {
				return objectMapper.readValue(response.body(), Boolean.class);
			}

			throw objectMapper.readValue(response.body(), PhoenixServerException.class);
		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new PhoenixClientException("Failed to check route existence of source: " + source, e);
		}
	}

	@Override
	public boolean ping() {
		try {
			// send some test data
			String testData = "owo-" + System.currentTimeMillis();
			HttpRequest request = getAuthorizedBuilder("ping")
					.POST(HttpRequest.BodyPublishers.ofString(testData))
					.header("Content-Type", "application/octet-stream")
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (isSuccess(response.statusCode())) {
				// response should be echoed back
				return testData.equals(response.body());
			}

			throw objectMapper.readValue(response.body(), PhoenixServerException.class);
		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	private String urlEncode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private boolean isSuccess(int statusCode) {
		return statusCode >= 200 && statusCode < 300;
	}
}
