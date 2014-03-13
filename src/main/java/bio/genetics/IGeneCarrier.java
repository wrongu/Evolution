package bio.genetics;

import environment.Environment;

public interface IGeneCarrier<T> {
	public T beget(Environment e);
}
