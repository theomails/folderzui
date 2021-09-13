package net.progressit.folderzui.ui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.google.common.eventbus.Subscribe;

import net.progressit.folderzui.ui.VFScanSettingsPanel.VFSSPPathChangedEvent;
import net.progressit.folderzui.ui.VFScanSettingsPanel.VFSSPScanClickedEvent;
import net.progressit.pcomponent.PComponent;
import net.progressit.pcomponent.PComponent.PEventListener;
import net.progressit.pcomponent.PComponent.PPlacementHandler;

public class PDisplayWindow extends JFrame{
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		PDisplayWindow window = new PDisplayWindow();
		window.init();
		window.setVisible(true);
	}
	
	
	private VFScanSettingsPanel pnlSettings = new VFScanSettingsPanel(new PPlacementHandler() {
		@Override
		public void removeUiComponent(JComponent component) {
			System.out.println("Removing " + component + " from " + pnlMain);
			pnlMain.remove(component);
		}
		
		@Override
		public void placeUiComponent(JComponent component) {
			System.out.println("Adding " + component + " to " + pnlMain);
			pnlMain.add(component, BorderLayout.NORTH);
		}
	}, this);
	
	private VFScanSettingsPanel.VFScanSettings data = new VFScanSettingsPanel.VFScanSettings(System.getProperty("user.home"));
	private JPanel pnlMain = new JPanel(new BorderLayout());
	private void init() {
		getContentPane().add(pnlMain, BorderLayout.CENTER);
		PComponent.place(pnlSettings, new PEventListener() {
			@Subscribe
			public void on(VFSSPPathChangedEvent e) {
				data = new VFScanSettingsPanel.VFScanSettings( e.getPath() );
				pnlSettings.setData(data);
			}
			@Subscribe
			public void on(VFSSPScanClickedEvent e) {
				System.out.println("Scan clicked... " + data.getPath());
			}
		}, data);
		
		pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		setSize(800, 600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Visualize Folder");
	}
}
