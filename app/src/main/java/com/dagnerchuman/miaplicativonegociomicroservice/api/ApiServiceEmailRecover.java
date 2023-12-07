// ApiServiceEmailRecover.java
package com.dagnerchuman.miaplicativonegociomicroservice.api;

import com.dagnerchuman.miaplicativonegociomicroservice.entity.EmailValuesDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiServiceEmailRecover {

    String baseUser = "gateway/email";

    @POST( baseUser + "/send-html")
    Call<Void> sendEmail(@Body EmailValuesDto emailValuesDto);
}
