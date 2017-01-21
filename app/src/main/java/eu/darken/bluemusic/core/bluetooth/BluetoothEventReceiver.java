package eu.darken.bluemusic.core.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import eu.darken.bluemusic.core.BlueMusicService;
import timber.log.Timber;


public class BluetoothEventReceiver extends WakefulBroadcastReceiver {
    public static final String EXTRA_DEVICE_EVENT = "eu.darken.bluemusic.core.bluetooth.event";

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.v("onReceive(%s, %s)", context, intent);

        SourceDevice sourceDevice = new SourceDeviceWrapper((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
        String actionString = intent.getAction();
        Timber.d("Device: %s | Action: %s", sourceDevice, actionString);

        SourceDevice.Event.Type actionType = null;
        if ("android.bluetooth.device.action.ACL_CONNECTED".equals(actionString)) {
            actionType = SourceDevice.Event.Type.CONNECTED;
        } else if ("android.bluetooth.device.action.ACL_DISCONNECTED".equals(actionString)) {
            actionType = SourceDevice.Event.Type.DISCONNECTED;
        }

        if (!isValid(sourceDevice) || actionType == null) {
            Timber.d("Invalid device action!");
            return;
        }
        SourceDevice.Event deviceEvent = new SourceDevice.Event(sourceDevice, actionType);

        Intent service = new Intent(context, BlueMusicService.class);
        service.putExtra(EXTRA_DEVICE_EVENT, deviceEvent);
        final ComponentName componentName = context.startService(service);
        if (componentName != null) Timber.v("Service is already running.");
    }

    public static boolean isValid(SourceDevice sourceDevice) {
        return sourceDevice.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.AUDIO_VIDEO;
    }
}
