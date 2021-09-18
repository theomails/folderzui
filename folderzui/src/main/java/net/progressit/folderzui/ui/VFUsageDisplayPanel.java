package net.progressit.folderzui.ui;

import java.util.Set;

import javax.swing.JScrollPane;

import lombok.Data;
import net.progressit.folderzui.DrawPanel;
import net.progressit.folderzui.DrawPanel.DPRenderException;
import net.progressit.folderzui.model.Scanner.FolderDetails;
import net.progressit.folderzui.ui.VFUsageDisplayPanel.VFUDErrorEvent;
import net.progressit.progressive.PComponent;
import net.progressit.progressive.PComponent.PChildrenPlan;
import net.progressit.progressive.PComponent.PDataPeekers;
import net.progressit.progressive.PComponent.PLifecycleHandler;
import net.progressit.progressive.PComponent.PPlacers;
import net.progressit.progressive.PComponent.PRenderers;
import net.progressit.progressive.PComponent.PSimpleLifecycleHandler;

public class VFUsageDisplayPanel extends PComponent<FolderDetails, FolderDetails>{
	@Data
	public static class VFUDErrorEvent{
		private final Throwable error;
	}
	
	private DrawPanel drawPanel = new DrawPanel();
	private JScrollPane spDrawPanel = new JScrollPane(drawPanel);

	
	public VFUsageDisplayPanel(PPlacers placers) {
		super(placers);
	}

	@Override
	protected PDataPeekers<FolderDetails> getDataPeekers() {
		return new PDataPeekers<FolderDetails>( (data)->Set.of(data), (data)->Set.of() );
	}

	@Override
	protected PRenderers<FolderDetails> getRenderers() {
		return new PRenderers<FolderDetails>( ()-> spDrawPanel, (data)->{
			try {
				drawPanel.setDetails(data); //Null is fine
			} catch (DPRenderException e) {
				post( new VFUDErrorEvent(e) );
			}
		}, (data)->new PChildrenPlan() );
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
