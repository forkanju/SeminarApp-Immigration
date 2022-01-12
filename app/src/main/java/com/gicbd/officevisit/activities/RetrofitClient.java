package com.gicbd.officevisit.activities;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;


public interface RetrofitClient {
    //String QRCode = "75w";
    @Headers({"pass:1234"})
    @GET("{scan_qr}")
    Call<Response> getResponse(@Path("scan_qr") String scan_qr);  //Call<User> // here User is Main Response Model Class

}
