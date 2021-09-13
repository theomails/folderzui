package net.progressit.folderzui.ui;

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
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
		return new PDataHandler<String>( (data)->Set.of(data), (data)->Set.of() );
	}

	@Override
	protected PRenderHandler<String> getRenderHandler() {
		return new PRenderHandler<String>( ()-> textField, (data)->{
			if(!textField.getText().equals(data)) {
				textField.setText(data);
			}
		}, (data)-> new PChildrenPlan());

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
						postChange();
					}
					public void removeUpdate(DocumentEvent e) {
						postChange();
					}
					public void insertUpdate(DocumentEvent e) {
						postChange();
					}
					
					private void postChange() {
						//Needed because getText doesn't stabilise until all the remove/insert events have fired.
						SwingUtilities.invokeLater(()->{
							String text = textField.getText();
							post(new PSTFValueEvent(text));
						});
					}
				});
			}
		};
	}

}
