package net.progressit.folderzui.ui;

import java.util.Optional;
import java.util.Set;

import javax.swing.JPanel;

import lombok.Builder;
import lombok.Data;
import net.miginfocom.swing.MigLayout;
import net.progressit.folderzui.ui.VFStatusPanel.VFStatusData;
import net.progressit.pcomponent.PComponent;

public class VFStatusPanel extends PComponent<VFStatusData, VFStatusData>{
	@Data
	@Builder(toBuilder = true)
	public static class VFStatusData{
		private final String info;
	}
	
	private JPanel panel = new JPanel(new MigLayout("insets 1","[grow, fill]","[]"));
	private PPlacers simplePlacers = new PPlacers( (component)->panel.add(component), (component)->panel.remove(component) );

	private PLabel lblStatus = new PLabel( simplePlacers );
	public VFStatusPanel(PPlacers placers) {
		super(placers);
	}

	@Override
	protected PDataPeekers<VFStatusData> getDataPeekers() {
		return new PDataPeekers<VFStatusData>( (data)->Set.of(), (data)->Set.of(data.getInfo()) );
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
