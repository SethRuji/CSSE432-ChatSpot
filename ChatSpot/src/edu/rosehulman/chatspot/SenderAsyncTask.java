package edu.rosehulman.chatspot;

import java.io.OutputStream;
import java.net.Socket;

import android.os.AsyncTask;
import android.util.Log;

public class SenderAsyncTask extends AsyncTask<Void, Void, Boolean> {
	private MainActivity context;
	private String receiverAddress;
	private Message message;

	public SenderAsyncTask(MainActivity context, String receiverAddress, String text, long timestamp) {
		this.context = context;
		this.receiverAddress = receiverAddress;
		this.message = new Message(context.getLocalIpAddress(), receiverAddress, text, context.getMessageColor(), timestamp);
	}
	
	public SenderAsyncTask(MainActivity context, String receiverAddress, Message message) {
		this.context = context;
		this.receiverAddress = receiverAddress;
		this.message = new Message(context.getLocalIpAddress(), receiverAddress, message.getText(), message.getColor(), message.getTimestamp());
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
			context.addMessageToContainer(message);
		}
	}
	
}
