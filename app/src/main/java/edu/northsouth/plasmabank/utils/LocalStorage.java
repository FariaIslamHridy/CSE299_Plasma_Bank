package edu.northsouth.plasmabank.utils;

/*
*
* this class is a wrapper for shared preference
* using this class we store authentication information and some of the user information
* not only store the user information
* we retrieve data by using this class
*
*/

import android.content.Context;
import android.content.SharedPreferences;

import edu.northsouth.plasmabank.models.Location;
import edu.northsouth.plasmabank.models.User;

public class LocalStorage {
    SharedPreferences sharedPreferences;
    Context context;

    public LocalStorage(Context context) {
        this.context = context;

        sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
    }

    public void saveUserData(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", user.getName());
        editor.putString("blood", user.getBlood());
        editor.putString("phone", user.getPhone());
        editor.putString("email", user.getEmail());

        editor.putString("latitude", String.valueOf(user.getLocation().getLatitude()));
        editor.putString("longitude", String.valueOf(user.getLocation().getLongitude()));
        editor.putString("city", String.valueOf(user.getLocation().getCity()));

        editor.putInt("sex", user.getSex());
        editor.putString("type", user.getType());

        editor.apply();
    }

    public User getUserData() {
        String name = sharedPreferences.getString("name", "");
        String blood = sharedPreferences.getString("blood", "");
        String phone = sharedPreferences.getString("phone", "");
        String email = sharedPreferences.getString("email", "");

        double latitude = Double.parseDouble(sharedPreferences.getString("latitude", "0.0"));
        double longitude = Double.parseDouble(sharedPreferences.getString("longitude", "0.0"));
        String city = sharedPreferences.getString("city", "");


        int sex = sharedPreferences.getInt("sex", 1);
        String type = sharedPreferences.getString("type", "");

        return  new User(name, sex, type, blood, phone, email, new Location(latitude, longitude, city));
    }

    public String getUserType () {
        return sharedPreferences.getString("type", "");
    }

    public String getBloodGroup() {
        return sharedPreferences.getString("blood", "");
    }
}
