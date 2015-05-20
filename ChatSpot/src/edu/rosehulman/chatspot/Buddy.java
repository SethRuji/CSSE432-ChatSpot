package edu.rosehulman.chatspot;


public class Buddy {

	private String address;
	private int color;

	public Buddy(String address, int color) {
		this.address = address;
		this.color = color;
	}

	public String getAddress() {
		return address;
	}

	public int getColor() {
		return color;
	}
	
	public void setColor(int color){
		this.color = color;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this){
			return true;
		}
		if(!(o instanceof Buddy)){
			return false;
		}
		
		Buddy other = (Buddy) o;
		return this.address.equals(other.getAddress());
	}
	
	
	@Override
	public int hashCode() {
		return address.hashCode();
	}
}
