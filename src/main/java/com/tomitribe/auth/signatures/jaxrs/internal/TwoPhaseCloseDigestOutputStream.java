/*
 * Tomitribe Confidential
 *
 * Copyright(c) Tomitribe Corporation. 2016
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package com.tomitribe.auth.signatures.jaxrs.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

public class TwoPhaseCloseDigestOutputStream extends DigestOutputStream {
    private final OnClose callback;
    private boolean digested;

    public TwoPhaseCloseDigestOutputStream(final OutputStream out, final MessageDigest instance, final OnClose callback) {
        super(out, instance);
        this.callback = callback;
    }

    @Override
    public void write(final int b) throws IOException {
        super.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        super.write(b, off, len);
    }

    @Override
    public void write(final byte[] b) throws IOException {
        super.write(b);
    }

    @Override
    public void flush() throws IOException {
        if (out != null) {
            super.flush();
        }
    }

    @Override
    public void close() throws IOException {
        if (!digested) {
            addDigestHeader();
        }
        if (out != null) {
            super.close();
        }
    }

    public void addDigestHeader() {
        callback.onClose(getMessageDigest().digest());
        digested = true;
    }

    public interface OnClose {
        void onClose(final byte[] digest);
    }
}
