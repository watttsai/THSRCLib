package com.symlink.thsrlib;

public interface IDataListener {
    void OnDataAvailable(Message paramMessage);

    default void OnDataAvailable(byte[] data) {
    }
}
