package so.codeweaver.muzei.ponies.derpi

import android.content.Context
import androidx.work.*
import com.google.android.apps.muzei.api.provider.ProviderContract
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import so.codeweaver.muzei.ponies.art.PonyArtProvider
import so.codeweaver.muzei.ponies.derpi.DerpibooruService.DEFAULT_TAGS
import so.codeweaver.muzei.ponies.util.StringUtils
import timber.log.Timber
import java.io.IOException

class DerpibooruWorker(context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {
    companion object {
        const val KEY_TAGS = "DERPI_WORKER_TAGS"
        const val KEY_API_KEY = "DERPI_WORKER_API_KEY"
        const val KEY_COUNT = "DERPI_WORKER_COUNT"
        const val KEY_PAGE = "DERPI_WORKER_PAGE"
        const val KEY_ENQUEUE = "DERPI_WORKER_ENQUEUE"
        const val KEY_RESULT = "DERPI_WORKER_RESULT"

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
        val tagString = inputData.getString(KEY_TAGS) ?: DEFAULT_TAGS
        val keyString = inputData.getString(KEY_API_KEY)
        val count = inputData.getInt(KEY_COUNT, 20)

        val res = try {
            service.search(StringUtils.buildDerpibooruTagString(tagString), DerpibooruService.SORT_FORMAT_RANDOM, DerpibooruService.SORT_DIRECTION_DESC, keyString, count).execute()
        } catch (e: IOException) {
            Timber.e(e)
            return Result.FAILURE
        }

        val body: DerpibooruResult = res.body() ?: return Result.FAILURE

        if (body.total < 1) {
            Timber.w("Query of %1s came back with no results", tagString)
            return Result.FAILURE
        }

        val shouldEnqueue = inputData.getBoolean(KEY_ENQUEUE, true)
        if (shouldEnqueue) {
            body.search.map { image -> image.buildArtwork() }.forEach { artwork ->
                ProviderContract.Artwork.addArtwork(
                        applicationContext,
                        PonyArtProvider::class.java,
                        artwork
                )
            }
        }

        return Result.SUCCESS
    }
}