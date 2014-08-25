package lib.resolver;

public class ResolverException extends Exception {
	public ResolverException() {
		super();
	}

	public ResolverException(Throwable t) {
		super(t);
	}
	
	public ResolverException(String message) {
		super(message);
	}
}
