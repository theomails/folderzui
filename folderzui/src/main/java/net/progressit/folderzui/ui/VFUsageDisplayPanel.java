package net.progressit.folderzui.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

import com.google.common.collect.Sets;

import lombok.Data;
import net.progressit.folderzui.model.Scanner.FolderDetails;
import net.progressit.folderzui.swing.UsageDisplayPanel;
import net.progressit.folderzui.swing.UsageDisplayPanel.DrawPanelMode;
import net.progressit.folderzui.swing.UsageDisplayPanel.UDPRenderException;
import net.progressit.folderzui.ui.VFUsageDisplayPanel.VFUDData;
import net.progressit.progressive.PComponent;

public class VFUsageDisplayPanel extends PComponent<VFUDData, VFUDData>{
	@Data
	public static class VFUDData{
		private final DrawPanelMode mode;
		private final FolderDetails folderDetails;
	}
	@Data
	public static class VFUDErrorEvent{
		private final Throwable error;
	}
	
	private UsageDisplayPanel drawPanel = new UsageDisplayPanel();
	private JScrollPane spDrawPanel = new JScrollPane(drawPanel);

	
	public VFUsageDisplayPanel(PPlacers placers) {
		super(placers);
	}

	@Override
	protected PDataPeekers<VFUDData> getDataPeekers() {
		return new PDataPeekers<VFUDData>( (data)->Sets.newHashSet(data), (data)->Sets.newHashSet() );
	}

	@Override
	protected PRenderers<VFUDData> getRenderers() {
		return new PRenderers<VFUDData>( ()-> spDrawPanel, (data)->{
			try {
				drawPanel.setDetails(data.getFolderDetails(), data.getMode()); //Null is fine
			} catch (UDPRenderException e) {
				post( new VFUDErrorEvent(e) );
			}
		}, (data)->new PChildrenPlan() );
	}

	@Override
	protected PLifecycleHandler getLifecycleHandler() {
		return new PSimpleLifecycleHandler() {
			@Override
			public void prePlacement() {
				drawPanel.setBorder(BorderFactory.createLineBorder( Color.GRAY ));
				drawPanel.setBackground(new Color(245,245,245));
				
				spDrawPanel.addMouseWheelListener(new MouseWheelListener() {
					@Override
					public void mouseWheelMoved(MouseWheelEvent e) {
						// System.out.println(e);
						if (e.isControlDown()) {
							int rotation = -1 * e.getWheelRotation();
							rotation %= 3;

							double scale = (5d + dbl(rotation)) / 5d;
							int width = (int) (dbl(drawPanel.getWidth()) * scale);
							int height = (int) (dbl(drawPanel.getWidth()) * scale);
							width = ensureMinimum(width, spDrawPanel.getWidth()-20);
							height = ensureMinimum(height, spDrawPanel.getHeight()-20);
							drawPanel.setPreferredSize(new Dimension(width, height));
							drawPanel.setSize(new Dimension(width, height));
							drawPanel.repaint();
						} else {
							// pass the event on to the scroll pane
							// getParent().dispatchEvent(e);
						}
					}

					private double dbl(long val) {
						return ((double) val);
					}
					
					private int ensureMinimum(int input, int minLimit) {
						return input<minLimit?minLimit:input;
					}
				});
			}
			
			@Override
			public void postProps() {
				setData( getProps() );
			}
		};
	}

}
