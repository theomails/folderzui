package net.progressit.folderzui;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.progressit.folderzui.ui.PDisplayWindow;

public class Main {



	public static void main(String[] args) throws IOException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		SwingUtilities.invokeLater( ()->{
			PDisplayWindow dw = new PDisplayWindow();
			dw.init();
			dw.setExtendedState(JFrame.MAXIMIZED_BOTH);
			dw.setVisible(true);
		} );
	}


}
