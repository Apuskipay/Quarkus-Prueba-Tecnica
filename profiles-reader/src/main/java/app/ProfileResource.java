package app;

import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.inject.Inject;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class ProfileResource
{
    @Inject JsonWebToken jwt;

    @GET
    @Path("get-profile")
    @Authenticated
    public Response get (@QueryParam("email") String email, @QueryParam("id") String id)
    {
        if (jwt==null || !"read:profile".equals(jwt.getClaim("scope")))
        {
            return Response.status(403).entity("{\"error\":\"insufficient scope\"}").build();
        }

        Profile profile = null;
        if (email!=null && !email.isBlank())
        {
            profile = Profile.find("email", email).firstResult();
        }
        else if (id!=null && !id.isBlank())
        {
            profile = Profile.findById(new ObjectId(id));
        }
        else
        {
            throw new BadRequestException("Provide email or id");
        }

        if (profile==null) throw  new NotFoundException("profile not found");
        return Response.ok(profile).build();
    }
}
