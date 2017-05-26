package com.sismics.music.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.sismics.util.dbi.DBIF;
import com.sismics.util.filter.RequestContextFilter;
import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import javax.json.JsonObject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class of integration tests with Jersey.
 * 
 * @author jtremeaux
 */
public abstract class BaseJerseyTest extends JerseyTest {
    /**
     * Test email server.
     */
    protected Wiser wiser;
    
    /**
     * Test HTTP server.
     */
    HttpServer httpServer;

    /**
     * The response from the last request.
     */
    protected Response response;

    /**
     * The set of current cookies.
     */
    protected Map<String, String> cookies = new HashMap<>();

    /**
     * Utility class for the REST client.
     */
    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }
    
    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        return new Application();
    }
    
    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri(super.getBaseUri()).path("music").build();
    }
    
    protected String getFakeHttpUri() {
        return "http://localhost:" + getPort() + "/fakehttp";
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        wiser = new Wiser();
        wiser.setPort(2500);
        wiser.start();

        // Force shutdown
        DBIF.reset();
        
        String httpRoot = URLDecoder.decode(new File(getClass().getResource("/").getFile()).getAbsolutePath(), "utf-8");
        httpServer = HttpServer.createSimpleServer(httpRoot, "localhost", getPort());
        WebappContext context = new WebappContext("GrizzlyContext", "/music");
        context.addFilter("requestContextFilter", RequestContextFilter.class)
                .addMappingForUrlPatterns(null, "/*");
        context.addFilter("tokenBasedSecurityFilter", TokenBasedSecurityFilter.class)
                .addMappingForUrlPatterns(null, "/*");
        ServletRegistration reg = context.addServlet("jerseyServlet", ServletContainer.class);
        reg.setInitParameter("jersey.config.server.provider.packages", "com.sismics.music.rest.resource");
        reg.setInitParameter("jersey.config.server.provider.classnames", "org.glassfish.jersey.media.multipart.MultiPartFeature");
        reg.setInitParameter("jersey.config.server.response.setStatusOverSendError", "true");
        reg.setLoadOnStartup(1);
        reg.addMapping("/*");
        reg.setAsyncSupported(true);
        context.deploy(httpServer);
        httpServer.start();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        if (httpServer != null) {
            httpServer.shutdownNow();
        }
        if (wiser != null) {
            wiser.stop();
        }
    }

    /**
     * Extracts an email from the queue and consumes the email.
     * 
     * @return Text of the email
     */
    protected String popEmail() throws MessagingException, IOException {
        List<WiserMessage> wiserMessageList = wiser.getMessages();
        if (wiserMessageList.isEmpty()) {
            return null;
        }
        WiserMessage wiserMessage = wiserMessageList.get(wiserMessageList.size() - 1);
        wiserMessageList.remove(wiserMessageList.size() - 1);
        MimeMessage message = wiserMessage.getMimeMessage();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        message.writeTo(os);
        String body = os.toString();
        
        return body;
    }
    
    /**
     * Encodes a string to "quoted-printable" characters to compare with the contents of an email.
     * 
     * @param input String to encode
     * @return Encoded string
     */
    protected String encodeQuotedPrintable(String input) throws MessagingException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = MimeUtility.encode(baos, "quoted-printable");
        os.write(input.getBytes());
        os.close();
        return baos.toString();
    }

    /**
     * Creates a user.
     *
     * @param username Username
     */
    public void createUser(String username) {
        // Login admin to create the user
        String adminAuthenticationToken = login("admin", "admin", false);

        // Create the user
        Form form = new Form();
        form.param("username", username);
        form.param("email", username + "@music.com");
        form.param("password", "12345678");
        form.param("time_zone", "Asia/Tokyo");
        target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(form), JsonObject.class);

        // Logout admin
        logout();
    }


    public void assertIsOk() {
        assertIsOk(response);
    }

    public void assertIsOk(Response response) {
        assertStatus(200, response);
    }

    public void assertIsBadRequest() {
        assertIsBadRequest(response);
    }

    public void assertIsBadRequest(Response response) {
        assertStatus(400, response);
    }

    public void assertIsForbidden() {
        assertIsForbidden(response);
    }

    public void assertIsForbidden(Response response) {
        assertStatus(403, response);
    }

    public void assertIsNotFound() {
        assertIsNotFound(response);
    }

    public void assertIsNotFound(Response response) {
        assertStatus(404, response);
    }

    public void assertIsInternalServerError() {
        assertIsInternalServerError(response);
    }

    public void assertIsInternalServerError(Response response) {
        assertStatus(500, response);
    }

    public void assertStatus(int status, Response response) {
        Assert.assertEquals("Response status error, out: " + response.toString(), status, response.getStatus());
    }

    /**
     * Connects a user to the application.
     *
     * @param username Username
     * @param password Password
     * @param remember Remember user
     * @return Authentication token
     */
    public String login(String username, String password, Boolean remember) {
        POST("/user/login", ImmutableMap.of(
                "username", username,
                "password", password,
                "remember", remember.toString()));
        assertIsOk();

        return getAuthenticationCookie(response);
    }

    /**
     * Connects a user to the application.
     *
     * @param username Username
     * @return The authentication token
     */
    public String login(String username) {
        return login(username, "12345678", false);
    }

    /**
     * Login the admin user.
     *
     * @return The authentication token
     */
    protected String loginAdmin() {
        return login("admin", "admin", false);
    }

    /**
     * Disconnects a user from the application.
     *
     */
    public void logout() {
        POST("/user/logout");
    }

    /**
     * Extracts the authentication token of the response.
     *
     * @param response Response
     * @return Authentication token
     */
    public String getAuthenticationCookie(Response response) {
        String authToken = null;
        for (NewCookie cookie : response.getCookies().values()) {
            if (TokenBasedSecurityFilter.COOKIE_NAME.equals(cookie.getName())) {
                authToken = cookie.getValue();
            }
        }
        return authToken;
    }

    protected void GET(String url, Map<String, String> queryParams) {
        WebTarget resource = target().path(url);
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            resource = resource.queryParam(entry.getKey(), entry.getValue());
        }
        response = builder(resource).get();
    }

    protected void GET(String resource) {
        GET(resource, new HashMap<>());
        addCookiesFromResponse();
    }

    protected void PUT(String url, Map<String, String> putParams) {
        WebTarget resource = target().path(url);
        Form form = new Form();
        for (Map.Entry<String, String> entry : putParams.entrySet()) {
            form = form.param(entry.getKey(), entry.getValue());
        }
        response = builder(resource).put(Entity.form(form));
        addCookiesFromResponse();
    }

    protected void PUT(String url, Multimap<String, String> putParams) {
        WebTarget resource = target().path(url);
        Form form = new Form();
        for (Map.Entry<String, String> entry : putParams.entries()) {
            form = form.param(entry.getKey(), entry.getValue());
        }
        response = builder(resource).put(Entity.form(form));
        addCookiesFromResponse();
    }

    protected void PUT(String url, Map<String, String> putParams, Map<String, File> files) {
        WebTarget resource = target()
                .register(MultiPartFeature.class)
                .path(url);
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        List<InputStream> isList = new ArrayList<>();
        try {
            for (Map.Entry<String, File> file : files.entrySet()) {
                InputStream is = new FileInputStream(file.getValue());
                isList.add(is);
                formDataMultiPart.bodyPart(new StreamDataBodyPart(file.getKey(), is, file.getValue().getName()));
            }
            response = builder(resource).put(Entity.entity(formDataMultiPart,
                    MediaType.MULTIPART_FORM_DATA_TYPE));
            addCookiesFromResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            for (InputStream is : isList) {
                try {
                    is.close();
                } catch (Exception e) {
                    // NOP
                }
            }
        }
    }

    protected void PUT(String url) {
        PUT(url, new HashMap<>());
    }

    protected void POST(String url, Map<String, String> postParams) {
        WebTarget resource = target().path(url);
        Form form = new Form();
        for (Map.Entry<String, String> entry : postParams.entrySet()) {
            form = form.param(entry.getKey(), entry.getValue());
        }
        response = builder(resource).post(Entity.form(form));
        addCookiesFromResponse();
    }

    protected void POST(String url, Multimap<String, String> postParams) {
        WebTarget resource = target().path(url);
        Form form = new Form();
        for (Map.Entry<String, String> entry : postParams.entries()) {
            form = form.param(entry.getKey(), entry.getValue());
        }
        response = builder(resource).post(Entity.form(form));
        addCookiesFromResponse();
    }

    protected void POST(String url) {
        POST(url, new HashMap<>());
    }

    protected void DELETE(String url) {
        WebTarget resource = target().path(url);
        response = builder(resource).delete(Response.class);
        addCookiesFromResponse();
    }

    protected Invocation.Builder builder(WebTarget resource) {
        Invocation.Builder builder = resource.request();
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            builder.cookie(entry.getKey(), entry.getValue());
        }
        return builder;
    }

    private void addCookiesFromResponse() {
        for (Cookie cookie : response.getCookies().values()) {
            if (cookie.getName().equals(TokenBasedSecurityFilter.COOKIE_NAME)) {
                cookies.put(cookie.getName(), cookie.getValue());
            }
        }
    }

    protected JsonObject getJsonResult() {
        return response.readEntity(JsonObject.class);
    }

    protected String getItemId() {
        JsonObject json;
        json = getJsonResult();
        JsonObject item = json.getJsonObject("item");
        return item.getString("id");
    }
}
