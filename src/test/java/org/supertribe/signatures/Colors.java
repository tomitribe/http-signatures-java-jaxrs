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

import org.tomitribe.util.Join;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("colors")
@Singleton
@Lock(LockType.READ)
public class Colors {

    @GET
    @Path("preferred")
    public String preferred() {
        return "orange";
    }

    @GET
    @Path("hsb")
    public String hsb(@QueryParam("hue") short hue, @QueryParam("saturation") float saturation, @QueryParam("brightness") float brightness) {
        return Join.join(":", hue, saturation, brightness);
    }

    @POST
    @Path("preferred")
    public String preferredPost(final String c) {
        return c;
    }

    @PUT
    @Path("preferred")
    public String preferredPut(final String c) {
        return c;
    }

    @GET
    @Path("refused")
    @RolesAllowed("not usable role")
    public String refused() {
        throw new IllegalStateException("Should never reach this exception");
    }

    @GET
    @Path("authorized")
    @RolesAllowed("exploitation")
    public String onlyIfAllowed() {
        return "you rock guys";
    }
}
