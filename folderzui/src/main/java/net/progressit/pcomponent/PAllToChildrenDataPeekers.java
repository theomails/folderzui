package net.progressit.pcomponent;

import java.util.Set;
import java.util.function.Function;

import net.progressit.pcomponent.PComponent.PDataPeekers;

public class PAllToChildrenDataPeekers<T> extends PDataPeekers<T>{

	public PAllToChildrenDataPeekers() {
		super((data)->Set.of(), (data)->Set.of(data));
	}

}
