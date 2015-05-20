package edu.rosehulman.chatspot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BuddiesListDialog extends DialogFragment {
	private ArrayList<Buddy> buddiesList;
	
	public BuddiesListDialog(Set<Buddy> buddiesList){
		
		this.buddiesList = new ArrayList<Buddy>();
		this.buddiesList.addAll(buddiesList);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater pump = getActivity().getLayoutInflater();
		
		View view = pump.inflate(R.layout.buddies_list_dialog, null);

		ListView buddiesListview = (ListView) view.findViewById(R.id.buddiesList);
		buddiesListview.setDividerHeight(0);
		buddiesListview.setAdapter(new BuddyListArrayListAdapter(getActivity(), R.layout.buddies_list_dialog, R.id.buddies_list_dialog_item_text, buddiesList));
		
		builder.setView(view);
		builder.setTitle("Buddies");
		return builder.create();
	}
	
	public class BuddyListArrayListAdapter extends ArrayAdapter<Buddy>{

		public BuddyListArrayListAdapter(Context context, int resource,	int textViewResourceId, List<Buddy> objects) {
			super(context, resource, textViewResourceId, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
	       	Buddy b = getItem(position);
	        
	        if(row == null)
	        {
	            LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
	            row = inflater.inflate(R.layout.buddies_list_dialog_item, parent, false);
	        }
	        
	        TextView txt = (TextView) row.findViewById(R.id.buddies_list_dialog_item_text);
	        txt.setText(b.getAddress());
	        txt.setTextColor(b.getColor());	        
	        
			return row;
		}
		
	}
}
