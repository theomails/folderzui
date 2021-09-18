package net.progressit.pcomponent;

import java.util.Set;
import java.util.function.Function;

import net.progressit.pcomponent.PComponent.PDataPeekers;

public class PAllToSelfDataPeekers<T> extends PDataPeekers<T>{

	public PAllToSelfDataPeekers() {
		super((data)->Set.of(data), (data)->Set.of());
	}

}
