package com.symlink.thsrlib;

public class MessageBase {
    private boolean _isOK;

    private String _message;

    public MessageBase() {
        this._isOK = true;
        this._message = "";
    }

    public MessageBase(String message) {
        this._message = message;
    }

    public boolean GetIsOK() {
        return this._isOK;
    }

    public String GetMessage() {
        return this._message;
    }

    public void SetMessage(String message) {
        this._message = message;
    }

    public void SetError(String message) {
        this._isOK = false;
        this._message = message;
    }
}
