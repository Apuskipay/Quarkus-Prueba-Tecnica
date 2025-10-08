package app;

import io.quarkus.security.Authenticated;
import io.smallrye.jwt.build.Jwt;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.print.attribute.standard.Media;
import java.time.Duration;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProfileResources
{
    @ConfigProperty(name="jwt.issuer", defaultValue = "profiles-crud") String issuer;
    @ConfigProperty(name="jwt.audience", defaultValue = "profiles-reader") String audience;
    @ConfigProperty(name="jwt.ttl.seconds", defaultValue = "900") long ttlSeconds;

    @POST
    @Path("create-profile")
    @Authenticated
    public Response create (Profile dto)
    {
        if(dto==null || dto.email==null)
        {
            throw new BadRequestException("Invalid payload");
        }
        if(Profile.find("email", dto.email).firstResult()!=null)
        {
            return Response.status(409).entity(Map.of("error", "Email already exists")).build();
        }

        Profile.persist(dto);
        String id = dto.id.toString();
        String token = Jwt.issuer(issuer).audience(audience).subject(id)
                .claim("scope","read:profile")
                .expiresIn(Duration.ofSeconds(ttlSeconds))
                .sign(); //firma con privateKey.pem
        return Response.status(201).entity(Map.of("id", id, "token", token)).build();
    }

    @DELETE
    @Path("delete-profile/{id}")
    @Authenticated
    public Response delete(@PathParam("id") String id)
    {
        boolean ok = Profile.deleteById(new ObjectId(id));

        if(!ok)
        {
            throw  new NotFoundException("Profile not found");
        }

        return Response.noContent().build();
    }
}
