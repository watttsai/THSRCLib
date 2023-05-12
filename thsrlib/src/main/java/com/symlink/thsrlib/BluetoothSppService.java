package com.symlink.thsrlib;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.symlink.lib.model.BluetoothConnModel;

import java.util.Set;

/**
 * A Bluetooth SPP (Serial Port Profile) service that provides methods for connecting,
 * disconnecting, and sending commands to a Bluetooth device with SPP profile.
 * This service also includes a notification channel to run in the foreground.
 */
public class BluetoothSppService extends Service
        implements BluetoothConnModel.BluetoothConnModelCallback {
    public static final String ACTION_GATT_CONNECTED = "com.symlink.thsrlib.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_CONNECTING = "com.symlink.thsrlib.ACTION_GATT_CONNECTING";
    public static final String ACTION_GATT_DISCONNECTED = "com.symlink.thsrlib.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "com.symlink.thsrlib.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_DATA_AVAILABLE = "com.symlink.thsrlib.ACTION_DATA_AVAILABLE";
    public static final String ACTION_MESSAGE_AVAILABLE = "com.symlink.thsrlib.ACTION_MESSAGE_AVAILABLE";
    public static final String EXTRA_DATA = "com.symlink.thsrlib.EXTRA_DATA";

    private static final String TAG = BluetoothSppService.class.getSimpleName();
    private static final String SHARE_PREF_NAME = "SymlinkThsrLibSharedPrefs";
    private static final String DEVICE_NAME_KEY = "DeviceName";
    private static final int SERVICE_ID = 8787;
    private static final String NOTIFICATION_CHANNEL_ID = "com.symlink.thsrlib.BluetoothSppService";
    private static final String NOTIFICATION_CHANNEL_NAME = "BluetoothSppService Channel";

    private final IBinder binder = new BluetoothSppService.LocalBinder();
    private BroadcastReceiver bluetoothStatusReceiver;
    private Handler mainHandler;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothConnModel bluetoothConnModel;
    private String targetAddress = "";

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        super.onCreate();
        createNotificationChannel();
        startForeground(SERVICE_ID, createNotification());
    }

    @SuppressWarnings("squid:S1874")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        // Get the application context
        Context context = getApplicationContext();

        // Create a new handler associated with the main looper
        mainHandler = new Handler(Looper.getMainLooper());

        // Check if Bluetooth permission has been granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
                // Bluetooth permission has not been granted, show a Toast message
                mainHandler.post(() -> Toast.makeText(context, R.string.bluetooth_permission_not_granted, Toast.LENGTH_LONG)
                        .show());
                stopSelf();
                return START_NOT_STICKY;
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
                // Bluetooth permission has not been granted, show a Toast message
                mainHandler.post(() -> Toast.makeText(context, R.string.bluetooth_permission_not_granted, Toast.LENGTH_LONG)
                        .show());
                stopSelf();
                return START_NOT_STICKY;
            }
        }

        // Get the default Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            // Bluetooth is not supported, show a Toast message
            mainHandler.post(() -> Toast.makeText(context, R.string.bluetooth_not_supported, Toast.LENGTH_LONG)
                    .show());
            stopSelf();
            return START_NOT_STICKY;
        }

        Log.d(TAG, "Obtain a BluetoothAdapter successful.");
        // Register a Bluetooth status receiver
        bluetoothStatusReceiver = new BluetoothStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStatusReceiver, filter);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        super.onDestroy();
        // Unregister the Bluetooth status receiver
        if (bluetoothStatusReceiver != null) {
            unregisterReceiver(bluetoothStatusReceiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");

        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");

        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind");
    }

    /**
     * Creates a Notification object for the Bluetooth SPP service.
     * Builds a Notification object with the given properties.
     * If the SDK version is lower than Oreo, an empty Notification object is returned.
     *
     * @return a Notification object for the Bluetooth SPP service
     */
    private Notification createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setAutoCancel(true)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setOngoing(true)
                    .setPriority(NotificationManager.IMPORTANCE_LOW)
                    .build();
        } else {
            return new Notification();
        }
    }

    /**
     * Initializes a Notification channel for the Bluetooth SPP service.
     * Creates a Notification channel with a given channel ID, channel name and channel description.
     * Registers the channel with the system.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the Notification Channel
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW // or IMPORTANCE_DEFAULT if you want higher importance
            );
            // Set the Notification Channel's description
            channel.setDescription(NOTIFICATION_CHANNEL_NAME);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Connects to the device with given device name.
     *
     * @param name the device name to be connected
     */
    public synchronized void connect(String name) {
        updateDeviceName(name);
        connect();
    }

    /**
     * Connects to the device with device name which were provided by {@link #updateDeviceName}.
     */
    public synchronized void connect() {
        if (bluetoothConnModel == null) {
            bluetoothConnModel = new BluetoothConnModel(this, mainHandler, this);
            bluetoothConnModel.startSession();
        }
        scanSppDevice();
    }

    /**
     * Disconnects from the connected device.
     */
    public synchronized void disconnect() {
        if (bluetoothConnModel != null) {
            bluetoothConnModel.disconnectSocketFromAddress(targetAddress);
        }
    }

    /**
     * Returns the saved device name.
     *
     * @return the saved device name
     */
    public String getDeviceName() {
        return getSharedPreferences(SHARE_PREF_NAME, Context.MODE_PRIVATE)
                .getString(DEVICE_NAME_KEY, "");
    }

    /**
     * Updates the device name.
     *
     * @param name the device name to be saved
     */
    public void updateDeviceName(String name) {
        getSharedPreferences(SHARE_PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(DEVICE_NAME_KEY, name)
                .apply();
    }

    /**
     * Sends a command to the connected device.
     *
     * @param command the byte array representation of command to be sent
     */
    public void sendCommand(byte[] command) {
        bluetoothConnModel.writeToAllSockets(command);
    }

    /**
     * Scans for paired devices and connects to the device with the saved device name if found.
     * <p>
     * Otherwise, starts discovery and waits for a device with the saved device name to be found.
     */
    @SuppressLint("MissingPermission")
    public void scanSppDevice() {
        if (bluetoothAdapter == null) {
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        boolean hasDevice = false;
        for (BluetoothDevice device : pairedDevices) {
            if (connectDevice(device.getName(), device.getAddress())) {
                cancelDiscovery();
                hasDevice = true;
                break;
            }
        }

        if (!hasDevice) {
            broadcastMessage(ACTION_MESSAGE_AVAILABLE, "沒有配對過此裝置，藍牙開始掃描配對中...");
            cancelDiscovery();
            bluetoothAdapter.startDiscovery();
        }
    }

    /**
     * Connects to the specified device if its name matches the saved device name
     * and the address is valid.
     *
     * @param name    the name of the device
     * @param address the address of the device
     * @return true if successfully connected, false otherwise
     */
    @SuppressLint("MissingPermission")
    private synchronized boolean connectDevice(String name, String address) {
        if (!getDeviceName().equals(name)) {
            Log.e(TAG, "Target device name not matches: " + getDeviceName() + ", " + name);
            return false;
        }

        if (!address.matches("([0-9a-fA-F][0-9a-fA-F]:){5}([0-9a-fA-F][0-9a-fA-F])")) {
            Log.e(TAG, "Invalid address: " + address + " with length: " + address.length());
            return false;
        }

        broadcastGattUpdate(ACTION_GATT_CONNECTING);
        targetAddress = address;
        if (bluetoothAdapter == null) {
            return false;
        }
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        bluetoothConnModel.connectTo(device);
        Log.i(TAG, "Connecting to device: " + device.getName());
        return true;
    }

    @SuppressLint("MissingPermission")
    private void cancelDiscovery() {
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    /**
     * Broadcasts an Intent with the given action and extra data.
     *
     * @param action    the action to be performed
     * @param extraData the extra data to be included in the Intent
     */
    private void broadcastMessage(String action, String extraData) {
        sendBroadcast(new Intent(action).putExtra(EXTRA_DATA, extraData));
    }

    /**
     * Broadcasts an Intent with the given action.
     *
     * @param action the action to be performed
     */
    private void broadcastGattUpdate(String action) {
        sendBroadcast(new Intent(action));
    }

    @Override
    public void onSPPConnection() {
        Log.d(TAG, "onSPPConnection: ");
        broadcastGattUpdate(ACTION_GATT_CONNECTED);
    }

    @Override
    public void onSPPConnectFailed() {
        Log.d(TAG, "onSPPConnectFailed: ");
        broadcastGattUpdate(ACTION_GATT_DISCONNECTED);
    }

    @Override
    public void onSPPReceiveData(String msg, byte[] data, int size) {
        Log.d(TAG, "onSPPReceiveData: msg=" + msg);
        broadcastMessage(ACTION_DATA_AVAILABLE, HexUtil.encodeHexStr(data, 0, size));
    }

    public class LocalBinder extends Binder {
        BluetoothSppService getService() {
            return BluetoothSppService.this;
        }
    }

    /**
     * A BroadcastReceiver for handling Bluetooth status changes.
     */
    private class BluetoothStateReceiver extends BroadcastReceiver {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = device.getBondState();
                if (bondState != BluetoothDevice.BOND_BONDED && connectDevice(device.getName(), device.getAddress())) {
                    cancelDiscovery();
                }
            }
        }
    }
}
