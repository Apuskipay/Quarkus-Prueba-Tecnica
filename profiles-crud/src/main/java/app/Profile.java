package app;

import io.quarkus.mongodb.panache.PanacheMongoEntity;

public class Profile extends PanacheMongoEntity
{
    public String name;
    public String lastName;
    public String cellphone;
    public String email;
    public String address;
}