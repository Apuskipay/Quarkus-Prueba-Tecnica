package app;

import jakarta.validation.constraints.*;

public class CreateProfileDTO
{
    @NotBlank(message = "name is required")
    @Size(max = 80, message = "name max length is 80")
    public String name;

    @NotBlank(message = "lastName is required")
    @Size(max = 80, message = "lastName max length is 80")
    public String lastName;

    @NotBlank(message = "cellphone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "cellphone must be E.164 (e.g. +18095551234)")
    public String cellphone;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email")
    @Size(max = 254, message = "email max length is 254")
    public String email;

    @Size(max = 200, message = "address max length is 200")
    public String address;
}
