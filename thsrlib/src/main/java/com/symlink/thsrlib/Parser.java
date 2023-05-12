package com.symlink.thsrlib;

import android.util.Log;

import com.symlink.thsrlib.commands.fields.Cmd;
import com.symlink.thsrlib.commands.CommandResponse;
import com.symlink.thsrlib.commands.CreditCardInquiryResponse;
import com.symlink.thsrlib.commands.CreditCardSaleResponse;
import com.symlink.thsrlib.commands.CreditCardTotalsResponse;
import com.symlink.thsrlib.commands.CreditCardVoidResponse;
import com.symlink.thsrlib.commands.ErrorResponse;
import com.symlink.thsrlib.commands.InquireCardResponse;
import com.symlink.thsrlib.commands.ReaderConnectionResponse;
import com.symlink.thsrlib.commands.SuccessResponse;
import com.symlink.thsrlib.commands.exceptions.ParseErrorException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.Objects;

public class Parser {
    private static final String TAG = Parser.class.getSimpleName();
    private static final Map<Integer, Class<? extends CommandResponse>> parserMap = Map.of(
            0x0001, ReaderConnectionResponse.class,
            0x0202, InquireCardResponse.class,
            0x0402, CreditCardSaleResponse.class,
            0x0404, CreditCardVoidResponse.class,
            0x0501, CreditCardInquiryResponse.class,
            0x0502, CreditCardTotalsResponse.class
    );

    private Parser() {
    }

    /**
     * Parses the given byte array into a Message object.
     * The byte array should contain a valid message in the protocol format.
     *
     * @param bytes the byte array to parse
     * @return the parsed Message object
     * @throws IllegalArgumentException if the byte array is not a valid message in the protocol format
     */
    public static Message parseMessage(byte[] bytes) {
        CommandResponse response = parseResponse(bytes);
        if (response.isError()) {
            return Message.fail(response.getReturnCode(), response.format());
        } else {
            return Message.success(response.format());
        }
    }

    /**
     * Parses the given byte array into a Response object.
     * The byte array should contain a valid message in the protocol format.
     *
     * @param bytes the byte array to parse
     * @return the parsed Response object
     * @throws IllegalArgumentException if the byte array is not a valid message in the protocol format
     */
    public static CommandResponse parseResponse(byte[] bytes) {
        byte expectLrc = bytes[bytes.length - 1];
        byte gotLrc = HexUtil.ByteArrayXOR(bytes, 1, bytes.length - 2);
        if (gotLrc != expectLrc) {
            Log.e(TAG, String.format("Invalid LRC, expect=0x%02X, got=0x%02X", expectLrc, gotLrc));
            return error(-1, -1, Constants.CODE_ERROR_CHECK_CODE);
        }

        int dataLength = ByteBuffer.wrap(bytes, 1, 2)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getShort();
        // 4 bytes: SOH(1) dataLength(2) + LRC(1)
        // 5 bytes: minimum payload size: SN(1), CMD(2), SC(2), Data(0)
        if (dataLength < 5 || dataLength != bytes.length - 4) {
            Log.e(TAG, String.format("Invalid data length=%d, total length=%d", dataLength, bytes.length));
            return error(-1, -1, Constants.CODE_ERROR_DATA_LENGTH);
        }

        ByteBuffer buf = ByteBuffer.wrap(bytes, 3, dataLength);
        byte sn = buf.get();
        int cmd = buf.order(ByteOrder.BIG_ENDIAN).getShort();
        int sc = buf.order(ByteOrder.BIG_ENDIAN).getShort();
        int returnCode = mapSCToReturnCode(sc);
        Log.d(TAG, String.format("Response: SN=%02X, CMD=%04X, SC=%04X(%d, %s), Data='%s'",
                sn, cmd, sc, returnCode, Constants.GetMessage(returnCode), new String(buf.array(), buf.position(), buf.remaining())));
        if (returnCode != Constants.CODE_SUCCESS) {
            return error(sn, cmd, returnCode);
        }

        if (parserMap.get(cmd) == null) {
            return success(sn, cmd);
        }
        try {
            return Objects.requireNonNull(parserMap.get(cmd)).newInstance().parse(sn, Cmd.findByCode(cmd), buf);
        } catch (NullPointerException | IllegalAccessException | InstantiationException | ParseErrorException e) {
            e.printStackTrace();
            return error(sn, cmd, Constants.CODE_ERROR_DATA);
        }
    }

    private static CommandResponse error(int sn, int cmd, int errorCode) {
        return new ErrorResponse(sn, Cmd.findByCode(cmd), errorCode, Constants.GetMessage(errorCode));
    }

    private static CommandResponse success(int sn, int cmd) {
        return new SuccessResponse(sn, Cmd.findByCode(cmd), Constants.GetMessage(Constants.CODE_SUCCESS));
    }

    private static int mapSCToReturnCode(int sc) {
        switch (sc) {
            case 0x0000:
                return Constants.CODE_SUCCESS;
            case 0x0001:
                return Constants.CODE_ERROR_DATA_LENGTH;
            case 0x0002:
                return Constants.CODE_ERROR_CHECK_CODE;
            case 0x0003:
                return Constants.CODE_FAIL_INVALID_CARD;
            case 0x0004:
                return Constants.CODE_FAIL_READER_SETUP;
            case 0x0005:
                return Constants.CODE_FAIL_LACK_PAPER;
            case 0x0006:
                return Constants.CODE_FAIL_PRINT;
            case 0x0007:
                return Constants.CODE_ERROR_DATA;
            case 0x1001:
                return Constants.CODE_FAIL_INVALID_STATE;
            case 0x1002:
                return Constants.CODE_FAIL_INVALID_AMOUNT;
            case 0x1003:
                return Constants.CODE_FAIL_USER_CANCEL;
            case 0x1004:
                return Constants.CODE_FAIL_RECORD_NOT_FOUND;
            default:
                return Constants.CODE_UNKNOWN;
        }
    }
}
