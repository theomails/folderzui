package net.progressit.pcomponent;

import java.awt.Container;

import net.progressit.pcomponent.PComponent.PPlacers;

public class PSimpleContainerPlacers extends PPlacers {

	public PSimpleContainerPlacers(Container container) {
		super( (component)->{container.add(component);} , (component)->{container.remove(component);});
	}

}
