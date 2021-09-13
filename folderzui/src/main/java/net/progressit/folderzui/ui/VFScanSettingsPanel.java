package net.progressit.folderzui.ui;

import java.io.File;
import java.util.Optional;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import com.google.common.eventbus.Subscribe;

import lombok.Data;
import net.miginfocom.swing.MigLayout;
import net.progressit.folderzui.ui.PSimpleButton.PSBActionEvent;
import net.progressit.folderzui.ui.PSimpleTextField.PSTFValueEvent;
import net.progressit.folderzui.ui.VFScanSettingsPanel.VFScanSettings;
import net.progressit.pcomponent.PComponent;

public class VFScanSettingsPanel extends PComponent<VFScanSettings>{
	@Data
	public static class VFScanSettings{
		private final String path;
	}
	
	@Data
	public static class VFSSPPathChangedEvent{
		private final String path;
	}
	@Data
	public static class VFSSPScanClickedEvent{
	}
	
	private class SimplePlacementHandler implements PPlacementHandler {
		@Override
		public void removeUiComponent(JComponent component) {
			panel.remove(component);
		}
		@Override
		public void placeUiComponent(JComponent component) {
			System.out.println("Adding " + component + " to " + panel);
			panel.add(component);
		}
	}
	
	private PDisplayWindow window;
	private JPanel panel = new JPanel(new MigLayout("insets 1","[]5[grow, fill]10[]5[]","[]"));
	private PLabel lblPath = new PLabel( new SimplePlacementHandler() );
	private PSimpleTextField txtPath = new PSimpleTextField( new SimplePlacementHandler() );
	private PSimpleButton btnBrowse = new PSimpleButton( new SimplePlacementHandler() );
	private PSimpleButton btnScan = new PSimpleButton( new SimplePlacementHandler() );
	
	public VFScanSettingsPanel(PPlacementHandler placementHandler, PDisplayWindow window) {
		super(placementHandler);
		this.window = window;
	}

	@Override
	protected PDataHandler<VFScanSettings> getDataHandler() {
		return new PDataHandler<VFScanSettings>() {
			@Override
			public Set<Object> grabSelfData(VFScanSettings data) {
				return Set.of();
			}
			@Override
			public Set<Object> grabChildrenData(VFScanSettings data) {
				return Set.of(data.getPath()); //Nothing to react to
			}
		};
	}

	@Override
	protected PRenderHandler<VFScanSettings> getRenderHandler() {
		return new PRenderHandler<VFScanSettings>() {
			@Override
			public JComponent getUiComponent() {
				return panel;
			}
			@Override
			public void renderSelf(VFScanSettings data) {
				
			}
			@Override
			public PChildrenPlan renderChildrenPlan(VFScanSettings data) {
				PChildrenPlan plans = new PChildrenPlan();
				
				PChildPlan plan = PChildPlan.builder().component(lblPath).data("Folder to Scan: ").listener(Optional.empty()).build();
				plans.addChildPlan(plan);
				
				plan = PChildPlan.builder().component(txtPath).data(data.path).listener( Optional.of( new PEventListener() {
					@Subscribe
					public void handle(PSTFValueEvent e) {
						post(new VFSSPPathChangedEvent(e.getValue()));
					}
				} )).build();
				plans.addChildPlan(plan);
				
				plan = PChildPlan.builder().component(btnBrowse).data("Browse...").listener(Optional.of( new PEventListener() {
					@Subscribe
					public void handle(PSBActionEvent e) {
						onBrowseClick();
					}
				} )).build();
				plans.addChildPlan(plan);
				
				plan = PChildPlan.builder().component(btnScan).data("Scan").listener(Optional.of( new PEventListener() {
					@Subscribe
					public void handle(PSBActionEvent e) {
						onScanClick();
					}
				} )).build();
				plans.addChildPlan(plan);
				
				return plans;
			}
		};
	}
	
	public void onBrowseClick() {
		JFileChooser chooser = new JFileChooser();
		System.out.println("Trying to open folder.." + getData().getPath());
		chooser.setCurrentDirectory(new File( getData().getPath() ));
		chooser.setDialogTitle("Choose Folder to Scan...");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// disable the "All files" option.
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
			File result = chooser.getSelectedFile();
			if(!result.isDirectory()) {
				result = chooser.getCurrentDirectory();
			}
			String sPath = result.toPath().toString();
			post(new VFSSPPathChangedEvent(sPath));
		}
	}
	public void onScanClick() {
		post(new VFSSPScanClickedEvent());
	}

	@Override
	protected PLifecycleHandler getLifecycleHandler() {
		return new PSimpleLifecycleHandler();
	}

}
