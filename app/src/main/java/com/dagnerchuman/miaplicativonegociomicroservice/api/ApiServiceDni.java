package com.dagnerchuman.miaplicativonegociomicroservice.api;

import com.dagnerchuman.miaplicativonegociomicroservice.entity.DniResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiServiceDni {

    @GET("dni/{dni}")
    Call<DniResponse> getDniData(@Path("dni") String dni, @Query("token") String token);
}
