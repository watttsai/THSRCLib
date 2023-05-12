package com.symlink.thsrlib;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.text.StringContainsInOrder.stringContainsInOrder;

import com.symlink.thsrlib.commands.fields.CardType;
import com.symlink.thsrlib.commands.fields.EntryFlag;
import com.symlink.thsrlib.commands.fields.StationId;
import com.symlink.thsrlib.commands.fields.SystemId;
import com.symlink.thsrlib.commands.fields.UsedRange;

import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

class ParserTest {

    private static Stream<Arguments> provideErrorCodeParameters() {
        return Stream.of(
                Arguments.of(createPacket(0, 0, 0x0000, ""), Constants.CODE_SUCCESS),
                Arguments.of(createPacket(0, 0, 0x0001, ""), Constants.CODE_ERROR_DATA_LENGTH),
                Arguments.of(createPacket(0, 0, 0x0002, ""), Constants.CODE_ERROR_CHECK_CODE),
                Arguments.of(createPacket(0, 0, 0x0003, ""), Constants.CODE_FAIL_INVALID_CARD),
                Arguments.of(createPacket(0, 0, 0x0004, ""), Constants.CODE_FAIL_READER_SETUP),
                Arguments.of(createPacket(0, 0, 0x0005, ""), Constants.CODE_FAIL_LACK_PAPER),
                Arguments.of(createPacket(0, 0, 0x0006, ""), Constants.CODE_FAIL_PRINT),
                Arguments.of(createPacket(0, 0, 0x0007, ""), Constants.CODE_ERROR_DATA),
                Arguments.of(createPacket(0, 0, 0x1001, ""), Constants.CODE_FAIL_INVALID_STATE),
                Arguments.of(createPacket(0, 0, 0x1002, ""), Constants.CODE_FAIL_INVALID_AMOUNT),
                Arguments.of(createPacket(0, 0, 0x1003, ""), Constants.CODE_FAIL_USER_CANCEL),
                Arguments.of(createPacket(0, 0, 0x1004, ""), Constants.CODE_FAIL_RECORD_NOT_FOUND)
        );
    }

    private static String createPacket(int sn, int cmd, int sc, String data) {
        int payloadLength = 5 + data.length();
        ByteBuffer buf = ByteBuffer.allocate(4 + payloadLength);
        buf.put((byte) 0x01);
        buf.order(ByteOrder.LITTLE_ENDIAN)
                .putShort((short) payloadLength);
        buf.put((byte) sn);
        buf.order(ByteOrder.BIG_ENDIAN)
                .putShort((short) cmd);
        buf.order(ByteOrder.BIG_ENDIAN)
                .putShort((short) sc);
        buf.put(data.getBytes(StandardCharsets.ISO_8859_1));
        byte lrc = HexUtil.ByteArrayXOR(buf.array(), 1, buf.position());
        buf.put(lrc);
        return HexUtil.encodeHexStr(buf.array());
    }

    @Test
    void testParseShouldReturnCodeErrorDataLength_whenGivenInvalidLengthData() {
        byte[] bytes = HexUtil.hexStringToByteArray("011234010001000026");
        Message msg = Parser.parseMessage(bytes);
        assertThat(msg, notNullValue());
        assertThat(msg.Code, equalTo(Constants.CODE_ERROR_DATA_LENGTH));
    }

    @Test
    void testParseShouldReturnCodeErrorCheckCode_whenGivenInvalidLrcValue() {
        byte[] bytes = HexUtil.hexStringToByteArray("0105000100010000FF");
        Message msg = Parser.parseMessage(bytes);
        assertThat(msg, notNullValue());
        assertThat(msg.Code, equalTo(Constants.CODE_ERROR_CHECK_CODE));
    }

    @ParameterizedTest
    @MethodSource("provideErrorCodeParameters")
    void testParseShouldReturnSpecificErrorCode(String packet, int expectErrorCode) {
        Message msg = Parser.parseMessage(HexUtil.hexStringToByteArray(packet));
        assertThat(msg, notNullValue());
        assertThat(msg.Code, equalTo(expectErrorCode));
    }

    @Test
    void testParseWithUnknownCmd() {
        Message msg = Parser.parseMessage(HexUtil.hexStringToByteArray(createPacket(0, 0x9999, 0, "1234")));
        assertThat(msg, notNullValue());
        assertThat(msg.Code, equalTo(Constants.CODE_SUCCESS));
        assertThat(msg.GetMessage(), equalTo(Constants.GetMessage(Constants.CODE_SUCCESS)));
    }

