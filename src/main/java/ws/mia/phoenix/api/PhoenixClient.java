package ws.mia.phoenix.api;

import ws.mia.phoenix.api.model.Route;
import ws.mia.phoenix.api.model.response.*;

import java.util.List;
import java.util.Optional;

public interface PhoenixClient {

	FlushRouteCacheResponse flushRouteCache(String source);

	/**
	 * Flushes cache for all routes
	 */
	FlushRouteCacheResponse flushRouteCache();

	List<Route> getRoutes();

	Optional<Route> getRoute(String source);

	PushRouteResponse pushRoute(Route route);

	ModifyRouteResponse modifyRoute(String source, Route newRoute);

	RemoveRouteResponse removeRoute(String source);

	PasswordProtectedResponse isPasswordProtected(String source);

	boolean routeExists(String source);

	boolean ping();

}
