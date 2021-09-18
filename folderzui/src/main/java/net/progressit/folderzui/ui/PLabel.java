package net.progressit.folderzui.ui;

import java.awt.event.ActionEvent;

import javax.swing.JLabel;

import lombok.Data;
import net.progressit.pcomponent.PAllToSelfDataPeekers;
import net.progressit.pcomponent.PComponent;

public class PLabel extends PComponent<String, String>{
	@Data
	public static class PLActionEvent{
		private final ActionEvent event;
	}
	
	private JLabel label = new JLabel();
	public PLabel(PPlacers placers) {
		super(placers);
	}

	@Override
	protected PDataPeekers<String> getDataPeekers() {
		return new PAllToSelfDataPeekers<String>();
	}

	@Override
	protected PRenderers<String> getRenderers() {
		return new PRenderers<String>( ()-> label, (data)->{
			label.setText(data);
		}, (data)-> new PChildrenPlan());
	}

	@Override
	protected PLifecycleHandler getLifecycleHandler() {
		return new PSimpleLifecycleHandler() {
			@Override
			public void postProps() {
				setData(getProps());
			}
		};
	}

}
