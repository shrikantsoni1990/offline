package com.softhinkers.offlinepayment;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;


public class WifiDirect extends AppCompatActivity implements SalutDataCallback, View.OnClickListener {

/*
        This simple activity demonstrates how to use the Salut library from a host and client perspective.
     */

    public static final String TAG = "SalutTestApp";
    public SalutDataReceiver dataReceiver;
    public SalutServiceData serviceData;
    public Salut network;
    public Button hostingBtn;
    public Button discoverBtn;
    SalutDataCallback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_direct);

        hostingBtn = (Button) findViewById(R.id.hosting_button);
        discoverBtn = (Button) findViewById(R.id.discover_services);

        hostingBtn.setOnClickListener(this);
        discoverBtn.setOnClickListener(this);


        /*Create a data receiver object that will bind the callback
        with some instantiated object from our app. */
        dataReceiver = new SalutDataReceiver(this, this);


        /*Populate the details for our awesome service. */
        serviceData = new SalutServiceData("testAwesomeService", 60606,
                "HOST");

        /*Create an instance of the Salut class, with all of the necessary data from before.
        * We'll also provide a callback just in case a device doesn't support WiFi Direct, which
        * Salut will tell us about before we start trying to use methods.*/
        network = new Salut(dataReceiver, serviceData, new SalutCallback() {
            @Override
            public void call() {
                // wiFiFailureDiag.show();
                // OR
                Log.e(TAG, "Sorry, but this device does not support WiFi Direct.");
            }
        });

    }

    private void setupNetwork() {
        if (!network.isRunningAsHost) {
            network.startNetworkService(new SalutDeviceCallback() {
                @Override
                public void call(SalutDevice salutDevice) {
                    Toast.makeText(getApplicationContext(), "Device: " + salutDevice.instanceName + " connected.", Toast.LENGTH_SHORT).show();
                }
            });

            hostingBtn.setText("Stop Service");
            discoverBtn.setAlpha(0.5f);
            discoverBtn.setClickable(false);
        } else {
            network.stopNetworkService(false);
            hostingBtn.setText("Start Service");
            discoverBtn.setAlpha(1f);
            discoverBtn.setClickable(true);
        }
    }

    private void discoverServices() {
        if (!network.isRunningAsHost && !network.isDiscovering) {
            network.discoverNetworkServices(new SalutCallback() {
                @Override
                public void call() {
                    Toast.makeText(getApplicationContext(), "Device: " + network.foundDevices.get(0).instanceName + " found.", Toast.LENGTH_SHORT).show();
                }
            }, true);
            discoverBtn.setText("Stop Discovery");
            hostingBtn.setAlpha(0.5f);
            hostingBtn.setClickable(false);
        } else {
            network.stopServiceDiscovery(true);
            discoverBtn.setText("Discover Services");
            hostingBtn.setAlpha(1f);
            hostingBtn.setClickable(false);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_base, menu);
//        return true;
//    }

    /*Create a callback where we will actually process the data.*/
    @Override
    public void onDataReceived(Object o) {
        //Data Is Received
    }

    @Override
    public void onClick(View v) {

        if (!Salut.isWiFiEnabled(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "Please enable WiFi first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (v.getId() == R.id.hosting_button) {
            setupNetwork();
        } else if (v.getId() == R.id.discover_services) {
            discoverServices();
        }
    }
}
