package se.saqib.flickrbrowser

import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

enum class DownloadStatus {
    OK, IDLE, NOT_INITIALISED, FAILED_OR_EMPTY, PERMISSIONS_ERROR, ERROR
}

class GetRawData(private val listener: OnDownloadComplete) : AsyncTask<String, Void, String>() {
    private val TAG = "getRawData"
    private var downloadStatus = DownloadStatus.IDLE

    interface OnDownloadComplete {

        fun onDownloadComplete(data: String, status: DownloadStatus) {

        }
    }

    override fun onPostExecute(result: String) {

        Log.d(TAG, "onPostExecute called")
        listener.onDownloadComplete(result, downloadStatus)
    }

    override fun doInBackground(vararg params: String?): String {
        if (params[0] == null) {
            downloadStatus = DownloadStatus.NOT_INITIALISED
            return "No URL specified"
        }

        try {
            downloadStatus = DownloadStatus.OK
            return URL(params[0]).readText()
        } catch (e: Exception) {
            val TAG = "doInBackground:"
            val errorMessage = when (e) {
                is MalformedURLException -> {
                    downloadStatus = DownloadStatus.NOT_INITIALISED
                    "$TAG Invalid URL ${e.message}"
                }
                is IOException -> {
                    downloadStatus = DownloadStatus.FAILED_OR_EMPTY
                    "$TAG IO Exception reading data: ${e.message}"
                }
                is SecurityException -> {
                    downloadStatus = DownloadStatus.PERMISSIONS_ERROR
                    "$TAG Security exception: Needs permission? ${e.message}"
                }
                else -> {
                    downloadStatus = DownloadStatus.ERROR
                    "Unknown error: ${e.message}"
                }
            }
            Log.e(TAG, errorMessage)
            return errorMessage
        }
    }
}
