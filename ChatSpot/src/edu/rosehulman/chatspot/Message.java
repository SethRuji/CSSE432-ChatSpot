package edu.rosehulman.chatspot;

import java.util.UUID;

import org.json.JSONObject;

public class Message implements Comparable<Message> {
	public static final String ID_KEY = "id";
	public static final String SENDER_KEY = "send";
	public static final String RECEIVER_KEY = "recv";
	public static final String TEXT_KEY = "text";
	public static final String COLOR_KEY = "color";
	public static final String TIMESTAMP_KEY = "time";

	private UUID id;
	private String sender;
	private String receiver;
	private String text;
	private int color;
	private long timestamp;
	
	public Message(String sender, String receiver, String text, int color, long timestamp ){
		this(UUID.randomUUID(), sender, receiver, text, color, timestamp);
	}
	public Message(UUID id, String sender, String receiver, String text, int color, long timestamp ){
		this.id = id;
		this.sender = sender;
		this.receiver = receiver;
		this.text = text;
		this.color = color;
		this.timestamp = timestamp;
	}
	
	public UUID getId(){
		return this.id;
	}
	
	public String getSender(){
		return sender;
	}

	public void setSender(String sender){
		this.sender = sender;
	}
	
	public String getReceiver(){
		return receiver;
	}
	
	public void setReceiver(String recv){
		this.receiver = recv;
	}
	
	public String getText(){
		return text;
	}
	
	public int getColor(){
		return color;
	}
	
	public long getTimestamp(){
		return timestamp;
	}
	
	public String getJSON(){
		try{
			JSONObject container = new JSONObject();
			container.put(ID_KEY, id.toString());
			container.put(SENDER_KEY, sender);
			container.put(RECEIVER_KEY, receiver);
			container.put(TEXT_KEY, text);		
			container.put(COLOR_KEY, color);
			container.put(TIMESTAMP_KEY, timestamp +"");
			return container.toString();
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}		
	}
	
	public static Message fromJSON(String raw){
		try{
			JSONObject object = new JSONObject(raw);
			UUID id = UUID.fromString(object.getString(ID_KEY));
			String sender = object.getString(SENDER_KEY);
			String receiver = object.getString(RECEIVER_KEY);
			String text = object.getString(TEXT_KEY);
			int color = object.getInt(COLOR_KEY);
			long timestamp = Long.valueOf(object.getString(TIMESTAMP_KEY));
			return new Message(id, sender, receiver, text, color, timestamp);			
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o){
			return true;
		}
		
		if(!(o instanceof Message)){
			return false;
		}
		Message another = (Message) o;
		return this.id.equals(another.getId());
	}
	
	@Override
	public int compareTo(Message another) {
		int comparison = Long.valueOf(timestamp).compareTo(Long.valueOf(another.getTimestamp()));
		if(comparison==0){
			comparison = text.compareTo(another.getText());
		}		
		return comparison;
	}
}
