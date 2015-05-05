package edu.rosehulman.chatspot;

import android.app.Activity;
import android.os.Bundle;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mSendButton = (Button) findViewById(R.id.send);
		mMessageBox = (EditText) findViewById(R.id.messageBox);
		mMessageContainer = (LinearLayout) findViewById(R.id.messageContainer);

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
	}

	public void sendMessage() {
		TextView tv = new TextView(this);
		tv.setTextSize(24);
		tv.setText(mMessageBox.getText().toString());
		mMessageContainer.addView(tv);
	}
}
