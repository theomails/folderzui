package net.progressit.pcomponent;

import java.awt.Container;

import net.progressit.pcomponent.PComponent.PPlacers;

public class PSimpleContainerPlacer extends PPlacers {

	public PSimpleContainerPlacer(Container container) {
		super( (component)->{container.add(component);} , (component)->{container.remove(component);});
	}

}
