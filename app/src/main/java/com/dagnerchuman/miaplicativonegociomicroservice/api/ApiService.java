package com.dagnerchuman.miaplicativonegociomicroservice.api;

import com.dagnerchuman.miaplicativonegociomicroservice.entity.User;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
    String baseUser = "api/authentication";
    String user = "api/user";
    String userPath = user + "/{userId}";

    @POST(baseUser + "/sign-in")
    Call<User> signIn(@Body User user);

    @Headers("Authorization: Bearer")
    @GET(user)
    Call<User> getCurrentUser();

    @POST(baseUser + "/sign-up")
    Call<User> signUp(@Body User user);

    @Headers("Authorization: Bearer")
    @GET(userPath)
    Call<User> getUsuarioById(@Path("userId") Long userId);

    @Headers("Authorization: Bearer")
    @DELETE(userPath)
    Call<Void> deleteUserById(@Path("userId") Long userId);

    @Multipart
    @Headers("Authorization: Bearer")
    @PUT(userPath)
    Call<User> updateUserPicture(@Path("userId") Long userId, @Part MultipartBody.Part picture);

    @Headers("Authorization: Bearer")
    @PUT(userPath)
    Call<User> updateUser(@Path("userId") Long userId, @Body User user);
}
