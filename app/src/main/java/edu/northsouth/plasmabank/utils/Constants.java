package edu.northsouth.plasmabank.utils;

public class Constants {

    public static String[] BLOOD_GROUPS = {
            " ",
            "A+(ve)", "B+(ve)", "O+(ve)", "AB+(ve)",
            "A-(ne)", "B-(ne)", "O-(ne)", "AB-(ne)"
    };

    public static String RECEIVER = "Receiver";

    public static String DONOR = "Donor";

    public static int getPosition(String group) {

        int result = 0;

        for (int i = 0; i < BLOOD_GROUPS.length; i++) {
            if(BLOOD_GROUPS[i].equals(group)) {
                result = i;
                break;
            }
        }

        return result;
    }

    public static boolean getUserType(String type) {
        if(type.equals(DONOR)) {
            return true;
        } else {
            return false;
        }
    }
}
