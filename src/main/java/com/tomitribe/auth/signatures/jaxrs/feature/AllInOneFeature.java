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

import lombok.Builder;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * All in one feature for date, signature and digest ones.
 */
@Builder
public class AllInOneFeature implements Feature {
    private String dateHeader;
    private String dateFormat;
    private String digestHeader;
    private String digestAlgorithm;
    private String signatureHeader;
    private final String key;
    private final String algorithm;
    private final String alias;
    private final String[] headers;

    @Override
    public boolean configure(final FeatureContext context) {
        for (final Feature f : new Feature[]{
                DateFeature.builder().header(dateHeader).format(dateFormat).build(),
                DigestFeature.builder().header(digestHeader).algorithm(digestAlgorithm).build(),
                SignatureFeature.builder().header(signatureHeader).key(key).algorithm(algorithm).alias(alias).headers(headers).build()
        }) {
            f.configure(context);
        }
        return true;
    }
}
