package ws.mia.phoenix.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.*;
import java.util.stream.StreamSupport;

///
/// Model for a Phoenix Web Route.
///
@JsonDeserialize(builder = Route.Builder.class)
public final class Route {

	///
	/// Normalized source URI (i.e. `subdomain.domain/path`) with no trailing slash.
	///
	/// May be null only if fallback is true, indicating a default fallback route.
	///
	@JsonProperty("source")
	private final String source;

	///
	/// Additional source URIs that should route to the same destination.
	///
	/// These are normalized the same way as the primary source.
	///
	@JsonProperty("aliases")
	private final List<String> aliases;

	///
	/// Normalized destination URI with no trailing slash. <br>
	/// This is where requests matching the source will be forwarded to. <br>
	/// May use {@code <path>} to inject the source path anywhere. <br>
	/// If {@code <path>} is used, the path will not be appended onto the end by Phoenix.
	///
	/// Example: <br>
	/// {@code source = "example.com/api"} <br>
	/// {@code destination = "backend.local/<path>Service"} <br>
	/// => a request to {@code example.com/api/test} proxies to {@code backend.local/testService} <br>
	///
	/// Example: <br>
	/// {@code source = "a.example.com/test"} <br>
	/// {@code destination = "<path>"} <br>
	/// => a request to {@code a.example.com/test/https://google.com} proxies to {@code https://google.com} <br>
	///
	@JsonProperty("destination")
	private final String destination;

	///
	/// Optional password required to access this route.
	///
	/// Cannot be used with redirect routes.
	///
	@JsonProperty("password")
	private final String password;

	///
	/// Whether this route represents a default fallback.
	/// Fallback routes match any source that doesn't match other routes.
	/// When true, source may be null.
	///
	/// If multiple routes are fallback routes, a random one is chosen.
	///
	@JsonProperty("fallback")
	private final boolean fallback;

	///
	/// Whether to ignore the source's path and serve the destination verbatim.
	/// When false, the source path is appended to the destination.
	///
	/// Example: `source=example.com/api`, `destination=w3.org/v1`, `request=example.com/api/users`
	///   - `verbose=false`: forwards to `w3.org/v1/users` <br>
	///   - `verbose=true`: forwards to `w3.org/v1`
	///
	@JsonProperty("verbose")
	private final boolean verboseDestination;

	///
	/// Whether this route performs a redirect instead of proxying.
	///
	/// When true, returns a redirect response to the destination. <br>
	/// When false, proxies the request and returns the destination's content.
	///
	/// Redirect routes cannot have replacements, omitted classes, or passwords.
	///
	@JsonProperty("redirect")
	private final boolean redirect;

	///
	/// Private constructor - use Builder or fromDocument factory method
	///
	private Route(Builder builder) {
		this.source = builder.source != null ? normalizeUri(builder.source).toLowerCase() : null;
		this.aliases = builder.aliases.stream().map(Route::normalizeUri).toList();
		this.destination = normalizeUri(builder.destination);
		this.fallback = builder.fallback;
		this.verboseDestination = builder.verboseDestination;
		this.redirect = builder.redirect;
		this.password = builder.password;

		validate();
	}

	///
	/// Creates a Route from a JSON node with full validation.
	///
	/// @return validated Route instance
	/// @throws IllegalArgumentException if document contains invalid configuration
	///
	public static Route fromJsonNode(JsonNode node) {
		if (node == null) {
			throw new IllegalArgumentException("JSON node cannot be null");
		}

		try {
			return new Builder()
					.source(node.path("source").asText(null))
					.aliases(StreamSupport.stream(node.path("aliases").spliterator(), false)
							.map(JsonNode::asText)
							.toList())
					.destination(node.path("destination").asText(null))
					.fallback(node.path("fallback").asBoolean(false))
					.verboseDestination(node.path("verbose").asBoolean(false))
					.redirect(node.path("redirect").asBoolean(false))
					.password(node.path("password").asText(null))
					.build();
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to parse JSON route config: " + e.getMessage(), e);
		}
	}

	///
	/// Normalizes a URI by removing trailing slashes
	///
	private static String normalizeUri(String uri) {
		if (uri == null) {
			return null;
		}
		String trimmed = uri.trim();
		return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
	}

