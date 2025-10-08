package app;

import io.quarkus.security.Authenticated;
import io.smallrye.jwt.build.Jwt;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Locale;
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
    public Response create (@Valid CreateProfileDTO dto)
    {
        if(dto==null || dto.email==null)
        {
            throw new BadRequestException("Invalid payload");
        }
        if (Profile.find("email", dto.email).firstResult() != null)
        {
            return Response.status(409).entity(Map.of("error", "Email already exists")).build();
        }

        Profile profile = new Profile();
        profile.name = dto.name.trim();
        profile.lastName = dto.lastName.trim();
        profile.cellphone = dto.cellphone.trim();
        profile.email = dto.email.trim().toLowerCase(Locale.ROOT);
        profile.address = dto.address != null ? dto.address.trim() : null;
        profile.persist();

        String id = profile.id.toString();
        String token = Jwt.issuer(issuer).audience(audience).subject(id)
                .claim("scope","read:profile")
                .expiresIn(Duration.ofSeconds(ttlSeconds))
                .sign();
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

    @PUT
    @Path("/update-profile/{id}")
    @Authenticated
    public Response update(@PathParam("id") String id, @Valid UpdateProfileDTO dto)
    {
        ObjectId objectId;
        try
        {
            objectId = new ObjectId(id);
        }
        catch (Exception e)
        {
            throw new NotFoundException("Invalid id");
        }

        Profile profile = Profile.findById(objectId);
        if (profile == null)
        {
            throw new NotFoundException("Profile not found");
        }

        if (dto.name != null && dto.name.isBlank())
        {
            throw new BadRequestException("name cannot be blank");
        }
        if (dto.lastName != null && dto.lastName.isBlank())
        {
            throw new BadRequestException("lastName cannot be blank");
        }
        if (dto.cellphone != null && dto.cellphone.isBlank())
        {
            throw new BadRequestException("cellphone cannot be blank");
        }
        if (dto.email != null && dto.email.isBlank())
        {
            throw new BadRequestException("email cannot be blank");
        }
        if (dto.address != null && dto.address.isBlank())
        {
            throw new BadRequestException("address cannot be blank");
        }

        if (dto.email != null)
        {
            String newEmail = dto.email.trim().toLowerCase(Locale.ROOT);
            if (!newEmail.equals(profile.email))
            {
                if (Profile.find("email", newEmail).firstResult() != null)
                {
                    return Response.status(409).entity(Map.of("error", "Email already exists")).build();
                }
                profile.email = newEmail;
            }
        }
        if (dto.name != null)
        {
            profile.name = dto.name.trim();
        }
        if (dto.lastName != null)
        {
            profile.lastName = dto.lastName.trim();
        }
        if (dto.cellphone != null)
        {
            profile.cellphone = dto.cellphone.trim();
        }
        if (dto.address != null)
        {
            profile.address = dto.address.trim();
        }

        String token = Jwt.issuer(issuer).audience(audience).subject(id)
                .claim("scope","read:profile")
                .expiresIn(Duration.ofSeconds(ttlSeconds))
                .sign();

        profile.update();
        return Response.ok(profile).header("X-Token", token).build();
    }
}
