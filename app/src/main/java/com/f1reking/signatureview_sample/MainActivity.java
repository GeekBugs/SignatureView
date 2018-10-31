package com.f1reking.signatureview_sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.f1reking.signatureview.SignatureView;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private SignatureView mSignatureView;
    private Button btnClear;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mSignatureView = findViewById(R.id.view_signature);
        btnClear = findViewById(R.id.btn_clear);
        btnSave = findViewById(R.id.btn_save);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignatureView.clear();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSignatureView.getTouched()) {
                    try {
                        mSignatureView.save("/sdcard/sign.png", true, 10);
                        Toast.makeText(MainActivity.this, "图片保存在："+mSignatureView.getSavePath(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "请先签名", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
