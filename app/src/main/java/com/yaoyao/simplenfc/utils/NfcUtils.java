package com.yaoyao.simplenfc.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Parcelable;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author wangyao
 * @description: NFC 工具类
 * @date: 2019/7/16 22:00
 */
public class NfcUtils {

    private static final String TAG = "NfcUtils";
    private NfcAdapter mNfcAdapter;
    private IntentFilter[] mIntentFilter = null;
    private PendingIntent mPendingIntent = null;
    private String[][] mTechList = null;
    private Activity activity;

    public NfcUtils(Activity activity) {
        this.activity = activity;
        check();
    }

    /**
     * nfc检测
     */
    private void check() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (mNfcAdapter == null) {
            ToastUtils.showShort("设备不支持NFC功能!");
        } else {
            if (!mNfcAdapter.isEnabled()) {
                showSettingDailog();
            } else {
                Logger.e(TAG, "NFC功能已打开!");
                init();
            }
        }
    }

    /**
     * 初始化nfc设置
     */
    private void init() {
        Intent intent = new Intent(activity, activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mPendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);
        //intentFilter过滤----ndef
        IntentFilter ndefFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            //文本类型
            ndefFilter.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        //intentFilter过滤----非ndef
        IntentFilter techFilter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        //intentFilter过滤器列表
        mIntentFilter = new IntentFilter[]{ndefFilter, techFilter};
        //匹配的数据格式列表
        mTechList = new String[][]{
                {MifareClassic.class.getName()},
                {NfcA.class.getName()},
                {Ndef.class.getName()},
                {NdefFormatable.class.getName()}};
    }

    /**
     * Nfc监听intent
     */
    public void enableForegroundDispatch() {
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            mNfcAdapter.enableForegroundDispatch(activity, mPendingIntent, mIntentFilter, mTechList);
        }
    }

    /**
     * 取消监听Nfc
     */
    public void disableForegroundDispatch() {
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            mNfcAdapter.disableForegroundDispatch(activity);
        }
    }


    /**
     * 打开权限弹窗
     */
    private void showSettingDailog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("是否跳转到设置页面打开NFC功能");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 进入设置系统应用权限界面
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                activity.startActivity(intent);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }


    /**
     * 读取nfcID
     */
    public String readNFCId(Tag tag) {
        return ByteArrayToHexString(tag.getId());
    }

    public boolean getTagType(Tag tag, String args) {
        boolean result = false;
        for (String type : tag.getTechList()) {
            Logger.e(TAG, "TechList:" + type);
            if (args != null && type.contains(args)) {
                return true;
            }
        }
        return result;
    }

    /**
     * 读取Tag
     *
     * @param intent
     * @return
     */
    public Tag getNFCTag(Intent intent) {
        return intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    }


    /**
     * 读取NFC的数据
     */
    public String readMessage(Intent intent) {
        String info = "";
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            //NDEF格式数据
            info = readNdef(intent);
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            if (getTagType(getNFCTag(intent), MifareClassic.class.getSimpleName())) {
                //MifareClassic格式数据
                info = readMifareClassic(intent);
            }
        }
        return info;
    }

    /**
     * 读取Ndef的数据
     *
     * @return
     */
    private String readNdef(Intent intent) {
        String info = "";
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msgs[] = null;
        if (rawMsgs != null) {
            msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
            }
            NdefRecord record = msgs[0].getRecords()[0];
            if (record != null) {
                byte[] payload = record.getPayload();
                //下面代码分析payload：状态字节+ISO语言编码（ASCLL）+文本数据（UTF_8/UTF_16）
                //其中payload[0]放置状态字节：如果bit7为0，文本数据以UTF_8格式编码，如果为1则以UTF_16编码
                //bit6是保留位，默认为0
                /*
                 * payload[0] contains the "Status Byte Encodings" field, per the
                 * NFC Forum "Text Record Type Definition" section 3.2.1.
                 *
                 * bit7 is the Text Encoding Field.
                 *
                 * if (Bit_7 == 0): The text is encoded in UTF-8 if (Bit_7 == 1):
                 * The text is encoded in UTF16
                 *
                 * Bit_6 is reserved for future use and must be set to zero.
                 *
                 * Bits 5 to 0 are the length of the IANA language code.
                 */
                String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8"
                        : "UTF-16";
                //处理bit5-0。bit5-0表示语言编码长度（字节数）
                int languageCodeLength = payload[0] & 0x3f;
                //获取语言编码（从payload的第2个字节读取languageCodeLength个字节作为语言编码）
                try {
                    String languageCode = new String(payload, 1, languageCodeLength,
                            "US-ASCII");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                //解析出实际的文本数据
                try {
                    info = new String(payload, languageCodeLength + 1,
                            payload.length - languageCodeLength - 1, textEncoding);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return info;
    }

    private String readMifareClassic(Intent intent) {
        String info = "";
        List<byte[]> list = new ArrayList<>();
        boolean auth = false;
        //读取TAG
        MifareClassic mfc = MifareClassic.get(getNFCTag(intent));
        try {
            mfc.connect();
            if (mfc.isConnected()) {
                int sectorCount = mfc.getSectorCount();//获取TAG中包含的扇区数
                for (int j = 0; j < sectorCount; j++) {
                    auth = mfc.authenticateSectorWithKeyA(j,
                            MifareClassic.KEY_NFC_FORUM);
                    int bCount;
                    int bIndex;
                    if (auth) {
                        // 读取扇区中的块
                        bCount = mfc.getBlockCountInSector(j);
                        bIndex = mfc.sectorToBlock(j);
                        for (int i = 0; i < bCount; i++) {
                            byte[] data = mfc.readBlock(bIndex);
                            if (i < bCount - 1) {
                                if (!bytesToHexString(data).equals("0x00000000000000000000000000000000")) {
                                    list.add(data);
                                }
                            }
                            bIndex++;
                        }
                    }
                }
                if (list.size() > 0) {
                    byte[] aa = new byte[list.size() * list.get(0).length];
                    for (int i = 0; i < list.size(); i++) {
                        byte[] bytes = list.get(i);
                        System.arraycopy(bytes, 0, aa, bytes.length * i, bytes.length);
                    }
                    info = new String(trim(aa), Charset.forName("utf-8"));
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, e);
        } finally {
            try {
                mfc.close();
            } catch (IOException e) {
                Logger.e(TAG, e);
            }
        }
        return info;
    }

    public void writeMifareClassic(String data, Intent intent) throws IOException {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        MifareClassic mfc = MifareClassic.get(tag);
        try {
            mfc.connect();
            int sectorCount = mfc.getSectorCount();//获取TAG中包含的扇区数
            for (int j = 0; j < sectorCount; j++) {
                if (mfc.authenticateSectorWithKeyA(j,
                        MifareClassic.KEY_NFC_FORUM)) {
                    // 读取扇区中的块
                    int bCount = mfc.getBlockCountInSector(j);
                    int bIndex = mfc.sectorToBlock(j);
                    for (int i = 0; i < bCount - 1; i++) {
                        if (!bytesToHexString(mfc.readBlock(bIndex + i)).equals("0x00000000000000000000000000000000")) {
                            mfc.writeBlock(bIndex + i, new byte[16]);
                        }
                    }
                }
            }
            byte[] bytes = new byte[16];
            System.arraycopy(data.getBytes(), 0, bytes, 0, data.getBytes().length);
            if (mfc.authenticateSectorWithKeyA(1,
                    MifareClassic.KEY_NFC_FORUM)) {
                mfc.writeBlock(4, bytes);
            }
            Logger.e(TAG, "写入MifareClassic成功");
        } finally {
            try {
                mfc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean clear(Intent intent) throws IOException {
        boolean result = false;
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        MifareClassic mfc = MifareClassic.get(tag);
        try {
            mfc.connect();
            int sectorCount = mfc.getSectorCount();//获取TAG中包含的扇区数
            for (int j = 0; j < sectorCount; j++) {
                if (mfc.authenticateSectorWithKeyA(j,
                        MifareClassic.KEY_NFC_FORUM)) {
                    // 读取扇区中的块
                    int bCount = mfc.getBlockCountInSector(j);
                    int bIndex = mfc.sectorToBlock(j);
                    for (int i = 0; i < bCount - 1; i++) {
                        if (!bytesToHexString(mfc.readBlock(bIndex + i)).equals("0x00000000000000000000000000000000")) {
                            mfc.writeBlock(bIndex + i, new byte[16]);
                        }
                    }
                }
            }
            result = true;
            Logger.e(TAG, "清空成功");
        } finally {
            try {
                mfc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 往nfc写入数据
     */
    public void writeNFCToTag(String text, Intent intent) throws IOException, FormatException {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        //生成语言编码的字节数组，中文编码
        byte[] langBytes = Locale.CHINA.getLanguage().getBytes(
                Charset.forName("US-ASCII"));
        //将要写入的文本以UTF_8格式进行编码
        Charset utfEncoding = Charset.forName("UTF-8");
        //由于已经确定文本的格式编码为UTF_8，所以直接将payload的第1个字节的第7位设为0
        byte[] textBytes = text.getBytes(utfEncoding);
        int utfBit = 0;
        //定义和初始化状态字节
        char status = (char) (utfBit + langBytes.length);
        //创建存储payload的字节数组
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        //设置状态字节
        data[0] = (byte) status;
        //设置语言编码
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        //设置实际要写入的文本
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length,
                textBytes.length);
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{record});
        //转换成字节获得大小
        int size = ndefMessage.toByteArray().length;
        //2.判断NFC标签的数据类型（通过Ndef.get方法）
        Ndef ndef = Ndef.get(tag);
        //判断是否为NDEF标签
        if (ndef != null) {
            ndef.connect();
            //判断是否支持可写
            if (!ndef.isWritable()) {
                return;
            }
            //判断标签的容量是否够用
            if (ndef.getMaxSize() < size) {
                return;
            }
            //3.写入数据
            ndef.writeNdefMessage(ndefMessage);
            Logger.e(TAG, "写入NDEF成功");
        } else {
            //当我们买回来的NFC标签是没有格式化的，或者没有分区的执行此步
            //Ndef格式类
            NdefFormatable format = NdefFormatable.get(tag);
            //判断是否获得了NdefFormatable对象，有一些标签是只读的或者不允许格式化的
            if (format != null) {
                //连接
                format.connect();
                //格式化并将信息写入标签
                format.format(ndefMessage);
                Logger.e(TAG, "格式化并写入NDEF成功");
            } else {
                Logger.e(TAG, "格式化并写入NDEF失败");
            }
        }
    }

    /**
     * 将字节数组转换为字符串
     */
    private String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";
        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }


    //字符序列转换为16进制字符串
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    private byte[] trim(byte[] srcArray) {
        byte[] result;
        byte[] descArray = new byte[srcArray.length];
        byte i = 0, j = 0, count = 0;
        for (i = 0; i < srcArray.length; ) {
            count = 0;
            if (srcArray[i] != 0) {
                descArray[j++] = srcArray[i];
                i++;
            } else {
                while (i < srcArray.length && (srcArray[i] == 0)) {
                    ++count;
                    i++;
                }
                if (count < 2) {
                    descArray[j++] = srcArray[i - 1];
                }
            }
        }
        result = new byte[j];
        for (i = 0; i < j; i++) {
            result[i] = descArray[i];
        }
        return result;
    }


    /**
     * 合并字节数组
     *
     * @param first
     * @param rest
     * @return
     */
    public byte[] concatAll(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            if (array != null) {
                totalLength += array.length;
            }
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            if (array != null) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
        }
        return result;
    }

}
