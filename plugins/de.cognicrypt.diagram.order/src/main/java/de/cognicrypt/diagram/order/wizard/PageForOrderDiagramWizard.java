/********************************************************************************
 * Copyright (c) 2015-2019 TU Darmstadt, Paderborn University
 * 

 * http://www.eclipse.org/legal/epl-2.0. SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

/**
 *
 */
package de.cognicrypt.diagram.order.wizard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xtext.common.types.access.impl.ClasspathTypeProvider;
import org.eclipse.xtext.resource.SaveOptions;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;

import com.google.inject.Injector;

import de.darmstadt.tu.crossing.CrySLStandaloneSetup;
import de.darmstadt.tu.crossing.StatemachineStandaloneSetup;
import de.darmstadt.tu.crossing.crySL.Domainmodel;
import de.darmstadt.tu.crossing.crySL.Expression;
import de.darmstadt.tu.crossing.crySL.SuperType;
import de.darmstadt.tu.crossing.statemachine.StateMachineGraph;
import de.darmstadt.tu.crossing.statemachine.StateMachineGraphBuilder;
import de.darmstadt.tu.crossing.statemachine.StateNode;
import de.darmstadt.tu.crossing.statemachine.Statemachine;
import de.darmstadt.tu.crossing.statemachine.StatemachineFactory;
import de.darmstadt.tu.crossing.statemachine.StatemachinePackage;
import de.darmstadt.tu.crossing.statemachine.TransitionEdge;

public class PageForOrderDiagramWizard extends WizardPage {

	TreeViewer treeViewer;
	
	/**
     * Serializations options.
     */
    // protected Map<Object, Object> options = new HashMap<Object, Object>();

	/**
	 * Create the wizard.
	 */
	public PageForOrderDiagramWizard(final String name, final String title, final String description) {
		super(name);
		setTitle(title);
		setDescription(description);
		setPageComplete(false);
	}

