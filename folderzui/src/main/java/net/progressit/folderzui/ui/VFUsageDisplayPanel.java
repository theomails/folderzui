package net.progressit.folderzui.ui;

import java.util.Set;

import javax.swing.JScrollPane;

import lombok.Data;
import net.progressit.folderzui.DrawPanel;
import net.progressit.folderzui.DrawPanel.DPRenderException;
import net.progressit.folderzui.model.Scanner.FolderDetails;
import net.progressit.folderzui.ui.VFUsageDisplayPanel.VFUsageDisplayData;
import net.progressit.pcomponent.PComponent;

public class VFUsageDisplayPanel extends PComponent<VFUsageDisplayData, VFUsageDisplayData>{
	@Data
	public static class VFUsageDisplayData{
		private final FolderDetails folderDetails;
	}
	@Data
	public static class VFUDErrorEvent{
		private final Throwable error;
	}
	
	private DrawPanel drawPanel = new DrawPanel();
	private JScrollPane spDrawPanel = new JScrollPane(drawPanel);

	
	public VFUsageDisplayPanel(PPlacementHandler placementHandler) {
		super(placementHandler);
	}

	@Override
	protected PDataHandler<VFUsageDisplayData> getDataHandler() {
		return new PDataHandler<VFUsageDisplayData>( (data)->Set.of(data.getFolderDetails()), (data)->Set.of() );
	}

	@Override
	protected PRenderHandler<VFUsageDisplayData> getRenderHandler() {
		return new PRenderHandler<VFUsageDisplayData>( ()-> spDrawPanel, (data)->{
			try {
				drawPanel.setDetails(data.getFolderDetails()); //Null is fine
			} catch (DPRenderException e) {
				post( new VFUDErrorEvent(e) );
			}
		}, (data)->new PChildrenPlan() );
	}

	@Override
	protected PLifecycleHandler getLifecycleHandler() {
		return new PSimpleLifecycleHandler();
	}

}
