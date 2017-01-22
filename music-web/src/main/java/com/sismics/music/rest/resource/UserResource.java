package com.sismics.music.rest.resource;

import com.sismics.music.core.constant.Constants;
import com.sismics.music.core.dao.dbi.AuthenticationTokenDao;
import com.sismics.music.core.dao.dbi.RoleBaseFunctionDao;
import com.sismics.music.core.dao.dbi.UserDao;
import com.sismics.music.core.dao.dbi.criteria.UserCriteria;
import com.sismics.music.core.dao.dbi.dto.UserDto;
import com.sismics.music.core.event.PasswordChangedEvent;
import com.sismics.music.core.event.UserCreatedEvent;
import com.sismics.music.core.event.async.LastFmUpdateLovedTrackAsyncEvent;
import com.sismics.music.core.event.async.LastFmUpdateTrackPlayCountAsyncEvent;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.model.dbi.AuthenticationToken;
import com.sismics.music.core.model.dbi.Playlist;
import com.sismics.music.core.model.dbi.User;
import com.sismics.music.core.service.lastfm.LastFmService;
import com.sismics.music.core.util.dbi.PaginatedList;
import com.sismics.music.core.util.dbi.PaginatedLists;
import com.sismics.music.core.util.dbi.SortCriteria;
import com.sismics.music.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.security.UserPrincipal;
import com.sismics.util.LocaleUtil;
import com.sismics.util.filter.TokenBasedSecurityFilter;
import de.umass.lastfm.Session;
import org.apache.commons.lang.StringUtils;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.Cookie;
import javax.ws.rs.*;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.Set;

/**
 * User REST resources.
 * 
 * @author jtremeaux
 */
@Path("/user")
public class UserResource extends BaseResource {
    /**
     * Creates a new user.
     * 
     * @param username User's username
     * @param password Password
     * @param email E-Mail
     * @param localeId Locale ID
     * @return Response
     */
    @PUT
    public Response register(
        @FormParam("username") String username,
        @FormParam("password") String password,
        @FormParam("locale") String localeId,
        @FormParam("email") String email) {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Validate the input data
        username = ValidationUtil.validateLength(username, "username", 3, 50);
        ValidationUtil.validateAlphanumeric(username, "username");
        password = ValidationUtil.validateLength(password, "password", 8, 50);
        email = ValidationUtil.validateLength(email, "email", 3, 50);
        ValidationUtil.validateEmail(email, "email");
        
        // Create the user
        User user = new User();
        user.setRoleId(Constants.DEFAULT_USER_ROLE);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setCreateDate(new Date());

        if (localeId == null) {
            // Set the locale from the HTTP headers
            localeId = LocaleUtil.getLocaleIdFromAcceptLanguage(request.getHeader("Accept-Language"));
        }
        user.setLocaleId(localeId);
        
        // Create the user
        UserDao userDao = new UserDao();
        String userId = null;
        try {
            userId = userDao.create(user);
        } catch (Exception e) {
            if ("AlreadyExistingUsername".equals(e.getMessage())) {
                throw new ServerException("AlreadyExistingUsername", "Login already used", e);
            } else {
                throw new ServerException("UnknownError", "Unknown Server Error", e);
            }
        }

        // Create the default playlist for this user
        Playlist playlist = new Playlist();
        playlist.setUserId(userId);
        Playlist.createPlaylist(playlist);

        // Raise a user creation event
        UserCreatedEvent userCreatedEvent = new UserCreatedEvent();
        userCreatedEvent.setUser(user);
        AppContext.getInstance().getAsyncEventBus().post(userCreatedEvent);

        // Always return OK
        JsonObject response = Json.createObjectBuilder()
                .add("status", "ok")
                .build();
        return Response.ok().entity(response).build();
    }

