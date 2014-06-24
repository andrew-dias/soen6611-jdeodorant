package metrics;

import java.util.List;
import java.util.Set;

import ast.ClassObject;
import ast.FieldInstructionObject;
import ast.FieldObject;
import ast.MethodInvocationObject;
import ast.MethodObject;

/* 
 * Implements Brito and Abreu's Coupling Factor metric from MOOD metric suite
 */
public class CF {

	// set of all classes in system to be evaluated
	private Set<ClassObject> classSet;
	
	// total number of classes in system
	private int tc;

	// constructor
	public CF(Set<ClassObject> classSet) {
		this.classSet = classSet;
		this.tc = classSet.size();
	}
	
	// compute CF for entire system
	public double systemCF(){
		double result = 0;
		
		// sum the individual CF values for each class
		for (ClassObject cls : classSet) {
			result += classCF(cls);
		}
		
		return result;
	}
	
	// compute CF for a single class
	public double classCF(ClassObject c1) {
		double numerator, denominator, result;
		
		numerator = 0;
		
		// loop through all classes in system and check if coupled to given class
		for (ClassObject c2 : classSet) {
			numerator += client(c1, c2);
		}
		
		// TC^2 - TC
		denominator = (tc*tc) - tc;
		
		result = numerator/denominator;
		
		return result;			
	}
	
	// return 1 if classes are coupled, 0 otherwise
	private int client(ClassObject c1, ClassObject c2)
	{
		int result = 0;
		boolean coupled = false;
		
		// 0 if classes are the same
		if (!c1.equals(c2)) {
			
			// check class-level field references to other classes
			List<FieldObject> fields = c1.getFieldList();
			for (FieldObject field : fields) {
				if (field.getType().getClassType().equals(c2.getName())) {
					coupled = true;
				}
			}

			if (!coupled) {
				// get list of class methods 
				List<MethodObject> methods = c1.getMethodList();
				
				// check for method coupling
				for (MethodObject m : methods) {
					if (isMethodCoupled(m, c2)) {
						coupled = true;
						break;
					}
				}
			}
			
			if (coupled) {
				result = 1;
			}
		}
		
		return result;
	}
	
	// check if given method has a reference to given class
	private boolean isMethodCoupled(MethodObject m1, ClassObject c1) {
		boolean coupled = false;
		
		// check for method invocations
		List<MethodInvocationObject> methodCalls = m1.getMethodInvocations();		
		
		for (MethodInvocationObject methodCall : methodCalls) {
			if (methodCall.getOriginClassName().equals(c1.getName())) {
				coupled = true;
				break;
			}
		}

		// check for field references
		if (!coupled) {
			List<FieldInstructionObject> fields = m1.getFieldInstructions();
			
			for (FieldInstructionObject f : fields) {
				if (f.getOwnerClass().equals(c1.getName())) {
					coupled = true;
					break;
				}
			}
		}
		
		return coupled;
	}
}
