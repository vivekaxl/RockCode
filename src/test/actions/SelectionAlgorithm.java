package test.actions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;




//import asm_test.MainMethod;
import rita.wordnet.RiWordnet;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;


public class SelectionAlgorithm {
	final double minisculeIncrement =  0.00000000001;
	final double bigNumber = 99;
	private Variables unDeclaredVariable;
	List<Variables> candidateVariables;
	private Map<Double,Variables> rankedList = new HashMap<Double,Variables>();
	
	public SelectionAlgorithm(Variables unDeclaredVariables, List<Variables> candidateVariables){
		this.candidateVariables = new ArrayList();
		this.unDeclaredVariable = unDeclaredVariables;
		this.candidateVariables = candidateVariables;
	}
	
	/*
	 * Prints a List
	 */
	public static  void printList (List<?> list2) 
	{      
		List<?> list= list2;
		for (int i = 0; i < list.size();  i++) 
			System.out.print(list2.get(i)+"\n");
		System.out.println();
	}
	
	List<RankingSkeleton> convertToList(Map<Double,Variables> map){
		List<RankingSkeleton> returnList = new ArrayList<RankingSkeleton> ();
		Set<Entry<Double, Variables>> set = map.entrySet();
        Iterator iterator = set.iterator();
        int rank=0;
        while(iterator.hasNext()) {
        	rank++;
        	Map.Entry me = (Map.Entry)iterator.next();
        	returnList.add(new RankingSkeleton(unDeclaredVariable.name, ((Variables)me.getValue()).name, rank));
        }
//        System.out.println("++++++++++++++++++++++++++++++++");
//        for(RankingSkeleton element: returnList){
//        	System.out.println(element.candidateDeclaredVariable);
//        }
//        System.out.println("++++++++++++++++++++++++++++++++");
        return returnList;
	}
	
	public List<RankingSkeleton> editDistance(){
        AbstractStringMetric metric = new Levenshtein();
        for(Variables element:candidateVariables){
			
			double result = 1 - metric.getSimilarity(unDeclaredVariable.name, element.name);
			while(rankedList.containsKey(result)!=false){
				//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>Conflict!!");
				if((rankedList.get(result).lineNumber - unDeclaredVariable.lineNumber <= element.lineNumber - unDeclaredVariable.lineNumber))
					result+=minisculeIncrement;
				else
					result-=minisculeIncrement;
			}
			//System.out.println("Undeclared Variables: " +unDeclaredVariable.name+" Candidates : "+element.name + " Similarity: "+result);
			rankedList.put(result,element);			
		}
	
		Map<Double,Variables> sortedRankedList = new  TreeMap<Double,Variables>(rankedList);		
		return convertToList(sortedRankedList);
	}
	
	public static List<String> FindSubstring(RiWordnet wordnet ,String string){
		int length = string.length(); 
		String sub = null;
		List<String> returnString = new ArrayList<String>();
		for( int c = 0 ; c < length ; c++ )
	      {
	         for( int j = 1 ; j <= length - c ; j++ )
	         {
	            sub = string.substring(c, c+j);
	            if(wordnet.exists(sub) && sub.length() >3 ){
	            	//System.out.println(sub);
	            	returnString.add(sub);
	            }
	         }
	      }
		return returnString;
	}
	
	public List<RankingSkeleton> navigationDistance(){
		for(Variables element:candidateVariables){
			//System.out.println("Undeclared Variables: " +unDeclaredVariable.name+ "Line Number: "+ unDeclaredVariable.lineNumber+" Candidates : "+element.name + " line Number: "+ element.lineNumber);
			double result = Math.abs(unDeclaredVariable.lineNumber - element.lineNumber);
			while(rankedList.containsKey(result)!=false){
				//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>Conflict!!");
				if((rankedList.get(result).lineNumber - unDeclaredVariable.lineNumber <= element.lineNumber - unDeclaredVariable.lineNumber))
					result+=minisculeIncrement;
				else
					result-=minisculeIncrement;
			}
			//System.out.println("Undeclared Variables: " +unDeclaredVariable.name+" Candidates : "+element.name + " Similarity: "+result);
			rankedList.put(result,element);	
		}
		Map<Double,Variables> sortedRankedList = new  TreeMap<Double,Variables>(rankedList);		
		return convertToList(sortedRankedList);
	}
	
	public List<RankingSkeleton> semanticDistance(){
        for(Variables element:candidateVariables){
			
			double result = 1 - subsemanticDistance(unDeclaredVariable.name, element.name);
			while(rankedList.containsKey(result)!=false){
				//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>Conflict!!");
				if((rankedList.get(result).lineNumber - unDeclaredVariable.lineNumber <= element.lineNumber - unDeclaredVariable.lineNumber))
					result+=minisculeIncrement;
				else
					result-=minisculeIncrement;
			}
			//System.out.println("Undeclared Variables: " +unDeclaredVariable.name+" Candidates : "+element.name + " Similarity: "+result);
			rankedList.put(result,element);			
		}
	
		Map<Double,Variables> sortedRankedList = new  TreeMap<Double,Variables>(rankedList);		
		return convertToList(sortedRankedList);
	}
	
