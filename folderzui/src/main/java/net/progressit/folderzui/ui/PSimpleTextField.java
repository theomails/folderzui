package net.progressit.folderzui.ui;

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lombok.Data;
import net.progressit.pcomponent.PComponent;

public class PSimpleTextField extends PComponent<String>{
	@Data
	public static class PSTFActionEvent{
		private final ActionEvent event;
	}
	@Data
	public static class PSTFValueEvent{
		private final String value;
	}
	
	private JTextField textField = new JTextField();
	
	public PSimpleTextField(PPlacementHandler placementHandler) {
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
				return textField;
			}
			@Override
			public void renderSelf(String data) {
				if(!textField.getText().equals(data)) {
					textField.setText(data);
				}
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
				textField.addActionListener((e)->{
					post(new PSTFActionEvent(e));
				});
				
				//Below gets fired even when we programmatically do setText on the UI field.
				textField.getDocument().addDocumentListener(new DocumentListener() {
					public void changedUpdate(DocumentEvent e) {
						post(new PSTFValueEvent(textField.getText()));
					}
					public void removeUpdate(DocumentEvent e) {
						post(new PSTFValueEvent(textField.getText()));
					}
					public void insertUpdate(DocumentEvent e) {
						post(new PSTFValueEvent(textField.getText()));
					}
				});
			}
		};
	}

}
