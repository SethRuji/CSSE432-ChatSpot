package edu.rosehulman.chatspot;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
	public static int NEW_MSG = 8008;
	public static int NEW_BUDDIES = 8009;
	
	private Button mSendButton;
	private EditText mMessageBox;
	private ListView mMessageContainer;
	private MessageArrayAdapter mMessageArrayAdapter;
	private WifiP2pManager mManager;
	private Channel mDiscoveryChannel;
	private Set<String> buddyAddresses;
	private int mMessageColor;
	
	private ReceiverThread recvThread;
	private DiscoverThread discThread;
	private final Handler messageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 8008) {
				edu.rosehulman.chatspot.Message m = (edu.rosehulman.chatspot.Message)msg.obj;
				MainActivity.this.addMessageToContainer(m);
			}
			super.handleMessage(msg);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		mDiscoveryChannel = mManager.initialize(this, getMainLooper(), null);
		buddyAddresses = new HashSet<String>();

		mSendButton = (Button) findViewById(R.id.send);
		mMessageBox = (EditText) findViewById(R.id.messageBox);
		mMessageContainer = (ListView) findViewById(R.id.messageContainer);
		mMessageContainer.setDividerHeight(0);
		mMessageArrayAdapter = new MessageArrayAdapter(this);
		mMessageContainer.setAdapter(mMessageArrayAdapter);


		getActionBar().setTitle("ChatSpot - " + getLocalIpAddress());
		
		initColors();
		
		discoveryServiceInit();
		discoverService();		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(discThread == null){
			discThread = new DiscoverThread(this);			
		}
		discThread.start();
		
		if(recvThread == null){
			recvThread = new ReceiverThread(messageHandler);			
		}
		recvThread.start();		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(discThread != null){
			discThread.terminate();
			discThread = null;
		}
		
		if(recvThread != null){
			recvThread.terminate();		
			recvThread = null;
		}	
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void initColors(){
		Random rnd = new Random();
		int index = rnd.nextInt(getResources().getStringArray(R.array.message_colors).length);
		mMessageColor = Color.parseColor(getResources().getStringArray(R.array.message_colors)[index]);
		//set actionbar
		getActionBar().setBackgroundDrawable(new ColorDrawable(mMessageColor));
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowTitleEnabled(true);
		//set title text color
		int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
		TextView abTitle = (TextView) findViewById(titleId);
		abTitle.setTextColor(Color.parseColor(getResources().getStringArray(R.array.action_bar_text_colors)[index]));		
		//set status bar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		    Window window = getWindow();
		    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		    window.setStatusBarColor(Color.parseColor(getResources().getStringArray(R.array.message_colors_tints)[index]));
		}		

		mSendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("SENDER", MainActivity.this.buddyAddresses.toString());
				long timestamp = new Date().getTime();
				for (String sockAddr : MainActivity.this.buddyAddresses) {
					String message = MainActivity.this.mMessageBox.getText().toString();
					new SenderAsyncTask(MainActivity.this, sockAddr, message, timestamp).execute();
				}
			}
		});
	}

	public void discoveryServiceInit(){
		mManager.clearServiceRequests(mDiscoveryChannel, new EmptyCallback());
		// setup map that contains info to send to devices on connect
		Map<String, String> record = new HashMap<String, String>();
		record.put("recvHostAddr", getLocalIpAddress());

		WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("discovery", "_presence._tcp", record);

		//add the service
		mManager.addLocalService(mDiscoveryChannel, serviceInfo, new EmptyCallback());
		
		//what to do when devices meet
		DnsSdTxtRecordListener onConnectionListener = new DnsSdTxtRecordListener() {
			@Override
			public void onDnsSdTxtRecordAvailable(String fullDomain, Map<String, String> record, WifiP2pDevice device) {
				String addr = (String) record.get("recvHostAddr");
				
				if(MainActivity.this.buddyAddresses.add(addr)){
					Log.d("HHH", "connected to " + addr);			
				}
			}
		};

		DnsSdServiceResponseListener servListener = new DnsSdServiceResponseListener() {
			@Override
			public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice resourceType) {	

			}
		};
		mManager.setDnsSdResponseListeners(mDiscoveryChannel, servListener, onConnectionListener);
	}
	
	public void discoverService() {
		WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();		
		mManager.addServiceRequest(mDiscoveryChannel, serviceRequest, new EmptyCallback());
	}

	//copied from stack overflow
	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					//Log.d("ADDRESS", inetAddress.getHostAddress());
					//make sure it's a valid IP address
					if (!inetAddress.isLoopbackAddress()&& inetAddress.getHostAddress().matches("^(?!127.0.0.1)(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})$")) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("ERRROR", ex.toString());
		}
		return null;
	}
	
	public int getMessageColor(){
		return mMessageColor;
	}
	
	public Channel getDiscoveryChannel(){
		return mDiscoveryChannel;
	}
	
	public WifiP2pManager getWifiManager(){
		return mManager;
	}
	
	public void addMessageToContainer(edu.rosehulman.chatspot.Message message){
		if(!hasMessage(message)){
			mMessageArrayAdapter.add(message);
			if(!message.getSender().equals(getLocalIpAddress()) && buddyAddresses.add(message.getSender())){
				Log.d("ADDMESSAGE", "connected to " + message.getSender());
			}
		}
	}


	public void forwardMessageToBuddies(edu.rosehulman.chatspot.Message m) {
		for (String sockAddr : MainActivity.this.buddyAddresses) {
			if(!sockAddr.equals(m.getSender())){		
				new SenderAsyncTask(MainActivity.this, sockAddr, m).execute();
			}
		}
	}
	
	public boolean hasMessage(edu.rosehulman.chatspot.Message m){
		return mMessageArrayAdapter.containsMessage(m);
	}
	
}
