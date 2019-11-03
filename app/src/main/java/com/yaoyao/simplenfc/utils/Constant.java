package com.yaoyao.simplenfc.utils;

/**
 * @author wangyao
 * @description:
 * @date: 2019/7/16 21:37
 */
public class Constant {

    //卡片的ID
    public static final String NFC_ID = "nfcID";
    //卡片的内容
    public static final String NFC_MESSAGE = "nfcMessage";
    //nfc-写NDEF数据
    public static final int NFC_WRITE_NDEF = 0x0001;
    //nfc-写MiareClassic数据
    public static final int NFC_WRITE_MIARECLASSIC = 0x0002;
    //nfc-读卡
    public static final int NFC_READ = 0x0003;
    //nfc-清除数据
    public static final int NFC_CLEAR = 0x0004;

}
