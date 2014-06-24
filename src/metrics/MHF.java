package metrics;

import java.util.Set;

import ast.Access;
import ast.ClassObject;
import ast.MethodObject;
import ast.TypeObject;

public class MHF {

	// set of all classes in system to be evaluated
	private Set<ClassObject> classSet;

	// total number of classes in system
	private int tc;

	// constructor
	public MHF(Set<ClassObject> classSet) {
		this.classSet = classSet;
		this.tc = classSet.size();
	}
	
	// compute MHF for entire system
	public double systemMHF(){
		double result = 0;
		double visibility = 0;
		int methodCount = 0;
		
		// sum up visibility over all methods in system
		for (ClassObject c : classSet) {
			for (MethodObject m : c.getMethodList()) {
				visibility += v(m, c);
				methodCount++;
			}
		}
		
		// divide total visibility by number of methods in system
		result = visibility/methodCount;
		
		return result;
	}

	// compute MHF for a single class 
	public double classMHF(ClassObject c){
		double result = 0;
		double visibility = 0;
		int methodCount = 0;

		// sum up visibility over all methods in class
		for (MethodObject m : c.getMethodList()) {
			visibility += v(m, c);
			methodCount++;
		}
			
		// divide total visibility by number of methods in class
		if (methodCount > 0) {		
			result = visibility/methodCount;
		}
				
		return result;
	}

	// compute visibility of given method
	private double v(MethodObject m, ClassObject c)
	{
		double result, visibility;
		
		Access a = m.getAccess();
		
		switch (a) {
		// public methods are visible to all classes in the system (does not count method's classs
		case PUBLIC:
			visibility = tc-1;
			break;
		// protected methods are visible to classes in same package and subclasses 
		case PROTECTED:			
			visibility = packageClassNum(c)-1 + subclassNum(c);
			break;
		// default visibility is same package
		case NONE:
			visibility = packageClassNum(c)-1;
			break;
		// private methods are not visible outside their class
		case PRIVATE:
			visibility = 0;
			break;
		// should not get to default case
		default:
			visibility = -1;
			break;
		}

		// divide by total classes - 1
		result = visibility/(tc-1);	
		
		return result;
	}

	// count the number of classes in the same package as the given class
	private int packageClassNum(ClassObject c1) {
		int classCount = 0;
		String pkgName1 = getPackageName(c1.getName());
		
		// loop through all system classes and look for matching package names
		for (ClassObject c2 : classSet) {
			String pkgName2 = getPackageName(c2.getName());
			if (pkgName1.equals(pkgName2)) {
				classCount++;
			}
		}

		return classCount;
	}
	
	// count the number of subclasses of a given class
	private int subclassNum(ClassObject c1) {
		int classCount = 0;
		String scName;
		
		// loop through all system classes
		for (ClassObject c2 : classSet) {
			TypeObject sc = c2.getSuperclass();
			
			// if superclass exists, compare against given class name
			if (sc != null) {
				scName = sc.getClassType();

				if (scName.equals(c1.getName())) {
					classCount++;
				}
			}
		}

		return classCount;
	}
	
	// parses the package name from a fully qualified class name
	private String getPackageName(String className) {
		String pkgName = "";
		int i = className.lastIndexOf(".");
		if (i != -1) {
			pkgName = className.substring(0,i);
		}
		return pkgName;		
	}
}
