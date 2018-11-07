package so.codeweaver.muzei.ponies

import android.app.Application
import timber.log.Timber

/**
 * Created by berwyn on 23/08/2016.
 */
class PoniesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
