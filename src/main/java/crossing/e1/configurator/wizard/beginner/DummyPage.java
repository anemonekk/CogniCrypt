package crossing.e1.configurator.wizard.beginner;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class DummyPage extends WizardPage{

	public DummyPage(){
		super("Dummy Page");
		
	}
	
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);

		container.setBounds(10, 10, 450, 200);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		
		Label label = new Label(container, SWT.CENTER);
		label.setText("DUMMMYYYY PAGE");
		
		setControl(container);
	}
}