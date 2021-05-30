package de.cognicrypt.diagram.order.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import de.cognicrypt.diagram.order.UIConstants;
//import de.cognicrypt.diagram.order.wizard.OrderDiagramWizardDialog;
//import de.cognicrypt.diagram.order.wizard.OrderDiagramWizard;
import de.cognicrypt.diagram.order.wizard.OrderDiagramWizardDialog;
import de.cognicrypt.diagram.order.wizard.OrderDiagramWizard;

public class RunOrderDiagramHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("inside execute of diagram Handler");
		//UIConstants.WizardActionFromContextMenuFlag = true; // not necessary for now
		final OrderDiagramWizardDialog dialog = new OrderDiagramWizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), new OrderDiagramWizard());
		dialog.setHelpAvailable(false);
		return dialog.open();
	}
	
}

