package test.actions;

import java.util.ArrayList;
import java.util.List;


public class Variables {
	
	public Variables(String name, String type, String returnType,String packageImport) {
		this.name = name;
		this.type = type;
		this.variableType = new ArrayList<String>();
		this.returnType = returnType;
		this.packageImport = packageImport;
		this.lineNumber=-1;
	}
	
	public Variables(String name, String type,String variableType, String returnType,String packageImport,int lineNumber) {
		this.variableType = new ArrayList<String>();
		this.name = name;
		this.type = type;
		this.variableType.add(variableType);
		this.returnType = returnType;
		this.packageImport = packageImport;
		this.lineNumber=lineNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Variables other = (Variables) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String name;
	public String getName() {
		return name;
	}

	public String type;
	public void setType(String type) {
		this.type = type;
		if(type=="variable"){
			this.packageImport = "NA";
		}
		else
			this.returnType = "NA";
	}
	public List<String> variableType;
	String returnType;
	public String packageImport;
	public Integer lineNumber; //first line where the variable was used
}

