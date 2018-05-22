package eu.darken.bluemusic.main.ui.managed;

import android.app.Activity;
import android.app.NotificationManager;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import eu.darken.bluemusic.IAPHelper;
import eu.darken.bluemusic.bluetooth.core.BluetoothSource;
import eu.darken.bluemusic.main.core.audio.AudioStream;
import eu.darken.bluemusic.main.core.audio.StreamHelper;
import eu.darken.bluemusic.main.core.database.DeviceManager;
import eu.darken.bluemusic.main.core.database.ManagedDevice;
import eu.darken.bluemusic.util.ApiHelper;
import eu.darken.mvpbakery.base.Presenter;
import eu.darken.mvpbakery.injection.ComponentPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@ManagedDevicesComponent.Scope
public class ManagedDevicesPresenter extends ComponentPresenter<ManagedDevicesPresenter.View, ManagedDevicesComponent> {
    private final StreamHelper streamHelper;
    private final IAPHelper iapHelper;
    private final BluetoothSource bluetoothSource;
    private final NotificationManager notificationManager;
    private DeviceManager deviceManager;
    private Disposable deviceSub = Disposables.disposed();
    private Disposable upgradeSub = Disposables.disposed();
    private Disposable bluetoothSub = Disposables.disposed();

    @Inject
    ManagedDevicesPresenter(DeviceManager deviceManager,
                            StreamHelper streamHelper,
                            IAPHelper iapHelper,
                            BluetoothSource bluetoothSource,
                            NotificationManager notificationManager) {
        this.deviceManager = deviceManager;
        this.streamHelper = streamHelper;
        this.iapHelper = iapHelper;
        this.bluetoothSource = bluetoothSource;
        this.notificationManager = notificationManager;
    }

    @Override
    public void onBindChange(@Nullable View view) {
        super.onBindChange(view);
        if (view != null) {
            bluetoothSub = bluetoothSource.isEnabled()
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(enabled -> onView(v -> v.displayBluetoothState(enabled)));

            upgradeSub = iapHelper.isProVersion()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(isProVersion -> onView(v -> v.updateUpgradeState(isProVersion)));

            deviceSub = deviceManager.devices()
                    .subscribeOn(Schedulers.computation())
                    .map(managedDevices -> {
                        List<ManagedDevice> sorted = new ArrayList<>(managedDevices.values());
                        Collections.sort(sorted, (d1, d2) -> Long.compare(d2.getLastConnected(), d1.getLastConnected()));
                        return sorted;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(devs -> onView(v -> v.displayDevices(devs)));
        } else {
            deviceSub.dispose();
            upgradeSub.dispose();
            bluetoothSub.dispose();
        }
    }

    void onUpdateMusicVolume(ManagedDevice device, float percentage) {
        device.setVolume(AudioStream.Type.MUSIC, percentage);
        deviceManager.save(Collections.singleton(device))
                .subscribeOn(Schedulers.computation())
                .subscribe(managedDevices -> {
                    if (!device.isActive()) return;
                    streamHelper.changeVolume(device.getStreamId(AudioStream.Type.MUSIC), device.getVolume(AudioStream.Type.MUSIC), true, 0);
                });
    }

    void onUpdateCallVolume(ManagedDevice device, float percentage) {
        device.setVolume(AudioStream.Type.CALL, percentage);
        deviceManager.save(Collections.singleton(device))
                .subscribeOn(Schedulers.computation())
                .subscribe(managedDevices -> {
                    if (!device.isActive()) return;
                    streamHelper.changeVolume(device.getStreamId(AudioStream.Type.CALL), device.getVolume(AudioStream.Type.CALL), true, 0);
                });
    }

    void onUpdateRingVolume(ManagedDevice device, float percentage) {
        device.setVolume(AudioStream.Type.RINGTONE, percentage);
        deviceManager.save(Collections.singleton(device))
                .subscribeOn(Schedulers.computation())
                .subscribe(managedDevices -> {
                    if (!device.isActive()) return;
                    if (ApiHelper.hasMarshmallow() && !notificationManager.isNotificationPolicyAccessGranted()) {
                        Timber.w("Tried to set ring volume but notification policy permissions were missing.");
                    } else {
                        streamHelper.changeVolume(device.getStreamId(AudioStream.Type.RINGTONE), device.getVolume(AudioStream.Type.RINGTONE), true, 0);
                    }
                });
    }

    public void onUpdateNotificationVolume(ManagedDevice device, float percentage) {
        device.setVolume(AudioStream.Type.NOTIFICATION, percentage);
        deviceManager.save(Collections.singleton(device))
                .subscribeOn(Schedulers.computation())
                .subscribe(managedDevices -> {
                    if (!device.isActive()) return;
                    if (ApiHelper.hasMarshmallow() && !notificationManager.isNotificationPolicyAccessGranted()) {
                        Timber.w("Tried to set notification volume but notification policy permissions were missing.");
                    } else {
                        streamHelper.changeVolume(device.getStreamId(AudioStream.Type.NOTIFICATION), device.getVolume(AudioStream.Type.NOTIFICATION), true, 0);
                    }
                });
    }

    void onUpgradeClicked(Activity activity) {
        iapHelper.buyProVersion(activity);
    }

    interface View extends Presenter.View {
        void updateUpgradeState(boolean isProVersion);

        void displayDevices(List<ManagedDevice> managedDevices);

        void displayBluetoothState(boolean enabled);
    }
}
