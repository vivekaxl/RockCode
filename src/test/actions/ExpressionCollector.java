package test.actions;


import java.util.List;

public class ExpressionCollector {
	public static  void printList (List<?> list2) 
	{      
		List<?> list= list2;
		for (int i = 0; i < list.size();  i++) 
			System.out.print(list2.get(i)+"\n");
	}
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public String getReturnType() {
		return returnType;
	}
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}
	
	private String constantExpression;
	
	public String getConstantExpression() {
		return constantExpression;
	}
	public void setConstantExpression(String constantExpression) {
		this.constantExpression = constantExpression;
	}
	public ExpressionCollector(String expression,String variableName, String returnType,String arguments) {
		super();
		this.expression = expression;
		this.variableName = variableName;
		this.returnType = returnType;
		this.arguments = arguments;
		this.returnTypeList = null;
		this.argumentList = null;
		this.constantExpression = null;
	}
	
	public ExpressionCollector(String expression,String variableName, String returnType,String arguments, List<String> returnTypeList, List<String> argumentList) {
		super();
		this.expression = expression;
		this.variableName = variableName;
		this.returnType = returnType;
		this.arguments = arguments;
		this.returnTypeList = returnTypeList;
		this.argumentList = argumentList;
		this.constantExpression = null;
	}
	
	public ExpressionCollector(String expression, String constantExpression){
		this.expression = expression;
		this.constantExpression = constantExpression;
	}
	
	
	String expression;
	String variableName;
	public String getVariableName() {
		return variableName;
	}
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
	String returnType;
	String arguments;
	List<String> returnTypeList;
	public List<String> getReturnTypeList() {
		return returnTypeList;
	}
	public void setReturnTypeList(List<String> returnTypeList) {
		this.returnTypeList = returnTypeList;
	}
	public List<String> getArgumentList() {
		return argumentList;
	}
	public void setArgumentList(List<String> argumentList) {
		this.argumentList = argumentList;
	}
	List<String> argumentList;
	
	public void printData(){
		System.out.println("Expression: " + this.expression);
		if(this.variableName != null)
			System.out.println("Variable Name: " + this.variableName);
		if(this.returnType != null)
			System.out.println("Return Type: " + this.returnType);
		if(this.arguments != null)
			System.out.println("Arguments: " + this.arguments);
		if(this.returnTypeList!= null && this.returnTypeList.size() != 1){
			System.out.println("Return Type List: ");
			printList(this.returnTypeList);
		}
		if(this.argumentList != null && this.argumentList.size() != 1){
			System.out.println("Argument Type List: ");
			printList(this.argumentList);
		}
		if(this.constantExpression != null)
			if(this.constantExpression.equals("") )
				System.out.println("Constant Expression: 'emptyString'");
			else if(this.constantExpression.equals(" "))
				System.out.println("Constant Expression: 'space'");
			else
				System.out.println("Constant Expression: " + this.constantExpression);
		System.out.println();
	}
	
}

