package com.bignerdranch.android.photogallery.api

import com.bignerdranch.android.photogallery.entity.GalleryItem
import com.google.gson.annotations.SerializedName


class PhotoResponse {
    @SerializedName("photo")    //см. 540 стр.
    lateinit var galleryItems: List<GalleryItem>
}