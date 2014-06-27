package metrics;

import java.util.HashSet;
import java.util.ListIterator;
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
		if (methodCount > 0) {		
			result = visibility/methodCount;
		}
		
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
		double result = 0;
		double visibility;
		
		Set<ClassObject> visibleClasses = new HashSet<ClassObject>();
		
		Access a = m.getAccess();
		
		switch (a) {
		// public methods are visible to all classes in the system (does not count method's classs
		case PUBLIC:
			visibility = tc-1;
			break;
		// protected methods are visible to classes in same package and subclasses 
		case PROTECTED:	
			// union of sets
			visibleClasses.addAll(packageClasses(c));
			visibleClasses.addAll(subclasses(c));
			// subtract 1 to exclude method's class
			visibility = visibleClasses.size()-1;
			break;
		// default visibility is same package
		case NONE:
			visibleClasses.addAll(packageClasses(c));
			// subtract 1 to exclude method's class
			visibility = visibleClasses.size()-1;
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
		if (tc>1) {
			result = visibility/(tc-1);
		}
		
		return result;
	}

	// generate set of classes that share package of given class
	private Set<ClassObject> packageClasses(ClassObject c1) {
		Set<ClassObject> classes = new HashSet<ClassObject>();		
		String pkgName1 = getPackageName(c1.getName());
		
		// loop through all system classes and look for matching package names
		for (ClassObject c2 : classSet) {
			String pkgName2 = getPackageName(c2.getName());
			if (pkgName1.equals(pkgName2)) {
				classes.add(c2);
			}
		}
		
		return classes;
	}
	
	// generate set of subclasses of given class
	private Set<ClassObject> subclasses(ClassObject c1) {
		Set<ClassObject> classes = new HashSet<ClassObject>();
		
		// loop through all system classes
		for (ClassObject c2 : classSet) {
			if (isSubclass(c2, c1)) {
				classes.add(c2);
			}
		}
		
		return classes;
	}
	
	// checks if child is subclass of parent
	private boolean isSubclass(ClassObject subClass, ClassObject superClass) {
		boolean isSubclass = false;
		boolean classFound = false;
		ClassObject lSubClass = subClass;
		
		TypeObject superType = lSubClass.getSuperclass();
			
		// iteratively travel up class hierarchy
		while (superType != null) {
//			System.out.println("Loop: " + lSubClass.getName() + " ? " + superType.getClassType());
			// check for match
			if (superType.getClassType().equals(superClass.getName())) {
				isSubclass = true;
				superType = null;
			}
			else {
				// find class object for superType
				for (ClassObject c : classSet) {
					classFound = false;
					if (c.getName().equals(superType.getClassType())) {
						lSubClass = c;
						superType = lSubClass.getSuperclass();
						classFound = true;
						break;
					}
				}
				
				// means supertype is outside of system so we stop looking
				if (!classFound) {
					superType = null;
				}
			}
		}
		return isSubclass;
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
