package net.progressit.folderzui.ui;

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;

import lombok.Data;
import net.progressit.pcomponent.PComponent;

public class PSimpleButton extends PComponent<String>{
	@Data
	public static class PSBActionEvent{
		private final ActionEvent event;
	}
	
	private JButton button = new JButton();
	
	public PSimpleButton(PPlacementHandler placementHandler) {
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
				return button;
			}
			@Override
			public void renderSelf(String data) {
				button.setText(data);
			}
			@Override
			public PChildrenPlan renderChildrenPlan(String data) {
				return new PChildrenPlan();
			}
		};
	}

	@Override
	protected PLifecycleHandler getLifecycleHandler() {
		return new PSimpleLifecycleHandler() {
			@Override
			public void prePlacement() {
				button.addActionListener((e)->{
					post(new PSBActionEvent(e));
				});
			}
		};
	}

}
