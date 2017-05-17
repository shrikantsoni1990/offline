package com.nyaruka.androidrelay;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.nyaruka.androidrelay.AndroidRelay.PhoneState;
import com.nyaruka.log.LogCollector;

public class CheckService extends WakefulIntentService {
	public static final String TAG = AndroidRelay.TAG;
	
	public CheckService() {
		super(CheckService.class.getName());
	}
	
	/**
	 * Checks whether we have a mobile network connected.  This hopefully catches the case where the phone
	 * drops its connection for some reason.
	 * @param
	 * @return
	 */
	public boolean isRadioOn(){
		Context context = getApplicationContext();
		
		boolean isOn = false;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] networks = cm.getAllNetworkInfo();
		for(int i=0;i<networks.length;i++){
			if(networks[i].getType() == ConnectivityManager.TYPE_MOBILE && networks[i].isConnectedOrConnecting()){
				isOn = true;
			}	
		}
		
		// if our radio is off, output some debugging
		Log.d(TAG, "_RADIO STATUS");
		for(int i=0;i<networks.length;i++){
			Log.d(TAG, "__ " + networks[i].getTypeName() + "  connection? " + networks[i].isConnectedOrConnecting());
		}
		
		// check our telephony manager
		TelephonyManager tele = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		Log.d(TAG, "__ call state: " + tele.getCallState());
		Log.d(TAG, "__ data state: " + tele.getDataState());
		Log.d(TAG, "__ network type: " + tele.getNetworkType());
		
		PhoneState phoneState = AndroidRelay.getPhoneState(context);
		Log.d(TAG, "__ phone state: " + phoneState.state);
		Log.d(TAG, "__ signal strength: " + phoneState.strength);
		
