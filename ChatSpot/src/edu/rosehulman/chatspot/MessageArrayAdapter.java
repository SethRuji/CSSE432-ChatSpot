package edu.rosehulman.chatspot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MessageArrayAdapter extends ArrayAdapter<Message> {
	public static final SimpleDateFormat timestampFormatter = new SimpleDateFormat("HH:mm:ss");
	
	public MessageArrayAdapter(Context context) {
		super(context, R.layout.message_list_item, new ArrayList<Message>());
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
       	Message m = getItem(position);
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
            row = inflater.inflate(R.layout.message_list_item, parent, false);
        }
        
        TextView listText = (TextView) row.findViewById(R.id.message_list_text);
        listText.setText(m.getText());
        listText.setTextColor(m.getColor());
        
        TextView timeText = (TextView) row.findViewById(R.id.message_list_time);
        timeText.setText(timestampFormatter.format(new Date(m.getTimestamp())));
        timeText.setTextColor(m.getColor());
        
        return row;
	}
	
	public boolean containsMessage(Message m){
		for(int i=0; i<getCount(); i++){
			if(getItem(i).equals(m)){
				return true;
			}
		}
		return false;
	}
}
