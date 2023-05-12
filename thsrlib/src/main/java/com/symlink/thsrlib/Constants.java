package com.symlink.thsrlib;

public class Constants {
    public static final String TAG = "thsrc";

    public static final String SP_NAME = "YifengSP";

    public static final String SP_KEY_DEVICE_NAME = "DeviceName";

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_RECEVING = 1;
    public static final int CODE_ERROR_DATA_LENGTH = 501;
    public static final int CODE_ERROR_CHECK_CODE = 502;
    public static final int CODE_ERROR_DATA = 503;
    public static final int CODE_FAIL_READER_NO_RESPONSE = 600;
    public static final int CODE_FAIL_READER_SETUP = 601;
    public static final int CODE_FAIL_PRINT = 610;
    public static final int CODE_FAIL_LACK_PAPER = 611;
    public static final int CODE_FAIL_INVALID_CARD = 620;
    public static final int CODE_FAIL_INVALID_STATE = 701;
    public static final int CODE_FAIL_INVALID_AMOUNT = 702;
    public static final int CODE_FAIL_USER_CANCEL = 703;
    public static final int CODE_FAIL_RECORD_NOT_FOUND = 704;
    public static final int CODE_FAIL_INVALID_CARDTYPE_USE_CREDIT_CARD_INSTEAD = 705;
    public static final int CODE_FAIL_INVALID_CARDTYPE_USE_CUP_CARD_INSTEAD = 706;
    public static final int CODE_FAIL_BLUETOOTH_DISCONNECTED_AFTER_CONNECTION = 800;
    public static final int CODE_FAIL_BLUETOOTH_CLOSED = 900;
    public static final int CODE_FAIL_NO_MATCH_DEVICE = 901;
    public static final int CODE_FAIL_UNABLE_TO_PAIR_BLUETOOTH = 902;
    public static final int CODE_FAIL_BLUETOOTH_NOT_CONNECTED = 903;
    public static final int CODE_UNKNOWN = 999;

    public static final String MESSAGE_SUCCESS = "成功";
    public static final String MESSAGE_RECEVING = "接收中";
    public static final String MESSAGE_ERROR_DATA_LENGTH = "資料長度錯誤";
    public static final String MESSAGE_ERROR_CHECK_CODE = "資料檢核碼錯誤";
    public static final String MESSAGE_ERROR_DATA = "資料內容錯誤";
    public static final String MESSAGE_FAIL_READER_NO_RESPONSE = "無法連線刷卡機";
    public static final String MESSAGE_FAIL_READER_SETUP = "刷卡機設定失敗";
    public static final String MESSAGE_FAIL_PRINT = "列印失敗";
    public static final String MESSAGE_FAIL_LACK_PAPER = "刷卡機缺紙";
    public static final String MESSAGE_FAIL_INVALID_CARD = "無效卡片";
    public static final String MESSAGE_FAIL_INVALID_STATE = "未關閉票卡查詢功能";
    public static final String MESSAGE_FAIL_INVALID_AMOUNT = "交易金額為0";
    public static final String MESSAGE_FAIL_USER_CANCEL = "操作錯誤/交易終止";
    public static final String MESSAGE_FAIL_RECORD_NOT_FOUND = "無此筆交易紀錄";
    public static final String MESSAGE_FAIL_INVALID_CARDTYPE_USE_CREDIT_CARD_INSTEAD = "卡別錯誤，請使用信用卡";
    public static final String MESSAGE_FAIL_INVALID_CARDTYPE_USE_CUP_CARD_INSTEAD = "卡別錯誤，請使用銀聯卡";
    public static final String MESSAGE_FAIL_BLUETOOTH_DISCONNECTED_AFTER_CONNECTION = "藍牙連線後斷線";
    public static final String MESSAGE_FAIL_BLUETOOTH_CLOSED = "藍牙未開啟";
    public static final String MESSAGE_FAIL_NO_MATCH_DEVICE = "藍牙無法搜尋到裝置";
    public static final String MESSAGE_FAIL_UNABLE_TO_PAIR_BLUETOOTH = "無法配對藍牙";
    public static final String MESSAGE_FAIL_BLUETOOTH_NOT_CONNECTED = "藍牙未連線";
    public static final String MESSAGE_UNKNOWN = "未知訊息";

    public static String GetMessage(int code) {
        switch (code) {
            case CODE_SUCCESS: // 0
                return MESSAGE_SUCCESS;
            case CODE_RECEVING: // 1
                return MESSAGE_RECEVING;
            case CODE_ERROR_DATA_LENGTH: // 501
                return MESSAGE_ERROR_DATA_LENGTH;
            case CODE_ERROR_CHECK_CODE: // 502
                return MESSAGE_ERROR_CHECK_CODE;
            case CODE_ERROR_DATA: // 503
                return MESSAGE_ERROR_DATA;
            case CODE_FAIL_READER_NO_RESPONSE: // 600
                return MESSAGE_FAIL_READER_NO_RESPONSE;
            case CODE_FAIL_READER_SETUP: // 601
                return MESSAGE_FAIL_READER_SETUP;
            case CODE_FAIL_PRINT: // 610
                return MESSAGE_FAIL_PRINT;
            case CODE_FAIL_LACK_PAPER: // 611
                return MESSAGE_FAIL_LACK_PAPER;
            case CODE_FAIL_INVALID_CARD: // 620
                return MESSAGE_FAIL_INVALID_CARD;
            case CODE_FAIL_INVALID_STATE: // 701
                return MESSAGE_FAIL_INVALID_STATE;
            case CODE_FAIL_INVALID_AMOUNT: // 702
                return MESSAGE_FAIL_INVALID_AMOUNT;
            case CODE_FAIL_USER_CANCEL: // 703
                return MESSAGE_FAIL_USER_CANCEL;
            case CODE_FAIL_RECORD_NOT_FOUND: // 704
                return MESSAGE_FAIL_RECORD_NOT_FOUND;
            case CODE_FAIL_INVALID_CARDTYPE_USE_CREDIT_CARD_INSTEAD: // 705
                return MESSAGE_FAIL_INVALID_CARDTYPE_USE_CREDIT_CARD_INSTEAD;
            case CODE_FAIL_INVALID_CARDTYPE_USE_CUP_CARD_INSTEAD: // 706
                return MESSAGE_FAIL_INVALID_CARDTYPE_USE_CUP_CARD_INSTEAD;
            case CODE_FAIL_BLUETOOTH_DISCONNECTED_AFTER_CONNECTION: // 800
                return MESSAGE_FAIL_BLUETOOTH_DISCONNECTED_AFTER_CONNECTION;
            case CODE_FAIL_BLUETOOTH_CLOSED: // 900
                return MESSAGE_FAIL_BLUETOOTH_CLOSED;
            case CODE_FAIL_NO_MATCH_DEVICE: // 901
                return MESSAGE_FAIL_NO_MATCH_DEVICE;
            case CODE_FAIL_UNABLE_TO_PAIR_BLUETOOTH: // 902
                return MESSAGE_FAIL_UNABLE_TO_PAIR_BLUETOOTH;
            case CODE_FAIL_BLUETOOTH_NOT_CONNECTED: // 903
                return MESSAGE_FAIL_BLUETOOTH_NOT_CONNECTED;
            case CODE_UNKNOWN: // 999
            default:
                // include the error code in error message
                return code + ":" + MESSAGE_UNKNOWN;
        }
    }
}
