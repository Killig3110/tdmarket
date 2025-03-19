package com.android.tdfruitstore.data;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;


public interface ApiService {
    @GET("Killig3110/tdmarket/master/app/src/main/res/raw/products.json")
    Call<ResponseBody> getRawProductJson();

    @GET("Killig3110/tdmarket/master/app/src/main/res/raw/categories.json")
    Call<ResponseBody> getRawCategoryJson();
}

