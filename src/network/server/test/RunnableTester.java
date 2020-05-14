package network.server.test;

public class RunnableTester {

	public static void main(String[] args) {

		TestRunnable tester = new TestRunnable("A", "A");
		
		Thread t = new Thread(tester);
		t.start();
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		tester.setData("B");
		t = new Thread(tester);
		t.start();
		
		
	}

}

class TestRunnable implements Runnable {
	
	private String id;
	private String data;
	
	public TestRunnable() {
		// TODO Auto-generated constructor stub
	}
	
	public TestRunnable(String id, String data) {
		this.id = id;
		this.data = data;
	}
	
	public void setData(String data) {
		this.data = data;
	}
	
	@Override
	public void run() {
		for (int i = 0; i < 5; i++) {
			System.out.println(this.data);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
