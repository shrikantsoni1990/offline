package com.softhinkers_wallet.smartcoinswallet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.softhinkers.offlinepayment.BuildConfig;
import com.softhinkers.offlinepayment.R;
import com.softhinkers_wallet.database.SCWallDatabase;
import com.softhinkers_wallet.models.AccountDetails;
import com.softhinkers_wallet.utils.Application;
import com.softhinkers_wallet.utils.BinHelper;
import com.softhinkers_wallet.utils.Crypt;
import com.softhinkers_wallet.utils.TinyDB;
import com.softhinkerswallet.graphenej.Address;
import com.softhinkerswallet.graphenej.BrainKey;
import com.softhinkerswallet.graphenej.UserAccount;
import com.softhinkerswallet.graphenej.api.GetAccounts;
import com.softhinkerswallet.graphenej.api.GetAccountsByAddress;
import com.softhinkerswallet.graphenej.interfaces.WitnessResponseListener;
import com.softhinkerswallet.graphenej.models.AccountProperties;
import com.softhinkerswallet.graphenej.models.BaseResponse;
import com.softhinkerswallet.graphenej.models.WitnessResponse;

import org.bitcoinj.core.ECKey;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BrainkeyActivity extends BaseActivity {
    private final String TAG = this.getClass().getName();
   @BindView(R.id.etPin)
    EditText etPin;

   @BindView(R.id.etPinConfirmation)
    EditText etPinConfirmation;

   @BindView(R.id.etBrainKey)
    EditText etBrainKey;

   @BindView(R.id.tvBlockNumberHead_brain_key_activity)
    TextView tvBlockNumberHead;

   @BindView(R.id.tvAppVersion_brain_key_activity)
    TextView tvAppVersion;

   @BindView(R.id.ivSocketConnected_brain_key_activity)
    ImageView ivSocketConnected;

    ProgressDialog progressDialog;
    TinyDB tinyDB;
    Boolean settingScreen = false;

   @BindView(R.id.tvPin)
    TextView tvPin;

   @BindView(R.id.tvPinConfirmation)
    TextView tvPinConfirmation;

    /* Database interface */
    private SCWallDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brainkey);
        ButterKnife.bind(this);
        setBackButton(true);
        setTitle(getResources().getString(R.string.app_name));

        database = new SCWallDatabase(this);

        progressDialog = new ProgressDialog(this);
        tinyDB = new TinyDB(getApplicationContext());
        tvAppVersion.setText("v" + BuildConfig.VERSION_NAME + getString(R.string.beta));
        updateBlockNumberHead();
    }

    @OnClick(R.id.btnCancel)
    public void cancel(Button button) {
        this.finish();
    }

    @OnClick(R.id.btnWallet)
    public void wallet(Button button) {
        if (etBrainKey.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), R.string.please_enter_brainkey, Toast.LENGTH_SHORT).show();
        } else {
            String trimmedBrainKey = etBrainKey.getText().toString().trim();
            etBrainKey.setText(trimmedBrainKey);
            if (etPin.getText().length() == 0) {
                Toast.makeText(getApplicationContext(), R.string.please_enter_6_digit_pin, Toast.LENGTH_SHORT).show();
            }
            //PIN must have minimum of 6-digit
            else if (etPin.getText().length() < 6) {
                Toast.makeText(getApplicationContext(), R.string.pin_number_warning, Toast.LENGTH_SHORT).show();
            } else if (!etPinConfirmation.getText().toString().equals(etPin.getText().toString())) {
                Toast.makeText(getApplicationContext(), R.string.mismatch_pin, Toast.LENGTH_SHORT).show();
            } else {
                load(etPin.getText().toString());
            }
        }
    }

    void load(String pinCode) {
        String brainKeyText = etBrainKey.getText().toString();
        if (brainKeyText.contains(" ")) {
            String arr[] = brainKeyText.split(" ");
            if (arr.length >= 12 && arr.length <= 16) {

                if (checkBrainKeyExist(brainKeyText)) {
                    Toast.makeText(getApplicationContext(), R.string.account_already_exist, Toast.LENGTH_SHORT).show();
                } else {
                    showDialog("", getString(R.string.importing_your_wallet));
                    getAccountFromBrainkey(brainKeyText, pinCode);
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.please_enter_correct_brainkey, Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(getApplicationContext(), R.string.please_enter_correct_brainkey, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkBrainKeyExist(String brainKey) {
        boolean isBrainKey = false;
        ArrayList<AccountDetails> accountDetails = tinyDB.getListObject(getString(R.string.pref_wallet_accounts), AccountDetails.class);

        for (int i = 0; i < accountDetails.size(); i++) {
            try {
                if (brainKey.equals(accountDetails.get(i).brain_key)) {
                    isBrainKey = true;
                    break;
                }
            } catch (Exception ignored) {
            }
        }
        return isBrainKey;

    }

    public void getAccountFromBrainkey(final String brainKey, final String pinCode) {
        try {
            BrainKey bKey = new BrainKey(brainKey, 0);

            /* Storing brainkey in database */
            database.insertKey(bKey);

            Address address = new Address(ECKey.fromPublicOnly(bKey.getPrivateKey().getPubKey()));
            final String encryptedPrivateKey = Crypt.getInstance().encrypt_string(bKey.getWalletImportFormat());
            final String pubkey = address.toString();
            Log.d(TAG, String.format("Brain key: '%s'", bKey.getBrainKey()));
            Log.d(TAG, String.format("Brainkey would generate address: %s", address.toString()));

            new WebsocketWorkerThread(new GetAccountsByAddress(address, new WitnessResponseListener() {
                @Override
                public void onSuccess(WitnessResponse response) {
                    final List<List<UserAccount>> resp = (List<List<UserAccount>>) response.result;
                    Log.d(TAG, "getAccountByAddress.onSuccess");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (resp.size() > 0) {
                                List<UserAccount> accounts = resp.get(0);
                                if (accounts.size() > 0) {
                                    for (UserAccount account : accounts) {
                                        getAccountById(account.getObjectId(), encryptedPrivateKey, pubkey, brainKey, pinCode);
                                    }
                                } else {
                                    hideDialog();
                                    Toast.makeText(getApplicationContext(), R.string.error_invalid_account, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                hideDialog();
                                Toast.makeText(getApplicationContext(), R.string.error_invalid_account, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    hideDialog();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.unable_to_load_brainkey, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }), 0).start();
        } catch (IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | InvalidAlgorithmParameterException e) {
            hideDialog();
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        } catch (NoSuchPaddingException e) {
            hideDialog();
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            hideDialog();
            Toast.makeText(getApplicationContext(), R.string.txt_no_internet_connection, Toast.LENGTH_SHORT).show();
        }

    }

    private void getAccountById(String accountId, final String privaKey, final String pubKey, final String brainkey, final String pinCode) {
        try {
            new WebsocketWorkerThread((new GetAccounts(accountId, new WitnessResponseListener() {
                @Override
                public void onSuccess(WitnessResponse response) {
                    if (response.result.getClass() == ArrayList.class) {
                        List list = (List) response.result;
                        if (list.size() > 0) {
                            if (list.get(0).getClass() == AccountProperties.class) {
                                AccountProperties accountProperties = (AccountProperties) list.get(0);
                                AccountDetails accountDetails = new AccountDetails();
                                accountDetails.account_name = accountProperties.name;
                                accountDetails.account_id = accountProperties.id;
                                accountDetails.wif_key = privaKey;
                                accountDetails.pub_key = pubKey;
                                accountDetails.brain_key = brainkey;
                                accountDetails.securityUpdateFlag = AccountDetails.POST_SECURITY_UPDATE;
                                accountDetails.isSelected = true;
                                accountDetails.status = "success";
                                accountDetails.pinCode = pinCode;
                                //Avoid window leak
                                hideDialog();
                                addWallet(accountDetails, brainkey, pinCode);
                            } else {
                                hideDialog();
                                Toast.makeText(getApplicationContext(), R.string.unable_to_get_account_properties, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            hideDialog();
                            Toast.makeText(getApplicationContext(), R.string.try_again, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        hideDialog();
                        Toast.makeText(getApplicationContext(), R.string.try_again, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    hideDialog();
                    Toast.makeText(getApplicationContext(), R.string.unable_to_load_brainkey, Toast.LENGTH_SHORT).show();
                }
            })), 0).start();
            //mWebSocket.connect();
        } catch (Exception e) {
            hideDialog();
            Toast.makeText(getApplicationContext(), R.string.txt_no_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }

    private void showDialog(String title, String msg) {
        if (progressDialog != null) {
            if (!progressDialog.isShowing()) {
                progressDialog.setTitle(title);
                progressDialog.setMessage(msg);
                progressDialog.show();
            }
        }
    }

    private void hideDialog() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.cancel();
            }
        }
    }

    private void updateBlockNumberHead() {
        final Handler handler = new Handler();

        final Activity myActivity = this;

        final Runnable updateTask = new Runnable() {
            @Override
            public void run() {
                if (Application.isConnected()) {
                    ivSocketConnected.setImageResource(R.drawable.icon_connecting);
                    tvBlockNumberHead.setText(Application.blockHead);
                    ivSocketConnected.clearAnimation();
                } else {
                    ivSocketConnected.setImageResource(R.drawable.icon_disconnecting);
                    Animation myFadeInAnimation = AnimationUtils.loadAnimation(myActivity.getApplicationContext(), R.anim.flash);
                    ivSocketConnected.startAnimation(myFadeInAnimation);
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(updateTask, 1000);
    }

    void addWallet(AccountDetails accountDetail, String brainKey, String pinCode) {

        //Success Import(Set app lock to false)
        Application app = (Application) getApplicationContext();
        app.setLock(false);
        BinHelper myBinHelper = new BinHelper();
        myBinHelper.addWallet(accountDetail, getApplicationContext(), this);


        Intent intent;
        if (myBinHelper.numberOfWalletAccounts(getApplicationContext()) <= 1) {
            intent = new Intent(getApplicationContext(), BackupBrainkeyActivity.class);
        } else {
            intent = new Intent(getApplicationContext(), TabActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


}
