/*
 * Copyright (C) 2011 Wireless Network and Multimedia Laboratory, NCU, Taiwan
 *
 * You can reference http://wmlab.csie.ncu.edu.tw
 *
 * This class defines that the related socket state and I/O thread (ConnectedThread).
 *
 *
 * @author Fiona
 * @version 0.0.1
 *
 */


package com.symlink.lib.model;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class BluetoothSocketConfig {
    public static final int SOCKET_NONE = 0;
    public static final int SOCKET_CONNECTED = 1;
    public static final int FIELD_CONNECTED_THREAD = 0;
    public static final int FIELD_SOCKET_STATE = 1;
    private static final String TAG = "BluetoothSocketConfig";
    private static final boolean D = true;
    private static BluetoothSocketConfig mBtSocketConfig = null;
    private final Map<BluetoothSocket, BluetoothSocketInfo> mBluetoothSocekts = new HashMap<>();

    public static BluetoothSocketConfig getInstance() {
        if (mBtSocketConfig == null)
            synchronized (BluetoothSocketConfig.class) {
                if (mBtSocketConfig == null)
                    mBtSocketConfig = new BluetoothSocketConfig();
            }
        return mBtSocketConfig;
    }

    public boolean registerSocket(BluetoothSocket socket, BluetoothConnModel.ConnectedThread t, int socketState) {
        Log.d("BluetoothSocketConfig", "[registerSocket] start");
        boolean status = true;
        if (socketState == 1) {
            Set<BluetoothSocket> socketSets = containSockets(socket.getRemoteDevice().getAddress());
            for (BluetoothSocket tmp : socketSets) {
                unregisterSocket(tmp);
                status = false;
            }
        }
        BluetoothSocketInfo socketInfo = new BluetoothSocketInfo();
        socketInfo.setBluetoothSocket(socket);
        socketInfo.setConnectedThread(t);
        socketInfo.setSocketState(socketState);
        Log.i("BluetoothSocketConfig", "put socket to mBluetoothSocekts");
        this.mBluetoothSocekts.put(socket, socketInfo);
        return status;
    }

    public void updateSocketInfo(BluetoothSocket socket, int field, Object arg) {
        if (this.mBluetoothSocekts.containsKey(socket)) {
            BluetoothSocketInfo socketInfo = this.mBluetoothSocekts.get(socket);
            if (field == 0) {
                BluetoothConnModel.ConnectedThread t = (BluetoothConnModel.ConnectedThread) arg;
                socketInfo.setConnectedThread(t);
            } else if (field == 1) {
                int socketState = ((Integer) arg).intValue();
                socketInfo.setSocketState(socketState);
            }
            this.mBluetoothSocekts.put(socket, socketInfo);
        } else {
            Log.e("BluetoothSocketConfig", "[updateSocketInfo] Socket doesn't exist.");
        }
    }

    public void unregisterSocket(BluetoothSocket socket) {
        Log.d("BluetoothSocketConfig", "try remove socket");
        if (this.mBluetoothSocekts.containsKey(socket)) {
            BluetoothSocketInfo socketInfo = this.mBluetoothSocekts.get(socket);
            try {
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
                if (socket != null)
                    socket.close();
            } catch (IOException iOException) {
            }
            socketInfo.setConnectedThread(null);
            socketInfo.setSocketState(0);
            socketInfo.setBluetoothSocket(null);
            this.mBluetoothSocekts.remove(socket);
        }
    }

    public Set<BluetoothSocket> containSockets(String address) {
        Set<BluetoothSocket> socketSets = new HashSet<>();
        Iterator<BluetoothSocket> it = this.mBluetoothSocekts.keySet().iterator();
        while (it.hasNext()) {
            BluetoothSocket socket = it.next();
            if (socket.getRemoteDevice().getAddress().contains(address))
                socketSets.add(socket);
        }
        return socketSets;
    }

    public Set<BluetoothSocket> getConnectedSocketList() {
        return this.mBluetoothSocekts.keySet();
    }

    public BluetoothConnModel.ConnectedThread getConnectedThread(BluetoothSocket socket) {
        try {
            if (socket == null)
                return null;
            if (this.mBluetoothSocekts == null)
                return null;
            BluetoothSocketInfo socketInfo = this.mBluetoothSocekts.get(socket);
            if (socketInfo == null)
                return null;
            return socketInfo.getConnectedThread(socket);
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean isSocketConnected(BluetoothSocket socket) {
		return this.mBluetoothSocekts.containsKey(socket);
	}

    private class BluetoothSocketInfo {
        private int mState = 0;

        private BluetoothSocket mBluetoothSocket;

        private BluetoothConnModel.ConnectedThread mConnectedThread;

        private BluetoothSocketInfo() {
        }

        public BluetoothSocket getBluetoothSocket() {
            return this.mBluetoothSocket;
        }

        protected void setBluetoothSocket(BluetoothSocket socket) {
            this.mBluetoothSocket = socket;
        }

        public BluetoothConnModel.ConnectedThread getConnectedThread(BluetoothSocket socket) {
            return this.mConnectedThread;
        }

        protected void setSocketState(int socketState) {
            this.mState = socketState;
        }

        protected void setConnectedThread(BluetoothConnModel.ConnectedThread t) {
            this.mConnectedThread = t;
        }
    }
}