    @Test
    void testParseWithUnknownSc() {
        Message msg = Parser.parseMessage(HexUtil.hexStringToByteArray(createPacket(0, 0x9999, 0x8278, "1234")));
        assertThat(msg, notNullValue());
        assertThat(msg.Code, equalTo(Constants.CODE_UNKNOWN));
        assertThat(msg.GetMessage(), equalTo(Constants.GetMessage(Constants.CODE_UNKNOWN)));
    }

    @Test
    void testParseCmdIs0001_shouldGetModelNameSuccessful() {
        String modelName = "AS350-56789012345   ";
        Message msg = Parser.parseMessage(HexUtil.hexStringToByteArray(createPacket(0x99, 0x0001, 0x0000, modelName)));
        assertThat(msg, notNullValue());
        assertThat(msg.Code, equalTo(Constants.CODE_SUCCESS));
        assertThat(msg.GetMessage(), containsString(modelName));
    }

    @Test
    void testParseCmdIs0201_shouldGetCardInfoSuccessful() {
        String cardNoStr = "123451234512345     ";
        String txnTimeStr = "yyyyMMddHHmmss";
        String systemIdStr = "001";
        SystemId systemId = SystemId.findById(systemIdStr);
        assertThat(systemId, IsNull.notNullValue());

        String stationIdStr = "01";
        StationId stationId = StationId.findById(stationIdStr);
        assertThat(stationId, IsNull.notNullValue());

        String entryFlagStr = "02";
        EntryFlag entryFlag = EntryFlag.findByCode(entryFlagStr);
        assertThat(entryFlag, IsNull.notNullValue());

        String usedRangeStr = "1112";
        UsedRange usedRange = new UsedRange(usedRangeStr);

        String cardTypeStr = "10";
        CardType cardType = CardType.findByCode(cardTypeStr);
        assertThat(cardType, IsNull.notNullValue());

        String payload = String.join("", cardNoStr, txnTimeStr, systemIdStr, stationIdStr, entryFlagStr, usedRangeStr, cardTypeStr);

        Message msg = Parser.parseMessage(HexUtil.hexStringToByteArray(createPacket(0, 0x0202, 0x0000, payload)));

        assertThat(msg, notNullValue());
        assertThat(msg.Code, equalTo(Constants.CODE_SUCCESS));
        assertThat(msg.GetMessage(), stringContainsInOrder(
                "票卡號碼：", cardNoStr,
                "日期時間：", txnTimeStr,
                "業者類別：", systemId.getId(), systemId.getSystemName(),
                "站別：", stationId.getId(), stationId.getStationName(),
                "進站/出站：", entryFlag.getCode(), entryFlag.getDescription(),
                "使用區間：", usedRange.toString(),
                "交易票證公司：", cardType.getCode(), cardType.getCardTypeName()
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0402", "0404"})
    void testParseCreditCardTransResponse_shouldGetCreditCardInfoSuccessful(String cmdHexString) {
        String hostId = "0";
        String amount = "000000000123";
        String thsrTransId = "12345678";
        String smisTransId = "1234567890123";
        String staffId = "87654321";
        String responseCode = "00";
        String invoiceNo = "000321";
        String cardNo = "123456******7890   ";
        String transDate = "230316";
        String transTime = "113655";
        String approveCode = "accept";
        String terminalId = "12341234";
        String merchantId = "123451234512345";
        String rrn = "123456123456";
        String batchNo = "000123";
        String creditCardType = "01";
        String posEntryMode = "00";
        String payload = String.join("", hostId, amount, thsrTransId, smisTransId, staffId, responseCode, invoiceNo, cardNo, transDate, transTime, approveCode, terminalId, merchantId, rrn, batchNo, creditCardType, posEntryMode);

        int cmd = Integer.parseInt(cmdHexString, 16);
        byte[] responseBytes = HexUtil.hexStringToByteArray(createPacket(0, cmd, 0x0000, payload));
        Message msg = Parser.parseMessage(responseBytes);

        assertThat(msg, notNullValue());
        assertThat(msg.Code, equalTo(Constants.CODE_SUCCESS));
        assertThat(msg.GetMessage(), stringContainsInOrder(
                "授權銀行：", hostId,
                "交易金額：", amount,
                "補票編號：", thsrTransId,
                "SMIS 票號：", smisTransId,
                "站務員編號：", staffId,
                "回應碼：", responseCode,
                "調閱編號：", invoiceNo,
                "卡號：", cardNo,
                "交易日期：", transDate,
                "交易時間：", transTime,
                "授權號碼：", approveCode,
                "端末機代號：", terminalId,
                "商店代號：", merchantId,
                "交易序號：", rrn,
                "批次號碼：", batchNo,
                "卡別：", creditCardType,
                "過卡方式：", posEntryMode
        ));
    }

    @Test
    void testParseCreditCardInquireLogResponse_shouldGetCreditCardInfoSuccessful() {
        String transType = "0";
        String hostId = "0";
        String amount = "000000000123";
        String thsrTransId = "12345678";
        String smisTransId = "1234567890123";
        String staffId = "87654321";
        String responseCode = "00";
        String invoiceNo = "000321";
        String cardNo = "123456******7890   ";
        String transDate = "230316";
        String transTime = "113655";
        String approveCode = "accept";
        String terminalId = "12341234";
        String merchantId = "123451234512345";
        String rrn = "123456123456";
        String batchNo = "000123";
        String creditCardType = "01";
        String posEntryMode = "00";
        String payload = String.join("", transType, hostId, amount, thsrTransId, smisTransId, staffId, responseCode, invoiceNo, cardNo, transDate, transTime, approveCode, terminalId, merchantId, rrn, batchNo, creditCardType, posEntryMode);

        int cmd = 0x0501;
        byte[] responseBytes = HexUtil.hexStringToByteArray(createPacket(0, cmd, 0x0000, payload));
        Message msg = Parser.parseMessage(responseBytes);

        assertThat(msg, notNullValue());
        assertThat(msg.Code, equalTo(Constants.CODE_SUCCESS));
        assertThat(msg.GetMessage(), stringContainsInOrder(
                "交易類別：", transType,
                "授權銀行：", hostId,
                "交易金額：", amount,
                "補票編號：", thsrTransId,
                "SMIS 票號：", smisTransId,
                "站務員編號：", staffId,
                "回應碼：", responseCode,
                "調閱編號：", invoiceNo,
                "卡號：", cardNo,
                "交易日期：", transDate,
                "交易時間：", transTime,
                "授權號碼：", approveCode,
                "端末機代號：", terminalId,
                "商店代號：", merchantId,
                "交易序號：", rrn,
                "批次號碼：", batchNo,
                "卡別：", creditCardType,
                "過卡方式：", posEntryMode
        ));
    }

    @Test
    void testParseCreditCardInquireTotalsResponse_shouldGetTotalsInfoSuccessful() {
        String creditSaleCount = "000123";
        String creditSaleAmount = "000000456789";
        String amexSaleCount = "000441";
        String amexSaleAmount = "000000044100";
        String cupSaleCount = "000441";
        String cupSaleAmount = "000000044100";
        String saleTotalCount = "000564";
        String saleTotalAmount = "000000500889";
        String creditVoidCount = "000003";
        String creditVoidAmount = "000000000120";
        String amexVoidCount = "000002";
        String amexVoidAmount = "000000000865";
        String cupVoidCount = "000002";
        String cupVoidAmount = "000000000865";
        String voidTotalCount = "000564";
        String voidTotalAmount = "000000500889";
        String payload = String.join("", creditSaleCount, creditSaleAmount, amexSaleCount, amexSaleAmount, cupSaleCount, cupSaleAmount, saleTotalCount, saleTotalAmount, creditVoidCount, creditVoidAmount, amexVoidCount, amexVoidAmount, cupVoidCount, cupVoidAmount, voidTotalCount, voidTotalAmount);

        int cmd = 0x0502;
        byte[] responseBytes = HexUtil.hexStringToByteArray(createPacket(0, cmd, 0x0000, payload));
        Message msg = Parser.parseMessage(responseBytes);

        assertThat(msg, notNullValue());
        assertThat(msg.Code, equalTo(Constants.CODE_SUCCESS));
        assertThat(msg.GetMessage(), stringContainsInOrder(
                "信用卡補票總筆數：", trim(creditSaleCount),
                "信用卡補票總金額：", trim(creditSaleAmount),
                "AMEX補票總筆數：", trim(amexSaleCount),
                "AMEX補票總金額：", trim(amexSaleAmount),
                "銀聯卡補票總筆數：", trim(cupSaleCount),
                "銀聯卡補票總金額：", trim(cupSaleAmount),
                "補票合計筆數：", trim(saleTotalCount),
                "補票合計金額：", trim(saleTotalAmount),
                "信用卡取消總筆數：", trim(creditVoidCount),
                "信用卡取消總金額：", trim(creditVoidAmount),
                "AMEX取消總筆數：", trim(amexVoidCount),
                "AMEX取消總金額：", trim(amexVoidAmount),
                "銀聯卡取消總筆數：", trim(cupVoidCount),
                "銀聯卡取消總金額：", trim(cupVoidAmount),
                "取消合計筆數：", trim(voidTotalCount),
                "取消合計金額：", trim(voidTotalAmount)
        ));
    }

    // trim leading zeros
    private String trim(String str) {
        int i = 0;
        while (i < str.length() && str.charAt(i) == '0') {
            i++;
        }
        return str.substring(i);
    }
}
