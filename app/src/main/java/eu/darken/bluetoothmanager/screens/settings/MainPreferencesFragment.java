package eu.darken.bluetoothmanager.screens.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.View;

import eu.darken.bluetoothmanager.R;


public class MainPreferencesFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_main);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}
