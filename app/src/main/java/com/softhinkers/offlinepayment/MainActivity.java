package com.softhinkers.offlinepayment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.softhinkers.offlinepayment.qrmodule.activity.QrScannerActivity;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Adarsh on 5/10/2017.
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.txt_scan_result)
    TextView mTxtScanResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.Main_btn_scan)
    void onScanBtnClick() {
        startActivityForResult(
                new Intent(MainActivity.this, QrScannerActivity.class),
                QrScannerActivity.QR_REQUEST_CODE);
    }

    @OnClick(R.id.Main_btn_generate)
    void onGenerateBtnClick() {
        startActivity(new Intent(MainActivity.this, QRGeneratorActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QrScannerActivity.QR_REQUEST_CODE) {
            mTxtScanResult.setText(
                    resultCode == RESULT_OK
                            ? data.getExtras().getString(QrScannerActivity.QR_RESULT_STR)
                            : "Scanned Nothing!");
        }
    }
}
