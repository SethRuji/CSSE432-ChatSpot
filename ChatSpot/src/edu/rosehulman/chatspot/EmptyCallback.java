package edu.rosehulman.chatspot;

import android.net.wifi.p2p.WifiP2pManager.ActionListener;

public class EmptyCallback implements ActionListener {
	@Override
	public void onSuccess() {
	}

	@Override
	public void onFailure(int code) {
	}
}
