package so.codeweaver.muzei.ponies

import android.content.Context
import android.net.Uri
import android.preference.PreferenceManager
import androidx.work.*
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.IOException

class DerpibooruWorker(context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {
    companion object {
        private const val TAG = "DerpibooruWorker"

        internal fun enqueueLoad() {
            val workManager = WorkManager.getInstance()
            workManager.enqueue(OneTimeWorkRequestBuilder<DerpibooruWorker>()
                    .setConstraints(Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()
            )
        }
    }

    private val service: DerpibooruService

    init {
        val client = OkHttpClient.Builder().build()

        service = Retrofit.Builder()
                .baseUrl("https://derpibooru.org")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(DerpibooruService::class.java)
    }

    override fun doWork(): Result {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val tagString: String = prefs.getString(DerpibooruService.PREF_TAGS, "safe,wallpaper,score.gte:300,width.gte:1920,height.gte:1080")
        val keyString: String? = prefs.getString(DerpibooruService.PREF_KEY, null)

        val res = try {
            service.search(tagString, DerpibooruService.SEARCH_FILTER_RANDOM, DerpibooruService.SEARCH_ORDER_DESC, keyString).execute()
        } catch (e: IOException) {
            Timber.e(e)
            return Result.FAILURE
        }

        val body: DerpibooruResult = res.body()
        if (body.total < 1) {
            Timber.w("Query of %1s came back with no results", tagString)
            return Result.FAILURE
        }

        body.search.map { image ->
            Artwork().apply {
                token = image.id
                title = "#${image.id}"
                byline = "Uploaded by ${image.uploader}"
                persistentUri = Uri.parse("https:${image.image}")
                webUri = Uri.parse("https://derpibooru.org/${image.id}")
            }
        }.forEach { artwork ->
            ProviderContract.Artwork.addArtwork(
                    applicationContext,
                    PonyArtProvider::class.java,
                    artwork
            )
        }

        return Result.SUCCESS;
    }
}