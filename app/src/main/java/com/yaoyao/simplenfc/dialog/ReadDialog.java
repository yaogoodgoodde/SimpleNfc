package com.yaoyao.simplenfc.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.yaoyao.simplenfc.R;
import com.yaoyao.simplenfc.utils.Constant;

/**
 * @author wangyao
 * @description: 读卡弹窗
 * @date: 2019/7/16 21:19
 */
public class ReadDialog extends DialogFragment {

    private TextView tvRead;
    private Button btnOk;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_MinWidth);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_nfc_read, container);
        tvRead = view.findViewById(R.id.tv_read_nfc);
        btnOk = view.findViewById(R.id.btn_read_nfc_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //支持输入法show.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE); 
        //获取数据
        Bundle bundle = getArguments();
        if (bundle != null) {
            String id = bundle.getString(Constant.NFC_ID);
            String message = bundle.getString(Constant.NFC_MESSAGE);
            StringBuilder builder = new StringBuilder();
            if (!TextUtils.isEmpty(id)) {
                builder.append("id:");
                builder.append(id);
                builder.append("\n");
            }
            if (!TextUtils.isEmpty(message)) {
                builder.append("message:");
                builder.append(message);
            }
            tvRead.setText(builder.toString());
        }
    }
}
