package so.codeweaver.muzei.ponies;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by berwyn on 23/08/2016.
 */
public class PoniesApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
