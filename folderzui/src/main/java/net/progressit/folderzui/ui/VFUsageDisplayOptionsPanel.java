package net.progressit.folderzui.ui;

import java.util.Optional;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.common.eventbus.Subscribe;

import lombok.Builder;
import lombok.Data;
import net.progressit.folderzui.model.Scanner.FolderDetails;
import net.progressit.folderzui.swing.DrawPanel.DrawPanelMode;
import net.progressit.folderzui.ui.VFUsageDisplayOptionsPanel.VFUDOData;
import net.progressit.folderzui.ui.VFUsageDisplayPanel.VFUDData;
import net.progressit.folderzui.ui.VFUsageDisplayPanel.VFUDErrorEvent;
import net.progressit.progressive.PComponent;

public class VFUsageDisplayOptionsPanel extends PComponent<VFUDOData, VFUDOData>{
	@Data
	@Builder(toBuilder = true)
	public static class VFUDOData{
		private final FolderDetails folderDetails;
		private final DrawPanelMode selectedMode;
	}
	@Data
	public static class VFUDOModeChanged{
		private final DrawPanelMode selectedMode;
	}
	
	private JTabbedPane tabbedPanel = new JTabbedPane();
	private VFUsageDisplayPanel sizePanel = new VFUsageDisplayPanel( new PPlacers( (comp)->{ tabbedPanel.setComponentAt(0, comp); } , (comp)->{ tabbedPanel.remove(comp); }) );
	private VFUsageDisplayPanel countPanel = new VFUsageDisplayPanel( new PPlacers( (comp)->{ tabbedPanel.setComponentAt(1, comp); } , (comp)->{ tabbedPanel.remove(comp); }) );
	public VFUsageDisplayOptionsPanel(PPlacers placers) {
		super(placers);
	}

	@Override
	protected PDataPeekers<VFUDOData> getDataPeekers() {
		return new PDataPeekers<VFUDOData>( (data)->Set.of(data.getSelectedMode()), (data)->Set.of(data.getFolderDetails()==null?new Object():data.getFolderDetails()) );
	}

	@Override
	protected PRenderers<VFUDOData> getRenderers() {
		return new PRenderers<VFUDOData>( ()-> tabbedPanel, (data)->{
			//Hope this doesn't again trigger an event!
			if(data.getSelectedMode()==DrawPanelMode.SIZE) {
				tabbedPanel.setSelectedIndex(0);
			}else {
				tabbedPanel.setSelectedIndex(1);
			}
		}, (data)->{
			PChildrenPlan plans = new PChildrenPlan();
			
			PChildPlan plan = PChildPlan.builder().component(sizePanel).listener(Optional.of( new PEventListener() {
				@Subscribe
				public void handle(VFUDErrorEvent event) {
					post(event);
				}
			} )).props( new VFUDData(DrawPanelMode.SIZE, data.getFolderDetails()) ).build();
			plans.addChildPlan(plan);
			
			plan = PChildPlan.builder().component(countPanel).listener(Optional.of( new PEventListener() {
				@Subscribe
				public void handle(VFUDErrorEvent event) {
					post(event);
				}
			} )).props( new VFUDData(DrawPanelMode.COUNT, data.getFolderDetails()) ).build();
			plans.addChildPlan(plan);
			
			return plans;
		} );
	}

	@Override
	protected PLifecycleHandler getLifecycleHandler() {
		return new PSimpleLifecycleHandler() {
			@Override
			public void prePlacement() {
				tabbedPanel.addTab("Size", new JLabel());
				tabbedPanel.addTab("Count", new JLabel());
				
				tabbedPanel.addChangeListener(new ChangeListener() {
			        public void stateChanged(ChangeEvent e) {
			            int selectedIndex = tabbedPanel.getSelectedIndex();
			            DrawPanelMode selectedMode = selectedIndex==0?DrawPanelMode.SIZE:DrawPanelMode.COUNT;
			            post(new VFUDOModeChanged(selectedMode));
			        }
			    });
			}
			
			@Override
			public void postProps() {
				setData( getProps() );
			}
		};
	}

}
