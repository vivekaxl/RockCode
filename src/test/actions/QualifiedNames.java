package test.actions;
import java.util.ArrayList;
import java.util.List;


public class QualifiedNames {

	public String className;
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	public String methodName;
	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	List<String> arguments;
	public List<String> getArguments() {
		return arguments;
	}

	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

	
	
	
	public QualifiedNames() {
		className = null;
		methodName =null;
		arguments = new ArrayList<String>();
	}
}