	/**
	 * Create contents of the wizard.
	 *
	 * @param parent
	 */
	@Override
	public void createControl(final Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		setControl(container);

		// make the page layout two-column
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(new GridLayout(2, false));
		
		String cryslFileName = "C";
		String cookie = "Cookie.crysl";
		String cryslFileName2 = "Other2.crysl";
		String cryslFileName3 = "Other3.crysl";
		ArrayList<String> cryslFiles = new ArrayList<String>();
		cryslFiles.add(cookie);
		cryslFiles.add(cryslFileName2);
		cryslFiles.add(cryslFileName3);
		final org.eclipse.swt.widgets.List fileList = new org.eclipse.swt.widgets.List(container, SWT.MULTI | SWT.BORDER);
		for(String f : cryslFiles) {
			fileList.add(f);
		}
		
		// file selection
		fileList.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event arg0) {
                if(fileList.getSelectionCount() > 0)
                    System.out.println(Arrays.toString(fileList.getSelection()));
                // here logic to select rule, later exchange by less hardcoded list traversal
                // better use Map interface?
               /* switch(cryslFileName) {
                case "Cookie.crysl":
                	EObject self = null;
                	System.out.println("case Cookie");
            		try {
            			self = giveEObject("C:\\Users\\T440s\\git\\Crypto-API-Rules\\JavaCryptographicArchitecture\\src\\Cookie.crysl");
            		} catch (MalformedURLException e2) {
            			// TODO Auto-generated catch block
            			e2.printStackTrace();
            		}
            		System.out.println(self);
            		break;
                case "Other.crysl":
                	System.out.println("other");
                	break;
                default:
                	System.out.println("default");
                	break;
                }*/
                
                EObject self = null;
                /*System.out.println("selected " + Arrays.toString(fileList.getSelection()));
                if(Arrays.toString(fileList.getSelection()).equals("[Cookie.crysl]")) {
                	System.out.println("case Cookie");
            		try {
            			self = giveEObject("C:\\Users\\T440s\\git\\Crypto-API-Rules\\JavaCryptographicArchitecture\\src\\Cookie.crysl");
            		} catch (MalformedURLException e2) {
            			// TODO Auto-generated catch block
            			e2.printStackTrace();
            		}
            		System.out.println(self);
                }*/
                
                File folder = new File("C:\\Users\\T440s\\git\\Crypto-API-Rules\\JavaCryptographicArchitecture\\src");
                File[] listOfFiles = folder.listFiles();

                for (int i = 0; i < listOfFiles.length; i++) {
                  if (listOfFiles[i].isFile()) {
                    System.out.println("File " + listOfFiles[i].getName());
                  } else if (listOfFiles[i].isDirectory()) {
                    System.out.println("Directory " + listOfFiles[i].getName());
                  }
                }
                
                for(String s : cryslFiles) {
                	if(s == "Cookie.crysl") {
                    	System.out.println("case Cookie");
                		try {
                			self = provideCrySLEObject("C:\\Users\\T440s\\git\\Crypto-API-Rules\\JavaCryptographicArchitecture\\src\\Cookie.crysl");
                			//self = provideCrySLEObject("C:\\Users\\T440s\\git\\Crypto-API-Rules\\JavaCryptographicArchitecture\\src\\GCMParameterSpec.crysl");
                			//self = provideCrySLEObject("C:\\Users\\T440s\\git\\Crypto-API-Rules\\JavaCryptographicArchitecture\\src\\KeyPairGenerator.crysl");
                			//self = provideCrySLEObject("C:\\Users\\T440s\\git\\Crypto-API-Rules\\JavaCryptographicArchitecture\\src\\KeyGenerator.crysl");
                			//self = provideCrySLEObject("C:\\Users\\T440s\\git\\Crypto-API-Rules\\JavaCryptographicArchitecture\\src\\KeyPair.crysl");
                			//self = provideCrySLEObject("C:\\Users\\T440s\\git\\Crypto-API-Rules\\JavaCryptographicArchitecture\\src\\Key.crysl");
                		} catch (MalformedURLException e2) {
                			e2.printStackTrace();
                		}
                		System.out.println(self);
                	}
                }
                
                //handle generation for current eObject, if click on Generate button
                generateStatemachineDiagramXtextNewTransformationLogic(self);
            }
        });

		/*switch (getName()) {
			case Constants.PAGE_NAME_FOR_MODE_OF_WIZARD:
				setCompositeChoiceForModeOfWizard(new CompositeChoiceForModeOfWizard(container, SWT.NONE, this));
				break;
			case Constants.PAGE_NAME_FOR_LINK_ANSWERS:
				setCompositeToHoldGranularUIElements(new CompositeToHoldGranularUIElements(container, getName()));
				// fill the available space on the with the big composite
				getCompositeToHoldGranularUIElements().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				break;
		}*/
	}
	
	public void generateStatemachineDiagramXtextNewTransformationLogic(EObject self) {
		final Domainmodel dm = (Domainmodel) self;
		Expression order = dm.getOrder();
    	
    	StateMachineGraph smgb = new StateMachineGraphBuilder(order).buildSMG();
    	Set<StateNode> stateNodes = smgb.getNodes();
    	java.util.List<TransitionEdge> transitionEdges = smgb.getEdges();
    	java.util.List<de.darmstadt.tu.crossing.crySL.Event> myTransitionEvents = new ArrayList<de.darmstadt.tu.crossing.crySL.Event>(); // only for labels as they do not provide info what is source and target	
    	    	
    	for(TransitionEdge e : transitionEdges) {
    		myTransitionEvents.add(e.getLabel());
    	}		
		
		StatemachineStandaloneSetup.doSetup(); 
		
		StatemachineStandaloneSetup stmStandaloneSetup = new StatemachineStandaloneSetup();
		final Injector injector = stmStandaloneSetup.createInjectorAndDoEMFRegistration();
		stmStandaloneSetup.register(injector);
		
		ResourceSet resourceSet = new ResourceSetImpl(); 
		StatemachinePackage.eINSTANCE.eClass();
		
		//Resource resource = createAndAddResourcePlatformResURI("CryptSL\\test_stm\\test13ms.statemachine", new String[] {"statemachine"}, set);
		//Resource resource = createAndAddResourcePlatformResURI("/de.darmstadt.tu.crossing.CrySL/src/test13ms.statemachine", new String[] {"statemachine"}, set); //same, resource does not exist
		//Resource resource = createAndAddResourcePlatformResURI("/de.cognicrypt.integrator.task/src/test13ms.statemachine", new String[] {"statemachine"}, set);
		//Resource resource = createAndAddResourcePlatformResURI("/de.darmstadt.tu.crossing.CrySL/src/test13ms.statemachine", new String[] {"statemachine"}, set);
		// provide place holder to insert name of rule here. Need to give via the selection in the wizard, store in variable
		String ruleName = "Cookie";
		// provide file ending as parameter?
		String path = "/de.cognicrypt.diagram.order/output/" + ruleName + ".statemachine";
		Resource resource = createAndAddXtextResourcePlatformPluginURI(path, /*new String[] {"statemachine"},*/ resourceSet);
		
		resourceSet.getResources().add(resource);
		
		Statemachine statemachine = StatemachineFactory.eINSTANCE.createStatemachine();

		de.darmstadt.tu.crossing.statemachine.State state = null;
		de.darmstadt.tu.crossing.statemachine.Event event = null;
		HashMap<StateNode, de.darmstadt.tu.crossing.statemachine.State> stateNodeMap = new HashMap<StateNode, de.darmstadt.tu.crossing.statemachine.State>();
		
		SuperType ev = null;
		int counter = 0;
		
		//proceed stateNodes separately, still unsorted
		for(StateNode s: stateNodes) {
			state = StatemachineFactory.eINSTANCE.createState();
			state.setName("s" + s.getName());
			statemachine.getStates().add(state);
			counter++;
			stateNodeMap.put(s, state);
		}
		
		//proceed transition edges for states and transitions
		for(int i = 0; i < transitionEdges.size(); i++) {
			if(transitionEdges.get(i).getLabel() instanceof SuperType) {
	    		ev = (SuperType) transitionEdges.get(i).getLabel();
		    	event = StatemachineFactory.eINSTANCE.createEvent();
		    	// check for duplicate events to avoid same naming for different edges (causes serialization error)
		    	if(sameEvent(transitionEdges, transitionEdges.get(i))) {
		    		event.setName(((SuperType) ev).getName() + i);
		    		event.setCode(((SuperType) ev).getName() + i);
				}
		    	else {
		    		event.setName(((SuperType) ev).getName());
		    		event.setCode(((SuperType) ev).getName());
		    	}
		    	statemachine.getEvents().add(event);
	    	}
			
			de.darmstadt.tu.crossing.statemachine.Transition transition = StatemachineFactory.eINSTANCE.createTransition();
			
			transition.setEvent(event);
			transition.setFromState(stateNodeMap.get(transitionEdges.get(i).from()));
	    	transition.setEndState(stateNodeMap.get(transitionEdges.get(i).to()));
	    	transition.getFromState().getTransitions().add(transition);
	    	statemachine.getTransitions().add(transition);
		}
		
		// save
		resource.getContents().add(statemachine);
	    
	    //final Map<Object, Object> saveOptions = new HashMap<Object, Object>();      
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	
        try {
        	//saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
            //saveOptions.put(Resource.OPTION_LINE_DELIMITER, Resource.OPTION_LINE_DELIMITER_UNSPECIFIED);
            //this.options.putAll(saveOptions);
        	
            //resource.save(outputStream, this.options);
            resource.save(outputStream, SaveOptions.newBuilder().format().getOptions().toOptionsMap());

        } catch (IOException e) {
        	e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
            	e.printStackTrace();
            }
        }
        
		 byte[] bytes = outputStream.toByteArray();

         // provide a relative path here?
         // provide place holder to insert name of rule
         String path2 = "C:\\Users\\T440s\\git\\CogniCrypt\\plugins\\de.cognicrypt.diagram.order\\output\\" + ruleName + ".statemachine";
         File file = new File(path2); //path not working
         
         try (OutputStream fileOutputStream = new FileOutputStream(file)) {
             try {
                 outputStream.writeTo(fileOutputStream);
             } catch (IOException ioe) {
                 ioe.printStackTrace();
             } finally {
            	 fileOutputStream.close();
             }
         } catch (IOException e2) {
             e2.printStackTrace();
         }
	}
	
	// method for checking if events occur more often
	public static boolean sameEvent(List<TransitionEdge> edges, TransitionEdge e) {
		int counter = 0;
		for(TransitionEdge ed: edges) {
			if(ed != e) {
				if(ed.getLabel().equals(e.getLabel())) {
					counter++;
				}
			}
		}
		if(counter > 0) {
			return true;
		}
		return false;
	}
	
	public static XtextResource createAndAddXtextResourcePlatformPluginURI(String outputFile, /*String[] fileextensions,*/ ResourceSet resourceSet) {	
	     
	     URI uri = URI.createPlatformPluginURI(outputFile, false);
	     XtextResource resource = (XtextResource) resourceSet.createResource(uri);
	     // ((ResourceImpl)resource).setIntrinsicIDToEObjectMap(new HashMap()); 
	     return resource;
	}		

	public static EObject provideCrySLEObject(String pathToCryslFile) throws MalformedURLException {
		// Loading model
    	CrySLStandaloneSetup crySLStandaloneSetup = new CrySLStandaloneSetup();
		final Injector injector = crySLStandaloneSetup.createInjectorAndDoEMFRegistration();
		XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
		String a = System.getProperty("java.class.path");
		String[] l = a.split(";");
		URL[] classpath = new URL[l.length];
		for (int i = 0; i < classpath.length; i++) {
			classpath[i] = new File(l[i]).toURI().toURL();
		}
		URLClassLoader ucl = new URLClassLoader(classpath);
		resourceSet.setClasspathURIContext(new URLClassLoader(classpath));
		new ClasspathTypeProvider(ucl, resourceSet, null, null);
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
		final Resource resource = resourceSet.getResource(URI.createFileURI(pathToCryslFile), true);
		final EObject eObject = resource.getContents().get(0);
		return eObject;
	}
}