    /**
     * Updates user informations.
     * 
     * @param password Password
     * @param email E-Mail
     * @param localeId Locale ID
     * @param firstConnection True if the user hasn't acknowledged the first connection wizard yet
     * @return Response
     */
    @POST
    public Response update(
        @FormParam("password") String password,
        @FormParam("email") String email,
        @FormParam("locale") String localeId,
        @FormParam("first_connection") Boolean firstConnection) {
        
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate the input data
        password = ValidationUtil.validateLength(password, "password", 8, 50, true);
        email = ValidationUtil.validateLength(email, "email", null, 100, true);
        localeId = com.sismics.music.rest.util.ValidationUtil.validateLocale(localeId, "locale", true);
        
        // Update the user
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(principal.getName());
        if (email != null) {
            user.setEmail(email);
        }
        if (localeId != null) {
            user.setLocaleId(localeId);
        }
        if (firstConnection != null && hasBaseFunction(BaseFunction.ADMIN)) {
            user.setFirstConnection(firstConnection);
        }
        
        user = userDao.update(user);
        
        if (StringUtils.isNotBlank(password)) {
            user.setPassword(password);
            user = userDao.updatePassword(user);
        }
        
        if (StringUtils.isNotBlank(password)) {
            // Raise a password updated event
            PasswordChangedEvent passwordChangedEvent = new PasswordChangedEvent();
            passwordChangedEvent.setUser(user);
            AppContext.getInstance().getAsyncEventBus().post(passwordChangedEvent);
        }
        
        // Always return "ok"
        JsonObject response = Json.createObjectBuilder()
                .add("status", "ok")
                .build();
        return Response.ok().entity(response).build();
    }

    /**
     * Updates user informations.
     * 
     * @param username Username
     * @param password Password
     * @param email E-Mail
     * @param localeId Locale ID
     * @return Response
     */
    @POST
    @Path("{username: [a-zA-Z0-9_]+}")
    public Response update(
        @PathParam("username") String username,
        @FormParam("password") String password,
        @FormParam("email") String email,
        @FormParam("locale") String localeId) {
        
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Validate the input data
        password = ValidationUtil.validateLength(password, "password", 8, 50, true);
        email = ValidationUtil.validateLength(email, "email", null, 100, true);
        localeId = com.sismics.music.rest.util.ValidationUtil.validateLocale(localeId, "locale", true);
        
        // Check if the user exists
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            throw new ClientException("UserNotFound", "The user doesn't exist");
        }

        // Update the user
        if (email != null) {
            user.setEmail(email);
        }
        if (localeId != null) {
            user.setLocaleId(localeId);
        }
        
        user = userDao.update(user);
        
        if (StringUtils.isNotBlank(password)) {
            checkBaseFunction(BaseFunction.PASSWORD);
            
            // Change the password
            user.setPassword(password);
            user = userDao.updatePassword(user);

            // Raise a password updated event
            PasswordChangedEvent passwordChangedEvent = new PasswordChangedEvent();
            passwordChangedEvent.setUser(user);
            AppContext.getInstance().getAsyncEventBus().post(passwordChangedEvent);
        }
        
