package net.progressit.folderzui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import net.progressit.folderzui.model.Scanner.FolderDetails;

class DrawPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private FolderDetails details=null;
	public void setDetails(FolderDetails details) {
		this.details = details;
		this.repaint();
	}
	
	private double scale = 1d;
	private void doDrawing(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		if(details==null) return;

		double total = details.getFullSize();
		double height = DrawPanel.this.getHeight();
		scale = height / total;
		drawBox(0d, 0d, 50d, childHeight(details, scale), height(details, scale), fileName(details), g2d); // Root folder fills :)
		int level = 1;
		drawChildren(scale, 0, level, details.getChildrenDetails(), g2d);
	}

	private void drawChildren(double scale, double startY, int level, Map<Path, FolderDetails> children,
			Graphics2D g2d) {
		double x = dbl(level) * 55d;
		double y = startY;
		Set<Path> paths = children.keySet();
		for (Path path : paths) {
			FolderDetails child = children.get(path);
			double height = dbl(child.getFullSize()) * scale;
			drawBox(x, y, 50d, childHeight(child, scale), height(child, scale), fileName(child), g2d);
			if (!child.getChildrenDetails().isEmpty()) {
				drawChildren(scale, y, level + 1, child.getChildrenDetails(), g2d);
			}
			y += height;
		}
	}

	private void drawBox(double x, double y, double width, double childHeight, double height, String fileName, Graphics2D g2d) {
		g2d.setPaint(Color.cyan);
		g2d.fillRect((int) x, (int) y, (int) width, (int) height);
		g2d.setPaint(Color.lightGray);
		g2d.fillRect((int) x+2, (int) y, (int) width-4, (int) childHeight); //Grey out child height on the first portion
		g2d.setPaint(Color.blue);
		if(height>30) g2d.drawString(fileName, (int) x, (int) y+15);
		g2d.drawRect((int) x, (int) y, (int) width, (int) height);
		
	}

	@Override
	public void paintComponent(Graphics g) {

		super.paintComponent(g);
		doDrawing(g);
	}

	@SuppressWarnings("unused")
	private String perc(FolderDetails details) {
		//double perc = dbl(details.getSize()) / dbl(details.getFullSize()) * 100d;
		//return String.format("%.2f of %dmb", perc, details.getFullSize()/1000_000);
		return String.format("%dmb", details.getFullSize()/1000_000);
	}
	private double dbl(long val) {
		return (double) val;
	}
	@SuppressWarnings("unused")
	private String dash(int len) {
		String dashes = "-------------------------------------------------------------------------------";
		return dashes.substring(0, len);
	}
	private String fileName(FolderDetails details) {
		Path fileName = details.getPath().getFileName();
		return fileName==null?"Drive":fileName.toString();
	}
	private double height(FolderDetails details, double scale) {
		return dbl(details.getFullSize()) * scale;
	}
	private double childHeight(FolderDetails details, double scale) {
		long childSize = details.getFullSize() - details.getSize();
		return dbl(childSize) * scale;
	}


}