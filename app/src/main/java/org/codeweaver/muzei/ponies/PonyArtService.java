package org.codeweaver.muzei.ponies;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

public class PonyArtService extends RemoteMuzeiArtSource {

    public PonyArtService() {
        super("PonyArtService");
    }

    @Override
    protected void onTryUpdate(int i) throws RetryException {

    }
}
