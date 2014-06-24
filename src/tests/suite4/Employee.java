package tests.suite4;

public abstract class Employee {

	private String name;
	protected Payment pmnt;
	
	public Employee(String s) {
		// TODO Auto-generated constructor stub
	}

	public String getName() {
		return null;
	}
	
	public String getPayType() {
		return null;
	}
	
	public abstract void calcSalary();
	
	public abstract String getEmpType();
	
	public void setPayment(Payment p) {
		
	}
}
