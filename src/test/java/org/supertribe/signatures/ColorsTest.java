/*
 * Tomitribe Confidential
 *
 * Copyright(c) Tomitribe Corporation. 2016
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package org.supertribe.signatures;

import com.tomitribe.auth.signatures.jaxrs.feature.AllInOneFeature;
import com.tomitribe.auth.signatures.jaxrs.feature.DateFeature;
import com.tomitribe.auth.signatures.jaxrs.feature.DigestFeature;
import com.tomitribe.auth.signatures.jaxrs.feature.SignatureFeature;
import org.apache.cxf.feature.LoggingFeature;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class ColorsTest {

    private static final String DIGEST_ALGORITHM = "sha1";
    private static final String[] SIGNATURE_HEADERS = {"(request-target)", "date", "digest"};

    /**
     * Build the web archive to test.
     *
     * @return The archive to deploy for the test
     */
    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "colors.war")
                .addPackages(true, "org.supertribe.signatures")
                .addAsManifestResource(new ClassLoaderAsset("META-INF/context.xml"), "context.xml");
    }

    /**
     * Arquillian will boot an instance of Tribestream with a random port. The URL with the random port is injected
     * into this field.
     */
    @ArquillianResource
    private URL webapp;

    /**
     * Tests accessing a signatures protected method with a GET request
     *
     * @throws Exception when test fails or an error occurs
     */
    @Test
    public void success() throws Exception {
        final Client client = newClient();
        try {
            assertEquals(
                    "orange",
                    client.register(allInOneFeature())
                            .target(webapp.toExternalForm())
                            .path("api/colors/preferred")
                            .request(MediaType.WILDCARD_TYPE)
                            .get(String.class));
        } finally {
            client.close();
        }
    }

    @Test
    public void features() throws Exception {
        final Client client = newClient();
        try {
            assertEquals(
                    "orange",
                    client.register(SignatureFeature.builder()
                            .key(KeystoreInitializer.SECRET)
                            .algorithm(KeystoreInitializer.ALGO)
                            .alias(KeystoreInitializer.KEY_ALIAS)
                            .headers(SIGNATURE_HEADERS)
                            .build())
                            .register(DigestFeature.builder().algorithm(DIGEST_ALGORITHM).build())
                            .register(DateFeature.auto())
                            .target(webapp.toExternalForm())
                            .path("api/colors/preferred")
                            .request(MediaType.WILDCARD_TYPE)
                            .get(String.class));
        } finally {
            client.close();
        }
    }

    /**
     * Tests accessing a signatures protected method with a GET request
     *
     * @throws Exception when test fails or an error occurs
     */
    @Test
    public void successQueryParams() throws Exception {
        final Client client = newClient();
        try {
            assertEquals(
                    "330:0.5:0.8",
                    client.register(allInOneFeature())
                            .target(webapp.toExternalForm())
                            .path("api/colors/hsb")
                            .queryParam("hue", 330)
                            .queryParam("saturation", .5)
                            .queryParam("brightness", .8)
                            .request(MediaType.WILDCARD_TYPE)
                            .get(String.class));
        } finally {
            client.close();
        }
    }

    /**
     * Tests accessing a signatures protected method with a POST request
     *
     * @throws Exception when test fails or an error occurs
     */
    @Test
    public void successPost() throws Exception {
        final Client client = newClient();
        try {
            assertEquals(
                    "Hello",
                    client.register(allInOneFeature())
                            .target(webapp.toExternalForm())
                            .path("api/colors/preferred")
                            .request(MediaType.WILDCARD_TYPE)
                            .post(Entity.entity("Hello", MediaType.WILDCARD_TYPE), String.class));
        } finally {
            client.close();
        }
    }

    /**
     * Tests accessing a signatures protected method with a PUT request
     *
     * @throws Exception when test fails or an error occurs
     */
    @Test
    public void successPut() throws Exception {
        final Client client = newClient();
        try {
            assertEquals(
                    "World",
                    client.register(allInOneFeature())
                            .target(webapp.toExternalForm())
                            .path("api/colors/preferred")
                            .request(MediaType.WILDCARD_TYPE)
                            .put(Entity.entity("World", MediaType.WILDCARD_TYPE), String.class));
        } finally {
            client.close();
        }
    }

    /**
     * Tests accessing a signatures protected method with a key that doesn't not have access to the resource
     *
     * @throws Exception when test fails or an error occurs
     */
    @Test(expected = ForbiddenException.class)
    public void fail() throws Exception {
        final Client client = newClient();
        try {
            client.register(allInOneFeature())
                    .target(webapp.toExternalForm())
                    .path("api/colors/refused")
                    .request(MediaType.WILDCARD_TYPE)
                    .get(String.class);
        } finally {
            client.close();
        }
    }

    /**
     * Tests accessing a signatures protected method with a GET request to a resource that requires a role
     *
     * @throws Exception when test fails or an error occurs
     */
    @Test
    public void authorized() throws Exception {
        final Client client = newClient();
        try {
            assertEquals(
                    "you rock guys",
                    client.register(allInOneFeature())
                            .target(webapp.toExternalForm())
                            .path("api/colors/authorized")
                            .request(MediaType.WILDCARD_TYPE)
                            .get(String.class));
        } finally {
            client.close();
        }
    }

    @Test(expected = ClientErrorException.class) // missing date header
    public void missingDate() throws Exception {
        final Client client = newClient();
        try {
            client.register(SignatureFeature.builder()
                    .key(KeystoreInitializer.SECRET)
                    .algorithm(KeystoreInitializer.ALGO)
                    .alias(KeystoreInitializer.KEY_ALIAS)
                    .headers(SIGNATURE_HEADERS))
                    .register(DigestFeature.builder().algorithm(DIGEST_ALGORITHM).build())
                    .target(webapp.toExternalForm())
                    .path("api/colors/authorized")
                    .request(MediaType.WILDCARD_TYPE)
                    .get(String.class);
        } finally {
            client.close();
        }
    }

    @Test(expected = ForbiddenException.class)
    public void missingDigest() throws Exception {
        final Client client = newClient();
        try {
            client.register(SignatureFeature.builder()
                    .key(KeystoreInitializer.SECRET)
                    .algorithm(KeystoreInitializer.ALGO)
                    .alias(KeystoreInitializer.KEY_ALIAS)
                    .headers(SIGNATURE_HEADERS))
                    .register(DateFeature.builder().build())
                    .target(webapp.toExternalForm())
                    .path("api/colors/authorized")
                    .request(MediaType.WILDCARD_TYPE)
                    .get(String.class);
        } finally {
            client.close();
        }
    }

    @Test(expected = ForbiddenException.class)
    public void missingSignatureWithRoleChecking() throws Exception {
        final Client client = newClient();
        try {
            client.register(DigestFeature.builder().algorithm(DIGEST_ALGORITHM).build())
                    .register(DateFeature.builder().build())
                    .target(webapp.toExternalForm())
                    .path("api/colors/authorized")
                    .request(MediaType.WILDCARD_TYPE)
                    .get(String.class);
        } finally {
            client.close();
        }
    }

    @Test
    // yes, no exception cause no authorization header so the security in tomcat is a passthrough! check org.apache.catalina.authenticator.AuthenticatorBase
    public void missingSignatureNoRole() throws Exception {
        final Client client = newClient();
        try {
            assertEquals(
                    "orange",
                    client.register(DigestFeature.builder().algorithm(DIGEST_ALGORITHM).build())
                            .register(DateFeature.builder().build())
                            .target(webapp.toExternalForm())
                            .path("api/colors/preferred")
                            .request(MediaType.WILDCARD_TYPE)
                            .get(String.class));
        } finally {
            client.close();
        }
    }

    private AllInOneFeature allInOneFeature() {
        return AllInOneFeature.builder()
                .digestAlgorithm(DIGEST_ALGORITHM)
                .key(KeystoreInitializer.SECRET)
                .algorithm(KeystoreInitializer.ALGO)
                .alias(KeystoreInitializer.KEY_ALIAS)
                .headers(SIGNATURE_HEADERS)
                .build();
    }

    private Client newClient() {
        final Client client = ClientBuilder.newClient();
        client.register(new LoggingFeature()); // to be able to debug on a CI
        return client;
    }
}
