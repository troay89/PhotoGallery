package com.bignerdranch.android.photogallery

import QueryPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.squareup.picasso.Picasso
import java.util.concurrent.TimeUnit

private const val TAG = "PhotoGalleryFragment"
private const val POLL_WORK = "POLL_WORK"

class PhotoGalleryFragment : VisibleFragment() {

    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var photoRecyclerView: RecyclerView
//    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true
        setHasOptionsMenu(true)

        photoGalleryViewModel = ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)

//        val responseHandler = Handler()
//        thumbnailDownloader =
//            ThumbnailDownloader(responseHandler) { photoHolder, bitmap ->
//                val drawable = BitmapDrawable(resources, bitmap)
//                photoHolder.bindDrawable(drawable)
//            }
//        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)

//        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build()
//        val workRequest = OneTimeWorkRequest.Builder(PollWorker::class.java).setConstraints(constraints).build()
//        WorkManager.getInstance().enqueue(workRequest)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        viewLifecycleOwner.lifecycle.addObserver(
//            thumbnailDownloader.viewLifecycleObserver
//        )
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        photoRecyclerView.layoutManager = GridLayoutManager(context, 3)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoGalleryViewModel.galleryItemLiveData.observe(
            viewLifecycleOwner, { galleryItems ->
                Log.d(TAG, "Have gallery items from view model $galleryItems")
                photoRecyclerView.adapter = PhotoAdapter(galleryItems)
            })
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        thumbnailDownloader.clearQueue()
//        viewLifecycleOwner.lifecycle.removeObserver(
//            thumbnailDownloader.viewLifecycleObserver
//        )
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        lifecycle.removeObserver(
//            thumbnailDownloader.fragmentLifecycleObserver
//        )
//    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.fragment_photo_gallery, menu)
        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView
        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(queryText: String): Boolean {
                    Log.d(TAG, "QueryTextSubmit: $queryText")
                    photoGalleryViewModel.fetchPhotos(queryText)
                    return true
                }

                override fun onQueryTextChange(queryText: String): Boolean {
                    Log.d(TAG, "QueryTextChange: $queryText")
                    return false
                }
            })
            setOnSearchClickListener {
                searchView.setQuery(photoGalleryViewModel.searchTerm, false)
            }
        }

        val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
        val isPolling = QueryPreferences.isPolling(requireContext())
        val toggleItemTitle = if (isPolling) {
            R.string.stop_polling
        } else {
            R.string.start_polling
        }
        toggleItem.setTitle(toggleItemTitle)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                photoGalleryViewModel.fetchPhotos("")
                true
            }
            R.id.menu_item_toggle_polling -> {
                val isPolling = QueryPreferences.isPolling(requireContext())
                if (isPolling) {
                    context?.let { WorkManager.getInstance(it).cancelUniqueWork(POLL_WORK) }
                    QueryPreferences.setPolling(requireContext(), false)
                } else {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build()
                    val periodicRequest = PeriodicWorkRequest
                        .Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build()
                    context?.let {
                        WorkManager.getInstance(it).enqueueUniquePeriodicWork(POLL_WORK, ExistingPeriodicWorkPolicy.KEEP, periodicRequest)
                    }
                    QueryPreferences.setPolling(requireContext(), true)
                }
                activity?.invalidateOptionsMenu()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private inner class PhotoHolder(private val itemImageView: ImageView) :
        RecyclerView.ViewHolder(itemImageView), View.OnClickListener {
//        val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable
        private lateinit var galleryItem: GalleryItem

        init {
            itemView.setOnClickListener(this)
        }

//??????????????????????????
        fun bindGalleryItem(item: GalleryItem) {
            Picasso.get()
                .load(item.url)
                .placeholder(R.drawable.bill_up_close)
                .into(itemImageView)
            galleryItem = item
        }

        override fun onClick(view: View) {
//            val intent = Intent(Intent.ACTION_VIEW, galleryItem.photoPageUri)
            val intent = PhotoPageActivity.newIntent(requireContext(), galleryItem.photoPageUri)
            startActivity(intent)
        }
    }

    private inner class PhotoAdapter(private val galleryItems: List<GalleryItem>) :
        RecyclerView.Adapter<PhotoHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val view = layoutInflater.inflate(
                R.layout.list_item_gallery,
                parent,
                false
            ) as ImageView
            return PhotoHolder(view)
        }

        override fun getItemCount(): Int = galleryItems.size

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = galleryItems[position]
//            val placeholder: Drawable = ContextCompat.getDrawable(
//                requireContext(),
//                R.drawable.bill_up_close
//            ) ?: ColorDrawable()
//            holder.bindDrawable(placeholder)
//            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
            holder.bindGalleryItem(galleryItem)
        }
    }

    companion object {
        fun newInstance() = PhotoGalleryFragment()
    }
}