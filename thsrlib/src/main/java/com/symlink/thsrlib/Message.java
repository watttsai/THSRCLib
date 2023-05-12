package com.symlink.thsrlib;

public class Message extends MessageBase {
    public boolean IsCardData = false;

    public int Code;

    public static Message success(String message) {
        Message msg = new Message();
        msg.Code = Constants.CODE_SUCCESS;
        msg.SetMessage(message);
        return msg;
    }
    /**
     * creates a failed Message with given error code and error message
     *
     * @param errorCode the error code for the created Message
     * @param errorMessage the error message for the created Message
     * @return the created message
     */
    public static Message fail(int errorCode, String errorMessage) {
        Message msg = new Message();
        msg.Code = errorCode;
        msg.SetError(errorMessage);
        return msg;
    }
}
