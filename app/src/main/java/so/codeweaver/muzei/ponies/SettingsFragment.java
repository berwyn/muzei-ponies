package so.codeweaver.muzei.ponies;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by berwyn on 09/04/15.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
