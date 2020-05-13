package network.server.test;

import java.sql.Date;

public class TimeTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Date now = new Date(System.currentTimeMillis());
		System.out.println(now.toString());
		System.out.println(now.toLocaleString());

	}

}
