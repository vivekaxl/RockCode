package test.actions;

//import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.StatusTextEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

//import test.ExpressionCollector;
//import testAll.DataCollector;
//import testAll.DataCollectorSchema;
//import testAll.QualifiedNames;
//import testAll.RankingSkeleton;
//import testAll.SelectionAlgorithm;
//import testAll.Variables;


/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class SampleAction implements IWorkbenchWindowActionDelegate {
	
	public static List<Variables> undeclaredVariables=null;
	public static String newText1 = null;
	private IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public SampleAction() {
	}

	
	protected static CompilationUnit parseStatementsCompilationUnit(String source) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setEnvironment( // apply classpath
				new String[] { "//home//vivek//WorkSpace//Test//bin" }, //
				null, null, true);
		parser.setUnitName("any_name");
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		return cu;

	}
	
	protected static void getVariables(ITextEditor editor, String source){
		final ASTNode root = parseStatementsCompilationUnit(source);
		List<ASTNode> listMethod = new ArrayList<ASTNode>();
		root.accept(new ASTVisitor() {
			public boolean visit(MethodDeclaration node) {
				listMethod.add(node);
				return false;     
			}
		});
		
		for(ASTNode test:listMethod){
			MethodDeclaration temp = (MethodDeclaration)test;
			System.out.println("Name: "+temp.getName().toString());
			System.out.println("Offset: "+temp.getStartPosition());
			System.out.println("Length: " +temp.getLength());
			((TextEditor)editor).selectAndReveal(temp.getStartPosition(), temp.getName().toString().length());
		}
		
		
	}
	
	static boolean checkReturnStatement(String source, String methodName){
		final CompilationUnit root = parseStatementsCompilationUnit(source);
		List<String> returnList = new ArrayList<String>();
		if(root == null)
			System.out.println("It's null");
		root.accept(new ASTVisitor() {
			public boolean visit(MethodDeclaration node) {
				if(node.isConstructor() != true && node.getName().toString().equals(methodName) == true){
					//System.out.println("Method Name: " + node.getName().toString());
					//System.out.println("Return Type of the MethodDeclaration is :: " +node.getReturnType2().toString());
					returnList.add(node.getName().toString());
					node.accept(new ASTVisitor(){
						public boolean visit(ReturnStatement node1){
							//System.out.println("Return Type in MethodBody ::  " +node1.getExpression().toString() );
							returnList.clear();
							return true;
						}
					});
					return true;
				}
				else
					return false;
			}
		});
		if(returnList.size() == 0)
			return true;
		else
			return false;
	}
	
	public static String addReturnStatements(String doc){

		Document document = new Document(doc);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(document.get().toCharArray());
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		AST ast = cu.getAST();
		ASTRewrite rewriter = ASTRewrite.create(ast);

		TypeDeclaration typeDecl = (TypeDeclaration) cu.types().get(0);
		MethodDeclaration methodDecl;


		System.out.println("# of methods: "+typeDecl.getMethods().length );
		for(int i=0;i<(typeDecl.getMethods().length);i++){
			methodDecl = typeDecl.getMethods()[i];
			System.out.print("Method Name: "+methodDecl.getName().toString());
			if(checkReturnStatement(doc,methodDecl.getName().toString()) == true)
				System.out.println(" :: It's fine");
			else{
				System.out.println(" :: Return statement missing!");
				Block block = methodDecl.getBody();

				System.out.println("Method Return Type :: " + methodDecl.getReturnType2().toString());
				if(methodDecl.getReturnType2().toString().equals("void") == true)
					return doc;

				String returnTypeVariable = "return" + methodDecl.getReturnType2().toString();

				ReturnStatement newReturnStatement = ast.newReturnStatement();
				newReturnStatement.setExpression(ast.newSimpleName(returnTypeVariable));

				ListRewrite listRewrite = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
				listRewrite.insertLast(newReturnStatement, null);

				TextEdit edits = rewriter.rewriteAST(document, null);

				try {
					UndoEdit undo = edits.apply(document);
				} catch (MalformedTreeException | BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}


		//	System.out.println(document.get().toCharArray());
		return document.get().toString();
	}
	
	/*
	 * Given a class name find the package it is associated to.
	 * Caveat: The packages it looks into are the packages that are already loaded
	 * @http://stackoverflow.com/questions/8742965/how-to-find-the-package-name-given-a-class-name
	 */
	static String convertClasstoPackage(String className){
		System.out.println("convertClasstoPackage:: className " +className);
		final Package[] packages = Package.getPackages();
		for (final Package p : packages) {
			final String pack = p.getName();
			final String tentative = pack + "." + className;
			try {
				Class.forName(tentative);
			} catch (final ClassNotFoundException e) {
				continue;
			}
			System.out.println("convertClasstoPackage:: "+pack);
			return(pack);
		}
		System.out.println("convertClasstoPackage:: Package not found!!");
		return("not found");
	}
	
	
	/*
	 * Given a class name find the package it is associated to.
	 */	
	static String convertClasstoPackage2(String className) {

		try {
			Class cls = Class.forName(className);

			// returns the name and package of the class
			// System.out.println("Class = " + cls.getName());
			// System.out.println("Package = " + cls.getPackage().getName());
			return cls.getPackage().getName();
		}
		catch(ClassNotFoundException ex) {
			System.out.println(ex.toString());
			return null;
		}
	}
	/*
	 * Prints all the undeclared variables
	 */
	static List<Variables> checkVariableDeclaration(String source){
		List<Variables> returnUndeclared = new ArrayList<Variables>();

		final CompilationUnit root = parseStatementsCompilationUnit(source);
		IProblem[] problems = root.getProblems();
		if (problems != null && problems.length > 0) {
			for (IProblem problem : problems) {
				System.out.println(problem.getMessage());
				System.out.println(problem.getID());

				System.out.println(IProblem.UnresolvedVariable);
				System.out.println(IProblem.UndefinedType);
				System.out.println(IProblem.UndefinedName);

				if(problem.getID() == IProblem.UnresolvedVariable){
					if(returnUndeclared.contains(new Variables(problem.getArguments()[0],"","",""))==false)
						returnUndeclared.add(new Variables(problem.getArguments()[0],"variable","","NA"));
				}
				else if(problem.getID() == IProblem.UndefinedType){
					if(returnUndeclared.contains(new Variables(problem.getArguments()[0],"","",""))==false)
						returnUndeclared.add(new Variables(problem.getArguments()[0],"type","NA",convertClasstoPackage(problem.getArguments()[0])));
				}
				else if(problem.getID() == IProblem.UndefinedName){
					System.out.println(problem.getArguments()[0]);
										if(returnUndeclared.contains(new Variables(problem.getArguments()[0],"","",""))==false)
											returnUndeclared.add(new Variables(problem.getArguments()[0],"type","","NA"));
				}
				else{
					;
				}

			}
		}
		return returnUndeclared;
	}
	
	
	public static DataCollector getTypesFromBaker(String source) throws FileNotFoundException, UnsupportedEncodingException, InterruptedException{
		String response = null;
		PrintWriter writer = new PrintWriter("temp.txt", "UTF-8");
		writer.println(source);
		writer.flush();
		writer.close();
		ProcessBuilder p=new ProcessBuilder("curl", "--data-urlencode","pastedcode@temp.txt", "http://gadget.cs.uwaterloo.ca:2145/snippet/getapijsonfromcode.php");
		try {
			System.out.println("Baker Start");
			final Process shell = p.start();
			InputStream shellIn = shell.getInputStream();
			int shellExitStatus = shell.waitFor();
			System.out.println(shellExitStatus);
			response = convertStreamToStr(shellIn);
			shellIn.close();
			System.out.println("Processed finished with status: " + shellExitStatus);
		} catch(Exception e){
			System.out.println(" getTypesFromBaker didn't work");
			e.printStackTrace();
		}
		String temp = response.substring(25, response.length()-3);
		DataCollector test = new DataCollector(source);
		test.insertData(temp);
		test.printData();
		return test;
	}
	public static String convertStreamToStr(InputStream is) throws IOException {

		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		}
		else {
			return "";
		}
	}
	public static List<String> getImportStatement(String source){
		List<String> ImportStatements = new ArrayList<String>();
		final CompilationUnit root = parseStatementsCompilationUnit(source);
		if(root == null)
			System.out.println("Something is wrong!");

		root.accept(new ASTVisitor() {		
			public boolean visit(ImportDeclaration node){
				ImportStatements.add(node.getName().getFullyQualifiedName());
				return true;
			}
		});
		return ImportStatements;
	}
	//To find constant expression
	static List<ExpressionCollector> extractHints(String tempString){

		final CompilationUnit root = parseStatementsCompilationUnit(tempString);
		List <ExpressionCollector> hintValue = new ArrayList<ExpressionCollector>();
		if(root == null)
			System.out.println("Something is wrong!");

		root.accept(new ASTVisitor() {
			public boolean visit(MethodDeclaration root1){
				//System.out.println("new MethodDeclaration " +root1.getName());
				root1.accept(new ASTVisitor(){
					public boolean visit( VariableDeclarationStatement node) {
						node.accept(new ASTVisitor(){
							public boolean visit(VariableDeclarationFragment node1){
								if(node1.getInitializer()!= null){
									System.out.println("################# VariableDeclarationFragment :: " + node1.getInitializer().toString());//.resolveConstantExpressionValue());
									if(node1.getInitializer().resolveConstantExpressionValue() != null){
										hintValue.add(new ExpressionCollector(node1.toString(),node1.getInitializer().resolveConstantExpressionValue().toString()));
										System.out.println("RIGHT!");
									}
									node1.accept(new ASTVisitor(){
										public boolean visit(MethodInvocation node2){
											System.out.println("MI>>>>>>>>>>>>>" +node2.toString());
											List<Expression> temp = node2.arguments();
											for(int i=0;i<temp.size();i++){
												//System.out.println("MethodInvocation inside VariableDeclarationStatement:: ################" +temp.get(i).resolveConstantExpressionValue());
												if(temp.get(i).resolveConstantExpressionValue() != null){
													hintValue.add(new ExpressionCollector(node1.toString(),temp.get(i).resolveConstantExpressionValue().toString()));
													//	System.out.println("RIGHT!");
												}
											}
											System.out.println("MethodInvocation inside VariableDeclarationStatement:: " + node2.arguments()); 
											return true;

										}
										public boolean visit(ClassInstanceCreation node2){
											System.out.println("ClassInstanceCreate :: " +node2.toString());
											System.out.println("ClassInstanceCreate :: " +node2.arguments());
											for(int i=0;i<node2.arguments().size();i++){
												if(((Expression)node2.arguments().get(i)).resolveConstantExpressionValue()!= null){
													hintValue.add(new ExpressionCollector(node1.toString(),((Expression)node2.arguments().get(i)).resolveConstantExpressionValue().toString()));
													//	System.out.println(((Expression)node2.arguments().get(i)).resolveConstantExpressionValue().toString());
												}
											}
											return true;
										}
										public boolean visit(TypeLiteral node2){
											System.out.println("TypeLiteral : "+node2.toString());
											hintValue.add(new ExpressionCollector(node1.toString(),node2.toString()));
											return true;
										}
									});			
								}
								return false;
							}
						});

						return false;
					}

					public boolean visit(MethodInvocation node){
						System.out.println("OUTTAMI >>>>>>>>>>>>>>>>>>" + node.toString());
						System.out.println(node.arguments());
						List<Expression> temp = node.arguments();
						for(int i=0;i<temp.size();i++){
							//System.out.println(" MethodInvocation:: ################ " +temp.get(i).resolveConstantExpressionValue());
							if(temp.get(i).resolveConstantExpressionValue() != null){
								//System.out.println("RIGHT!");
								hintValue.add(new ExpressionCollector(node.toString(),temp.get(i).resolveConstantExpressionValue().toString()));
							}
						}
						return false;

					}
				});
				return false;
			}
		});

		return hintValue;
	}
	
	static List<Variables> fillUndeclaredVariablesFromBaker(DataCollector data,List<Variables> undeclaredVariables){
		//Find elements with api_type
		for(Variables element:undeclaredVariables){
			for(DataCollectorSchema temp:data.listClasses){
				if(temp.type.equals("api_type"))
				{
					for(String tempElement:temp.elements){
						if(tempElement.contains(element.name) == true ){
							if(temp.elements.size() == 1)
								element.packageImport = tempElement;
							else
								element.packageImport = "not found";
						}						
					}
				}
			}
		}
		return undeclaredVariables;
	}
	
	private static List<ExpressionCollector> findReturnStatements(String tempString) {
		final CompilationUnit root = parseStatementsCompilationUnit(tempString);
		List <ExpressionCollector> returnValue = new ArrayList<ExpressionCollector>();
		System.out.println(ASTNode.BLOCK);
		if(root == null)
			System.out.println("Something is wrong!");
		//find all method declaration
		//find all return Statements
		//if resolvebinding is null
		//get the type from the method signature

		root.accept(new ASTVisitor() {
			public boolean visit(MethodDeclaration node) {
				node.accept(new ASTVisitor(){
					public boolean visit(ReturnStatement node1){
						System.out.println(node1.getExpression().toString());
						if(node1.getExpression().resolveTypeBinding() == null){
							System.out.println("Variable " + node1.getExpression().toString() +  " is not declared");
							ASTNode temp = node1;
							while(temp.getNodeType() != ASTNode.METHOD_DECLARATION){
								System.out.println(temp.getNodeType());
								temp=temp.getParent();
							}
							if(((MethodDeclaration)temp).getReturnType2().resolveBinding() != null){
								System.out.println(((MethodDeclaration)temp).getReturnType2().resolveBinding().getQualifiedName());
								returnValue.add(new ExpressionCollector(node1.toString(),node1.getExpression().toString(),((MethodDeclaration)temp).getReturnType2().resolveBinding().getQualifiedName(),""));
							}
							else
								returnValue.add(new ExpressionCollector(node1.toString(),node1.getExpression().toString(),((MethodDeclaration)temp).getReturnType2().toString(),""));
						}
						return false;
					}
				});
				return false;
			}
		});

		return returnValue;
	}
	
	/*
	 * Given an incomplete className.methodName, it parses through the data returned by Baker 
	 * and returns the complete list of matching qualified names	 * 
	 */
	static List<String> elementsMatchFromBaker(DataCollector data, String className){
		//Find elements with api_method
		List<String> returnMethodName = new ArrayList<String>();
		for(DataCollectorSchema temp:data.listClasses){
			//			if(temp.type.equals("api_method"))
			{
				for(String tempElement:temp.elements){
					if(tempElement.contains(className) == true){
						//System.out.println("ElementsMatchFromBaker:: " + tempElement);
						returnMethodName.add(tempElement);
					}						
				}
			}
		}
		return returnMethodName;
	}
	
	/*
	 * Resolve types of objects which are not resolved
	 * */
	static List<ExpressionCollector> findExpressionStatement(DataCollector data,String tempString){
		List <ExpressionCollector> returnValue = new ArrayList<ExpressionCollector>();
		List <MethodInvocation> expressionStatements = new ArrayList<MethodInvocation>();
		List<String> returnBakerType = new ArrayList<String>();
		List<String> returnArgumentBakerType = new ArrayList<String>();
		final CompilationUnit root = parseStatementsCompilationUnit(tempString);
		//Find all the ExpressionStatement node and then look for method invocation which doesn't have an assignment sign
		root.accept(new ASTVisitor() {
			public boolean visit(ExpressionStatement node) {
				node.accept(new ASTVisitor(){
					public boolean visit(MethodInvocation node){
						ASTNode temp=node;
						while(temp.getParent() != null){
							if(temp.getNodeType() == ASTNode.ASSIGNMENT)
								return false;
							temp=temp.getParent();
						}
						System.out.println("1");
						System.out.println(node.getExpression().toString());
						if(node.getExpression().resolveTypeBinding()==null){
							int noArguments= node.arguments().size();
							List<Expression> actualArguments = node.arguments();
							System.out.println(node.getExpression().toString() + " is not declared");
							String className = node.getName().toString();
							System.out.println("ClassName :: "+ className);
							List<String> elementBaker= elementsMatchFromBaker(data,className);
							//printList(elementBaker);
							System.out.println("Number of matching elements from Baker : " + elementBaker.size());
							//extra checking: checking if the arguments match (between baker elements and code)
							String tempElements = null;
							for(String e:elementBaker){
								System.out.println(noArguments);
								System.out.println(countMatches(e,",") +1);
								System.out.println(e);
								if((countMatches(e, ",") +1) != noArguments){
									System.out.println("Somethings wrong!");
									elementBaker.remove(e);
								}
								else{
									tempElements=e.split("\\."+className)[0];
									if(returnBakerType.contains(tempElements)==false){
										returnBakerType.add(tempElements);
										System.out.println(tempElements);
									}
									String tempElementsA = null;
									Pattern pattern = Pattern.compile("\\(([^\"]*)\\)");
									Matcher m = pattern.matcher(e);
									if (m.find()){ 
										tempElementsA = m.group(1);
									}
									if(returnArgumentBakerType.contains(tempElementsA)==false){
										returnArgumentBakerType.add(tempElementsA);
										System.out.println("RBT " + tempElementsA);
									}
								}
							}
							System.out.println("returnBakerType.size() : " + returnBakerType.size());
							System.out.println("returnArgumentBakerType.size() : " + returnArgumentBakerType.size());
							returnValue.add(new ExpressionCollector(node.toString(),node.getExpression().toString(),"confused","",returnBakerType,null));
							if(returnArgumentBakerType.size() ==1){
								List<String> tempArg = Arrays.asList(returnArgumentBakerType.get(0).split("\\,"));
								for(int i=0;i<tempArg.size();i++){
									System.out.println("returnArgumentBakerType :: >>>>>>>>>>>>." + actualArguments.get(i).toString());
									returnValue.add(new ExpressionCollector(node.toString(),actualArguments.get(i).toString(),tempArg.get(i),"NA"));
								}
							}


						}
						return false;
					}					
				});
				return false;
			}
		});

		return returnValue;

	}
	
	static public int countMatches(String line, String element){
		return line.length() - line.replace(element, "").length();
	}
	
	/*
	 * Trying to resolve parameters from a ExpressionStatement + MethodInvocation
	 */
	static List<ExpressionCollector> findTypeParameter(DataCollector data,String tempString){
		System.out.println("findTypeParameter :: ");
		List <ExpressionCollector> returnValue = new ArrayList<ExpressionCollector>();
		List <MethodInvocation> expressionStatements = new ArrayList<MethodInvocation>();
		List<String> returnBakerReturnType = new ArrayList<String>();
		List<String> returnBakerArguements = new ArrayList<String>();
		final CompilationUnit root = parseStatementsCompilationUnit(tempString);
		//Find all the ExpressionStatement node and then look for method invocation which doesn't have an assignment sign
		root.accept(new ASTVisitor() {
			public boolean visit(ExpressionStatement node) {
				node.accept(new ASTVisitor(){
					public boolean visit(MethodInvocation node){
						ASTNode temp=node;
						while(temp.getParent() != null){
							if(temp.getNodeType() == ASTNode.ASSIGNMENT)
								return false;
							temp=temp.getParent();
						}
						System.out.println("1 " +node.toString());
						int noArguments=-1;
						expressionStatements.add(node);
						if(node.getExpression().resolveTypeBinding() == null){
							System.out.println("Return");
							return false;
						}
						String className = node.getExpression().toString() + "." + node.getName().toString();
												System.out.println("2"+node.getExpression().resolveTypeBinding());
						System.out.println("1" + className);
						//						if(node.resolveMethodBinding() != null)
						//							System.out.println("1" + node.resolveMethodBinding().toString());
						noArguments = node.arguments().size();
						//Find from baker what is the api this corresponds to
						List<String> elementBaker= elementsMatchFromBaker(data,className);
						//printList(elementBaker);
						System.out.println("Number of arguments: " + noArguments);
						System.out.println("Number of matching elements from Baker : " + elementBaker.size());

						//extra checking: checking if the arguments match (between baker elements and code)
						String tempElements = null;
						for(String e:elementBaker){
							if((countMatches(e, ",") +1) != noArguments){
								System.out.println("Somethings wrong!");
								elementBaker.remove(e);
							}
							else{
								Pattern pattern = Pattern.compile("\\(([^\"]*)\\)");
								Matcher m = pattern.matcher(e);
								if (m.find()){ 
									tempElements = m.group(1);
								}
								if(returnBakerArguements.contains(tempElements)==false){
									returnBakerArguements.add(tempElements);
									System.out.println(tempElements);
								}

							}
						}
						System.out.println("returnBakerArguements.size() : " + returnBakerArguements.size());
						if(returnBakerArguements.size() == 1){
							System.out.println("Precision is 1");
							System.out.println(returnBakerArguements);
							String[] arg = returnBakerArguements.get(0).split("\\,");
							int count=0;
							for(Expression e2:(List<Expression>)node.arguments()){
								System.out.println("Test: " + e2.toString());
								returnValue.add(new ExpressionCollector(node.toString(),e2.toString(),arg[count++].replace(" ", ""),""));
							}
						}
						else{
							//TODO: Need to make a field in ExpressionCollector which would store all the possible types for a variable.
							System.out.println("Precision is >1");
							int count=0;
							for(int i=0;i<noArguments;i++){
								List<String> tempList = new ArrayList<String>();
								for(String bakerelement:returnBakerArguements){
									tempList.add(bakerelement.split("\\,")[count]);
								}
								returnValue.add(new ExpressionCollector(node.toString(),node.arguments().get(count).toString(),"confused","",tempList,null));
							}
						}
						//printList(returnBakerArguements);	


						return false;
					}
				});

				return true;	
			}
		});
		//returnValue.stream().forEach(p->System.out.println(p.getVariableName()+ " , "+p.getReturnType()));



		return returnValue;
	}
	
	static QualifiedNames processQualifiedNames(String name){
		QualifiedNames returnValue = new QualifiedNames();
		//Pattern pattern = Pattern.compile("\\(([A-Za-z0-9.,]+)\\)");
		Pattern pattern = Pattern.compile("\\(([^\"]*)\\)");
		Matcher m = pattern.matcher(name);
		if (m.find()){ 
			String[] tempElements = m.group(1).split(",");
			returnValue.setArguments(Arrays.asList(tempElements));
		}
		name = name.replaceAll("\\(.*\\)", "");
		String[] temp = name.split("\\.");
		returnValue.setMethodName(temp[temp.length-1]);

		String className = temp[0];
		for(int i=1;i<temp.length-1;i++)
			className = className + "." + temp[i];
		returnValue.setClassName(className);
		System.out.println("processQualifiedNames :: " +className);


		//		printList(returnValue.getArguments());
		//		System.out.println(returnValue.getMethodName());
		//		System.out.println(returnValue.getClassName());

		return returnValue;
	}
	
	/*
	 * Given class name and method, the method would return the return values of the methods
	 * Input: ClassName and Method Name
	 * Return: List of ReturnTypes. Sometime methods are overloaded.
	 */
	public static List<String> getReturnType(String className, String methodName){

		Class inspect = null;
		try {
			inspect = Class.forName(className);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Method[] methods = inspect.getDeclaredMethods();
		List<String> returnValues = new ArrayList<String>();
		for (int i = 0; i < methods.length; i++) {
			if(methods[i].getName().equals(methodName)){
				System.out.println("Method Found");
				Method methVal = methods[i];
				Class returnVal = methVal.getReturnType();
				//		        int mods = methVal.getModifiers();
				//		        String modVal = Modifier.toString(mods);
				Class[] paramVal = methVal.getParameterTypes();
				StringBuffer params = new StringBuffer();
				for (int j = 0; j < paramVal.length; j++) {
					if (j > 0)
						params.append(", ");
					params.append(paramVal[j].getName());
				}
				if(returnValues.contains(returnVal.getName())!=true){
					returnValues.add(returnVal.getName()+" - " + params);
					System.out.println("Return Type: " +returnVal.getName());
				}
				//		        
				//		        System.out.println("Method: " + methVal.getName() + "()");
				//		        System.out.println("Modifiers: " + modVal);
				//		        System.out.println("Return Type: " + returnVal.getName());
				//		        System.out.println("Parameters: " + params + "\n");
			}
		}
		if(returnValues.size()>1)
			//System.out.println(getMethodName(0) + "I am confused!");
			;
		return returnValues;
	}

	
	static List<ExpressionCollector> findLeftNodeType(DataCollector data, String tempString){

		//		String source = "if(cn == null){\n"
		//		+ "String driver = \"com.mysql.jdbc.Driver\"; \n"
		//		+ "Class.forName(driver); \n"
		//		+ "dbHost = \"jdbc:mysql://\"+dbHost;\n"
		//		+ "cn = DriverManager.getConnection(dbHost,dbUser,dbPassword);\n"
		//		+ "System.out.println(\"test\");\n"
		//		+ "}\n";
		List<String> tempList = new ArrayList<String>();
		List<String> returnBakerReturnType = new ArrayList<String>();
		List<String> returnBakerArguements = new ArrayList<String>();
		List <ExpressionCollector> returnValue = new ArrayList<ExpressionCollector>();
		List <Expression> expressionStatement = new ArrayList<Expression>();
		//String tempString = enclosedClasses(source);
		final CompilationUnit root = parseStatementsCompilationUnit(tempString);
		if(root == null)
			System.out.println("Something is wrong!!");
		root.accept(new ASTVisitor() {
			public boolean visit(Assignment node) {
				System.out.println("findLeftNodeType >>>>>>>>>> :: " + node.toString());
				if(node.getRightHandSide().resolveConstantExpressionValue() != null){
					System.out.println("vivek " + node.getRightHandSide().resolveTypeBinding().getTypeDeclaration().getQualifiedName());
					returnValue.add(new ExpressionCollector(node.toString(),node.getLeftHandSide().toString(), node.getRightHandSide().resolveTypeBinding().getTypeDeclaration().getQualifiedName(),"NA"));
				}
				else
					expressionStatement.add(node.getRightHandSide());
				return true;	
			}
		});
		for(Expression e: expressionStatement){
			Expression node = e;
			System.out.println("findLeftNodeType expression :: " + node.toString());
			node.accept(new ASTVisitor(){
			
				public boolean visit(MethodInvocation node){
					int noArguments=-1;
					String className =null;
					if(node.getExpression().resolveTypeBinding()!=null)
						className = node.getExpression().toString() + "." + node.getName().toString();
					else
						className = node.getName().toString();
					//					System.out.println(node.getName().toString());
					//					System.out.println("findLeftNodeType arguments :: "+node.arguments());
					//					System.out.println("findLeftNodeType arguments size :: "+node.arguments().size());
					noArguments = node.arguments().size();
					//Find from baker what is the api this corresponds to
					List<String> elementBakerReturnType = elementsMatchFromBaker(data,className);
					for(String element:elementBakerReturnType){
						//System.out.println(element);
						//parse the api returned by the baker and break it down to className, methodName and parameterList
						QualifiedNames tempQN = processQualifiedNames(element);
						//find the return type of api based on the className and methodName
						//TODO: can pass parameters as well then there would be only one returnValue rather than a list of returnValues
						List<String> tempReturnType = getReturnType(tempQN.className, tempQN.methodName);
						for(String element2:tempReturnType){
							System.out.println("Inside :: " +element2);
							if(returnBakerReturnType.contains(element2.split("\\-")[0])==false){
								returnBakerReturnType.add(element2.split("\\-")[0]);
							}
							if(returnBakerArguements.contains(element2.split("\\-")[1]) ==false 
									&& (countMatches(element2.split("\\-")[1],",") == (noArguments -1))){
								returnBakerArguements.add(element2.split("\\-")[1]);
							}
						}
					}
					System.out.println("4-Iamhere"+returnBakerReturnType.size());
					//					System.out.println("5-Iamhere"+returnBakerArguements.size());
					if(returnBakerReturnType.size() > 1 && returnBakerArguements.size() == 1){
						System.out.println("findLeftNodeType:: Different API but same parameter");
						returnValue.add(new ExpressionCollector(node.toString(),((Assignment)node.getParent()).getLeftHandSide().toString(), "confused",returnBakerArguements.get(0), returnBakerReturnType,returnBakerArguements));
					}
					else if(returnBakerReturnType.size() > 1 && returnBakerArguements.size() > 1){
						System.out.println("findLeftNodeType:: Different API and different parameter");
						returnValue.add(new ExpressionCollector(node.toString(),((Assignment)node.getParent()).getLeftHandSide().toString(), "confused","confused", returnBakerReturnType,returnBakerArguements));
					}
					else if(returnBakerReturnType.size() == 1 && returnBakerArguements.size() == 1){
						System.out.println("findLeftNodeType::Perfect");
						returnValue.add(new ExpressionCollector(node.toString(),((Assignment)node.getParent()).getLeftHandSide().toString(), returnBakerReturnType.get(0),returnBakerArguements.get(0)));
						List<Expression> tempArguments = node.arguments();
						//printList(tempArguments);
						List<String> tempReturnArguments = Arrays.asList(returnBakerArguements.get(0).replace(" ", "").split("\\,"));
						//printList(tempReturnArguments);
						if(tempArguments.size() == tempReturnArguments.size())
							for(int index=0;index<tempArguments.size();index++)
								returnValue.add(new ExpressionCollector(node.toString(),tempArguments.get(index).toString(),tempReturnArguments.get(index),"NA"));
						else
							System.out.println("Something is wrong!");
					}
					else if(returnBakerReturnType.size() == 1 && returnBakerArguements.size() == 0){
						System.out.println("findLeftNodeType::Perfect");
						returnValue.add(new ExpressionCollector(node.toString(),((Assignment)node.getParent()).getLeftHandSide().toString(), returnBakerReturnType.get(0),"NA"));
					}
					else if(returnBakerReturnType.size() == 1 && returnBakerArguements.size() > 1){
						System.out.println("findLeftNodeType::Perfect");
						returnValue.add(new ExpressionCollector(node.toString(),((Assignment)node.getParent()).getLeftHandSide().toString(), returnBakerReturnType.get(0),"confused", returnBakerReturnType,returnBakerArguements));
					}
					else if(returnBakerReturnType.size() == 0){
						returnValue.add(new ExpressionCollector(node.toString(),((Assignment)node.getParent()).getLeftHandSide().toString(), "unresolved","NA"));
					}
					return false;
				}
				/*
				 * Would return all the types of the operands in a Infix Expression
				 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.InfixExpression)
				 */
				public boolean visit(InfixExpression node){

					System.out.println("=============================Infix Expression : " +node.toString());
					//System.out.println(node.hasExtendedOperands());
					//System.out.println("================================== "+ node.getLeftOperand().toString());
					if(node.getLeftOperand().resolveTypeBinding() != null){
						//System.out.println(node.getLeftOperand().resolveTypeBinding().getTypeDeclaration().getName());
						tempList.add(node.getLeftOperand().resolveTypeBinding().getTypeDeclaration().getQualifiedName());
					}
					else;
					//System.out.println(node.getLeftOperand().resolveTypeBinding());
					//System.out.println("================================== "+ node.getRightOperand().toString());
					if(node.getRightOperand().resolveTypeBinding() != null){
						//System.out.println(node.getRightOperand().resolveTypeBinding().getTypeDeclaration().getName());
						tempList.add(node.getRightOperand().resolveTypeBinding().getTypeDeclaration().getQualifiedName());
					}
					else;
					//System.out.println(node.getRightOperand().resolveTypeBinding());
					//System.out.println(node.getRightOperand().resolveTypeBinding());
					List<Expression> extendedOperands = node.extendedOperands();
					for (Expression element : extendedOperands) {
						//System.out.println(element.toString());
						if(element.resolveTypeBinding()!=null){
							//System.out.println(element.resolveTypeBinding().getTypeDeclaration().getName());
							tempList.add(element.resolveTypeBinding().getTypeDeclaration().getQualifiedName());
						}
						else;
						//System.out.println(element.resolveTypeBinding());
					}
					if(tempList.size()>1){
						returnValue.add(new ExpressionCollector(node.toString(),((Assignment)node.getParent()).getLeftHandSide().toString(), "confused","NA"));
					}
					else if(tempList.size() == 0){
						returnValue.add(new ExpressionCollector(node.toString(),((Assignment)node.getParent()).getLeftHandSide().toString(), "unresolved","NA"));
					}
					else{//tempList.size() ==1 : I am assuming that InfixExpression would have all elements of same type
						List<String> elementInfix = findElementsInfixExpression(node);
						returnValue.add(new ExpressionCollector(node.toString(),((Assignment)node.getParent()).getLeftHandSide().toString(), tempList.get(0),"NA"));
						for(String element:elementInfix) //No need to check if the element is a part of undeclared variable. Would be checked in mergeExpressionCollector()
							returnValue.add(new ExpressionCollector(node.toString(),element, tempList.get(0),"NA"));

					}

					return false;
				}
			});


		}
		return returnValue;
	}
	
	static List<String> findElementsInfixExpression(InfixExpression node){
		List<String> returnValue = new ArrayList<String>();
		System.out.println("findElementsInfixExpression >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		if(node!=null){
			returnValue.add(node.getLeftOperand().toString());
			returnValue.add(node.getRightOperand().toString());
			List<Expression> extendedOperands = node.extendedOperands();
			for (Expression element : extendedOperands) {
				returnValue.add(element.toString());
			}
		}
		return returnValue;
	}
	static List<Variables> mergeExpressionCollector(List<ExpressionCollector>ec, List<Variables>undeclaredVariables, String source){
		//System.out.println("mergeExpressionCollector :: >>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		ec.stream().forEach(p->p.printData());
		System.out.println("=============================>>>>>>>>>>>>>>>>>>>=======================");
		for(Variables element: undeclaredVariables){
			for(ExpressionCollector e:ec){
				//				System.out.println("mergeExpressionCollector:" + element.name + " , " + e.getVariableName());
				//				System.out.println("mergeExpressionCollector12:" + e.getReturnType());
				if(element.name.equals("string1") && e.getVariableName().equals("string1"))
					System.out.println("It's here");
				if(element.name.equals(e.getVariableName())==true && (e.getReturnType().equals("confused")==false) && (e.getReturnType().equals("unresolved") ==false)){
					if(element.variableType.contains(e.getReturnType()) == false){
						element.variableType.add(e.getReturnType());
						element.packageImport = convertClasstoPackage2(((element.variableType).get(0)).replace(" ",""));	
						System.out.println(element.name + " , " + element.variableType + " , " + element.packageImport);
					}
				}
				else if(element.name.equals(e.getVariableName())==true && (e.getReturnType().equals("confused")==true) && (e.getReturnType().equals("unresolved") ==false)){
					element.variableType=e.getReturnTypeList();
					//element.packageImport = convertClasstoPackage2(((element.variableType).get(0)).replace(" ",""));	
					System.out.println(element.name + " , " + element.variableType + " , " + element.packageImport);
				}
			}
		}
		return undeclaredVariables;
	}
	
	public static Integer countList(List<Integer> list, Integer element){
		Integer count=0;
		for(Integer e:list)
			if(e==element)
				count++;
		return count;
	}
	
	static List<Variables> getInformationFromParameter(List<ExpressionCollector>ec, List<Variables>undeclaredVariables, String source){
		for(Variables element: undeclaredVariables){
			for(ExpressionCollector e:ec){
				//System.out.println("getInformationFromParameter >>>>>>>>>>>>> : " + e.getVariableName() + " " + element.name);
				//The type of element which was a undeclared variable before and the return type is not confused (can be more than one type) or unresolved
				if(element.name.equals(e.getVariableName())==true && (e.getReturnType().equals("confused")==false) && (e.getReturnType().equals("unresolved") ==false)){
					//TODO: try to find variable types from API signature returned by Baker
					if(e.getArgumentList()!= null && e.getArgumentList().size()>=1){ //Argument has more than or equal to one option
						//System.out.println("getInformationFromParameter Expression :: " +e.getExpression());
						//find all the variables in the expression
						final CompilationUnit root = parseStatementsCompilationUnit(source);
						root.accept(new ASTVisitor() {
							public boolean visit(MethodInvocation node) { //it has to be a method invocation
							//	System.out.println("getInformationFromParameter : " + node.toString());
								if(e.getExpression().contains(node.getName().toString())==true){ //find the method invocation node which contains the expression
									//System.out.println("getInformationFromParameter Found Expression :: " + node.arguments()); //argument of the method invocation
									int count =0;
									List<String> codeArguments = new ArrayList<String>();
									for(SimpleName name: (List<SimpleName>)node.arguments()){
										count++;
										//System.out.println("getInformationFromParameter Test 1 "+ name.toString());
										for(Variables tempElement: undeclaredVariables){ 
											if(tempElement.name.equals(name.toString())==true){//checking if the arguments are undeclared variables
												if(tempElement.variableType.size() == 1) //variable type of the undeclared Variable was resolved using fillUndeclaredVariablesFromBaker()
													codeArguments.add(tempElement.variableType.get(0));
												else
													codeArguments.add("X"); //if it has been not resolved add 'X' to the array
												//System.out.println("getInformationFromParameter : tempElement.name : " + tempElement.name + ", tempElement.variableType : "+ tempElement.variableType + ", argument number : " + count) ;
											}
										}
									}
									//System.out.println("Code Arguments >>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
									//printList(codeArguments);
									//System.out.println("Expression Collector Arguments >>>>>>>>>>>>>>>>>>>>>>>>>>>>");
									//Trying to see if some already resolved parameters can be used to find other parameters using resolved API
									List <Integer> similarity = new ArrayList<Integer>();
									for(String argument: e.getArgumentList()){
										List <String> expressionCollectorArg = Arrays.asList(argument.replace(" ", "").split("\\,"));
										//printList(expressionCollectorArg);
										if(codeArguments.size() != expressionCollectorArg.size()){
											//System.out.println("getInformationFromParameter: Something is wrong!!");
											return false;
										}
										else{
											int matches=0;
											for(int counter = 0; counter < codeArguments.size(); counter++) {
												//System.out.println("getInformationFromParameter:codeArgument "+codeArguments.get(counter));
												if(codeArguments.get(counter).equals(expressionCollectorArg.get(counter)) == true){
													//System.out.println("getInformationFromParameter: For " +e.getExpression() + "argument number " + counter + " matches");
													matches++;
												}
											}
											similarity.add(matches); //Find out how many signature matches (matches <= codeArguments.size() and matches <= expressionCollectorArg.size())
										}
									}
									//System.out.println("Maximum similarity : " +(float)Collections.max(similarity)/codeArguments.size());
									//System.out.println("Collisions: "+ countList(similarity, Collections.max(similarity)));
									if(countList(similarity, Collections.max(similarity)) == 1){
										int counter = 0;
										for(SimpleName name: (List<SimpleName>)node.arguments()){
											counter++;
											for(Variables tempElement: undeclaredVariables){
												if(tempElement.name.equals(name.toString())==true){
													String temp = (e.getArgumentList().get(similarity.indexOf(Collections.max(similarity))));
													if(tempElement.variableType.contains(temp.replace(" ", "").split("\\,")[counter-1])== false)
														tempElement.variableType.add(temp.replace(" ", "").split("\\,")[counter-1]);
												}
											}
										}
									}
								}
								return true;
							}
						});
					}
				}
			}
		}
		return undeclaredVariables;
	}

	
	static List<Variables> fillLineNumber(List<Variables> undeclaredVariables, String source){
		final CompilationUnit root = parseStatementsCompilationUnit(source);
		for(Variables element:undeclaredVariables){
			root.accept(new ASTVisitor() {
				public boolean visit(SimpleName node) {
					if(node.toString().equals(element.name)){
						if(element.lineNumber > root.getLineNumber(node.getStartPosition()) || element.lineNumber == -1 )
							element.lineNumber = root.getLineNumber(node.getStartPosition());
						return true;
					}
					else
						return true;
				}
			});
		}
		return undeclaredVariables;
	}
	
	static String getVariablesInScope(String source,String VariableName){
		List<String> returnName = new ArrayList<String>();
		final CompilationUnit root = parseStatementsCompilationUnit(source);

		root.accept(new ASTVisitor() {

			public boolean visit(SimpleName node) {
				if(node.toString().equals(VariableName)){
					ASTNode temp =(ASTNode)node;
					while(temp.getNodeType() != ASTNode.METHOD_DECLARATION)
						temp=(ASTNode)temp.getParent();
					//System.out.println(((MethodDeclaration)temp).getName());
					if(returnName.contains(((MethodDeclaration)temp).getName().toString())==false)
						returnName.add(((MethodDeclaration)temp).getName().toString());
				}
				return true;
			}
		});
		if(returnName.size() >1){
			System.out.println("Something's wrong");
			return "";
		}
		else
			return returnName.get(0);
	}
	
	static boolean ifMethodExisit(CompilationUnit cu, String methodName){
		List<String> methodNames = new ArrayList<String>();
		cu.accept(new ASTVisitor() {
			public boolean visit(MethodDeclaration node){
				methodNames.add(node.getName().toString());
				return false;
			}
		});
		if(methodNames.contains(methodName)==true)
			return true;
		else
			return false;
	}
	/*
	 * Element exist in list of Variables
	 */
	static boolean ifExist(List<Variables> list, String variableName){
		for(Variables element: list){
			if(element.name.equals(variableName))
				return false;
		}
		return true;
	}
	
	static List<Variables> getVariablesAndImport(String source,String methodName){

		List<Variables> returnDeclared = new ArrayList<Variables>();
		//source = enclosedClasses(source);

		final CompilationUnit root = parseStatementsCompilationUnit(source);
		if(ifMethodExisit(root, methodName)==false){
			System.out.println("Method doesn't exist!");
			return null;
		}

		root.accept(new ASTVisitor() {
			public boolean visit(ImportDeclaration node){
				//System.out.println(node.getName().getFullyQualifiedName());
				return true;
			}
			public boolean visit(VariableDeclarationFragment node) {
				//System.out.println("VariableDeclarationFragment " +node.toString());
				if(node.resolveBinding() != null){
					ASTNode temp = (ASTNode)node;
					//System.out.println(temp.toString());
					while(temp.getNodeType() != ASTNode.VARIABLE_DECLARATION_STATEMENT){
						//System.out.println(temp.toString());
						temp=temp.getParent();
						//for cases where there are only VariableDeclarationFragments
						if(temp == null){
							temp = (ASTNode)node;
							if(ifExist(returnDeclared, node.getName().toString())==true)
								returnDeclared.add(new Variables(node.getName().toString(),"variable",((VariableDeclarationFragment)temp).resolveBinding().getType().getQualifiedName(),"","NA",root.getLineNumber(temp.getStartPosition())));
							//System.out.println(((VariableDeclarationFragment)temp).resolveBinding().getType().getName().toString());
							return false;
						}
					}
					//System.out.println("((VariableDeclarationStatement) temp).getType().toString() : " + ((VariableDeclarationStatement) temp).getType().resolveBinding().getQualifiedName());
					if(ifExist(returnDeclared, node.getName().toString())==true)
						returnDeclared.add(new Variables(node.getName().toString(),"variable",((VariableDeclarationStatement) temp).getType().resolveBinding().getQualifiedName(),"","NA",root.getLineNumber(temp.getStartPosition())));
				}
				return false;
			}
			public boolean visit(SingleVariableDeclaration node) {

				//System.out.println("SingleVariableDeclaration " +node.toString());
				if(node.resolveBinding() != null){
					//System.out.println("!!!"+node.getType().toString());
					if(ifExist(returnDeclared, node.getName().toString())==true)
						returnDeclared.add(new Variables(node.getName().toString(),"variable",node.getType().toString(),"","NA",root.getLineNumber(node.getStartPosition())));
				}
				return true;
			}
			public boolean visit(VariableDeclaration node) {
				//System.out.println("VariableDeclaration " +node.toString());
				if(node.resolveBinding() != null){if(node.resolveBinding() != null){
					ASTNode temp = (ASTNode)node;
					while(temp.getNodeType() != ASTNode.VARIABLE_DECLARATION_STATEMENT)
						temp=temp.getParent();
					if(ifExist(returnDeclared, node.getName().toString())==true)
						returnDeclared.add(new Variables(node.getName().toString(),"variable",((VariableDeclarationStatement) temp).getType().resolveBinding().getQualifiedName(),"","NA",root.getLineNumber(temp.getStartPosition())));}
				}
				return false;     
			}
			public boolean visit(MethodDeclaration node){
				//System.out.println("MethodDeclaration ");
				if(node.getName().toString().equals(methodName)){
					node.accept(new ASTVisitor() {		            
						public boolean visit(VariableDeclarationFragment node) {
							if(node.resolveBinding() != null){
								ASTNode temp = (ASTNode)node;
								while(temp.getNodeType() != ASTNode.VARIABLE_DECLARATION_STATEMENT)
									temp=temp.getParent();
								if(ifExist(returnDeclared, node.getName().toString())==true)
									returnDeclared.add(new Variables(node.getName().toString(),"variable",((VariableDeclarationStatement) temp).getType().resolveBinding().getQualifiedName(),"","NA",root.getLineNumber(temp.getStartPosition())));
							}
							return false;
						}
						public boolean visit(SingleVariableDeclaration node) {
							if(node.resolveBinding() != null){
								if(ifExist(returnDeclared, node.getName().toString())==true)
									returnDeclared.add(new Variables(node.getName().toString(),"variable",node.getType().resolveBinding().getQualifiedName(),"","NA",root.getLineNumber(node.getStartPosition())));
							}
							return false;
						}
						public boolean visit(VariableDeclaration node) {
							if(node.resolveBinding() != null){
								if(node.resolveBinding() != null){
									ASTNode temp = (ASTNode)node;
									while(temp.getNodeType() != ASTNode.VARIABLE_DECLARATION_STATEMENT)
										temp=temp.getParent();
									if(ifExist(returnDeclared, node.getName().toString())==true)
										returnDeclared.add(new Variables(node.getName().toString(),"variable",((VariableDeclarationStatement) temp).getType().resolveBinding().getQualifiedName(),"","NA",root.getLineNumber(temp.getStartPosition())));
								}
							}
							return false;
						}
					});
				}
				return true;
			}
		});
		//				for(Variables element:returnDeclared){
		//					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>------------------------------------");
		//					System.out.println(element.name);
		//		//			System.out.println(element.type);
		//					System.out.println(element.variableType);
		//		//			System.out.println(element.packageImport);
		//		//			System.out.println(element.returnType);	
		//		//			System.out.println(element.lineNumber);
		//					System.out.println(element.lineNumber);
		//		//			System.out.println("------------------------------------");
		//				}
		return returnDeclared;
	}
	
	protected static String getPossibleNames(String focusVariable){
		if(undeclaredVariables == null)return null;
		else{

			String source = newText1;
			for(Variables element:SampleAction.undeclaredVariables){
				if(element.name.equals(focusVariable)==false)
					continue;

				List<Variables> rankingList = new ArrayList<Variables>();
				List<Variables> rankingTypeList = new ArrayList<Variables>();
				if(element.type == "variable"){
					int count=0;
					System.out.println("#####################################");
					System.out.println("Undeclared Variables: " +element.name + " , " + element.variableType);
					List<Variables> declaredVariables =new ArrayList<Variables>(); 
					String methodName = getVariablesInScope(source, element.name);
					declaredVariables = getVariablesAndImport(source,methodName );
					declaredVariables = fillLineNumber(declaredVariables, source);

					if(element.variableType.size()==1){
						for(Variables e:declaredVariables){
							rankingTypeList.add(e);
							if((e.variableType.get(0).replace(" ", "")).equals((element.variableType.get(0)).replace(" ", "")) == true){
								rankingList.add(e);
								count++;
							}
						}
						if(count==0)
							return("Variable " + element.name +" needs declaration of type " + element.variableType );

						else{
							SelectionAlgorithm sEditDistance = new SelectionAlgorithm(element, rankingList);
							List<RankingSkeleton> editDistance = sEditDistance.editDistance();
							SelectionAlgorithm sSemanticDistance = new SelectionAlgorithm(element, rankingList);
							List<RankingSkeleton> semanticDistance = sSemanticDistance.semanticDistance();
							SelectionAlgorithm sNavigationDistance = new SelectionAlgorithm(element, rankingList);
							List<RankingSkeleton> navigationDistance = sNavigationDistance.navigationDistance();
							SelectionAlgorithm sTypeDistance = new SelectionAlgorithm(element, rankingTypeList);
							List<RankingSkeleton> typeDistance = sTypeDistance.typeDistance();
							StringBuilder stringBuilder = new StringBuilder();
							for(int xx=0;xx<editDistance.size();xx++){
								stringBuilder.append(editDistance.get(xx).candidateDeclaredVariable);
								stringBuilder.append("\n");
							}
							return stringBuilder.toString();

						}
					}

				}
				else
					return "wrong2";
			}


			return "content not found";
		}
	}
	
	protected static List<Variables> getSuggestions(String source){
		DataCollector data = null;
		source = addReturnStatements(source);
		List<Variables> undeclaredVariables = checkVariableDeclaration(source);

		try {
			data =  getTypesFromBaker(source);
		} catch (FileNotFoundException | UnsupportedEncodingException| InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Done!");
		List<String> importStatements = getImportStatement(source);
		List<ExpressionCollector>hints = extractHints(source);
		List<String> printImportStatements = new ArrayList<String>();
		List<ExpressionCollector>returnValue4 = findReturnStatements(source); //Return Values
		System.out.println("=============================findReturnStatements=====================================================");
		returnValue4.stream().forEach(p->p.printData());
		System.out.println("===================================================================================");
		List<ExpressionCollector>returnValue3 = findExpressionStatement(data,source);
		System.out.println("===============================findExpressionStatement====================================================");
		returnValue3.stream().forEach(p->p.printData());
		System.out.println("===================================================================================");
		List<ExpressionCollector>returnValue2 = findTypeParameter(data,source); //MethodInvocation as expression
		System.out.println("=================================findTypeParameter==================================================");
		returnValue2.stream().forEach(p->p.printData());
		System.out.println("===================================================================================");
		List<ExpressionCollector>returnValue =  findLeftNodeType(data,source); //Assignment
		System.out.println("=====================================findLeftNodeType==============================================");
		returnValue.stream().forEach(p->p.printData());
		System.out.println("===================================================================================");

		//TODO: Need to check if addALL actually works for us!

		returnValue.addAll(returnValue4);
		returnValue.addAll(returnValue2);
		returnValue.addAll(returnValue3);
		System.out.println("===================================================================================");
		returnValue.stream().forEach(p->p.printData());
		System.out.println("===================================================================================");

		undeclaredVariables = fillUndeclaredVariablesFromBaker(data,undeclaredVariables);
		undeclaredVariables = mergeExpressionCollector(returnValue,undeclaredVariables,source);
		
		for(Variables element:undeclaredVariables){
			System.out.println("-----------------###AfterMergeExpressionCollector###----------------");
			System.out.println(element.name);
			System.out.println(element.type);
			System.out.println(element.variableType);
			System.out.println(element.packageImport);
			System.out.println(element.lineNumber);
		}
		
		undeclaredVariables = getInformationFromParameter(returnValue,undeclaredVariables,source);
		undeclaredVariables = fillLineNumber(undeclaredVariables, source);
		for(int i=0;i<10;i++)
			System.out.println();
		
		return undeclaredVariables;
		
//		for(Variables element:undeclaredVariables){
//			List<Variables> rankingList = new ArrayList<Variables>();
//			List<Variables> rankingTypeList = new ArrayList<Variables>();
//			if(element.type == "variable"){
//				int count=0;
//				System.out.println("#####################################");
//				System.out.println("Undeclared Variables: " +element.name + " , " + element.variableType);
//				List<Variables> declaredVariables =new ArrayList<Variables>(); 
//				String methodName = getVariablesInScope(source, element.name);
//				declaredVariables = getVariablesAndImport(source,methodName );
//				declaredVariables = fillLineNumber(declaredVariables, source);
//
//				if(element.variableType.size()==1){
//					for(Variables e:declaredVariables){
//						rankingTypeList.add(e);
//						if((e.variableType.get(0).replace(" ", "")).equals((element.variableType.get(0)).replace(" ", "")) == true){
//							rankingList.add(e);
//							count++;
//						}
//					}
//					if(count==0)
//						System.out.println("Variable " + element.name +" needs declaration of type " + element.variableType );
//					else{
//						SelectionAlgorithm sEditDistance = new SelectionAlgorithm(element, rankingList);
//						List<RankingSkeleton> editDistance = sEditDistance.editDistance();
//						SelectionAlgorithm sSemanticDistance = new SelectionAlgorithm(element, rankingList);
//						List<RankingSkeleton> semanticDistance = sSemanticDistance.semanticDistance();
//						SelectionAlgorithm sNavigationDistance = new SelectionAlgorithm(element, rankingList);
//						List<RankingSkeleton> navigationDistance = sNavigationDistance.navigationDistance();
//						SelectionAlgorithm sTypeDistance = new SelectionAlgorithm(element, rankingTypeList);
//						List<RankingSkeleton> typeDistance = sTypeDistance.typeDistance();
//						Object[][] table = list2Table(editDistance,semanticDistance,navigationDistance,typeDistance);
//						for (final Object[] row : table) {
//						    System.out.format("%25s%25s%25s%25s%25s\n", row);
//						}
//					}
//						
//				}
//				else
//					System.out.println("Variable " + element.name +" could not be resolved" );
//			}
//			else if(element.type == "type" && element.packageImport != "" && element.packageImport != "NA" ){
//				if(importStatements.contains(element.packageImport) == false)
//					printImportStatements.add("Add Imports : " +element.packageImport + " "+element.name);
//			}
//			else if(element.type == "type")
//				System.out.println("Type " + element.name + " could not be resolved");
//		}
//		System.out.println("#####################################");
//		System.out.println("Add following imports");
//		for(String element:printImportStatements)
//			System.out.println(element);
//		System.out.println("#####################################");
//		System.out.println("Consider the following hints");
//		for(ExpressionCollector element:hints){
//			System.out.println(element.getConstantExpression() + " might need to be changed in the expression " + element.getExpression());
//		}
	
	}
	public static String[][] list2Table(List<RankingSkeleton> editDistance, List<RankingSkeleton> semanticDistance, List<RankingSkeleton> navigationDistance, List<RankingSkeleton> typeDistance){
		 Object[][] table = new String[editDistance.size()+1][];
		 table[0]=new String[]{"Rank","NavigationDistance","StringEditDistance","SemanticDistance","TypeDistance"};
		 for(int i=0;i<(editDistance.size());i++)
				 table[i+1] = new String[]{String.valueOf(i+1),navigationDistance.get(i).candidateDeclaredVariable,editDistance.get(i).candidateDeclaredVariable,semanticDistance.get(i).candidateDeclaredVariable,typeDistance.get(i).candidateDeclaredVariable};
		 return (String[][]) table;
	}
	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		
//========================================================== +Find data in the clipboard
		
        Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
        String data=null;
        // print the last copied thing
        Transferable t = clipBoard.getContents(null);
        if (t.isDataFlavorSupported(DataFlavor.stringFlavor))
			try {
				data=(String) t.getTransferData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
        MessageDialog.openInformation(
                window.getShell(),
                "Clipboard data",
                data);
//========================================================== -Find data in the clipboard

//========================================================== +Extract code from editor
            try {               
                IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

                
                if ( part instanceof ITextEditor ) {
                    final ITextEditor editor = (ITextEditor)part;
                    IDocumentProvider prov = editor.getDocumentProvider();
                    IDocument doc = prov.getDocument( editor.getEditorInput() );
                	newText1 = doc.get();
                    MessageDialog.openInformation(
                            window.getShell(),
                            "editor",
                            newText1);
                    
//========================================================== -Extract code from editor
                   // getVariables(editor,newText1);
                    undeclaredVariables = getSuggestions(newText1);
          
                    
                    
//========================================================== +Selection                    
                    ISelection sel = editor.getSelectionProvider().getSelection();
                    if ( sel instanceof TextSelection ) {
                        final TextSelection textSel = (TextSelection)sel;
                        String newText = "/*" + textSel.getText() + "*/";
                        MessageDialog.openInformation(
                                window.getShell(),
                                "selection",
                                newText);
                        doc.replace( textSel.getOffset(), textSel.getLength(), newText );
                    }
                }else{
                    MessageDialog.openInformation(
                            window.getShell(),
                            "AnirudhPlugin",
                            "Not ITextEditor");
                }
//========================================================== -Selection
            } catch ( Exception ex ) {
                ex.printStackTrace();
            }
}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}