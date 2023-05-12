package com.symlink.lib.model;

/*
 * Copyright (C) 2011 Wireless Network and Multimedia Laboratory, NCU, Taiwan
 *
 * You can reference http://wmlab.csie.ncu.edu.tw
 *
 * This class is used to process connection operation, including server side or client side. *
 *
 * @author Fiona
 * @version 0.0.1
 *
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnModel {
    public static final String MONITOR_OUTPUT_NAME = "output.txt";
    private static final boolean D = true;
    private static final String TAG = "BluetoothConnModel";
    private static final String NAME = "BluetoothConn";
    private static final UUID CUSTOM_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter mAdapter;

    private final Handler mHandler;

    private final Context mContext;

    private ServerSocketThread mServerSocketThread;

    private BluetoothSocketConfig mSocketConfig = null;

    private final boolean mMonitor = false;

    private int mTxBytes = 0;

    private int mRxBytes = 0;

    private final int mMonitorBytes = 0;

    private final BluetoothConnModelCallback callback;

    public BluetoothConnModel(Context context, Handler handler, BluetoothConnModelCallback callback) {
        this.callback = callback;
        this.mHandler = handler;
        this.mContext = context;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public int getTxBytes() {
        return this.mTxBytes;
    }

    public int getRxBytes() {
        return this.mRxBytes;
    }

    public boolean getFileMonitor() {
        return this.mMonitor;
    }

    public synchronized void startSession() {
        Log.d("BluetoothConnModel", "[startSession] ServerSocketThread start...");
        if (this.mServerSocketThread == null) {
            Log.i("BluetoothConnModel", "[startSession] mServerSocketThread is dead");
            this.mServerSocketThread = new ServerSocketThread();
            this.mServerSocketThread.start();
        } else {
            Log.i("BluetoothConnModel", "[startSession] mServerSocketThread is alive : " + this);
        }
        this.mSocketConfig = BluetoothSocketConfig.getInstance();
    }

    public synchronized void connectTo(BluetoothDevice device) {
        SocketThread mSocketThread = new SocketThread(device);
        mSocketThread.start();
    }

    public synchronized void connected(BluetoothSocket socket) {
        ConnectedThread connectedThread = new ConnectedThread(socket);
        if (!this.mSocketConfig.registerSocket(socket, connectedThread, 1)) ;
        connectedThread.start();
    }

    public void SendFileToSocket(BluetoothSocket socket, String file) {
        SendFileThread sendFile = new SendFileThread(socket, file);
        sendFile.start();
    }

    public void SendFileToAllSockets(String file) {
        Log.d("BluetoothConnModel", "SendFileAllSockets start...");
        for (BluetoothSocket socket : this.mSocketConfig.getConnectedSocketList()) {
            synchronized (this) {
                SendFileToSocket(socket, file);
            }
        }
    }

    public void writeToSocket(BluetoothSocket socket, String out) {
        Log.d("BluetoothConnModel", "writeToDevice start...");
        ConnectedThread connectedThread = this.mSocketConfig.getConnectedThread(socket);
        if (connectedThread == null)
            return;
        Log.e("BluetoothConnModel", "[writeToDevice] connectedThread hashcode = " + connectedThread);
        if (this.mSocketConfig.isSocketConnected(socket)) {
            Log.w("BluetoothConnModel", "[writeToDevice] The socket is alived.");
            connectedThread.write(out);
        } else {
            Log.w("BluetoothConnModel", "[writeToDevice] The socket has been closed.");
        }
    }

    public void writeToSocket(BluetoothSocket socket, byte[] out) {
        Log.d("BluetoothConnModel", "writeToDevice start...");
        ConnectedThread connectedThread = this.mSocketConfig.getConnectedThread(socket);
        if (connectedThread == null)
            return;
        Log.e("BluetoothConnModel", "[writeToDevice] connectedThread hashcode = " + connectedThread);
        if (this.mSocketConfig.isSocketConnected(socket)) {
            Log.w("BluetoothConnModel", "[writeToDevice] The socket is alived.");
            connectedThread.write(out);
        } else {
            Log.w("BluetoothConnModel", "[writeToDevice] The socket has been closed.");
        }
    }

    public void writeToSockets(Set<BluetoothSocket> sockets, String out) {
        Log.d("BluetoothConnModel", "writeToDevices start...");
        for (BluetoothSocket socket : sockets) {
            synchronized (this) {
                writeToSocket(socket, out);
            }
        }
    }

    public void writeToAllSockets(String out) {
        Log.d("BluetoothConnModel", "writeToAllDevices start...");
        for (BluetoothSocket socket : this.mSocketConfig.getConnectedSocketList()) {
            synchronized (this) {
                writeToSocket(socket, out);
                Log.e("BluetoothConnModel", "[writeToAllDevices] currentTimeMillis: " +

                        System.currentTimeMillis());
            }
        }
    }

    public void writeToAllSockets(byte[] out) {
        Log.d("BluetoothConnModel", "writeToAllDevices start...");
        Log.i("BluetoothConnModel", "Connected Socket Size:" + this.mSocketConfig
                .getConnectedSocketList().size());
        for (BluetoothSocket socket : this.mSocketConfig.getConnectedSocketList()) {
            synchronized (this) {
                writeToSocket(socket, out);
                Log.e("BluetoothConnModel", "[writeToAllDevices] currentTimeMillis: " +

                        System.currentTimeMillis());
            }
        }
    }

    public void disconnectServerSocket() {
        if (this.mServerSocketThread != null) {
            this.mServerSocketThread.disconnect();
            this.mServerSocketThread = null;
        }
    }

    public void disconnectSocketFromAddress(String address) {
        Set<BluetoothSocket> socketSets = this.mSocketConfig.containSockets(address);
        Log.e("BluetoothConnModel", "Socket Set Size:" + socketSets.size());
        for (BluetoothSocket socket : socketSets)
            disconnectSocket(socket);
    }

    public synchronized void disconnectSocket(BluetoothSocket socket) {
        if (!this.mSocketConfig.isSocketConnected(socket))
            return;
        this.mSocketConfig.unregisterSocket(socket);
        this.callback.onSPPConnectFailed();
    }

    public void terminated() {
        disconnectServerSocket();
        for (BluetoothSocket socket : this.mSocketConfig.getConnectedSocketList())
            disconnectSocket(socket);
    }

    private void notifyUiFromToast(String str) {
    }

    public interface BluetoothConnModelCallback {
        void onSPPConnection();

        void onSPPConnectFailed();

        void onSPPReceiveData(String param1String, byte[] param1ArrayOfbyte, int param1Int);
    }

    private class ServerSocketThread implements Runnable {
        private BluetoothServerSocket mmServerSocket = null;

        private Thread thread = null;

        private boolean isServerSocketValid = false;

        public ServerSocketThread() {
            this.thread = new Thread(this);
            BluetoothServerSocket serverSocket = null;
            try {
                Log.i("BluetoothConnModel", "[ServerSocketThread] Enter the listen server socket");
                if (DeviceDependency.shouldUseSecure()) {
                    serverSocket = BluetoothConnModel.this.mAdapter.listenUsingRfcommWithServiceRecord("BluetoothConn", BluetoothConnModel
                            .CUSTOM_UUID);
                } else {
                    serverSocket = BluetoothConnModel.this.mAdapter.listenUsingInsecureRfcommWithServiceRecord("BluetoothConn", BluetoothConnModel
                            .CUSTOM_UUID);
                }
                Log.i("BluetoothConnModel", "[ServerSocketThread] serverSocket hash code = " + serverSocket
                        .hashCode());
                this.isServerSocketValid = true;
            } catch (IOException e) {
                Log.e("BluetoothConnModel", "[ServerSocketThread] Constructure: listen() failed", e);
                e.printStackTrace();
                BluetoothConnModel.this.notifyUiFromToast("Listen failed. Restart application again");
                this.isServerSocketValid = false;
                BluetoothConnModel.this.mServerSocketThread = null;
            }
            this.mmServerSocket = serverSocket;
            if (this.mmServerSocket != null) {
                String serverSocketName = this.mmServerSocket.toString();
                Log.i("BluetoothConnModel", "[ServerSocketThread] serverSocket name = " + serverSocketName);
            } else {
                Log.i("BluetoothConnModel", "[ServerSocketThread] serverSocket = null");
            }
        }

        public void start() {
            this.thread.start();
        }

        public void run() {
            Log.d("BluetoothConnModel", "BEGIN ServerSocketThread " + this + ", thread id = ");
            BluetoothSocket socket = null;
            while (this.isServerSocketValid) {
                try {
                    Log.i("BluetoothConnModel", "[ServerSocketThread] Enter while loop");
                    Log.i("BluetoothConnModel", "[ServerSocketThread] serverSocket hash code = " + this.mmServerSocket
                            .hashCode());
                    socket = this.mmServerSocket.accept();
                    Log.i("BluetoothConnModel", "[ServerSocketThread] Got client socket");
                } catch (IOException e) {
                    Log.e("BluetoothConnModel", "accept() failed", e);
                    break;
                }
                if (socket != null) {
                    synchronized (BluetoothConnModel.this) {
                        Log.i("BluetoothConnModel", "[ServerSocketThread] " + socket

                                .getRemoteDevice() + " is connected.");
                        BluetoothConnModel.this.connected(socket);
                        BluetoothConnModel.this.disconnectServerSocket();
                    }
                    break;
                }
            }
            Log.i("BluetoothConnModel", "[ServerSocketThread] break from while");
            BluetoothConnModel.this.startSession();
        }

        public void disconnect() {
            Log.d("BluetoothConnModel", "[ServerSocketThread] disconnect " + this);
            try {
                if (this.mmServerSocket != null) {
                    Log.i("BluetoothConnModel", "[ServerSocketThread] disconnect serverSocket name = " + this.mmServerSocket);
                    this.mmServerSocket.close();
                }
                Log.i("BluetoothConnModel", "[ServerSocketThread] mmServerSocket is closed.");
            } catch (IOException e) {
                Log.e("BluetoothConnModel", "close() of server failed", e);
            }
        }
    }

    private class SocketThread implements Runnable {
        private final BluetoothSocket mmSocket;

        private final BluetoothDevice mmDevice;

        private Thread thread = null;

        public SocketThread(BluetoothDevice device) {
            this.thread = new Thread(this);
            Log.i("BluetoothConnModel", "[SocketThread] Enter these server sockets");
            this.mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                if (DeviceDependency.shouldUseFixChannel()) {
                    try {
                        Method m = device.getClass().getMethod("createInsecureRfcommSocket", int.class);
                        tmp = (BluetoothSocket) m.invoke(device, new Object[]{Integer.valueOf(6)});
                    } catch (SecurityException e1) {
                        e1.printStackTrace();
                    } catch (NoSuchMethodException e1) {
                        e1.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } else if (DeviceDependency.shouldUseSecure()) {
                    // 加密傳輸，Android強制執行配對，彈窗顯示輸入配對碼，推薦使用這種
                    tmp = device.createRfcommSocketToServiceRecord(BluetoothConnModel.CUSTOM_UUID);
                } else {
                    // 明文傳輸(不安全)，無需配對，通常使用於藍牙2.1設備，因爲對於藍牙2.1設備
                    //                如果有任何設備沒有具有輸入和輸出能力或顯示數字鍵，無法進行安全套接字連接
                    tmp = device.createInsecureRfcommSocketToServiceRecord(BluetoothConnModel.CUSTOM_UUID);
                }
                Log.i("BluetoothConnModel", "[SocketThread] Constructure: Get a BluetoothSocket for a connection, create Rfcomm");
            } catch (Exception e) {
                Log.e("BluetoothConnModel", "create() failed", e);
            }
            this.mmSocket = tmp;
        }

        public void start() {
            this.thread.start();
        }

        public void run() {
            try {
                Log.i("BluetoothConnModel", "[SocketThread] try connecting...");
                this.mmSocket.connect();
                BluetoothConnModel.this.callback.onSPPConnection();
                Log.i("BluetoothConnModel", "[SocketThread] Return a successful connection");
            } catch (Exception e) {
                BluetoothConnModel.this.callback.onSPPConnectFailed();
                BluetoothConnModel.this.notifyUiFromToast("Unable to connect device: " + this.mmDevice
                        .getName());
                Log.i("BluetoothConnModel", "Unable to connect device: " + this.mmDevice.getName());
                Log.i("BluetoothConnModel", "[SocketThread] Connection failed", e);
                try {
                    this.mmSocket.close();
                    Log.i("BluetoothConnModel", "[SocketThread] Connect fail, close the client socket");
                } catch (IOException e2) {
                    Log.e("BluetoothConnModel", "unable to close() socket during connection failure", e2);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                this.thread = null;
                return;
            }
            synchronized (BluetoothConnModel.this) {
                BluetoothConnModel.this.connected(this.mmSocket);
                Log.i("BluetoothConnModel", "[SocketThread] " + this.mmDevice + " is connected.");
            }
            this.thread = null;
            Log.i("BluetoothConnModel", "END mConnectThread");
        }
    }

    public class ConnectedThread implements Runnable {
        protected BluetoothSocket mmSocket;

        private final InputStream mmInStream;

        private final OutputStream mmOutStream;

        private Thread thread = null;

        private ConnectedThread(BluetoothSocket socket) {
            this.thread = new Thread(this, socket.getRemoteDevice().toString());
            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                BluetoothConnModel.this.callback.onSPPConnection();
            } catch (IOException e) {
                BluetoothConnModel.this.callback.onSPPConnectFailed();
            }
            this.mmInStream = tmpIn;
            this.mmOutStream = tmpOut;
        }

        public void start() {
            this.thread.start();
        }

        public void run() {
            Log.d("BluetoothConnModel", "BEGIN ConnectedThread" + this);
            byte[] buffer = new byte[1024];
            BluetoothConnModel.this.mTxBytes = BluetoothConnModel.this.mRxBytes = 0;
            while (BluetoothConnModel.this.mSocketConfig.isSocketConnected(this.mmSocket)) {
                try {
                    Log.i("BluetoothConnModel", "read start");
                    int bytes = this.mmInStream.read(buffer, 0, 1024);
                    String msg = new String(buffer, 0, bytes, StandardCharsets.ISO_8859_1);
                    BluetoothConnModel.this.mRxBytes += bytes;
                    Log.i("BluetoothConnModel", "[ConnectedThread] read bytes: " + bytes);
                    BluetoothConnModel.this.callback.onSPPReceiveData(msg, buffer, bytes);
                } catch (IOException e) {
                    BluetoothConnModel.this.disconnectSocket(this.mmSocket);
                    break;
                }
            }
            Log.i("BluetoothConnModel", "[ConnectedThread] break from while");
        }

        public boolean write(String msg) {
            try {
                BluetoothConnModel.this.mTxBytes += msg.length();
                this.mmOutStream.write(msg.getBytes());
            } catch (IOException e) {
                Log.e("BluetoothConnModel", "[ConnectedThread] Exception during write", e);
                return false;
            }
            return true;
        }

        public boolean write(byte[] msg) {
            try {
                BluetoothConnModel.this.mTxBytes += msg.length;
                this.mmOutStream.write(msg);
            } catch (IOException e) {
                Log.e("BluetoothConnModel", "[ConnectedThread] Exception during write", e);
                return false;
            }
            return true;
        }
    }

    public class SendFileThread extends ConnectedThread {
        private final String fileName;

        private SendFileThread(BluetoothSocket socket, String file) {
            super(socket);
            this.fileName = file;
            Log.d("BluetoothConnModel", "SendFileThread Create: " + file);
        }

        public void run() {
            FileInputStream inputStream;
            Log.d("BluetoothConnModel", "BEGIN SendFileThread " + this);
            try {
                inputStream = new FileInputStream(this.fileName);
            } catch (Exception e) {
                Log.d("BluetoothConnModel", "Exception during new FileInputStream");
                return;
            }
            byte[] buffer = new byte[1024];
            int bytes = 0;
            while (BluetoothConnModel.this.mSocketConfig.isSocketConnected(this.mmSocket)) {
                try {
                    bytes = inputStream.read(buffer, 0, 1024);
                    if (bytes <= 0)
                        break;
                    Log.d("BluetoothConnModel", "length = " + bytes);
                    String msg = new String(buffer, 0, bytes, StandardCharsets.ISO_8859_1);
                    if (!write(msg))
                        break;
                    Log.d("BluetoothConnModel", "[send file]write OK");
                } catch (Exception e) {
                    Log.d("BluetoothConnModel", "[SendFile] Exception during send file", e);
                    break;
                }
            }
        }
    }
}
