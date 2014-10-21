package test.actions;


public class RankingSkeleton {

	public String undeclaredVariable;
	public String candidateDeclaredVariable;
	public int similarity;
	
	public RankingSkeleton(String undeclaredVariable,
			String candidateDeclaredVariable, int similarity) {
		super();
		this.undeclaredVariable = undeclaredVariable;
		this.candidateDeclaredVariable = candidateDeclaredVariable;
		this.similarity = similarity;
	}
}

