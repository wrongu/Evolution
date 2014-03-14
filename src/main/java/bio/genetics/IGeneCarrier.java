package bio.genetics;

import environment.Environment;

public interface IGeneCarrier<T, P> {
	public T beget(Environment e, P parent);
}
