package net.progressit.folderzui.ui;

import java.awt.event.ActionEvent;
import java.util.Set;

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
		return new PDataHandler<String>( (data)->Set.of(data), (data)->Set.of() );
	}

	@Override
	protected PRenderHandler<String> getRenderHandler() {
		return new PRenderHandler<String>( ()-> label, (data)->{
			label.setText(data);
		}, (data)-> new PChildrenPlan());
	}

	@Override
	protected PLifecycleHandler getLifecycleHandler() {
		return new PSimpleLifecycleHandler();
	}

}
