package app;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.*;
import java.util.stream.Collectors;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException>
{
    @Override
    public Response toResponse(ConstraintViolationException e)
    {
        List<Map<String, String>> errors = e.getConstraintViolations()
                .stream()
                .map(this::toError)
                .collect(Collectors.toList());

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Validation failed");
        body.put("errors", errors);

        return Response.status(Response.Status.BAD_REQUEST).entity(body).build();
    }

    private Map<String, String> toError(ConstraintViolation<?> v)
    {
        String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "";
        String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;

        Map<String, String> err = new LinkedHashMap<>();
        err.put("field", field);
        err.put("message", v.getMessage());
        return err;
    }
}
