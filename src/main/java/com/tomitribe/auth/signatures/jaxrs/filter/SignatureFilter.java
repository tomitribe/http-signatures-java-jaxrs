/*
 * Tomitribe Confidential
 *
 * Copyright(c) Tomitribe Corporation. 2016
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package com.tomitribe.auth.signatures.jaxrs.filter;

import com.tomitribe.auth.signatures.jaxrs.internal.TwoPhaseCloseDigestOutputStream;
import org.tomitribe.auth.signatures.Algorithm;
import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.Signer;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Priority(Priorities.AUTHENTICATION - 1)
public class SignatureFilter implements ClientRequestFilter {
    private final String header;
    private final Signer signer;

    public SignatureFilter(final String header, final Key key, final String keyAlias, final String algorithm, final String[] headers) {
        this.header = header == null ? "Authorization" : header;
        this.signer = new Signer(key, new Signature(keyAlias, Algorithm.get(algorithm), null, headers));
    }

    @Override
    public void filter(final ClientRequestContext requestContext) throws IOException {
        final OutputStream out = requestContext.getEntityStream();
        if (out == null) {
            requestContext.getHeaders().put(header, Collections.<Object>singletonList(getAuthorization(requestContext)));
        } else {
            final OutputStream dos = new FilterOutputStream(out) {
                @Override
                public void write(final byte[] b, final int off, final int len) throws IOException {
                    out.write(b, off, len);
                }

                @Override
                public void write(final byte[] b) throws IOException {
                    out.write(b);
                }

                @Override
                public void close() throws IOException {
                    final MultivaluedMap<String, Object> headers = requestContext.getHeaders();

                    // if Digest is used, force the stream to close so that the digest header gets added and no one
                    // else can change it
                    final TwoPhaseCloseDigestOutputStream dos = TwoPhaseCloseDigestOutputStream.class.cast(requestContext.getProperty(DigestFilter.KEY));
                    if (dos != null) {
                        dos.addDigestHeader();
                    }

                    headers.put(header, Collections.<Object>singletonList(getAuthorization(requestContext)));
                    super.close();
                }
            };
            requestContext.setEntityStream(dos);
        }
    }

    private String getAuthorization(final ClientRequestContext message) {
        final String existing = message.getHeaderString("authorization");

        // if there is already an header let it be
        if (existing != null && !existing.isEmpty()) {
            return existing;
        }

        final URL url;
        try {
            url = message.getUri().toURL();
        } catch (final MalformedURLException e) {
            throw new IllegalStateException(e);
        }

        try {
            return sign(message.getMethod(), url.getFile(), message.getStringHeaders());
        } catch (final NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String sign(final String method, final String uri, final Map<String, List<String>> headers) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        final Map<String, String> h = new HashMap<>(headers != null ? headers.size() : 0);
        if (headers != null) {
            for (final Map.Entry<String, List<String>> e : headers.entrySet()) {
                final List<String> value = e.getValue();
                if (value != null && !value.isEmpty()) {
                    h.put(e.getKey(), value.iterator().next());
                }
            }
        }
        return signer.sign(method, uri, h).toString();
    }

}
