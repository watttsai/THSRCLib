package com.symlink.thsrlib;

/**
 * Created by user on 2017/7/28.
 */

public class ReturnClass {
    public int Code;
    public byte[] Message = new byte[50];

    public ReturnClass() {
    }

    public ReturnClass(int code) {
        this.Code = code;
    }
}
