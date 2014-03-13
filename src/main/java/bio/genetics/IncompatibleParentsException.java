package bio.genetics;

public class IncompatibleParentsException extends Exception {
	public IncompatibleParentsException(String string) {
		super(string);
	}
	
	public IncompatibleParentsException(){
		super();
	}

	private static final long serialVersionUID = -6814839365942221757L;
}
