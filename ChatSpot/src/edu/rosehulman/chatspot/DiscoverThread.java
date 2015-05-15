package edu.rosehulman.chatspot;

import android.util.Log;


public class DiscoverThread extends Thread {
	private MainActivity mContext;
	private boolean stop;
	
	public DiscoverThread(MainActivity context){
		this.mContext = context;
		this.stop = false;
		this.setName("Discovery Thread");
	}
	
	@Override
	public void run() {
		stop = false;
		while(!stop){
			try {
				mContext.getWifiManager().discoverServices(mContext.getDiscoveryChannel(), new EmptyCallback());
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Log.d("DISCV_THREAD", "STOPPING");
	}
	
	public void terminate() {
		this.interrupt();
		stop = true;
	}
}
