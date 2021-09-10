package net.progressit.folderzui;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.progressit.folderzui.model.Scanner;

public class Main {



	public static void main(String[] args) throws IOException {
		final Scanner scanner = new Scanner();
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		SwingUtilities.invokeLater( ()->{
			DisplayWindow dw = new DisplayWindow(scanner);
			dw.init();
			dw.setExtendedState(JFrame.MAXIMIZED_BOTH);
			dw.setVisible(true);
		} );
	}


}