	public float subsemanticDistance(String start, String end){

		RiWordnet wordnet = new RiWordnet(null);

		String pos;
	
		List<String> listStart = FindSubstring(wordnet, start);
		List<String> listEnd = FindSubstring(wordnet, end);
		List<Float> distance = new ArrayList<Float>();
		try {
			for (String s : listStart)
				for (String e : listEnd) {
					pos = wordnet.getBestPos(s);
					float dist = 1 - wordnet.getDistance(s, e, pos);
					distance.add(dist);
					//System.out.println(s + " and " + e + " are related by a distance of: " + (dist));
				}
			return Collections.max(distance);
		} catch (NoSuchElementException e) {
			System.out.println("No Semantic Similarity");
			return 0;
		}
	}
	
	public List<RankingSkeleton> typeDistance(){
		for(Variables element:candidateVariables){
			double result = -1;
			try {
				result = Math.max(findDistance(changeToInnerClass(unDeclaredVariable.variableType.get(0).replace(" ", "")),changeToInnerClass(element.variableType.get(0).replace(" ", ""))),
						findDistance(changeToInnerClass(element.variableType.get(0).replace(" ", "")),changeToInnerClass(unDeclaredVariable.variableType.get(0).replace(" ", ""))));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Undeclared Variables: " +unDeclaredVariable.name+ " Type: "+ unDeclaredVariable.variableType.get(0)+
						" Candidates : "+element.name + " Type : " + element.variableType.get(0));
				e.printStackTrace();
			}

			while(rankedList.containsKey(result)!=false){
				//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>Conflict!! " +result );
				if((rankedList.get(result).lineNumber - unDeclaredVariable.lineNumber <= element.lineNumber - unDeclaredVariable.lineNumber)){
					result+=minisculeIncrement;
					//System.out.println("Result 1 : "+ result);
				}
				else{
					result-=minisculeIncrement;
					//System.out.println("Result 2 : "+ result);
				}
			}
			//System.out.println("Undeclared Variables: " +unDeclaredVariable.name+ " Type: "+ unDeclaredVariable.variableType.get(0)+
			//		" Candidates : "+element.name + " Type : " + element.variableType.get(0) + " Similarity: "+result);
			rankedList.put(result,element);			
		}

		Map<Double,Variables> sortedRankedList = new  TreeMap<Double,Variables>(rankedList);		
		return convertToList(sortedRankedList);
	}
	
	
	public int findDistance(String classA, String classB) throws IOException {
    	List<String> superClass = new ArrayList<String>();
    	if(classA.equals(classB)==true)
    		return 0;
        ClassVisitor cl=new ClassVisitor(Opcodes.ASM4) {
            /**
             * Called when a class is visited. This is the method called first
             */
            @Override
            public void visit(int version, int access, String name,String signature, String superName, String[] interfaces) {
                superClass.add(superName);
                super.visit(version, access, name, signature, superName, interfaces);
            }

			@Override
			public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void visitAttribute(Attribute arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void visitEnd() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public FieldVisitor visitField(int arg0, String arg1, String arg2,
					String arg3, Object arg4) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void visitInnerClass(String arg0, String arg1, String arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public MethodVisitor visitMethod(int arg0, String arg1,
					String arg2, String arg3, String[] arg4) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void visitOuterClass(String arg0, String arg1, String arg2) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void visitSource(String arg0, String arg1) {
				// TODO Auto-generated method stub
				
			}
        };
        int count=1;
        String className = classA;
        //System.out.println(":::::::::::::::::::::::::::::" +className);
        while(className!= null){
	        InputStream in=SelectionAlgorithm.class.getResourceAsStream(className);
	        ClassReader classReader=new ClassReader(in);
	        classReader.accept(cl, 0);
	       
	        if(superClass.get(0) == null)
	        	className=null;
	        else{
		        //System.out.println("Super count " +count+" is " + superClass.get(0).replace("/","."));
	        	className = "/" + superClass.get(0) + ".class";
	        	if(className.equals(classB)==true)
	        		return count;
	        	count++;
	        }
	        superClass.clear();
        }
		return (int)bigNumber;

    }
	
    public static String changeToInnerClass(String className){
    	return "/" + className.replace(".", "/") + ".class";
    }
    
//    public static void Test2() throws IOException{
//    	String classA = "java.util.ArrayList";
//    	String classB = "java.util.ArrayList";
//    	//int distance = Math.max(findDistance(changeToInnerClass(classA),changeToInnerClass(classB)),findDistance(changeToInnerClass(classB),changeToInnerClass(classA)));
//    	System.out.println("Distance between " + classA + " and " +classB +" is :: " + distance);
//    }

}

