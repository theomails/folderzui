package net.progressit.folderzui.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import net.progressit.folderzui.model.Scanner.FolderDetails;

public class TypesDisplayPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public static class TDPRenderException extends Exception{
		private static final long serialVersionUID = 1L;
		public TDPRenderException(String message, Exception sourceException) {
			super(message, sourceException);
		}
	}

	private FolderDetails details=null;
	public void setDetails(FolderDetails details) throws TDPRenderException {
		try {
			this.details = details;
			this.repaint();
		}catch(RuntimeException e) {
			throw new TDPRenderException("Error while rendering the folder usage.", e);
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {

		super.paintComponent(g);
		doDrawing(g);
	}
	
	//PRIVATE
	
	private double scale = 1d;
	private void doDrawing(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		if(details==null) return;

		double total = details.getFullSize();
		double height = TypesDisplayPanel.this.getHeight();
		scale = height / total;
		drawTypes(scale, details.getTypeFullSizes(), g2d);
	}

	private void drawTypes(double scale, Map<String, Long> typeFullSizes,
			Graphics2D g2d) {
		double x = 0;
		double y = 0;
		double width = TypesDisplayPanel.this.getWidth();
		Set<String> types = typeFullSizes.keySet();
		for (String type : types) {
			Long typeFullSize = typeFullSizes.get(type);
			double height = typeFullSize * scale;
			drawBox(x, y, width, height, type, g2d);
			y += height;
		}
	}

	private void drawBox(double x, double y, double width, double height, String fileName, Graphics2D g2d) {
		g2d.setPaint(Color.cyan);
		g2d.fillRect((int) x, (int) y, (int) width, (int) height);
		g2d.setPaint(Color.blue);
		if(height>30) g2d.drawString(fileName, (int) x, (int) y+15);
		g2d.drawRect((int) x, (int) y, (int) width, (int) height);
		
	}

}