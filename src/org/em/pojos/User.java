package org.em.pojos;

public class User {
	private String username;
	private String name;
	private String password;
	private String email;
	private int id;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public User(String  username, String password, String email, String name ){
		this.id = -1;
		this.username = username;
		this.password = password;
		this.email = email;
		this.name = name;
		
	}
	public int createOrUpdate(){
		
		return 0;
		
	}

}
