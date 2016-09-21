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

import com.tomitribe.auth.signatures.jaxrs.filter.DateFilter;
import lombok.Builder;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * Activates {@see com.tomitribe.auth.signatures.jaxrs.filter.DateFilter}
 */
@Builder
public class DateFeature implements Feature {
    private String header;
    private String format;

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(new DateFilter(header, format));
        return true;
    }

    /**
     * @return a default DateFeature.
     */
    public static DateFeature auto() {
        return new DateFeature(null, null);
    }
}
