package edu.rosehulman.chatspot;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

	private WifiP2pManager mManager;
	private Channel mChannel;
	private Activity mActivity;

	public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,	Activity activity) {
		super();
		this.mManager = manager;
		this.mChannel = channel;
		this.mActivity = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			// Check to see if Wi-Fi is enabled and notify appropriate activity
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
				// Wifi p2p enable
			} else {
				// Wifi p2p disabled
			}
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			// Call WifiP2pManager.requestPeers() to get a list of current peers
			Log.d("HHH", "HERE");
			if(mManager != null){
				mManager.requestPeers(mChannel, new PeerListListener() {
					@Override
					public void onPeersAvailable(WifiP2pDeviceList peers) {
						for( WifiP2pDevice device  : peers.getDeviceList()){
							Log.d("HHH", device.deviceName + "\n" + device.deviceAddress);
						}
					}
				});
			}
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
			// Respond to new connection or disconnections
			 if (mManager == null) {
	                return;
	            }

	            NetworkInfo networkInfo = (NetworkInfo) intent
	                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

	            if (networkInfo.isConnected()) {

	                // We are connected with the other device, request connection
	                // info to find group owner IP

	                mManager.requestConnectionInfo(mChannel, new ConnectionInfoListener() {
						
	                	@Override
	                    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
	                        // InetAddress from WifiP2pInfo struct.
	                        InetAddress groupOwnerAddress = info.groupOwnerAddress;

	                        // After the group negotiation, we can determine the group owner.
	                        if (info.groupFormed && info.isGroupOwner) {
	                            //server stuff
								new MessageAsyncTask(mActivity).execute();
	                        } else if (info.groupFormed) {
	                            //client stuff
	                        	Socket socket = new Socket();
	                        	byte buf[]  = new byte[1024];
	                        	try {
	                        	    /**
	                        	     * Create a client socket with the host,
	                        	     * port, and timeout information.
	                        	     */
	                        	    socket.bind(null);
	                        	    socket.connect((new InetSocketAddress(groupOwnerAddress.getHostAddress(), 8888)), 500);

	                        	    /**
	                        	     * Create a byte stream from a JPEG file and pipe it to the output stream
	                        	     * of the socket. This data will be retrieved by the server device.
	                        	     */
	                        	    InputStream in = socket.getInputStream();
	                        	    in.read(buf);
	                        	    
	                        	    Log.d("HHH", String.valueOf(buf));
	                        	    
	                        	    
	                        	    in.close();
	                        	} catch (Exception e) {
	                        	    //catch logic
	                        	}

	                        	/**
	                        	 * Clean up any open sockets when done
	                        	 * transferring or if an exception occurred.
	                        	 */
	                        	finally {
	                        	    if (socket != null) {
	                        	        if (socket.isConnected()) {
	                        	            try {
	                        	                socket.close();
	                        	            } catch (IOException e) {
	                        	                //catch logic
	                        	            }
	                        	        }
	                        	    }
	                        	}
	                        }
	                    }
					});
	            }
		} 
	}
}
