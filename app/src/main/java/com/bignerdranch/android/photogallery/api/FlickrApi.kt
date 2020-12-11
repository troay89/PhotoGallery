package com.bignerdranch.android.photogallery.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrApi {

//    @GET("services/rest/?method=flickr.interestingness.getList&api_key=e4d22caf60df34689db8d28e660c6427&format=json&nojsoncallback=1&extras=url_s")
    @GET("services/rest?method=flickr.interestingness.getList")
    fun fetchPhotos(): Call<FlickrResponse>

//    @GET
//    fun fetchUrlBytes(@Url url: String): Call<ResponseBody>

//    @GET
//    fun fetchUrlBytes(@Url url: String): Call<ResponseBody>

    @GET("services/rest?method=flickr.photos.search")
    fun searchPhotos(@Query("text") query: String): Call<FlickrResponse>
}