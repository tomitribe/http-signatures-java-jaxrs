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
import com.tomitribe.auth.signatures.jaxrs.internal.pool.LightPool;
import org.tomitribe.auth.signatures.Base64;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

/**
 * Technical filter to stream the output payload and
 * compute the digest header.
 * <p/>
 * The header will be automatically added when the Stream gets closed.
 */
@Priority(Priorities.AUTHENTICATION - 5)
public class DigestFilter implements ClientRequestFilter {
    static final String KEY = DigestFilter.class.getName() + ".stream";

    private final String header;
    private final String digest;

    private String emptyDigest; // cached
    private final LightPool<MessageDigest> messageDigests = new LightPool<MessageDigest>() {
        @Override
        protected MessageDigest create() {
            try {
                return MessageDigest.getInstance(digest);
            } catch (final NoSuchAlgorithmException e) {
                throw new IllegalArgumentException(e);
            }
        }
    };

    public DigestFilter(final String header, final String digest) {
        this.header = header == null ? "Digest" : header;
        this.digest = digest == null ? "sha1" : digest;
        messageDigests.consume(new LightPool.Consumer<MessageDigest>() {
            @Override
            public void accept(final MessageDigest instance) {
                emptyDigest = digest + "=" + new String(Base64.encodeBase64(instance.digest("".getBytes())));
            }
        });
    }

    @Override
    public void filter(final ClientRequestContext requestContext) throws IOException {
        messageDigests.consume(new LightPool.Consumer<MessageDigest>() {
            @Override
            public void accept(final MessageDigest md) {
                final OutputStream entityStream = requestContext.getEntityStream();
                if (entityStream == null) {
                    requestContext.getHeaders()
                            .put(header, Collections.<Object>singletonList(emptyDigest));
                } else {
                    // Note: this doesn't work with chunking so adjust payload max size, should we allow to slurp the payload whatever memory it takes?
                    final TwoPhaseCloseDigestOutputStream doc = new TwoPhaseCloseDigestOutputStream(
                            entityStream, md,
                            new TwoPhaseCloseDigestOutputStream.OnClose() {
                                @Override
                                public void onClose(final byte[] digest) {
                                    requestContext.getHeaders()
                                            .put(header, Collections.<Object>singletonList(toHeader(digest)));
                                }
                            });
                    requestContext.setProperty(KEY, doc);
                    requestContext.setEntityStream(doc);
                }
            }
        });
    }

    private String toHeader(final byte[] value) {
        return digest + "=" + new String(Base64.encodeBase64(value));
    }
}
