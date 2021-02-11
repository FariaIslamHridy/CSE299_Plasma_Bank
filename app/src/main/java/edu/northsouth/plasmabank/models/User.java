package edu.northsouth.plasmabank.models;

public class User {
    private String name;
    private String blood;
    private String phone;
    private String email;
    private Location location;
    private int sex;
    private String type;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, int sex, String type, String blood, String phone, String email, Location location) {
        this.name = name;
        this.sex = sex;
        this.type = type;
        this.blood = blood;
        this.phone = phone;
        this.email = email;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public int getSex() {
        return sex;
    }

    public Location getLocation() {
        return location;
    }

    public String getBlood() {
        return blood;
    }


    public String getType() {
        return type;
    }

}
