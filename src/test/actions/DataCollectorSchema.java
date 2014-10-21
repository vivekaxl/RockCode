package test.actions;

import java.util.ArrayList;
import java.util.List;


public class DataCollectorSchema {
	int precision;
	String name;
	int lineNumber;
	public String type;
	public List<String> elements;
	int character;
	String returnType; //added for findLeftNodeType()
	
	public DataCollectorSchema() {
		precision=0;
		name="";
		lineNumber=0;
		type="";
		elements = new ArrayList<String>();
		character=0;
		returnType="";
	}

}
