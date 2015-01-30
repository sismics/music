package com.sismics.music.rest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.After;
import org.junit.Before;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import com.sismics.music.rest.util.ClientUtil;
import com.sismics.util.dbi.DBIF;
import com.sismics.util.filter.RequestContextFilter;
import com.sismics.util.filter.TokenBasedSecurityFilter;

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
     * Utility class for the REST client.
     */
    protected ClientUtil clientUtil;
    
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
    
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        clientUtil = new ClientUtil(target());
        
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
        reg.setLoadOnStartup(1);
        reg.addMapping("/*");
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
     * @throws MessagingException
     * @throws IOException
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
     * @throws MessagingException
     * @throws IOException
     */
    protected String encodeQuotedPrintable(String input) throws MessagingException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = MimeUtility.encode(baos, "quoted-printable");
        os.write(input.getBytes());
        os.close();
        return baos.toString();
    }
}
