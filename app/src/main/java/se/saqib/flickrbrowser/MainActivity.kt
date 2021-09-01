package se.saqib.flickrbrowser

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager

import kotlinx.android.synthetic.main.content_main.*

class MainActivity : BaseActivity(), GetRawData.OnDownloadComplete,
    GetFlickrJsonData.OnDataAvailable, RecyclerItemClickListener.OnRecyclerClickListener {

    private val TAG = "mainActivity"

    private val flickrRecyclerViewAdapter = FlickrRecyclerViewAdapter(ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setSupportActionBar(toolbar)

        activitiesToolbar(false)
        recycle_view.layoutManager = LinearLayoutManager(this)
        recycle_view.addOnItemTouchListener(
            RecyclerItemClickListener(
                this, recycle_view,
                this
            )
        )

        recycle_view.adapter = flickrRecyclerViewAdapter

        Log.d(TAG, "onCreate ends")
    }

    override fun onItemClick(view: View, position: Int) {
        Log.d(TAG, ".onItemClick: Starts")
        Toast.makeText(this, "Normal tap at position $position", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onItemLongClick(view: View, position: Int) {
        Log.d(TAG, ".onItemLongClick: Starts")
        val photo = flickrRecyclerViewAdapter.getPhoto(position)
        if (photo != null) {
            val intent = Intent(this, PhotoDetailsActivity::class.java)

            intent.putExtra(PHOTO_TRANSFER, photo)
            startActivity(intent)
        }
    }

    private fun createUri(baseURL: String, searchCriteria: String, lang: String, matchAll: Boolean)
            : String {
        Log.d(TAG, ".createUri starts")

        return Uri.parse(baseURL)
            .buildUpon()
            .appendQueryParameter("tags", searchCriteria)

            .appendQueryParameter("tagmode", if (matchAll) "ALL" else "ANY")
            .appendQueryParameter("lang", lang)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .build().toString()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(TAG, "onCreateOptionsMenu called")
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionItemSelected called")
        return when (item.itemId) {
            R.id.action_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDownloadComplete(data: String, status: DownloadStatus) {
        if (status == DownloadStatus.OK) {
            Log.d(TAG, "onDownloadComplete called")

            val getFlickrJsonData = GetFlickrJsonData(this)

            getFlickrJsonData.execute(data)
        } else {
            Log.d(
                TAG, "onDownloadCompleted failed with status $status. Error message is: " +
                        data
            )
        }
    }

    override fun onDataAvailable(data: List<Photo>) {
        Log.d(TAG, ".onDataAvailable called")
        flickrRecyclerViewAdapter.loadNewData(data)
        Log.d(TAG, ".onDataAvailable ends")
    }

    override fun onError(exception: Exception) {
        Log.e(TAG, ".onError called with ${exception.message}")
    }

    override fun onResume() {
        Log.d(TAG, ".onResume starts")
        super.onResume()

        val sharePref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val queryResult = sharePref.getString(FLICKR_QUERY, "")

        if (queryResult!!.isNotEmpty()) {
            val url = createUri(
                "https://www.flickr.com/services/feeds/photos_public.gne?",
                queryResult/*"android,oreo"*/,
                "en-us",
                true
            )
            val getRawData = GetRawData(this)
            getRawData.execute(url)

        }
        Log.d(TAG, ".onResume ends")
    }
}
