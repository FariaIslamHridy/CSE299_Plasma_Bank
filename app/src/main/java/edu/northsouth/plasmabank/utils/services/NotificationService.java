package edu.northsouth.plasmabank.utils.services;

//import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;

public class NotificationService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
//        Log.d("onNewToken", "onNewToken : "+token);

        super.onNewToken(token);
    }
}