        // Always return "ok"
        JsonObject response = Json.createObjectBuilder()
                .add("status", "ok")
                .build();
        return Response.ok().entity(response).build();
    }

    /**
     * Checks if a username is available. Search only on active accounts.
     * 
     * @param username Username to check
     * @return Response
     */
    @GET
    @Path("check_username")
    public Response checkUsername(
        @QueryParam("username") String username) {
        
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        
        JsonObjectBuilder response = Json.createObjectBuilder();
        if (user != null) {
            response.add("status", "ko")
                    .add("message", "Username already registered");
        } else {
            response.add("status", "ok");
        }
        
        return Response.ok().entity(response.build()).build();
    }

    /**
     * This resource is used to authenticate the user and create a user session.
     * The "session" is only used to identify the user, no other data is stored in the session.
     * 
     * @param username Username
     * @param password Password
     * @param longLasted Remember the user next time, create a long lasted session
     * @return Response
     */
    @POST
    @Path("login")
    public Response login(
        @FormParam("username") String username,
        @FormParam("password") String password,
        @FormParam("remember") boolean longLasted) {
        
        // Validate the input data
        username = StringUtils.strip(username);
        password = StringUtils.strip(password);

        // Get the user
        UserDao userDao = new UserDao();
        String userId = userDao.authenticate(username, password);
        if (userId == null) {
            throw new ForbiddenClientException();
        }
            
        // Create a new session token
        AuthenticationTokenDao authenticationTokenDao = new AuthenticationTokenDao();
        AuthenticationToken authenticationToken = new AuthenticationToken();
        authenticationToken.setUserId(userId);
        authenticationToken.setLongLasted(longLasted);
        String token = authenticationTokenDao.create(authenticationToken);
        
        // Cleanup old session tokens
        authenticationTokenDao.deleteOldSessionToken(userId);

        int maxAge = longLasted ? TokenBasedSecurityFilter.TOKEN_LONG_LIFETIME : -1;
        NewCookie cookie = new NewCookie(TokenBasedSecurityFilter.COOKIE_NAME, token, "/", null, null, maxAge, false);
        return Response.ok()
                .entity(Json.createObjectBuilder().build())
                .cookie(cookie)
                .build();
    }

    /**
     * Logs out the user and deletes the active session.
     * 
     * @return Response
     */
    @POST
    @Path("logout")
    public Response logout() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the value of the session token
        String authToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (TokenBasedSecurityFilter.COOKIE_NAME.equals(cookie.getName())) {
                    authToken = cookie.getValue();
                }
            }
        }
        
        AuthenticationTokenDao authenticationTokenDao = new AuthenticationTokenDao();
        AuthenticationToken authenticationToken = null;
        if (authToken != null) {
            authenticationToken = authenticationTokenDao.get(authToken);
        }
        
        // No token : nothing to do
        if (authenticationToken == null) {
            throw new ForbiddenClientException();
        }
        
        // Deletes the server token
        try {
            authenticationTokenDao.delete(authToken);
        } catch (Exception e) {
            throw new ServerException("AuthenticationTokenError", "Error deleting authentication token: " + authToken, e);
        }
        
        // Deletes the client token in the HTTP response
        NewCookie cookie = new NewCookie(TokenBasedSecurityFilter.COOKIE_NAME, null);
        return Response.ok()
                .entity(Json.createObjectBuilder().build())
                .cookie(cookie)
                .build();
    }

    /**
     * Delete a user.
     * 
     * @return Response
     */
    @DELETE
    public Response delete() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Ensure that the admin user is not deleted
        if (hasBaseFunction(BaseFunction.ADMIN)) {
            throw new ClientException("ForbiddenError", "The admin user cannot be deleted");
        }
        
        // Delete the user
        UserDao userDao = new UserDao();
        userDao.delete(principal.getName());
        
        // Always return ok
        JsonObject response = Json.createObjectBuilder()
                .add("status", "ok")
                .build();
        return Response.ok().entity(response).build();
    }
    
    /**
     * Deletes a user.
     * 
     * @param username Username
     * @return Response
     */
    @DELETE
    @Path("{username: [a-zA-Z0-9_]+}")
    public Response delete(@PathParam("username") String username) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Check if the user exists
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            throw new ClientException("UserNotFound", "The user doesn't exist");
        }
        
        // Ensure that the admin user is not deleted
        RoleBaseFunctionDao userBaseFuction = new RoleBaseFunctionDao();
        Set<String> baseFunctionSet = userBaseFuction.findByRoleId(user.getRoleId());
        if (baseFunctionSet.contains(BaseFunction.ADMIN.name())) {
            throw new ClientException("ForbiddenError", "The admin user cannot be deleted");
        }
        
        // Delete the user
        userDao.delete(user.getUsername());
        
        // Always return ok
        JsonObject response = Json.createObjectBuilder()
                .add("status", "ok")
                .build();
        return Response.ok().entity(response).build();
    }

    /**
     * Returns the information about the connected user.
     * 
     * @return Response
     */
    @GET
    public Response info() {
        JsonObjectBuilder response = Json.createObjectBuilder();
        if (!authenticate()) {
            response.add("anonymous", true);

            String localeId = LocaleUtil.getLocaleIdFromAcceptLanguage(request.getHeader("Accept-Language"));
            response.add("locale", localeId);
            
            // Check if admin has the default password
            UserDao userDao = new UserDao();
            User adminUser = userDao.getActiveById("admin");
            if (adminUser != null && adminUser.getDeleteDate() == null) {
                response.add("is_default_password", Constants.DEFAULT_ADMIN_PASSWORD.equals(adminUser.getPassword()));
            }
        } else {
            response.add("anonymous", false);
            UserDao userDao = new UserDao();
            User user = userDao.getActiveById(principal.getId());
            response.add("username", user.getUsername())
                    .add("email", user.getEmail())
                    .add("locale", user.getLocaleId())
                    .add("lastfm_connected", user.getLastFmSessionToken() != null)
                    .add("first_connection", user.isFirstConnection());
            JsonArrayBuilder baseFunctions = Json.createArrayBuilder();
            for (String baseFunction : ((UserPrincipal) principal).getBaseFunctionSet()) {
                baseFunctions.add(baseFunction);
            }
            response.add("base_functions", baseFunctions)
                    .add("is_default_password", hasBaseFunction(BaseFunction.ADMIN) && Constants.DEFAULT_ADMIN_PASSWORD.equals(user.getPassword()));
        }
        
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Returns the information about a user.
     * 
     * @param username Username
     * @return Response
     */
    @GET
    @Path("{username: [a-zA-Z0-9_]+}")
    public Response view(@PathParam("username") String username) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            throw new ClientException("UserNotFound", "The user doesn't exist");
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("username", user.getUsername())
                .add("email", user.getEmail())
                .add("locale", user.getLocaleId());
        
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns all active users.
     * 
     * @param limit Page limit
     * @param offset Page offset
     * @param sortColumn Sort index
     * @param asc If true, ascending sorting, else descending
     * @return Response
     */
    @GET
    @Path("list")
    public Response list(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder users = Json.createArrayBuilder();
        
        PaginatedList<UserDto> paginatedList = PaginatedLists.create(limit, offset);
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);

        UserDao userDao = new UserDao();
        userDao.findByCriteria(paginatedList, new UserCriteria(), sortCriteria);
        for (UserDto userDto : paginatedList.getResultList()) {
            users.add(Json.createObjectBuilder()
                    .add("id", userDto.getId())
                    .add("username", userDto.getUsername())
                    .add("email", userDto.getEmail())
                    .add("create_date", userDto.getCreateTimestamp()));
        }
        response.add("total", paginatedList.getResultCount());
        response.add("users", users);
        
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Authenticates a user on Last.fm.
     *
     * @param lastFmUsername Last.fm username
     * @param lastFmPassword Last.fm password
     * @return Response
     */
    @PUT
    @Path("lastfm")
    public Response registerLastFm(
            @FormParam("username") String lastFmUsername,
            @FormParam("password") String lastFmPassword) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        ValidationUtil.validateRequired(lastFmUsername, "username");
        ValidationUtil.validateRequired(lastFmPassword, "password");

        // Get the value of the session token
        final LastFmService lastFmService = AppContext.getInstance().getLastFmService();
        Session session = lastFmService.createSession(lastFmUsername, lastFmPassword);
        // XXX We should be able to distinguish invalid user credentials from invalid api key -- update Authenticator?
        if (session == null) {
            throw new ClientException("InvalidCredentials", "The supplied Last.fm credentials is invalid");
        }

        // Store the session token (it has no expiry date)
        UserDao userDao = new UserDao();
        User user = userDao.getActiveById(principal.getId());
        user.setLastFmSessionToken(session.getKey());
        userDao.updateLastFmSessionToken(user);

        // Raise a Last.fm registered event
        AppContext.getInstance().getLastFmEventBus().post(new LastFmUpdateLovedTrackAsyncEvent(user));
        AppContext.getInstance().getLastFmEventBus().post(new LastFmUpdateTrackPlayCountAsyncEvent(user));

        // Always return ok
        JsonObject response = Json.createObjectBuilder()
                .add("status", "ok")
                .build();
        return Response.ok().entity(response).build();
    }

    /**
     * Returns the Last.fm information about the connected user.
     *
     * @return Response
     */
    @GET
    @Path("lastfm")
    public Response lastFmInfo() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        JsonObjectBuilder response = Json.createObjectBuilder();
        User user = new UserDao().getActiveById(principal.getId());

        if (user.getLastFmSessionToken() != null) {
            final LastFmService lastFmService = AppContext.getInstance().getLastFmService();
            de.umass.lastfm.User lastFmUser = lastFmService.getInfo(user);
    
            response.add("username", lastFmUser.getName())
                    .add("registered_date", lastFmUser.getRegisteredDate().getTime())
                    .add("play_count", lastFmUser.getPlaycount())
                    .add("url", lastFmUser.getUrl())
                    .add("image_url", lastFmUser.getImageURL());
        } else {
            response.add("status", "not_connected");
        }

        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Disconnect the current user from Last.fm.
     *  
     * @return Response
     */
    @DELETE
    @Path("lastfm")
    public Response unregisterLastFm() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Remove the session token
        UserDao userDao = new UserDao();
        User user = userDao.getActiveById(principal.getId());
        user.setLastFmSessionToken(null);
        userDao.updateLastFmSessionToken(user);

        // Always return ok
        JsonObject response = Json.createObjectBuilder()
                .add("status", "ok")
                .build();
        return Response.ok().entity(response).build();
    }
}
