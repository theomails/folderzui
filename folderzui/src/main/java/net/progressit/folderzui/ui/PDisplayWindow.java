package net.progressit.folderzui.ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import net.progressit.progressive.PComponent;
import net.progressit.progressive.PComponent.PEventListener;
import net.progressit.progressive.PComponent.PPlacers;

public class PDisplayWindow extends JFrame{
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		PDisplayWindow window = new PDisplayWindow();
		window.init();
		window.setVisible(true);
	}
	
	private String home = System.getProperty("user.home");
	private PPlacers simplePlacers = new PPlacers( (component)->getContentPane().add(component, BorderLayout.CENTER), (component)->getContentPane().remove(component) );
	private VisualizeFolderApp app = new VisualizeFolderApp(simplePlacers, this);

	public void init() {
		PComponent.place(app, new PEventListener() {}, home);
		
		setSize(800, 600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Visualize Folder");
	}
}
