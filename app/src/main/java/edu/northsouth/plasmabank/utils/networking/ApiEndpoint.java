package edu.northsouth.plasmabank.utils.networking;

import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiEndpoint {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA2rfGzCM:APA91bHiQdoBfy7eZhU4g0DZvrdV2G_nYmZPOvEjl3KKCQpEBxVFwrq8qhMkYiuq6Z1u0OrFoRh_wISS4sGuL2wOLfy7DNdAJqIGB-1scPY3BvTerTEilfrWHJEEXOZy7WRpioH1ebtU"
            }
    )
    @POST("fcm/send")
    Call<Response> sendNotification(@Body RequestBody body);

}

