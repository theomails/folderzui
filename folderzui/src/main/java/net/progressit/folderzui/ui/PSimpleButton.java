package net.progressit.folderzui.ui;

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.JButton;

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
		return new PDataHandler<String>( (data)->Set.of(data), (data)->Set.of() );
	}

	@Override
	protected PRenderHandler<String> getRenderHandler() {
		return new PRenderHandler<String>( ()-> button, (data)->{
			button.setText(data);
		}, (data)-> new PChildrenPlan());
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
