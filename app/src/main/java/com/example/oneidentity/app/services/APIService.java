package com.example.oneidentity.app.services;

import android.content.Context;

import androidx.paging.PagingConfig;

import com.example.oneidentity.R;
import com.example.oneidentity.app.models.UserPage;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.guava.GuavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class APIService {

    private static APIService apis;

    public static APIService getInstance(Context context)
    {
        if (apis == null)
            apis = new APIService(context);
        return apis;
    }

    private final APIEndpoint service;
    private final String apiKey;

    public APIService(Context context)
    {
        apiKey = context.getString(R.string.api_key);
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(chain -> {
            Request request = chain.request().newBuilder().addHeader("app-id", apiKey).build();
            return chain.proceed(request);
        });
        OkHttpClient client = builder.addInterceptor(interceptor).build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dummyapi.io/data/api/")
                .client(client)
                .addCallAdapterFactory(GuavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(APIEndpoint.class);
    }

    public interface APIEndpoint {
        @GET("user")
        ListenableFuture<UserPage> listUsers(@Query("page") int page, @Query("limit") int limit);
    }

    public APIEndpoint getService() {
        return service;
    }

//    public ListenableFuture<UserPage> getUsers(PagingSource.LoadParams<Integer> params) {
//        return null;
//    }

//    public UserPage getUsers(Integer page, PagingConfig config) {
//        return getUsers(config.pageSize, page);
//    }
//
//    public UserPage getUsers(int page, int size) {
//        Call<UserPage> users = service.listUsers(size, page);
//        try {
//            users.execute().body();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
}
