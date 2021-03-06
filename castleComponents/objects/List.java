package castleComponents.objects;

import java.util.ArrayList;
import java.util.Collection;

import stdSimLib.utilities.RandomGen;
import stdSimLib.utilities.Utilities;

public class List<T> extends ArrayList<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 947819955040569421L;

	int nextCounter = 0;

	public T next() {
		int currNext = nextCounter;
		nextCounter++;
		if (nextCounter == size() - 1) {
			nextCounter = 0;
		}
		return get(currNext);
	}
	
	public void resetNext() {
		nextCounter = 0;
	}

	public List(Collection<? extends T> x) {
		super(x);
	}

	public List() {
		super();
	}

	public void initialize(int size) {
		// Do nothing for now
	}

	public void addEntity(T t) {
		add(t);
	}

	public T peek() {
		return super.get(0);
	}

	public T getRandom() {
		if (size() == 0) {
			return null;
		}
		return get(RandomGen.generateRandomRangeInteger(0, size() - 1));
	}
	
	public T getLast() {
		if (size() == 0) {
			return null;
		}
		return get(size() -1);
	}
	
	public T getFirst() {
		return get(0);
	}

	@Override
	public String toString() {
		String str = "{";
		for (int i = 0; i < size() - 1; i++) {
			str += get(i).toString() + ", ";
		}
		str += get(size() - 1).toString() + "}";
		return str;
	}

}
