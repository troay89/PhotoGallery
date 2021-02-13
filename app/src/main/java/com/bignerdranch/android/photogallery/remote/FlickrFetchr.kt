package com.bignerdranch.android.photogallery.remote

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.photogallery.api.FlickrApi
import com.bignerdranch.android.photogallery.api.FlickrResponse
import com.bignerdranch.android.photogallery.api.PhotoInterceptor
import com.bignerdranch.android.photogallery.api.PhotoResponse
import com.bignerdranch.android.photogallery.entity.GalleryItem
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "FlickrFetchr"

//класс подготавливает и отправляет вебзапросы
class FlickrFetchr {

    //    возможно стоит реализавать google.github.io/dagger.
    private val flickrApi: FlickrApi

    //Создание объектра Retrofit  и привязка к нему базовго URL
    init {
        //    Добавление перехватчика в конфигурацию Retrofit
        val client = OkHttpClient.Builder().addInterceptor(PhotoInterceptor()).build()

        val retrofit = Retrofit.Builder().baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create())
            //            .addConverterFactory(ScalarsConverterFactory.create())
            .client(client)
            .build()
        flickrApi = retrofit.create(FlickrApi::class.java)
    }

//       подготавливается запрос на получение фотографий см. 617 PollWorker
    fun fetchPhotosRequest(): Call<FlickrResponse> {
        return flickrApi.fetchPhotos()
    }

//       смотреть PhotoGalleryViewModel
    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        return fetchPhotoMetadata(fetchPhotosRequest())
    }

//       подготавливается запрос на поиск фотографий см. 617 PollWorker
    fun searchPhotosRequest(query: String): Call<FlickrResponse> {
        return flickrApi.searchPhotos(query)
    }

//       смотреть PhotoGalleryViewModel
    fun searchPhotos(query: String): LiveData<List<GalleryItem>> {
        return fetchPhotoMetadata(searchPhotosRequest(query))
    }


    suspend fun searchPhotosRequestCoroutines(query: String): FlickrResponse {
        return flickrApi.searchPhotosCoroutines(query)
    }

    suspend fun fetchPhotosRequestCoroutines(): FlickrResponse {
        return flickrApi.fetchPhotosCoroutines()
    }

    //выполнение веб запроса
    private fun fetchPhotoMetadata(flickrRequest: Call<FlickrResponse>): LiveData<List<GalleryItem>> {

        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()    //???

        flickrRequest.enqueue(object : Callback<FlickrResponse> {

            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                Log.e(TAG, "Не удалось получить фото", t)
            }

            override fun onResponse(
                call: Call<FlickrResponse>,
                response: Response<FlickrResponse>
            ) {
                Log.d(TAG, "Получен ответ")
                val flickrResponse: FlickrResponse? = response.body()
                val photoResponse: PhotoResponse? = flickrResponse?.photos
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems ?: mutableListOf()
//                galleryItems = galleryItems.filterNot { it.url.isNullOrBlank() }
                galleryItems = galleryItems.filterNot { it.url.isBlank() }
                responseLiveData.value = galleryItems
            }
        })
        return responseLiveData
    }

    //запрос на загрузку фотографии с определёным url
//    @WorkerThread
//    fun fetchPhoto(url: String): Bitmap? {
//        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
//        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
//        Log.i(TAG, "Decoded bitmap=$bitmap from Response=$response")
//        return bitmap
//    }
}