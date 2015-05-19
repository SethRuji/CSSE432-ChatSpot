package edu.rosehulman.chatspot;

import java.io.OutputStream;
import java.net.Socket;

import android.os.AsyncTask;
import android.util.Log;

public class SenderAsyncTask extends AsyncTask<Void, Void, Boolean> {
	private MainActivity context;
	private String receiverAddress;
	private Message message;
	
	public SenderAsyncTask(MainActivity context, String receiverAddress, Message message) {
		this.context = context;
		this.receiverAddress = receiverAddress;
		this.message = message;
		this.message.setSender(context.getLocalIpAddress());
		this.message.setReceiver(receiverAddress);
	}


	@Override
	protected Boolean doInBackground(Void... params) {
		Socket receiver = null;
		try{
			Log.d("SENDER", "starting connection to " + receiverAddress);
			receiver = new Socket(receiverAddress, 8888);
			Log.d("SENDER", "connected");
			
			OutputStream out = receiver.getOutputStream();

			Log.d("SENDING", message.getJSON());
			out.write(message.getJSON().getBytes());
			
			out.close();
			receiver.close();
			return true;
			
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if(result){
			context.addSentMessageToContainer(message);
		} else {
			//couldn't send? probs can't connect so remove the friend from buddy list
			this.context.disconnectFromBuddy(this.receiverAddress);
		}
	}
	
}
