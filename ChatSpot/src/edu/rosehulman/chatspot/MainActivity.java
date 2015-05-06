package edu.rosehulman.chatspot;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends Activity {

	private Button mSendButton;
	private EditText mMessageBox;
	private LinearLayout mMessageContainer;
	private WifiP2pManager mManager;
	private Channel mChannel;
	private BroadcastReceiver mReceiver;
	private IntentFilter mIntentFilter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
	    mChannel = mManager.initialize(this, getMainLooper(), null);
	    mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
		

		mSendButton = (Button) findViewById(R.id.send);
		mMessageBox = (EditText) findViewById(R.id.messageBox);
		mMessageContainer = (LinearLayout) findViewById(R.id.messageContainer);
	    mIntentFilter = new IntentFilter();
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

	    
	    
		mMessageBox.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					MainActivity.this.sendMessage();
					return true;
				}
				return false;
			}
		});

		mSendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity.this.sendMessage();
			}
		});
		
		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
		    @Override
		    public void onSuccess() {
		    	Log.d("HHH", "SUCCESS");		    	
		    }

		    @Override
		    public void onFailure(int reasonCode) {
		    	Log.d("HHH", "FAILURE");
		    }
		});
		
	}

	@Override
	protected void onResume() {
		super.onResume();
	    registerReceiver(mReceiver, mIntentFilter);
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	    unregisterReceiver(mReceiver);
	}
	
	public void sendMessage() {
		TextView tv = new TextView(this);
		tv.setTextSize(24);
		tv.setText(mMessageBox.getText().toString());
		mMessageContainer.addView(tv);
	}
}
