package com.bignerdranch.android.photogallery

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.bignerdranch.android.photogallery.entity.GalleryItem
import com.bignerdranch.android.photogallery.model.QueryPreferences
import com.squareup.picasso.Picasso
import java.util.concurrent.TimeUnit


private const val TAG = "PhotoGalleryFragment"
private const val POLL_WORK = "POLL_WORK"

//см 641
class PhotoGalleryFragment : VisibleFragment() {

    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var photoRecyclerView: RecyclerView
//    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        привязывает экземпляр ThumbnailDownloader cм 586
        retainInstance = true

        setHasOptionsMenu(true)

        photoGalleryViewModel = ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)

//        val responseHandler = Handler()
//        получение готовых изображений из фонового потока
//        thumbnailDownloader = ThumbnailDownloader(responseHandler) { photoHolder, bitmap ->
//                val drawable = BitmapDrawable(resources, bitmap)

            //передача картинки холдер
//                photoHolder.bindDrawable(drawable)
//            }

        //    открыть фоновый поток ThumbnailDownloader
//        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)

//        условия для запроса к PollWorker
//        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build()
//        создание запроса и поставка очередь
//        val workRequest = OneTimeWorkRequest.Builder(PollWorker::class.java).setConstraints(constraints).build()
//        WorkManager.getInstance().enqueue(workRequest)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        Регистрация наблюдателя жизненного цикла представления см. 581
//        viewLifecycleOwner.lifecycle.addObserver(
//            thumbnailDownloader.viewLifecycleObserver
//        )
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)
//
        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        photoRecyclerView.layoutManager = GridLayoutManager(context, 3)

        return view
    }

//    запрашивает даные у класса PhotoGalleryViewModel и отправляет их в адаптер
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoGalleryViewModel.galleryItemLiveData.observe(
            viewLifecycleOwner, { galleryItems ->
                Log.d(TAG, "Есть элементы галереи из view model $galleryItems")
                photoRecyclerView.adapter = PhotoAdapter(galleryItems)
            })
    }

//    Отказ от регистрации наблюдателя жизненного цикла см. 581
//    override fun onDestroyView() {
//        super.onDestroyView()
////        thumbnailDownloader.clearQueue()
//        viewLifecycleOwner.lifecycle.removeObserver(
//            thumbnailDownloader.viewLifecycleObserver
//        )
//    }

//    закрыть фоновый поток ThumbnailDownloader
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

//        Регистрация событий SearchView.OnQueryTextListener
//        searchItem.
        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(queryText: String): Boolean {
                    Log.d(TAG, "QueryTextSubmit: $queryText")
                    photoGalleryViewModel.fetchPhotos(queryText)

                    clearFocus()
                    
                    return true
                }

                override fun onQueryTextChange(queryText: String): Boolean {
                    Log.d(TAG, "QueryTextChange: $queryText")
                    return false
                }
            })

//            Предварительное заполнение SearchView
            setOnSearchClickListener {
                searchView.setQuery(photoGalleryViewModel.searchTerm, false)
            }
        }

//        Установка правильного текста пункта меню
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
            //очистка поиска
            R.id.menu_item_clear -> {
                photoGalleryViewModel.fetchPhotos("")
                true
            }
            R.id.menu_item_toggle_polling -> {
                val isPolling = QueryPreferences.isPolling(requireContext())
                if (isPolling) {
                    //выключает PollWorker
                    context?.let { WorkManager.getInstance(it).cancelUniqueWork(POLL_WORK) }
                    QueryPreferences.setPolling(requireContext(), false)
                } else {
                    //включает PollWorker
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build()

//                    инитилизация PollWorker
                    val periodicRequest = PeriodicWorkRequest
                        .Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build()
                    context?.let {
                        //указывает как работать если PollWorker включен
                        WorkManager.getInstance(it).enqueueUniquePeriodicWork(
                            POLL_WORK,
                            ExistingPeriodicWorkPolicy.KEEP,
                            periodicRequest
                        )
                    }
                    QueryPreferences.setPolling(requireContext(), true)
                }
                activity?.invalidateOptionsMenu()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

//    холдер
    private inner class PhotoHolder(private val itemImageView: ImageView) :
        RecyclerView.ViewHolder(itemImageView), View.OnClickListener {

    //вставляет картинку в ImageView
//        val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable

        private lateinit var galleryItem: GalleryItem

        init {
            itemView.setOnClickListener(this)
        }

        fun bindGalleryItem(item: GalleryItem) {
            Picasso.get()
                .load(item.url)
                .placeholder(R.drawable.bill_up_close)
                .into(itemImageView)
            galleryItem = item
        }

        override fun onClick(view: View) {
//            Выдача неявных интентов при нажатии
//            val intent = Intent(Intent.ACTION_VIEW, galleryItem.photoPageUri)
//            Переключение на запуск activity PhotoPageActivity
            val intent = PhotoPageActivity.newIntent(requireContext(), galleryItem.photoPageUri)
            startActivity(intent)
        }
    }

//    адаптер
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

//           отправка задачи фонофому потоку на загрузку картинки
//            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
            holder.bindGalleryItem(galleryItem)
        }
    }
    
    companion object {
        fun newInstance() = PhotoGalleryFragment()
    }
}