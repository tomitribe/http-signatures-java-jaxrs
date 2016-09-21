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

import com.tomitribe.auth.signatures.jaxrs.internal.pool.LightPool;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

@Priority(Priorities.AUTHENTICATION - 10)
public class DateFilter implements ClientRequestFilter {
    private final String header;
    private final String format;

    // Avoid to leak if in a webapp
    private final LightPool<DateFormat> dateFormats = new LightPool<DateFormat>() {
        @Override
        protected DateFormat create() {
            return new SimpleDateFormat(format, Locale.US);
        }
    };

    public DateFilter(final String header, final String format) {
        this.header = header == null ? "Date" : header;
        this.format = format == null ? "EEE, dd MMM yyyy HH:mm:ss zzz" : format;
    }

    @Override
    public void filter(final ClientRequestContext requestContext) throws IOException {
        dateFormats.consume(new LightPool.Consumer<DateFormat>() {
            @Override
            public void accept(final DateFormat format) {
                final String date = format.format(new Date());
                requestContext.getHeaders().put(header, Collections.<Object>singletonList(date));
            }
        });
    }
}
