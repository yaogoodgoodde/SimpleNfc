package com.yaoyao.simplenfc.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.graphics.Color;
import android.nfc.FormatException;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yaoyao.simplenfc.R;
import com.yaoyao.simplenfc.dialog.ReadDialog;
import com.yaoyao.simplenfc.dialog.WriteDialog;
import com.yaoyao.simplenfc.utils.Constant;
import com.yaoyao.simplenfc.utils.Logger;
import com.yaoyao.simplenfc.utils.NfcUtils;
import com.yaoyao.simplenfc.utils.ToastUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wangyao
 * @description:
 * @date: 2019/7/16 21:00
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, WriteDialog.MsgListener {

    private Button btnNdef, btnMiareClassic, btnRead, btnClear;
    private NfcUtils nfcUtils;
    private String TAG = MainActivity.class.getSimpleName();
    private List<View> btns;
    private int selectMode = Constant.NFC_READ;
    private DialogFragment mDialog;
    private String msgWrite = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        nfcUtils = new NfcUtils(this);
    }

    private void initUI() {
        btnNdef = findViewById(R.id.btn_ndef);
        btnMiareClassic = findViewById(R.id.btn_mifareclassic);
        btnRead = findViewById(R.id.btn_read);
        btnClear = findViewById(R.id.btn_clear);
        btnNdef.setOnClickListener(this);
        btnMiareClassic.setOnClickListener(this);
        btnRead.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btns = new ArrayList<>();
        btns.add(btnNdef);
        btns.add(btnMiareClassic);
        btns.add(btnRead);
        btns.add(btnClear);
        btnRead.performClick();
    }

    @Override
    public void onClick(View view) {
        resetBtn(view);
        switch (view.getId()) {
            case R.id.btn_ndef:
                selectMode = Constant.NFC_WRITE_NDEF;
                showSaveDialog();
                break;
            case R.id.btn_mifareclassic:
                selectMode = Constant.NFC_WRITE_MIARECLASSIC;
                showSaveDialog();
                break;
            case R.id.btn_read:
                selectMode = Constant.NFC_READ;
                break;
            case R.id.btn_clear:
                selectMode = Constant.NFC_CLEAR;
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.e(TAG, "onResume");
        nfcUtils.enableForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.e(TAG, "onPause");
        nfcUtils.disableForegroundDispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        switch (selectMode) {
            case Constant.NFC_WRITE_NDEF:
                if (TextUtils.isEmpty(msgWrite)) {
                    ToastUtils.showShort("写入信息不能为空.");
                    break;
                }
                try {
                    nfcUtils.writeNFCToTag(msgWrite, intent);
                    dissDialog();
                    ToastUtils.showShort("写入成功.");
                } catch (IOException e) {
                    ToastUtils.showShort("写入失败：" + e.getMessage());
                } catch (FormatException e) {
                    ToastUtils.showShort("写入失败：" + e.getMessage());
                } finally {
                    msgWrite = null;
                }
                break;
            case Constant.NFC_WRITE_MIARECLASSIC:
                if (TextUtils.isEmpty(msgWrite)) {
                    ToastUtils.showShort("写入信息不能为空.");
                    break;
                }
                try {
                    nfcUtils.writeMifareClassic(msgWrite, intent);
                    dissDialog();
                    ToastUtils.showShort("写入成功.");
                } catch (IOException e) {
                    ToastUtils.showShort("写入失败：" + e.getMessage());
                } finally {
                    msgWrite = null;
                }
                break;
            case Constant.NFC_READ:
                if (mDialog != null &&
                        mDialog.getDialog() != null &&
                        mDialog.getDialog().isShowing()) {
                    ToastUtils.showShort("请关闭弹窗再继续刷卡");
                    return;
                }
                String id = nfcUtils.readNFCId(nfcUtils.getNFCTag(intent));
                Logger.e(TAG, "nfcID:" + id);
                String message = nfcUtils.readMessage(intent);
                Logger.e(TAG, "nfcMessage:" + message);
                showReadDialog(id, message);
                break;
            case Constant.NFC_CLEAR:
                try {
                    nfcUtils.clear(intent);
                    ToastUtils.showShort("清空成功");
                } catch (IOException e) {
                    ToastUtils.showShort("清除失败：" + e.getMessage());
                }
                break;
            default:
                ToastUtils.showShort("请先选择按钮");
                break;
        }
    }

    private void resetBtn(View view) {
        for (View my : btns) {
            if (view == null) {
                my.setBackgroundColor(Color.WHITE);
            } else {
                if (view.getId() == my.getId()) {
                    my.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    my.setBackgroundColor(Color.WHITE);
                }
            }
        }
    }

    private void showSaveDialog() {
        mDialog = new WriteDialog();
        mDialog.show(getSupportFragmentManager(), "mWriteDialog");
    }

    private void showReadDialog(String id, String message) {
        mDialog = new ReadDialog();
        Bundle bundle = new Bundle();
        bundle.putString(Constant.NFC_ID, id);
        bundle.putString(Constant.NFC_MESSAGE, message);
        mDialog.setArguments(bundle);
        mDialog.show(getSupportFragmentManager(), "mReadDialog");
    }

    private void dissDialog() {
        if (mDialog != null &&
                mDialog.getDialog() != null &&
                mDialog.getDialog().isShowing()) {
            mDialog.dismiss();
        }
    }


    @Override
    public void result(String msg) {
        msgWrite = msg;
    }

    @Override
    public void dissMiss() {
        selectMode = 0;
        resetBtn(null);
    }


}
