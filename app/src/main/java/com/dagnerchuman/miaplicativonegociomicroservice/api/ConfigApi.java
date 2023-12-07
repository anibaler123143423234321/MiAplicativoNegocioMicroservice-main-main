package com.dagnerchuman.miaplicativonegociomicroservice.api;

import android.content.Context;
import android.content.SharedPreferences;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConfigApi {
    private static final String BASE_URL = "https://dotval-app-6908ca550bb8.herokuapp.com/";
    private static final String SHARED_PREFERENCES_NAME = "UserData";
    private static final String KEY_AUTH_TOKEN = "userToken";

    private static ApiService apiService;
    private static ApiServiceNegocio apiServiceNegocio;
    private static ApiServiceProductos apiServiceProducto;
    private static ApiServiceCompras apiServiceCompra;
    private static ApiServiceCategorias apiServiceCategorias;
    private static ApiServiceDispositivo apiServiceDispositivo;
    private static ApiServiceEmailRecover apiServiceEmailRecover;

    private static ApiServiceDni apiServiceDni;

    private static String authToken = "";

    public static ApiService getInstance(Context context) {
        OkHttpClient client = createOkHttpClient(context);

        if (apiService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    public static ApiServiceNegocio getInstanceNegocio(Context context) {
        OkHttpClient client = createOkHttpClient(context);

        if (apiServiceNegocio == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiServiceNegocio = retrofit.create(ApiServiceNegocio.class);
        }
        return apiServiceNegocio;
    }

    public static ApiServiceProductos getInstanceProducto(Context context) {
        OkHttpClient client = createOkHttpClient(context);

        if (apiServiceProducto == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiServiceProducto = retrofit.create(ApiServiceProductos.class);
        }
        return apiServiceProducto;
    }

    public static ApiServiceCompras getInstanceCompra(Context context) {
        OkHttpClient client = createOkHttpClient(context);

        if (apiServiceCompra == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiServiceCompra = retrofit.create(ApiServiceCompras.class);
        }
        return apiServiceCompra;
    }

    public static ApiServiceCategorias getInstanceCategorias(Context context) {
        OkHttpClient client = createOkHttpClient(context);

        if (apiServiceCategorias == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiServiceCategorias = retrofit.create(ApiServiceCategorias.class);
        }
        return apiServiceCategorias;
    }

    public static ApiServiceDispositivo getInstanceDispositivo(Context context) {
        OkHttpClient client = createOkHttpClient(context);

        if (apiServiceDispositivo == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiServiceDispositivo = retrofit.create(ApiServiceDispositivo.class);
        }
        return apiServiceDispositivo;
    }


    public static ApiServiceEmailRecover getInstanceEmailRecover(Context context) {
        OkHttpClient client = createOkHttpClient(context);

        if (apiServiceEmailRecover == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiServiceEmailRecover = retrofit.create(ApiServiceEmailRecover.class);
        }
        return apiServiceEmailRecover;
    }

    private static OkHttpClient createOkHttpClient(Context context) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        if (!authToken.isEmpty()) {
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", "Bearer " + authToken);
                Request request = requestBuilder.build();
                return chain.proceed(request);
            });
        }
        return httpClient.build();
    }

    public static void setAuthToken(String token) {
        authToken = token;
    }
}
