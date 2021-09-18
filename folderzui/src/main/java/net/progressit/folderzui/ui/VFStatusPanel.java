package net.progressit.folderzui.ui;

import java.util.Optional;

import javax.swing.JPanel;

import lombok.Builder;
import lombok.Data;
import net.miginfocom.swing.MigLayout;
import net.progressit.folderzui.ui.VFStatusPanel.VFStatusData;
import net.progressit.progressive.PAllToChildrenDataPeekers;
import net.progressit.progressive.PComponent;
import net.progressit.progressive.PComponent.PChildPlan;
import net.progressit.progressive.PComponent.PChildrenPlan;
import net.progressit.progressive.PComponent.PDataPeekers;
import net.progressit.progressive.PComponent.PLifecycleHandler;
import net.progressit.progressive.PComponent.PPlacers;
import net.progressit.progressive.PComponent.PRenderers;
import net.progressit.progressive.PComponent.PSimpleLifecycleHandler;
import net.progressit.progressive.PLabel;
import net.progressit.progressive.PSimpleContainerPlacers;

public class VFStatusPanel extends PComponent<VFStatusData, VFStatusData>{
	@Data
	@Builder(toBuilder = true)
	public static class VFStatusData{
		private final String info;
	}
	
	private JPanel panel = new JPanel(new MigLayout("insets 1","[grow, fill]","[]"));
	private PPlacers simplePlacers = new PSimpleContainerPlacers(panel);

	private PLabel lblStatus = new PLabel( simplePlacers );
	public VFStatusPanel(PPlacers placers) {
		super(placers);
	}

	@Override
	protected PDataPeekers<VFStatusData> getDataPeekers() {
		return new PAllToChildrenDataPeekers<VFStatusData>();
	}

	@Override
	protected PRenderers<VFStatusData> getRenderers() {
		return new PRenderers<VFStatusData>( ()-> panel, (data)->{}, (data)->{
			PChildrenPlan plans = new PChildrenPlan();
			
			PChildPlan plan = PChildPlan.builder().component(lblStatus).props(data.getInfo()).listener(Optional.empty()).build();
			plans.addChildPlan(plan);
			
			return plans;
		} );
	}

	@Override
	protected PLifecycleHandler getLifecycleHandler() {
		return new PSimpleLifecycleHandler() {
			@Override
			public void postProps() {
				setData( getProps() );
			}
		};
	}

}
