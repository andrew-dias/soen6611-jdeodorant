package tests.suite2;

public class A {
	private B b;
	
	// A is coupled to B
	public void mA1(){
		b.mB1();
	}

}
