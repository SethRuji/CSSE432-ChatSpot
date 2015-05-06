package edu.rosehulman.chatspot;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class MessageAsyncTask extends AsyncTask<String, Void, String> {
	private Context context;

	public MessageAsyncTask(Context context) {
		this.context = context;
	}

	@Override
	protected String doInBackground(String... params) {
		try {
			ServerSocket serverSocket = new ServerSocket(8888);
			Socket client = serverSocket.accept();
			
			OutputStream out = client.getOutputStream();

			//TODO replace with sending message protocol
			out.write("Hello World!".getBytes());
			
			serverSocket.close();
			return "SUCCESS";
		} catch (IOException e) {
			Log.e("HHH", e.getMessage());
			return "FAILURE";
		}
	}

	@Override
	protected void onPostExecute(String result) {
		if (result != null) {
			Log.d("HHH", result);
		}
	}

}