	///
	/// Validates route configuration.
	///
	/// Called automatically during construction.
	///
	/// @throws IllegalArgumentException if validation fails
	///
	private void validate() {
		// Destination is always required
		if (destination == null || destination.isBlank()) {
			throw new IllegalArgumentException("Destination cannot be null or blank");
		}

		// Non-fallback routes must have a source
		if (source == null && !fallback) {
			throw new IllegalArgumentException(
					"Non-fallback routes must have a source. Either set a source or mark as fallback."
			);
		}

		// Redirect routes cannot modify content
		if (redirect) {
			List<String> violations = new ArrayList<>();

			if (password != null && !password.isBlank()) {
				violations.add("password protection");
			}

			if (!violations.isEmpty()) {
				throw new IllegalArgumentException(
						"Redirect routes cannot modify content or require passwords. " +
								"Found: " + String.join(", ", violations)
				);
			}
		}
	}

	public String getSource() {
		return source;
	}

	public List<String> getAliases() {
		return aliases;
	}

	public String getDestination() {
		return destination;
	}

	public boolean isFallback() {
		return fallback;
	}

	public boolean hasVerboseDestination() {
		return verboseDestination;
	}

	public boolean isRedirect() {
		return redirect;
	}

	public String getPassword() {
		return password;
	}

	@JsonIgnore
	public boolean isPasswordProtected() {
		return password != null && !password.isBlank();
	}

	///
	/// Returns all valid source URIs for this route (source + aliases).
	/// The primary source is always first in the list.
	///
	/// @return unmodifiable list of all source URIs
	///
	@JsonIgnore
	public List<String> getAllSources() {
		List<String> all = new ArrayList<>();
		if (source != null) {
			all.add(source);
		}
		all.addAll(aliases);
		return Collections.unmodifiableList(all);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Route route = (Route) o;
		return fallback == route.fallback &&
				verboseDestination == route.verboseDestination &&
				redirect == route.redirect &&
				Objects.equals(source, route.source) &&
				Objects.equals(aliases, route.aliases) &&
				Objects.equals(destination, route.destination) &&
				Objects.equals(password, route.password);
	}

	@Override
	public int hashCode() {
		return Objects.hash(source, aliases, destination,
				fallback, verboseDestination, redirect, password);
	}

	@Override
	public String toString() {
		return "Route{" +
				"source='" + source + '\'' +
				", aliases=" + aliases +
				", destination='" + destination + '\'' +
				", fallback=" + fallback +
				", verboseDestination=" + verboseDestination +
				", redirect=" + redirect +
				", passwordProtected=" + isPasswordProtected() +
				'}';
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static class Builder {
		@JsonProperty("source")
		private String source;
		@JsonProperty("aliases")
		private List<String> aliases = new ArrayList<>();
		@JsonProperty("destination")
		private String destination;
		@JsonProperty("fallback")
		private boolean fallback = false;
		@JsonProperty("verbose")
		private boolean verboseDestination = false;
		@JsonProperty("redirect")
		private boolean redirect = false;
		@JsonProperty("password")
		private String password;

		public Builder source(String source) {
			this.source = source;
			return this;
		}

		public Builder aliases(List<String> aliases) {
			this.aliases = aliases != null ? new ArrayList<>(aliases) : new ArrayList<>();
			return this;
		}

		public Builder addAlias(String alias) {
			if (alias != null && !alias.isBlank()) {
				this.aliases.add(alias);
			}
			return this;
		}

		public Builder destination(String destination) {
			this.destination = destination;
			return this;
		}

		public Builder fallback(boolean fallback) {
			this.fallback = fallback;
			return this;
		}

		public Builder verboseDestination(boolean verboseDestination) {
			this.verboseDestination = verboseDestination;
			return this;
		}

		public Builder redirect(boolean redirect) {
			this.redirect = redirect;
			return this;
		}

		public Builder password(String password) {
			this.password = password;
			return this;
		}

		public Builder from(Route route) {
			this.source = route.source;
			this.aliases = new ArrayList<>(route.aliases);
			this.destination = route.destination;
			this.fallback = route.fallback;
			this.verboseDestination = route.verboseDestination;
			this.redirect = route.redirect;
			this.password = route.password;
			return this;
		}

		///
		/// Builds and validates the Route.
		///
		/// @return validated Route instance
		/// @throws IllegalArgumentException if validation fails
		///
		public Route build() {
			return new Route(this);
		}
	}

}