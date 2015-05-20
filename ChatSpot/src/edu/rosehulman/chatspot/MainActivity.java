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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends Activity {
	public static int NEW_MSG = 8008;
	public static int NEW_BUDDIES = 8009;

	private EditText mMessageBox;
	private ListView mMessageContainer;
	private MessageArrayAdapter mMessageArrayAdapter;
	private WifiP2pManager mManager;
	private Channel mDiscoveryChannel;
	private Set<Buddy> buddies;
	private int mMessageColor;

	private ReceiverThread recvThread;
	private DiscoverThread discThread;
	private final Handler messageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 8008) {
				edu.rosehulman.chatspot.Message m = (edu.rosehulman.chatspot.Message) msg.obj;
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
		buddies = new HashSet<Buddy>();

		mMessageBox = (EditText) findViewById(R.id.messageBox);
		mMessageContainer = (ListView) findViewById(R.id.messageContainer);
		mMessageContainer.setDividerHeight(0);
		
		mMessageContainer.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		mMessageArrayAdapter = new MessageArrayAdapter(this);
		mMessageContainer.setAdapter(mMessageArrayAdapter);

		getActionBar().setTitle(getLocalIpAddress());

		initColors();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (discThread == null) {
			discThread = new DiscoverThread(this);
		}
		discThread.start();

		if (recvThread == null) {
			recvThread = new ReceiverThread(messageHandler);
		}
		recvThread.start();
		discoveryServiceInit();
		discoverService();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (discThread != null) {
			discThread.terminate();
			discThread = null;
		}

		if (recvThread != null) {
			recvThread.terminate();
			recvThread = null;
		}
		mManager.clearServiceRequests(mDiscoveryChannel, new EmptyCallback());
		mManager.clearLocalServices(mDiscoveryChannel, new EmptyCallback());
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void initColors() {
		Random rnd = new Random();
		int index = rnd.nextInt(getResources().getStringArray(R.array.message_colors).length);
		mMessageColor = Color.parseColor(getResources().getStringArray(R.array.message_colors)[index]);
		// set actionbar
		getActionBar().setBackgroundDrawable(new ColorDrawable(mMessageColor));
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowTitleEnabled(true);
		// set title text color
		int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
		TextView abTitle = (TextView) findViewById(titleId);
		abTitle.setTextColor(Color.parseColor(getResources().getStringArray(R.array.action_bar_text_colors)[index]));
		// set status bar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.parseColor(getResources().getStringArray(R.array.message_colors_tints)[index]));
		}

		//set send message action
		mMessageBox.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					Log.d("SENDER", MainActivity.this.buddies.toString());
					String message = MainActivity.this.mMessageBox.getText().toString();
					MainActivity.this.mMessageBox.setText("");
					edu.rosehulman.chatspot.Message msg = new edu.rosehulman.chatspot.Message(getLocalIpAddress(), "", message,	getMessageColor(), new Date().getTime());
					for (Buddy buddy : MainActivity.this.buddies) {
						new SenderAsyncTask(MainActivity.this, buddy.getAddress(), msg).execute();
						MainActivity.this.addSentMessageToContainer(msg);
					}
					handled = true;
				}
				return handled;
			}
		});

		mMessageBox.setTextColor(mMessageColor);
	}

	public void discoveryServiceInit() {
		// setup map that contains info to send to devices on connect
		Map<String, String> record = new HashMap<String, String>();
		record.put("recvHostAddr", getLocalIpAddress());
		record.put("buddyColor", mMessageColor + "");

		WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("discovery", "_presence._tcp", record);

		// add the service
		mManager.addLocalService(mDiscoveryChannel, serviceInfo, new EmptyCallback());

		// what to do when devices meet
		DnsSdTxtRecordListener onConnectionListener = new DnsSdTxtRecordListener() {
			@Override
			public void onDnsSdTxtRecordAvailable(String fullDomain, Map<String, String> record, WifiP2pDevice device) {
				String addr = (String) record.get("recvHostAddr");
				int color = Integer.valueOf((String) record.get("buddyColor"));
				Buddy b = new Buddy(addr, color);
				
				MainActivity.this.buddies.remove(b);
				if (MainActivity.this.buddies.add(b)) {
					Log.d("CONNECTION", "connected to " + addr);
				}
			}
		};

		DnsSdServiceResponseListener servListener = new DnsSdServiceResponseListener() {
			@Override
			public void onDnsSdServiceAvailable(String instanceName,
					String registrationType, WifiP2pDevice resourceType) {
			}
		};
		mManager.setDnsSdResponseListeners(mDiscoveryChannel, servListener,	onConnectionListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater pump = getMenuInflater();
		pump.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.list_buddies:
			openBuddies();
			return true;
		default:
			return super.onOptionsItemSelected(item); 
		}
	}
	
	private void openBuddies(){
		new BuddiesListDialog(buddies).show(getFragmentManager(), "buddies");
	}
	
	public void discoverService() {
		WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
		mManager.addServiceRequest(mDiscoveryChannel, serviceRequest, new EmptyCallback());
	}

	// copied from stack overflow
	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					// Log.d("ADDRESS", inetAddress.getHostAddress());
					// make sure it's a valid IP address
					if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().matches("^(?!127.0.0.1)(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})$")) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("ERRROR", ex.toString());
		}
		return null;
	}

	public int getMessageColor() {
		return mMessageColor;
	}

	public Channel getDiscoveryChannel() {
		return mDiscoveryChannel;
	}

	public WifiP2pManager getWifiManager() {
		return mManager;
	}

	public void addSentMessageToContainer(edu.rosehulman.chatspot.Message message) {
		if (!hasMessage(message)) {
			mMessageArrayAdapter.add(message);
		}
	}

	public void addMessageToContainer(edu.rosehulman.chatspot.Message message) {
		if (!hasMessage(message)) {
			mMessageArrayAdapter.add(message);
			forwardMessageToBuddies(message);
			if (!message.getSender().equals(getLocalIpAddress())) {
				buddies.add(new Buddy(message.getSender(), message.getColor()));
			}
		}
	}

	public void forwardMessageToBuddies(edu.rosehulman.chatspot.Message m) {
		for (Buddy buddy : MainActivity.this.buddies) {
			if (!buddy.getAddress().equals(m.getSender())) {
				new SenderAsyncTask(MainActivity.this, buddy.getAddress(), m).execute();
			}
		}
	}

	public boolean hasMessage(edu.rosehulman.chatspot.Message m) {
		return mMessageArrayAdapter.containsMessage(m);
	}

	public boolean disconnectFromBuddy(String buddyIP) {
		return this.buddies.remove(buddyIP);
	}

}
