package tests.generic.suite1;


public class C extends SuperC {
	private String aC1;
	private D d;
	private B b;
	
	public void mC1(){
		b.mB1();
		mSC1();
		b.mB2();
		b.mB1();		
	}
	public void mC2() {
		d.mB2();
		d.mD1();
		d.mB2();
	}
}
