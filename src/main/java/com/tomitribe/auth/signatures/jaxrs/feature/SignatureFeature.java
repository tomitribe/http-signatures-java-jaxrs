/*
 * Tomitribe Confidential
 *
 * Copyright(c) Tomitribe Corporation. 2016
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package com.tomitribe.auth.signatures.jaxrs.feature;

import com.tomitribe.auth.signatures.jaxrs.filter.SignatureFilter;
import lombok.Builder;

import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.io.UnsupportedEncodingException;

/**
 * Activates {@see com.tomitribe.auth.signatures.jaxrs.filter.SignatureFilter}
 */
@Builder
public class SignatureFeature implements Feature {
    private String header;
    private final String key;
    private final String alias;
    private final String algorithm;
    private final String[] headers;

    @Override
    public boolean configure(final FeatureContext context) {
        try {
            context.register(new SignatureFilter(header, new SecretKeySpec(key.getBytes("UTF-8"), algorithm), alias, algorithm, headers));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 should be supported");
        }
        return true;
    }
}
