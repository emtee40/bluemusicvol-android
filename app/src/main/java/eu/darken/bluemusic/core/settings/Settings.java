package eu.darken.bluemusic.core.settings;


import android.content.SharedPreferences;
import android.view.KeyEvent;

import javax.inject.Inject;

public class Settings {
    private static final String PREFKEY_VOLUMELISTENER = "core.volume.changelistener";
    public static final String PREFKEY_VISIBLE_ADJUSTMENTS = "core.volume.visibleadjustments";
    public static final String PREFKEY_AUTOPLAY_KEYCODE = "core.autoplay.keycode";
    public static final String PREFKEY_INSTALLTIME = "core.metrics.installtime";
    public static final String PREFKEY_LAUNCHCOUNT = "core.metrics.launchcount";
    public static final long DEFAULT_REACTION_DELAY = 5000;
    public static final long DEFAULT_ADJUSTMENT_DELAY = 250;
    private final SharedPreferences preferences;

    @Inject
    public Settings(SharedPreferences preferences) {
        this.preferences = preferences;
        if (!preferences.contains(PREFKEY_INSTALLTIME)) {
            preferences.edit().putLong(PREFKEY_INSTALLTIME, System.currentTimeMillis()).apply();
        }
        int currentLaunchCount = preferences.getInt(PREFKEY_LAUNCHCOUNT, 0);
        preferences.edit().putInt(PREFKEY_LAUNCHCOUNT, ++currentLaunchCount).apply();
    }

    public boolean isBugReportingEnabled() {
        return preferences.getBoolean("core.bugreporting.enabled", true);
    }

    public boolean isVolumeAdjustedVisibly() {
        return preferences.getBoolean(PREFKEY_VISIBLE_ADJUSTMENTS, true);
    }

    public boolean isVolumeChangeListenerEnabled() {
        return preferences.getBoolean(PREFKEY_VOLUMELISTENER, false);
    }

    public boolean isEnabled() {
        return preferences.getBoolean("core.enabled", true);
    }

    public int getAutoplayKeycode() {
        return preferences.getInt(PREFKEY_AUTOPLAY_KEYCODE, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
    }

    public void setAutoplayKeycode(int keycode) {
        preferences.edit().putInt(PREFKEY_AUTOPLAY_KEYCODE, keycode).apply();
    }

    public int getLaunchCount() {
        return preferences.getInt(PREFKEY_LAUNCHCOUNT, 0);
    }

    public long getInstallTime() {
        return preferences.getLong(PREFKEY_INSTALLTIME, System.currentTimeMillis());
    }
}
