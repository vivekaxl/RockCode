package test.actions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;


public class DataCollector {
	public List<DataCollectorSchema> listClasses;
	String InputString;
	public DataCollector(String Input) {
		listClasses = new ArrayList<DataCollectorSchema>();
		InputString = Input;
	}
	public void insertData(String input){
		outerparser(input);
	}
	public List<DataCollectorSchema> getData(){
		return listClasses;
	}
	protected int numberOfParamenter(String str){
				return(StringUtils.countMatches(str, ","));
	}
	public void printData(){
		for(DataCollectorSchema temp:listClasses){
			System.out.println("Precision: " + temp.precision);
			System.out.println("Name: " + temp.name);
			System.out.println("Line Number: "+temp.lineNumber);
			System.out.println("Type: " + temp.type);
			System.out.println("Elements: ");
			for(String str:temp.elements){
				System.out.println(str);
			}
			System.out.println("Character :" + temp.character);
			System.out.println();
		}
	}
	
	
	List<DataCollectorSchema> outerparser(String input){
		List<String> tempOuters = new ArrayList<String>();
		Pattern p = Pattern.compile("\\{([^}]*)\\}");
		Matcher m = p.matcher(input);
		while (m.find()) {
		 tempOuters.add(m.group(1));
		}
		for(String tempOuter:tempOuters){
			listClasses.add(innerParser(tempOuter));
		}
		return listClasses;
	}
	
	DataCollectorSchema innerParser(String temp){
		temp.replace("\n", "").replace("\r", "");
		DataCollectorSchema returnValue = new DataCollectorSchema();
		Pattern p = Pattern.compile("\\\"precision\": \"([^}]*)\\\"name\"");
		Matcher m = p.matcher(temp);
		while (m.find()) {
			returnValue.precision = Integer.parseInt(m.group(1).replace("\"", "").replace(",","").replace("\n","").replace(" ", ""));
			}
		
		p = Pattern.compile("\\\"name\": \"([^}]*)\\\"line_number\"");
		m = p.matcher(temp);
		while (m.find()) {
			 returnValue.name = m.group(1).replace(" ","").replace("\n", "").replace("\"", "").replace(",","");
			}
		
		p = Pattern.compile("\\\"line_number\": \"([^}]*)\\\"type\"");
		m = p.matcher(temp);
		while (m.find()) {
			returnValue.lineNumber = Integer.parseInt(m.group(1).replace("\"", "").replace(",","").replace("\n","").replace(" ",""));
			}
		

		p = Pattern.compile("\\\"type\": \"([^}]*)\\\"elements\"");
		m = p.matcher(temp);
		while (m.find()) {
			 returnValue.type = m.group(1).replace("\"", "").replace(",","").replace("\n","").replace(" ","");
			}
		
		String tempElement = temp.replace("\"\"","");
		p = Pattern.compile("\\\"elements\": \\[([^}]*)\\],");
		m = p.matcher(tempElement);
		while (m.find())
			tempElement = m.group(1);
		//can't use ',' as a separator so I first replace ), with )|| and then split it with ||
		tempElement = tempElement.replace(" ","").replace("\n", "").replace("),",")||");
		String[] tempElements;
		if(returnValue.type.equals("api_type")== true)
			tempElements = tempElement.split("\\,");
		else
			tempElements = tempElement.split("\\|\\|");
		returnValue.elements = Arrays.asList(tempElements);
		

		p = Pattern.compile("\\\"character\": \"([^}]*)\\\"");
		m = p.matcher(temp);
		while (m.find()) {
			returnValue.character = Integer.parseInt(m.group(1).replace("\"", "").replace(",","").replace(" ",""));
			}
		
		return returnValue;
	}

}

