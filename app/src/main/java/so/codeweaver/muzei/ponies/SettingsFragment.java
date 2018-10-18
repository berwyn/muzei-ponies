package so.codeweaver.muzei.ponies;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

/**
 * Created by berwyn on 09/04/15.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
