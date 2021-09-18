package net.progressit.folderzui.ui;

import java.io.File;
import java.util.Optional;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import com.google.common.eventbus.Subscribe;

import lombok.Data;
import net.miginfocom.swing.MigLayout;
import net.progressit.folderzui.ui.VFScanSettingsPanel.VFSSPPathChangedEvent;
import net.progressit.progressive.PAllToChildrenDataPeekers;
import net.progressit.progressive.PComponent;
import net.progressit.progressive.PComponent.PChildPlan;
import net.progressit.progressive.PComponent.PChildrenPlan;
import net.progressit.progressive.PComponent.PDataPeekers;
import net.progressit.progressive.PComponent.PEventListener;
import net.progressit.progressive.PComponent.PLifecycleHandler;
import net.progressit.progressive.PComponent.PPlacers;
import net.progressit.progressive.PComponent.PRenderers;
import net.progressit.progressive.PComponent.PSimpleLifecycleHandler;
import net.progressit.progressive.PLabel;
import net.progressit.progressive.PSimpleButton;
import net.progressit.progressive.PSimpleButton.PSBActionEvent;
import net.progressit.progressive.PSimpleContainerPlacers;
import net.progressit.progressive.PSimpleTextField;
import net.progressit.progressive.PSimpleTextField.PSTFValueEvent;

public class VFScanSettingsPanel extends PComponent<String, String>{
	//EVENTS
	@Data
	public static class VFSSPPathChangedEvent{
		private final String path;
	}
	@Data
	public static class VFSSPScanClickedEvent{
	}
	
	
	private PDisplayWindow window;
	private JPanel panel = new JPanel(new MigLayout("insets 1","[]5[grow, fill]10[]5[]","[]"));
	private PPlacers simplePlacers = new PSimpleContainerPlacers(panel);

	private PLabel lblPath = new PLabel( simplePlacers );
	private PSimpleTextField txtPath = new PSimpleTextField( simplePlacers );
	private PSimpleButton btnBrowse = new PSimpleButton( simplePlacers );
	private PSimpleButton btnScan = new PSimpleButton( simplePlacers );

	public VFScanSettingsPanel(PPlacers placers, PDisplayWindow window) {
		super(placers);
		this.window = window;
	}

	@Override
	protected PDataPeekers<String> getDataPeekers() {
		return new PAllToChildrenDataPeekers<String>();
	}

	@Override
	protected PRenderers<String> getRenderers() {
		return new PRenderers<String>( ()-> panel, (data)->{}, (data)->{
			PChildrenPlan plans = new PChildrenPlan();
			
			PChildPlan plan = PChildPlan.builder().component(lblPath).props("Folder to Scan: ").listener(Optional.empty()).build();
			plans.addChildPlan(plan);
			
			plan = PChildPlan.builder().component(txtPath).props(data).listener( Optional.of( new PEventListener() {
				@Subscribe
				public void handle(PSTFValueEvent e) {
					post(new VFSSPPathChangedEvent(e.getValue()));
				}
			} )).build();
			plans.addChildPlan(plan);
			
			plan = PChildPlan.builder().component(btnBrowse).props("Browse...").listener(Optional.of( new PEventListener() {
				@Subscribe
				public void handle(PSBActionEvent e) {
					onBrowseClick();
				}
			} )).build();
			plans.addChildPlan(plan);
			
			plan = PChildPlan.builder().component(btnScan).props("Scan").listener(Optional.of( new PEventListener() {
				@Subscribe
				public void handle(PSBActionEvent e) {
					onScanClick();
				}
			} )).build();
			plans.addChildPlan(plan);
			
			return plans;
		} );
	}
	
	public void onBrowseClick() {
		JFileChooser chooser = new JFileChooser();
		System.out.println("Trying to open folder.." + getData());
		chooser.setCurrentDirectory(new File( getData() ));
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
			System.out.println("SEND: " + sPath);
			post(new VFSSPPathChangedEvent(sPath));
		}
	}
	public void onScanClick() {
		post(new VFSSPScanClickedEvent());
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
