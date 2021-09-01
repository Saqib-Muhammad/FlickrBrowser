package se.saqib.flickrbrowser

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.widget.SearchView

class SearchActivity : BaseActivity() {
    private val TAG = "SearchActivity"
    private var searchView: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, ".onCreate: starts")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
//        setSupportActionBar(toolbar)
        activitiesToolbar(true)
        Log.d(TAG, ".onCreate: ends")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(TAG, ".onCreateOptionsMenu: starts")
        menuInflater.inflate(R.menu.menu_search, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.app_bar_search).actionView as SearchView
        val searchableInfo = searchManager.getSearchableInfo(componentName)

        searchView?.setSearchableInfo(searchableInfo)

        searchView?.isIconified = false

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d(TAG, ".onQueryTextSubmit: called")

                val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                sharedPref.edit().putString(FLICKR_QUERY, query)
                    .apply()/*We're getting a shared preferences object by calling the Preference Manager's getDefaultSharedPreferences function, passing it a context. Now here we want to pass the application context rather than using this, because the date is going to be retrieved by a different activity to the one that saved it.*/
                searchView?.clearFocus()/*video 42 time 02:43.120 --> 02:46.410 Now it's not obvious why we need to call clear focus in there*/

                finish()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        searchView?.setOnCloseListener {
            finish()
            false
        }

        Log.d(TAG, ".onCreateOptionsMenu: returning")
        return true
    }
}
