package model;

public class Worker implements java.io.Serializable {
	
	private static final long serialVersionUID = -5174950454384771612L;
	private int id;
	private String status;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
