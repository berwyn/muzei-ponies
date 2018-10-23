package so.codeweaver.muzei.ponies

import android.content.Context
import android.preference.PreferenceManager
import androidx.work.*
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
        val tagString = prefs.getString(DerpibooruService.PREF_TAGS, "safe,wallpaper,score.gte:300,width.gte:1920,height.gte:1080")!!
        val keyString = prefs.getString(DerpibooruService.PREF_KEY, null)

        val res = try {
            service.search(tagString, DerpibooruService.SORT_FORMAT_RANDOM, DerpibooruService.SORT_DIRECTION_DESC, keyString, 20).execute()
        } catch (e: IOException) {
            Timber.e(e)
            return Result.FAILURE
        }

        val body: DerpibooruResult = res.body() ?: return Result.FAILURE

        if (body.total < 1) {
            Timber.w("Query of %1s came back with no results", tagString)
            return Result.FAILURE
        }

        body.search.map { image -> image.buildArtwork() }.forEach { artwork ->
            ProviderContract.Artwork.addArtwork(
                    applicationContext,
                    PonyArtProvider::class.java,
                    artwork
            )
        }

        return Result.SUCCESS;
    }
}