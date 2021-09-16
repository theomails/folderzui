package net.progressit.folderzui.ui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.google.common.eventbus.Subscribe;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.miginfocom.swing.MigLayout;
import net.progressit.folderzui.model.Scanner;
import net.progressit.folderzui.model.Scanner.FolderDetails;
import net.progressit.folderzui.ui.VFResultsTreePanel.VFRTPFolderClickEvent;
import net.progressit.folderzui.ui.VFScanSettingsPanel.VFSSPPathChangedEvent;
import net.progressit.folderzui.ui.VFScanSettingsPanel.VFSSPScanClickedEvent;
import net.progressit.folderzui.ui.VFStatusPanel.VFStatusData;
import net.progressit.folderzui.ui.VisualizeFolderApp.VisualizeFolderAppData;
import net.progressit.pcomponent.PComponent;

public class VisualizeFolderApp extends PComponent<VisualizeFolderAppData, String>{
	
	@Data
	@Builder(toBuilder = true)
	public static class VisualizeFolderAppData{
		private final String scanPath;
		private final FolderDetails usageData;
		private final VFStatusData statusData;		
	}
	
	//Swing Context 
	private PDisplayWindow window;
	//My swing
	private JPanel pnlMain = new JPanel(new BorderLayout()); //export
	private JPanel pnlResults = new JPanel(new MigLayout("insets 0","[300::, grow, fill]5[400::,grow, fill]","10[grow, fill]10"));
	
	//Progressive support
	private PPlacementHandler mainNorthPlacementHandler = new PPlacementHandler( (component)->pnlMain.add(component, BorderLayout.NORTH), (component)->pnlMain.remove(component) );
	private PPlacementHandler mainSouthPlacementHandler = new PPlacementHandler( (component)->pnlMain.add(component, BorderLayout.SOUTH), (component)->pnlMain.remove(component) );
	private PPlacementHandler resultsPlacementHandler = new PPlacementHandler( (component)->pnlResults.add(component), (component)->pnlResults.remove(component) );
	//Progressive
	private VFScanSettingsPanel settingsPanel = new VFScanSettingsPanel(mainNorthPlacementHandler, window);
	private VFResultsTreePanel treePanel = new VFResultsTreePanel(resultsPlacementHandler);
	private VFUsageDisplayPanel usagePanel = new VFUsageDisplayPanel(resultsPlacementHandler);
	private VFStatusPanel statusPanel = new VFStatusPanel(mainSouthPlacementHandler);
	
	//STATE
	private VisualizeFolderAppData defaultData = VisualizeFolderAppData.builder()
			.scanPath("")
			.usageData(null)
			.statusData(new VFStatusData("Ready."))
			.build();
	
	
	public VisualizeFolderApp(PPlacementHandler placementHandler, PDisplayWindow window) {
		super(placementHandler);
		this.window = window;
	}

	@Override
	protected PDataHandler<VisualizeFolderAppData> getDataHandler() {
		return new PDataHandler<VisualizeFolderAppData>( (data)->Set.of(), (data)->Set.of(data) );
	}

	@Override
	protected PRenderHandler<VisualizeFolderAppData> getRenderHandler() {
		return new PRenderHandler<VisualizeFolderAppData>( ()-> pnlMain, (data)->{}, (data)->{
			PChildrenPlan plans = new PChildrenPlan();
			
			PChildPlan plan = PChildPlan.builder().component(settingsPanel).props(data.scanPath).listener( Optional.of( new PEventListener() {
				@Subscribe
				public void handle(VFSSPPathChangedEvent e) {
					String path = e.getPath();
					setData(getData().toBuilder().scanPath(path).build());
				}
				@Subscribe
				public void handle(VFSSPScanClickedEvent e) {
					startScan();
				}
			} )).build();
			plans.addChildPlan(plan);
			
			plan = PChildPlan.builder().component(treePanel).props( Paths.get(data.scanPath) ).listener( Optional.of( new PEventListener() {
				@Subscribe
				public void handle(VFRTPFolderClickEvent e) {
					//TODO Click and Double click
				}
			} )).build();
			plans.addChildPlan(plan);
			
			plan = PChildPlan.builder().component(usagePanel).props(data.usageData).listener(Optional.of( new PEventListener() {
				//Need to listen to mouse motion
			} )).build();
			plans.addChildPlan(plan);
			
			plan = PChildPlan.builder().component(statusPanel).props(data.statusData).listener(Optional.empty()).build();
			plans.addChildPlan(plan);
			
			return plans;
		} );
	}
	
	private void startScan() {
		//pathNodesMap.clear();
		//treeFolders.removeAll();
		//DefaultTreeModel treeModel = (DefaultTreeModel) treeFolders.getModel();
		//treeModel.setRoot(nodeDummyRoot);

		try {
			setData(getData().toBuilder().statusData(new VFStatusData("Scanning...")).build());
			
			Path rootFolder = Paths.get( getData().getScanPath() );
			new InvokeScannerThread(rootFolder).start();
		}catch(RuntimeException ex) {
			setData(getData().toBuilder().statusData(new VFStatusData("Invalid path for the scan")).build());
			System.err.println(ex);
			ex.printStackTrace();
		}		
	}
	
	@RequiredArgsConstructor
	private class InvokeScannerThread extends Thread{
		private final Path rootFolder;
		private final Scanner scanner = new Scanner();
		
		@Override
		public void run() {
			try {
				Map<Path, FolderDetails> allDetailsContainer = new HashMap<>();
				scanner.scan( rootFolder, allDetailsContainer, treePanel );
				
				doLater( ()->{
					try {
						FolderDetails rootDetails = allDetailsContainer.get(rootFolder);
						setData(getData().toBuilder().usageData(rootDetails).build());
						setData(getData().toBuilder().statusData(new VFStatusData("Ready.")).build());
					} catch (Exception ex) {
						setData(getData().toBuilder().statusData(new VFStatusData("Error rendering usage data.")).build());
						System.err.println(ex);
						ex.printStackTrace();
					}
				} );
			} catch (IOException ex) {
				setData(getData().toBuilder().statusData(new VFStatusData("Error scanning the folder: " + rootFolder)).build());
				System.err.println(ex);
				ex.printStackTrace();
			}
		}
	}
	
	public static void doLater(Runnable r) {
		SwingUtilities.invokeLater( r );
	}

	@Override
	protected PLifecycleHandler getLifecycleHandler() {
		return new PSimpleLifecycleHandler() {
			@Override
			public void prePlacement() {
				pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
				pnlMain.add(pnlResults, BorderLayout.CENTER);
			}
			@Override
			public void postProps() {
				VisualizeFolderAppData data = getData();
				data = data==null?defaultData:data;
				setData(data.toBuilder().scanPath( getProps() ).build());
			}
		};
	}

}
