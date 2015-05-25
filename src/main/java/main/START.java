package main;

import dao.PropfileDao;
import logic.APIListener;
import logic.SmtpListener;
import logic.parse.User;

public class START {
	public static void main(String args[]) {
		SmtpListener smtp = new SmtpListener();
		APIListener api = new APIListener();
	
		Thread t1 = new Thread(smtp);
		Thread t2 = new Thread(api);
		
		t1.start();
		t2.start();	
	}
}
