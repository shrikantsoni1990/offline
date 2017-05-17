package com.softhinkers.offlinepayment;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by Adarsh on 5/10/2017.
 */
public class DemoActivity extends AppCompatActivity {
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        listView = (ListView) findViewById(R.id.list);
        String[] values = new String[]{"Centralized Wallet", "User Network, Merchant Online",
                "User Network, Merchant Offline ", "User Offline, Merchant Offline(SMS Relay) ", "Wifi Direct"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == 0) {
                    Intent myIntent = new Intent(view.getContext(), WalletActivity.class);
                    startActivityForResult(myIntent, 0);
                }

                if (position == 1) {
                    Intent myIntent = new Intent(view.getContext(), MainActivity.class);
                    startActivityForResult(myIntent, 0);
                }

                if (position == 2) {
                    Intent myIntent = new Intent(view.getContext(), MainActivity.class);
                    startActivityForResult(myIntent, 0);
                }

                if (position == 3) {
                    Intent myIntent = new Intent(view.getContext(), com.nyaruka.androidrelay.MainActivity.class);
                    startActivityForResult(myIntent, 0);
                }

                if (position == 4) {
                    Intent myIntent = new Intent(view.getContext(), WifiDirect.class);
                    startActivityForResult(myIntent, 0);
                }
            }
        });
    }
}