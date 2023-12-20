package com.dagnerchuman.miaplicativonegociomicroservice.api;

import com.dagnerchuman.miaplicativonegociomicroservice.entity.Dispositivo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiServiceDispositivo {

    String baseUser = "/api/gateway/dispositivo";

    @POST(baseUser + "/saveDevice")
    Call<Dispositivo> registerDevice(@Body Dispositivo dispositivo);

    @POST(baseUser + "/sendNotification/{deviceId}")
    Call<String> sendNotification(@Path("deviceId") int deviceId, @Body Object notification);


}
