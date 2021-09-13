package net.progressit.folderzui.ui;

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;

import lombok.Data;
import net.progressit.pcomponent.PComponent;

public class PLabel extends PComponent<String>{
	@Data
	public static class PLActionEvent{
		private final ActionEvent event;
	}
	
	private JLabel label = new JLabel();
	
	public PLabel(PPlacementHandler placementHandler) {
		super(placementHandler);
	}

	@Override
	protected PDataHandler<String> getDataHandler() {
		return new PDataHandler<String>() {
			@Override
			public Set<Object> grabSelfData(String data) {
				return Set.of(data);
			}
			@Override
			public Set<Object> grabChildrenData(String data) {
				return Set.of();
			}
		};
	}

	@Override
	protected PRenderHandler<String> getRenderHandler() {
		return new PRenderHandler<String>() {
			@Override
			public JComponent getUiComponent() {
				return label;
			}
			@Override
			public void renderSelf(String data) {
				label.setText(data);
			}
			@Override
			public PChildrenPlan renderChildrenPlan(String data) {
				return new PChildrenPlan();
			}
		};
	}

	@Override
	protected PLifecycleHandler getLifecycleHandler() {
		return new PSimpleLifecycleHandler();
	}

}
