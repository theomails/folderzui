package net.progressit.folderzui.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

import lombok.Data;
import net.progressit.folderzui.model.Scanner.FolderDetails;
import net.progressit.folderzui.swing.TypesDisplayPanel;
import net.progressit.folderzui.swing.TypesDisplayPanel.TDPRenderException;
import net.progressit.progressive.PComponent;

public class VFTypesDisplayPanel extends PComponent<FolderDetails, FolderDetails>{
	@Data
	public static class VFTDErrorEvent{
		private final Throwable error;
	}
	
	private TypesDisplayPanel drawPanel = new TypesDisplayPanel();
	private JScrollPane spDrawPanel = new JScrollPane(drawPanel);

	
	public VFTypesDisplayPanel(PPlacers placers) {
		super(placers);
	}

	@Override
	protected PDataPeekers<FolderDetails> getDataPeekers() {
		return new PDataPeekers<FolderDetails>( (data)->Set.of(data), (data)->Set.of() );
	}

	@Override
	protected PRenderers<FolderDetails> getRenderers() {
		return new PRenderers<FolderDetails>( ()-> spDrawPanel, (data)->{
			try {
				drawPanel.setDetails(data); //Null is fine
			} catch (TDPRenderException e) {
				post( new VFTDErrorEvent(e) );
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
				
				/* Not much use zooming into types data.
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
				*/
			}
			
			@Override
			public void postProps() {
				setData( getProps() );
			}
		};
	}

}
