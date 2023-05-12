package com.symlink.thsrlib;

import static android.content.Context.BIND_AUTO_CREATE;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

public class TicketManager {
    private static final String TAG = TicketManager.class.getSimpleName();
    private static final BluetoothModeStatus bluetoothMode = BluetoothModeStatus.SPP;
    private static TicketManager instance;

    private final BluetoothAdapter bluetoothAdapter;
    private final BroadcastReceiver gattUpdateReceiver;
    private final BluetoothStateReceiver bluetoothStateReceiver;

    // FIXME: use WeakReference to avoid memory leaks
    private Context context;
    private Handler mainThreadHandler;
    private ServiceConnection serviceConnection;
    private BluetoothSppService bluetoothSppService;

    // These listeners are provided by the client and will push status changes or data on the callback.
    private IBluetoothStatusListener bluetoothStatusListener;
    private IDeviceStatusListener deviceStatusListener;
    private IReaderStatusListener readerStatusListener;
    private IDataListener dataListener;

    private BluetoothStatus bluetoothStatus = BluetoothStatus.None;
    private DeviceStatus deviceStatus = DeviceStatus.Off;
    private ReaderStatus readerStatus = ReaderStatus.Off;
    private int sn = 1; // latest serial no

    private TicketManager() {
        gattUpdateReceiver = new GattUpdateReceiver();
        bluetoothStateReceiver = new BluetoothStateReceiver();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            updateBluetoothStatus(BluetoothStatus.None);
            showToast(context.getString(R.string.bluetooth_not_supported));
        }
    }

    public static TicketManager getInstance() {
        if (instance == null) {
            instance = new TicketManager();
        }

        return instance;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothSppService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothSppService.ACTION_GATT_CONNECTING);
        intentFilter.addAction(BluetoothSppService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothSppService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothSppService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothSppService.ACTION_MESSAGE_AVAILABLE);
        return intentFilter;
    }

    public BluetoothModeStatus getBluetoothMode() {
        return bluetoothMode;
    }

    public void Release() {
        try {
            if (bluetoothSppService != null) {
                bluetoothSppService.stopForeground(true);
                bluetoothSppService.stopSelf();
            }
//            Intent service = new Intent(_context, BluetoothSppService.class);
//            _context.stopService(mBluetoothSppService);
            if (context != null) {
                context.unbindService(serviceConnection);
                context.unregisterReceiver(gattUpdateReceiver);
                context.unregisterReceiver(bluetoothStateReceiver);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            context = null;
            Log.d(TAG, "Release");
        }
    }

    public void Initial(Context context) {
        Disconnect();
        Release();

        this.context = context;
        this.context.registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        this.context.registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        Intent intent = new Intent(context, BluetoothSppService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                bluetoothSppService = ((BluetoothSppService.LocalBinder) binder).getService();
                bluetoothSppService.connect();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bluetoothSppService = null;
            }
        };
        this.context.bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        if (!bluetoothAdapter.isEnabled()) {
            updateBluetoothStatus(BluetoothStatus.Off);
            if (this.context instanceof Activity && ContextCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((Activity) this.context).startActivityForResult(bluetoothIntent, 1);
            }
        } else {
            updateBluetoothStatus(BluetoothStatus.On);
        }

        if (!isLocationEnable(context)) {
            Toast.makeText(this.context, "因刷卡機可能未曾配對過，需開啟定位(位置)，使藍牙可以掃描到刷卡機！", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isLocationEnable(Context context) {
        try {
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            return mode != Settings.Secure.LOCATION_MODE_OFF;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void Connect() {
        if (bluetoothSppService == null) {
            Log.e(TAG, "Connect: Service is not Ready.");
            return;
        }
        if (bluetoothStatus == BluetoothStatus.On) {
            if (deviceStatus == DeviceStatus.Off) {
                bluetoothSppService.connect();
            } else if (deviceStatus == DeviceStatus.None) {
                // "Device not setup. Call SetDeviceName first."
                showToast("刷卡機尚未設定，請先掃描刷卡機QRCode");
            } else if (deviceStatus == DeviceStatus.On) {
                // "Device already connected"
                showToast("已經連結刷卡機！");
            }
        } else if (bluetoothStatus == BluetoothStatus.Off) {
            showToast("藍牙未開啟！");
        } else if (bluetoothStatus == BluetoothStatus.TURNING_ON) {
            showToast("藍牙啟動中，請重新掃描QRCode連接裝置！");
        }
    }

    public void Disconnect() {
        if (bluetoothSppService == null) {
            Log.e(TAG, "Disconnect: Service is not Ready.");
            return;
        }
        if (bluetoothStatus == BluetoothStatus.On && readerStatus == ReaderStatus.On) {
            CloseReader();
        }
        if (deviceStatus == DeviceStatus.On) {
            bluetoothSppService.disconnect();
        }
    }

    public void SetDeviceName(String deviceName) {
        if (bluetoothSppService == null) {
            Log.e(TAG, "SetDeviceName: Service is not Ready.");
            return;
        }
        bluetoothSppService.updateDeviceName(deviceName);
    }

    public String GetDeviceName() {
        if (bluetoothSppService == null) {
            Log.e(TAG, "GetDeviceName: Service is not Ready.");
            return "";
        }
        return bluetoothSppService.getDeviceName();
    }

    public void OpenReader() {
        if (bluetoothStatus == BluetoothStatus.On) {
            if (deviceStatus != DeviceStatus.On) {
                // TODO: Maybe we should use the XXX error code here, which is more appropriate
                updateData(Message.fail(Constants.CODE_UNKNOWN, "Device disconnected"));
                showToast("尚未與刷卡機連接！請重新掃描QRCode");
            } else {
                if (readerStatus == ReaderStatus.Off) {
                    byte[] data = new byte[50];
                    Arrays.fill(data, (byte) 32);
                    data[0] = '1';
                    bluetoothSppService.sendCommand(createCommand(this.sn++, 0x0201, data));
                    updateReaderStatus(ReaderStatus.On);
                } else {
                    showToast("刷卡機已開啟讀卡功能！");
                }
            }
        } else {
            showToast("藍牙尚未啟用成功！");
        }
    }

    public void CloseReader() {
        if (bluetoothStatus == BluetoothStatus.On) {
            if (deviceStatus != DeviceStatus.On) {
                // TODO: Maybe we should use the XXX error code here, which is more appropriate
                updateData(Message.fail(Constants.CODE_UNKNOWN, "Device disconnected"));
                return;
            }

            if (readerStatus == ReaderStatus.On) {
                byte[] data = new byte[50];
                Arrays.fill(data, (byte) 32);
                data[0] = '0';
                bluetoothSppService.sendCommand(createCommand(this.sn++, 0x0201, data));
                updateReaderStatus(ReaderStatus.Off);
            } else {
                showToast("刷卡機已經關閉讀卡功能！");
            }
        }
    }

    public boolean Print(List<String> data) {
        if (bluetoothStatus == BluetoothStatus.On) {
            if (deviceStatus != DeviceStatus.On) {
                // TODO: Maybe we should use the XXX error code here, which is more appropriate
                updateData(Message.fail(Constants.CODE_UNKNOWN, "Device disconnected"));
                return false;
            }

            if (readerStatus == ReaderStatus.On) {
                CloseReader();
                delay(500);
            }
            for (String content : data) {
                try {
                    bluetoothSppService.sendCommand(createCommand(this.sn++, 0x0301, content.getBytes("big5")));
                    delay(1000);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            OpenReader();
            return true;
        } else {
            showToast("藍牙尚未啟用成功！");
            return false;
        }
    }

    public void SetupPrinter(byte[] data) {
        if (bluetoothStatus == BluetoothStatus.On) {
            if (deviceStatus != DeviceStatus.On) {
                // TODO: Maybe we should use the XXX error code here, which is more appropriate
                updateData(Message.fail(Constants.CODE_UNKNOWN, "Device disconnected"));
                return;
            }

            if (readerStatus == ReaderStatus.On) {
                CloseReader();
            }

            delay(300);
            bluetoothSppService.sendCommand(createCommand(this.sn++, 0x0101, data));
        } else {
            showToast("藍牙尚未啟用成功！");
        }
    }

    public void executeCreditCardSale(byte[] data) {
        if (bluetoothStatus == BluetoothStatus.On) {
            if (deviceStatus != DeviceStatus.On) {
                // TODO: Maybe we should use the XXX error code here, which is more appropriate
                updateData(Message.fail(Constants.CODE_UNKNOWN, "Device disconnected"));
                return;
            }

            if (readerStatus == ReaderStatus.On) {
                CloseReader();
            }

            delay(300);
            bluetoothSppService.sendCommand(createCommand(this.sn++, 0x0401, data));
        } else {
            showToast("藍牙尚未啟用成功！");
        }
    }

    public void executeCreditCardVoid(byte[] data) {
        if (bluetoothStatus == BluetoothStatus.On) {
            if (deviceStatus != DeviceStatus.On) {
                // TODO: Maybe we should use the XXX error code here, which is more appropriate
                updateData(Message.fail(Constants.CODE_UNKNOWN, "Device disconnected"));
                return;
            }

            if (readerStatus == ReaderStatus.On) {
                CloseReader();
            }

            delay(300);
            bluetoothSppService.sendCommand(createCommand(this.sn++, 0x0403, data));
        } else {
            showToast("藍牙尚未啟用成功！");
        }
    }

    public void executeCreditCardInquireLog(byte[] data) {
        if (bluetoothStatus == BluetoothStatus.On) {
            if (deviceStatus != DeviceStatus.On) {
                // TODO: Maybe we should use the XXX error code here, which is more appropriate
                updateData(Message.fail(Constants.CODE_UNKNOWN, "Device disconnected"));
                return;
            }

            if (readerStatus == ReaderStatus.On) {
                CloseReader();
            }

            delay(300);
            bluetoothSppService.sendCommand(createCommand(this.sn++, 0x0501, data));
        } else {
            showToast("藍牙尚未啟用成功！");
        }
    }

    public void executeCreditCardInquireTotals() {
        if (bluetoothStatus == BluetoothStatus.On) {
            if (deviceStatus != DeviceStatus.On) {
                // TODO: Maybe we should use the XXX error code here, which is more appropriate
                updateData(Message.fail(Constants.CODE_UNKNOWN, "Device disconnected"));
                return;
            }

            if (readerStatus == ReaderStatus.On) {
                CloseReader();
            }

            delay(300);
            bluetoothSppService.sendCommand(createCommand(this.sn++, 0x0502, new byte[0]));
        } else {
            showToast("藍牙尚未啟用成功！");
        }
    }

    public byte[] createCommand(int sn, int cmd, byte[] data) {
        // packet: SOH(1) + payloadLength(2) + payload + LRC(1)
        // payload: SN(1) + CMD(2) + data(n)
        ByteBuffer buf = ByteBuffer.allocate(7 + data.length);
        int payloadLength = 3 + data.length;
        buf.put((byte) 0x01);
        buf.order(ByteOrder.LITTLE_ENDIAN).putShort((short) payloadLength);
        buf.put((byte) sn);
        buf.order(ByteOrder.BIG_ENDIAN).putShort((short) cmd);
        buf.put(data);
        byte lrc = HexUtil.ByteArrayXOR(buf.array(), 1, buf.position());
        buf.put(lrc);
        Log.d(TAG, String.format("Request: SN=%02X, CMD=%04X, Data='%s'",
                sn, cmd, HexUtil.encodeHexStr(data)));

        return buf.array();
    }

    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void updateBluetoothStatus(BluetoothStatus status) {
        bluetoothStatus = status;
        if (bluetoothStatusListener != null) {
            bluetoothStatusListener.OnStatusChange(status);
        }
    }

    @SuppressWarnings("squid:S3398")
    private void updateDeviceStatus(DeviceStatus status) {
        deviceStatus = status;
        if (deviceStatusListener != null) {
            deviceStatusListener.OnStatusChange(status);
        }
    }

    private void updateReaderStatus(ReaderStatus status) {
        readerStatus = status;
        if (readerStatusListener != null) {
            readerStatusListener.OnStatusChange(status);
        }
    }

    private void updateData(Message data) {
        if (dataListener != null) {
            dataListener.OnDataAvailable(data);
        }
    }

    @SuppressWarnings("squid:S3398")
    private void updateData(byte[] data) {
        if (dataListener != null) {
            dataListener.OnDataAvailable(data);
        }
    }

    public void RegisterBluetoothListener(IBluetoothStatusListener listener) {
        bluetoothStatusListener = listener;
    }

    public void RegisterDeviceListener(IDeviceStatusListener listener) {
        deviceStatusListener = listener;
    }

    public void RegisterReaderListener(IReaderStatusListener listener) {
        readerStatusListener = listener;
    }

    public void RegisterDataListener(IDataListener listener) {
        dataListener = listener;
    }

    public Boolean getKeepConnected() {
        return deviceStatus == DeviceStatus.On;
    }

    public void showToast(String content) {
        if (context == null) {
            return;
        }
        if (Looper.getMainLooper() == Looper.myLooper()) {
            Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
        } else {
            if (context instanceof Application) {
                if (mainThreadHandler == null) {
                    mainThreadHandler = new Handler(Looper.getMainLooper());
                }
                mainThreadHandler.post(() -> Toast.makeText(context, content, Toast.LENGTH_SHORT).show());
            } else if (context instanceof Activity) {
                Activity activity = (Activity) context;
                activity.runOnUiThread(() -> Toast.makeText(context, content, Toast.LENGTH_SHORT).show());
            }
        }
    }

    private class BluetoothStateReceiver extends BroadcastReceiver {
        private BluetoothStateReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            switch (state) {
                case BluetoothAdapter.STATE_TURNING_ON:
                    updateBluetoothStatus(BluetoothStatus.TURNING_ON);
                    break;
                case BluetoothAdapter.STATE_ON:
                    updateBluetoothStatus(BluetoothStatus.On);
                    String name = GetDeviceName();
                    if (!name.isEmpty()) {
                        SetDeviceName(name);
                    }
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Disconnect();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    updateBluetoothStatus(BluetoothStatus.Off);
                    updateDeviceStatus(DeviceStatus.Off);
                    updateReaderStatus(ReaderStatus.Off);
                    break;

                default:
                    break;
            }
        }
    }

    private class GattUpdateReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothSppService.ACTION_GATT_CONNECTED.equals(action)) {
                updateDeviceStatus(DeviceStatus.On);
                if (readerStatus != ReaderStatus.On) {
                    OpenReader();
                }
            } else if (BluetoothSppService.ACTION_GATT_CONNECTING.equals(action)) {
                updateDeviceStatus(DeviceStatus.Connecting);
            } else if (BluetoothSppService.ACTION_GATT_DISCONNECTED.equals(action)) {
                if (bluetoothStatus == BluetoothStatus.On && deviceStatus == DeviceStatus.On) {
                    showToast("與刷卡機連結斷開！");
                }
                updateDeviceStatus(DeviceStatus.Off);
                updateReaderStatus(ReaderStatus.Off);
            } else if (BluetoothSppService.ACTION_DATA_AVAILABLE.equals(action)) {
                try {
                    String receivedData = intent.getStringExtra(BluetoothSppService.EXTRA_DATA);
                    byte[] dataBuff = HexUtil.hexStringToByteArray(receivedData);
                    updateData(dataBuff);
                    Message message = Parser.parseMessage(dataBuff);
                    updateData(message);
                } catch (Exception e) {
                    showToast(e.getMessage());
                }
            } else if (BluetoothSppService.ACTION_MESSAGE_AVAILABLE.equals(action)) {
                showToast(intent.getStringExtra(BluetoothSppService.EXTRA_DATA));
            }
        }
    }
}