	    return isOn;
	}
	
	public void tickleAirplaneMode(){
		Context context = getApplicationContext();
		Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 1);

		// reload our settings to take effect
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", true);
		sendBroadcast(intent);
		
		// sleep 30 seconds for things to take effect
		try{
			Thread.sleep(30000);
		} catch (Throwable t){}
		
		// then toggle back
		Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);

		// reload our settings to take effect
		intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", false);
		sendBroadcast(intent);
		
		// sleep 30 seconds for things to take effect
		try{
			Thread.sleep(30000);
		} catch (Throwable t){}
	}

	/**
	 * Restores our WIFI/DATA state to whatever is in our preference file.  No-op if 
	 * our current state is the same as our preferred state.
	 */
	public void restoreDefaultNetwork(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isWifiPreferred = Integer.parseInt(prefs.getString("pref_net", "0")) % 2 == 0;
		WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		
		if (wifi.isWifiEnabled() != isWifiPreferred){
			// toggle back to the preferred network
			wifi.setWifiEnabled(isWifiPreferred);
			
			try{
				Thread.sleep(30000);
			} catch (Throwable t){}
		}
	}
	
	@Override
	protected void doWakefulWork(Intent intent) {
		Log.d(TAG, "==Check service running");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean toggleAirplane = prefs.getBoolean("toggle_airplane", false);
        boolean toggleConnection = Integer.parseInt(prefs.getString("pref_net", "0")) < 2;

        // make sure our SMS modem is hooked up
		if (!BootStrapper.checkService(this.getApplicationContext())){
			Log.d(TAG, "RelayService not started yet, waiting.");
			schedule(this.getApplicationContext());
			return;
		}

		// grab the relayer service, seeing if it started
		RelayService relayer = RelayService.get();

		if (relayer == null){
			Log.d(TAG, "No RelayService started yet, awaiting.");
			return;
		}
		
		if (RelayService.doReset && toggleAirplane){
			Log.d(TAG, "__RESTING PROCESS");
			try{
				Log.d(TAG, "__REST - tickling airplane mode");
				tickleAirplaneMode();
				Log.d(TAG, "__REST - done tickling airplane mode");
				relayer.tickleDefaultAPN();
				Log.d(TAG, "__REST - done tickling default APN mode");	
					
				// disable the reset message
				RelayService.doReset = false;
			} catch (Throwable t){
				Log.d(TAG, "Error thrown checking network connectivity", t);
			}
		}

		// check our power levels
		try{
			relayer.checkPowerStatus();
		} catch (Throwable t){
			Log.d(TAG, "Error thrown checking power status", t);
		}
		
		try{
			// do all the work of sending messages and checking for new ones
			doCheckWork(relayer);
		} catch (Throwable t){
			Log.d(TAG, "Error running check service.", t);
		}
		
		// are we meant to send a log?  do so
		if (RelayService.doSendLog){
			String log = LogCollector.collectLog();
			
			if (log != null){
				if (RelayService.sendAlert(getApplicationContext(), "Relay Log", log)){
					RelayService.doSendLog = false;
				} else {
                    if (toggleConnection){
                        relayer.toggleConnection();
                        if (RelayService.sendAlert(getApplicationContext(), "Relay Log", log)){
                            RelayService.doSendLog = false;
                        } else {
                            Log.d(TAG, "Failed sending log after two attempts, will retry on next check");
                        }
                    } else {
                        Log.d(TAG, "Failed sending log, will retry on next check");
                    }
				}
			} else {
				Log.d(TAG, "Failed collecting log, will retry on next check");
			}
		}
		
		// reset our connect if needbe
		restoreDefaultNetwork();
				
		// reschedule ourselves
		schedule(this.getApplicationContext());
	}
	
	protected void doCheckWork(RelayService relayer){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		boolean process_incoming = prefs.getBoolean("process_incoming", false);
		boolean process_outgoing = prefs.getBoolean("process_outgoing", false);
        boolean toggleConnection = Integer.parseInt(prefs.getString("pref_net", "0")) < 2;

        if (process_outgoing){
			try{
				relayer.resendErroredSMS();
			} catch (Throwable t){
				Log.d(TAG, "Error resending SMSes.", t);
			}
		}
		
		// set our network to our default
		restoreDefaultNetwork();
		
		if (process_incoming){
			try{
				Log.d(TAG, "__ SENDING PENDING MESSAGES");
				relayer.sendPendingMessagesToServer();
			} catch (IOException e){
				try{
                    if (toggleConnection){
                        Log.d(TAG, "Error resending to server, toggling connection", e);
                        relayer.toggleConnection();
                        relayer.sendPendingMessagesToServer();
                    } else {
                        Log.d(TAG, "__ FAILED TO SEND PENDING MESSAGES");
                        RelayService.doReset = true;
                    }
				} catch (IOException e1){
                    Log.d(TAG, "__ FAILED TO SEND PENDING MESSAGES, SET RESET LABEL TO true");
                    RelayService.doReset = true;
				} catch (Throwable tt){
					Log.d(TAG, "Error sending messages to server", e);
				}				
			} catch (Throwable t){
				Log.d(TAG, "Error sending messages to server", t);
			}
		}

		if (process_outgoing) {
			// set our network to our default
			restoreDefaultNetwork();
			
			try{
				Log.d(TAG, "__ MARKING DELIVERIES");
				relayer.markDeliveriesOnServer();
			} catch (IOException e){
				try{
                    if (toggleConnection){
                        Log.d(TAG, "Error marking deliveries on the server, toggling connection", e);
                        relayer.toggleConnection();
                        relayer.markDeliveriesOnServer();
                    } else {
                        Log.d(TAG, "__ FAILED TO MARK DELIVERIES, SET RESET LABEL TO true");
                        RelayService.doReset = true;
                    }
				} catch (IOException e1) {
                    Log.d(TAG, "__ FAILED TO MARK DELIVERIES, SET RESET LABEL TO true");
                    RelayService.doReset = true;
				} catch (Throwable tt){
					Log.d(TAG, "Error marking deliveries on the server", e);
				}
			} catch (Throwable t){
				Log.d(TAG, "Error marking deliveries on the server", t);
			}
			
			// set our network to our default
			restoreDefaultNetwork();
		
			try{
				Log.d(TAG, "__ CHECKING OUTBOX");
				relayer.checkOutbox();
			} catch (IOException e){
				try{
                    if (toggleConnection){
                        Log.d(TAG, "Error checking outbox, toggling connection", e);
                        relayer.toggleConnection();
                        relayer.checkOutbox();
                    } else {
                        Log.d(TAG, "__ FAILED TO CHECK OUTBOX, SET RESET LABEL TO: 'true'");
                        RelayService.doReset = true;
                    }
				} catch (Exception e1) {
                    Log.d(TAG, "__ FAILED TO CHECK OUTBOX, SET RESET LABEL TO: 'true'");
                    RelayService.doReset = true;
				} catch (Throwable tt){
					Log.d(TAG, "Error checking outbox", e);
				}
			} catch (Throwable t){
				Log.d(TAG, "Error checking outbox", t);
			}
		}
				
		try {
			relayer.trimMessages();					
		} catch (Throwable t){
			Log.d(TAG, "Error trimming message", t);
		}
		
		// set our network to our default
		restoreDefaultNetwork();
	}

	public static void schedule(Context context){
		Log.d(TAG, "__ STARTING SCHEDULED TASK");
		WakefulIntentService.scheduleAlarms(new com.nyaruka.androidrelay.AlarmListener(), context);
	}
}
