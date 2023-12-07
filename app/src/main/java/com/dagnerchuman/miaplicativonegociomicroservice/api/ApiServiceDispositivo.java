package com.dagnerchuman.miaplicativonegociomicroservice.api;

import com.dagnerchuman.miaplicativonegociomicroservice.entity.Dispositivo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiServiceDispositivo {

    String baseUser = "/api/gateway/dispositivo";

    @POST(baseUser + "/saveDevice")
    Call<Dispositivo> registerDevice(@Body Dispositivo dispositivo);




}
