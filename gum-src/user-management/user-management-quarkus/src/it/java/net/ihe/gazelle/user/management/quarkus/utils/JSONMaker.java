package net.ihe.gazelle.user.management.quarkus.utils;

public class JSONMaker {

    public static String makeOrganization(String shortname, String name) {
        return "{\"shortname\": \"" + shortname + "\",\"name\": \"" + name + "\"}";
    }

    public static String makeOrganization(String id, String shortname, String name) {
        return "{\"id\": \"" + id + "\",\"shortname\": \"" + shortname + "\",\"name\": \"" + name + "\"}";
    }

    public static String makeUserCreation(String firstname, String lastname, String email, String organizationShortname, String organizationName) {
        return "{\"lastName\":\"" + lastname + "\", \"firstName\":\"" + firstname + "\"," +
                " \"email\":\"" + email + "\", \"organization\":{ " +
                " \"shortname\":\"" + organizationShortname + "\"," +
                " \"name\":\"" + organizationName + "\"" +
                "}," +
                " \"password\":\"Password2&\"," +
                " \"passwordConfirmation\":\"Password2&\"," +
                " \"consent\": true}";
    }

    public static String makeUserRegistration(String firstname, String lastname, String email, String organizationId) {
        return "{\"lastName\":\"" + lastname + "\", \"firstName\":\"" + firstname + "\"," +
                " \"email\":\"" + email + "\", \"organizationId\":\"" + organizationId + "\"," +
                " \"password\":\"Password1&\"," +
                " \"passwordConfirmation\":\"Password1&\"," +
                " \"consent\": true}";
    }
}
