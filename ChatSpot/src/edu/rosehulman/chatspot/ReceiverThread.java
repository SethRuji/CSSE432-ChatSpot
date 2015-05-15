package edu.rosehulman.chatspot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ReceiverThread extends Thread {
	private Handler messageHandler;
	private ServerSocket socket;
	private boolean stop;

	public ReceiverThread(Handler messageHandler) {
		this.messageHandler = messageHandler;
		this.stop = false;
		this.setName("Receiver Thread");
	}

	
	@Override
	public void run() {
		stop = false;
		try {
			socket = new ServerSocket(8888);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		char[] buf = new char[256];
		while (!stop) {
			try {
				Log.d("RECEIVER", "waiting for connection");
				Socket sender = socket.accept();
				Log.d("RECEIVER", "connected to " + sender.getInetAddress().getHostAddress());
				BufferedReader in = new BufferedReader(new InputStreamReader(sender.getInputStream()));
				
				in.read(buf);
				String received = String.valueOf(buf);
				Log.d("RECEIVED", received);
				edu.rosehulman.chatspot.Message receivedMsg = edu.rosehulman.chatspot.Message.fromJSON(received);
				
				Message msg = messageHandler.obtainMessage();
				msg.what = 8008;
				msg.obj = receivedMsg;
				messageHandler.sendMessage(msg);
				
				in.close();
				sender.close();
				
				//send to buddies
				
			} catch (Exception e) {
				Log.e("ERROR", e.getMessage());
				break;
			}
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d("RECV_THREAD", "STOPPING");
	}

	public void terminate() {
		stop = true;
		if(socket!=null){
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
