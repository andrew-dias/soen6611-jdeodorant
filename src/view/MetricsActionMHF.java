package view;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import metrics.CF;
import metrics.LCOM;
import metrics.MHF;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import ast.ASTReader;
import ast.ClassObject;
import ast.CompilationUnitCache;
import ast.SystemObject;

public class MetricsActionMHF  implements IObjectActionDelegate {
	
	private IWorkbenchPart part;
	private ISelection selection;
	
	private IJavaProject selectedProject;
	private IPackageFragmentRoot selectedPackageFragmentRoot;
	private IPackageFragment selectedPackageFragment;
	private ICompilationUnit selectedCompilationUnit;
	private IType selectedType;
	private IMethod selectedMethod;
	
	public void run(IAction arg0) {
		try {
			CompilationUnitCache.getInstance().clearCache();
			if(selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection)selection;
				Object element = structuredSelection.getFirstElement();
				if(element instanceof IJavaProject) {
					selectedProject = (IJavaProject)element;
					selectedPackageFragmentRoot = null;
					selectedPackageFragment = null;
					selectedCompilationUnit = null;
					selectedType = null;
					selectedMethod = null;
				}
				else if(element instanceof IPackageFragmentRoot) {
					IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot)element;
					selectedProject = packageFragmentRoot.getJavaProject();
					selectedPackageFragmentRoot = packageFragmentRoot;
					selectedPackageFragment = null;
					selectedCompilationUnit = null;
					selectedType = null;
					selectedMethod = null;
				}
				else if(element instanceof IPackageFragment) {
					IPackageFragment packageFragment = (IPackageFragment)element;
					selectedProject = packageFragment.getJavaProject();
					selectedPackageFragment = packageFragment;
					selectedPackageFragmentRoot = null;
					selectedCompilationUnit = null;
					selectedType = null;
					selectedMethod = null;
				}
				else if(element instanceof ICompilationUnit) {
					ICompilationUnit compilationUnit = (ICompilationUnit)element;
					selectedProject = compilationUnit.getJavaProject();
					selectedCompilationUnit = compilationUnit;
					selectedPackageFragmentRoot = null;
					selectedPackageFragment = null;
					selectedType = null;
					selectedMethod = null;
				}
				else if(element instanceof IType) {
					IType type = (IType)element;
					selectedProject = type.getJavaProject();
					selectedType = type;
					selectedPackageFragmentRoot = null;
					selectedPackageFragment = null;
					selectedCompilationUnit = null;
					selectedMethod = null;
				}
				else if(element instanceof IMethod) {
					IMethod method = (IMethod)element;
					selectedProject = method.getJavaProject();
					selectedMethod = method;
					selectedPackageFragmentRoot = null;
					selectedPackageFragment = null;
					selectedCompilationUnit = null;
					selectedType = null;
				}
				IWorkbench wb = PlatformUI.getWorkbench();
				IProgressService ps = wb.getProgressService();
				ps.busyCursorWhile(new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						if(ASTReader.getSystemObject() != null && selectedProject.equals(ASTReader.getExaminedProject())) {
							new ASTReader(selectedProject, ASTReader.getSystemObject(), monitor);
						}
						else {
							new ASTReader(selectedProject, monitor);
						}
						
						if(selectedPackageFragmentRoot != null) {
							// package fragment root selected
							SystemObject system = ASTReader.getSystemObject();
							Set<ClassObject> classes = system.getClassObjects(selectedPackageFragmentRoot);

							// Method Hiding Factor
							// calculate for system
							double mhfVal;
							MHF mhf = new MHF(classes);
							mhfVal = mhf.systemMHF();
							System.out.println("MHF\t" + selectedPackageFragmentRoot.getElementName() + "\t" + mhfVal);

							// calculate for individual classes
							for (ClassObject c1 : classes) {
								mhfVal = mhf.classMHF(c1);
								System.out.println("MHF\t" + c1.getName() + "\t" + mhfVal);
							}	
						}
						else if(selectedPackageFragment != null) {
							// package fragment selected
							// used for the purpose of generating metrics on a test suite without regard to classes outside of test suite
							SystemObject system = ASTReader.getSystemObject();
							Set<ClassObject> classes = system.getClassObjects(selectedPackageFragment);

							// Method Hiding Factor
							// calculate for a package
							MHF mhf = new MHF(classes);
							double mhfVal = mhf.systemMHF();
							System.out.println("MHF\t" + selectedPackageFragment.getElementName() + "\t" + mhfVal);

							// calculate for individual classes
							System.out.println("----Class level-----");
							for (ClassObject c1 : classes) {
								mhfVal = mhf.classMHF(c1);
								System.out.println("MHF\t" + c1.getName() + "\t" + mhfVal);
							}	
						}
						else if(selectedCompilationUnit != null) {
							// compilation unit selected							
							SystemObject system = ASTReader.getSystemObject();
							Set<ClassObject> classes = system.getClassObjects(selectedCompilationUnit);						

							// calculates MHF for a given class
							MHF mhf = new MHF(system.getClassObjects());							
							for (ClassObject c1 : classes) {
								double mhfVal = mhf.classMHF(c1);
								System.out.println("MHF\t" + c1.getName() + "\t" + mhfVal);
							}						
						}
						else if(selectedType != null) {
							// type selected
						}
						else if(selectedMethod != null) {
							// method selected
						}
						else {
							// java project selected
							SystemObject system = ASTReader.getSystemObject();
							LCOM lcom = new LCOM(system);
							System.out.print(lcom.toString());
						}
					}
				});
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.part = targetPart;
	}
}
