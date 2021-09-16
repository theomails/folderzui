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
	private PPlacementHandler simplePlacementHandler = new PPlacementHandler( (component)->panel.add(component), (component)->panel.remove(component) );

	private PLabel lblStatus = new PLabel( simplePlacementHandler );
	public VFStatusPanel(PPlacementHandler placementHandler) {
		super(placementHandler);
	}

	@Override
	protected PDataHandler<VFStatusData> getDataHandler() {
		return new PDataHandler<VFStatusData>( (data)->Set.of(), (data)->Set.of(data.getInfo()) );
	}

	@Override
	protected PRenderHandler<VFStatusData> getRenderHandler() {
		return new PRenderHandler<VFStatusData>( ()-> panel, (data)->{}, (data)->{
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
