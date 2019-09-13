package de.cognicrypt.codegenerator.wizard;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.clafer.instance.InstanceClafer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import crypto.rules.CryptSLMethod;
import crypto.rules.CryptSLObject;
import crypto.rules.CryptSLRule;
import crypto.rules.TransitionEdge;
import de.cognicrypt.codegenerator.Activator;
import de.cognicrypt.codegenerator.crysl.CrySLCodeGenerator;
import de.cognicrypt.codegenerator.featuremodel.clafer.InstanceGenerator;
import de.cognicrypt.codegenerator.generator.CodeGenCrySLObject;
import de.cognicrypt.codegenerator.generator.CodeGenCrySLRule;
import de.cognicrypt.codegenerator.generator.CodeGenerator;
import de.cognicrypt.codegenerator.generator.CrySLBasedCodeGenerator;
import de.cognicrypt.codegenerator.generator.GeneratorClass;
import de.cognicrypt.codegenerator.generator.GeneratorMethod;
import de.cognicrypt.codegenerator.generator.XSLBasedGenerator;
import de.cognicrypt.codegenerator.question.Answer;
import de.cognicrypt.codegenerator.question.Question;
import de.cognicrypt.codegenerator.tasks.Task;
import de.cognicrypt.codegenerator.utilities.CodeGenUtils;
import de.cognicrypt.codegenerator.wizard.beginner.BeginnerModeQuestionnaire;
import de.cognicrypt.codegenerator.wizard.beginner.BeginnerTaskQuestionPage;
import de.cognicrypt.core.Constants;
import de.cognicrypt.core.Constants.CodeGenerators;
import de.cognicrypt.utils.DeveloperProject;
import de.cognicrypt.utils.Utils;

public class AltConfigWizard extends Wizard {

	private TaskSelectionPage taskListPage;
	private HashMap<Question, Answer> constraints;
	private BeginnerModeQuestionnaire beginnerQuestions;
	private CodeGenerators generator;
	private static boolean crySLMode = true;

	public static void switchMode(boolean isCrySLMode) {
		crySLMode = isCrySLMode;
	}

	public static boolean isCrySLMode() {
		return crySLMode;
	}

	public AltConfigWizard(CodeGenerators codeGen) {
		super();
		// Set the Look and Feel of the application to the operating
		// system's look and feel.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			Activator.getDefault().logError(e);
		}
		setWindowTitle("CogniCrypt");

		final ImageDescriptor image = AbstractUIPlugin.imageDescriptorFromPlugin("de.cognicrypt.codegenerator", "platform:/plugin/de.cognicrypt.core/icons/cognicrypt-medium.png ");
		setDefaultPageImageDescriptor(image);
		this.constraints = new HashMap<>();
		generator = codeGen;
	}

	@Override
	public void addPages() {
		this.taskListPage = new TaskSelectionPage();
		setForcePreviousAndNextButtons(true);
		addPage(this.taskListPage);
	}

	@Override
	public boolean canFinish() {
		final IWizardPage page = getContainer().getCurrentPage();
		return page instanceof LocatorPage && page.isPageComplete();

	}

	private boolean checkifInUpdateRound() {
		boolean updateRound = false;
		final StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (final StackTraceElement el : stack) {
			if (el.getMethodName().contains("updateButtons")) {
				updateRound = true;
				break;
			}
		}
		return updateRound;
	}

	/**
	 * This method returns the next page. If current page is task list or any but the last question page, the first/next question page is returned. If the current page is the last
	 * question page, the instance list page is returned.
	 *
	 * @param currentPage
	 *        current page
	 * @return either next question page or instance list page
	 */
	@Override
	public IWizardPage getNextPage(final IWizardPage currentPage) {
		if (checkifInUpdateRound()) {
			return currentPage;
		}
		final Task selectedTask = this.taskListPage.getSelectedTask();
		if (currentPage instanceof TaskSelectionPage) {
			this.beginnerQuestions = new BeginnerModeQuestionnaire(selectedTask, selectedTask.getQuestionsJSONFile());
			// It is possible that now questions are within a BeginnerModeQuestionnaire

			if (this.beginnerQuestions.hasPages()) {
				final BeginnerTaskQuestionPage questionPage = new BeginnerTaskQuestionPage(this.beginnerQuestions.nextPage(), this.beginnerQuestions.getTask(), null);
				addPage(questionPage);
				return questionPage;
			} else {
				return addLocatorPage();
			}
		}

		//Only case that is left: BeginnerTaskQuestionPage
		final BeginnerTaskQuestionPage curQuestionPage = (BeginnerTaskQuestionPage) currentPage;
		final HashMap<Question, Answer> curQuestionAnswerMap = curQuestionPage.getMap();

		for (final Entry<Question, Answer> entry : curQuestionAnswerMap.entrySet()) {
			this.constraints.put(entry.getKey(), entry.getValue());
		}

		final int nextPageid = curQuestionPage.getPageNextID();
		if (this.beginnerQuestions.hasMorePages() && nextPageid > -1) {
			final BeginnerTaskQuestionPage questionPage = new BeginnerTaskQuestionPage(this.beginnerQuestions.getPageByID(nextPageid), this.beginnerQuestions.getTask(), null);
			addPage(questionPage);
			return questionPage;
		} else {
			final InstanceGenerator instanceGenerator = new InstanceGenerator(CodeGenUtils.getResourceFromWithin(selectedTask.getModelFile())
				.getAbsolutePath(), "c0_" + selectedTask.getName(), selectedTask.getDescription());

			instanceGenerator.generateInstances(this.constraints);

			if (instanceGenerator.getNoOfInstances() > 0) {
				return addLocatorPage();
			} else {
				final String message = Constants.NO_POSSIBLE_COMBINATIONS_BEGINNER;
				MessageDialog.openError(new Shell(), "Error", message);
			}
		}
		return currentPage;
	}

	private IWizardPage addLocatorPage() {
		final LocatorPage locatorPage = new LocatorPage("Locator");
		addPage(locatorPage);
		return locatorPage;
	}

	/**
	 * This method returns previous page. If currentPage is the first question, the task list page is returned. If it is any other question page or the instance list page, the
	 * previous question page is returned.
	 *
	 * @param currentPage
	 *        current page, either instance list page or question page
	 * @return either previous question or task selection page
	 */
	@Override
	public IWizardPage getPreviousPage(final IWizardPage currentPage) {
		if (!checkifInUpdateRound()) {
			final IWizardPage[] pages = getPages();
			for (int i = 0; i < pages.length; i++) {
				if (currentPage.equals(pages[i])) {
					if (currentPage instanceof BeginnerTaskQuestionPage) {
						((BeginnerTaskQuestionPage) currentPage).setPageInactive();
					}
					final BeginnerTaskQuestionPage prevPage = (BeginnerTaskQuestionPage) pages[i - 1];
					for (final Entry<Question, Answer> quesAns : prevPage.getSelection().entrySet()) {
						this.constraints.remove(quesAns.getKey());
					}
					return prevPage;
				}
			}
		}

		return super.getPreviousPage(currentPage);
	}

	/**
	 * This method is called once the user selects an instance. It writes the instance to an xml file and calls the code generation.
	 *
	 * @return <code>true</code>/<code>false</code> if writing instance file and code generation are (un)successful
	 */
	@Override
	public boolean performFinish() {
		boolean ret = false;
		final Task selectedTask = this.taskListPage.getSelectedTask();
		CodeGenerator codeGenerator = null;
		String additionalResources = selectedTask.getAdditionalResources();
		final LocatorPage currentPage = (LocatorPage) getContainer().getCurrentPage();
		IResource selectedFile = (IResource) currentPage.getSelectedResource().getFirstElement();

		Map<String, String> taskCrySL = new HashMap<String, String>();
		if (crySLMode) {
			taskCrySL.put("Encryption", "encryptionstrings");
			taskCrySL.put("SecurePassword", "passwordhashing");
			taskCrySL.put("DigitalSignatures", "digitalsigning");
		}
		String taskName = selectedTask.getName();
		if (!taskCrySL.containsKey(taskName)) {
			generator = CodeGenerators.XSL;
		}
		JOptionPane optionPane = new JOptionPane("CogniCrypt is now generating code that implements " + selectedTask.getDescription() + "\ninto file " + ((selectedFile != null)
			? selectedFile.getName()
			: "Output.java") + ". This should take no longer than a few seconds.", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {}, null);
		JDialog waitingDialog = optionPane.createDialog("Generating Code");
		waitingDialog.setModal(false);
		waitingDialog.setVisible(true);
		Configuration chosenConfig = null;

		switch (generator) {
			case CrySL:
				CrySLBasedCodeGenerator.clearParameterCache();
				File templateFolder = CodeGenUtils.getResourceFromWithin("src/main/java/de/cognicrypt/codegenerator/crysl/templates/" + taskCrySL.get(taskName));

				File templateFilea = templateFolder.listFiles()[0];
				String projectRelDir = Constants.outerFileSeparator + "src" + Constants.outerFileSeparator + Constants.PackageName + Constants.outerFileSeparator;
				String projectRelPath = projectRelDir + templateFilea.getName();
				String resFileOSPath = selectedFile.getProject().getRawLocation().toOSString() + projectRelPath;

				try {
					Files.createDirectories(Paths.get(selectedFile.getProject().getRawLocation().toOSString() + projectRelDir));
					Files.copy(templateFilea.toPath(), Paths.get(resFileOSPath), StandardCopyOption.REPLACE_EXISTING);

				} catch (IOException e1) {
					Activator.getDefault().logError(e1);
				}
				try {
					selectedFile.getProject().refreshLocal(IProject.DEPTH_INFINITE, null);
				} catch (CoreException e1) {
					Activator.getDefault().logError(e1);
				}
				IFile file = selectedFile.getProject().getFile(projectRelPath);

				ASTParser parser = ASTParser.newParser(AST.JLS11);
				parser.setSource((ICompilationUnit) JavaCore.create(file));

				parser.setResolveBindings(true);
				parser.setBindingsRecovery(true);
				parser.setKind(ASTParser.K_COMPILATION_UNIT);
				CompilationUnit cu = (CompilationUnit) parser.createAST(null);
				final Map<Integer, Integer> methLims = new HashMap<>();

				GeneratorClass templateClass = new GeneratorClass();

				final ASTVisitor astVisitor = new ASTVisitor(true) {

					GeneratorMethod curMethod = null;
					CryptSLObject retObj = null;
					List<CodeGenCrySLObject> pars = new ArrayList<>();
					Map<SimpleName, CryptSLObject> variableDefinitions = new HashMap<SimpleName, CryptSLObject>();

					List<CodeGenCrySLRule> rules = new ArrayList<CodeGenCrySLRule>();

					@SuppressWarnings("unchecked")
					@Override
					public boolean visit(MethodInvocation node) {
						MethodInvocation mi = node;
						String calledMethodName = mi.getName().getFullyQualifiedName();

						List arguments = mi.arguments();
						if ("addReturnObject".equals(calledMethodName)) {
							for (SimpleName var : variableDefinitions.keySet()) {
								String varfqn = var.getFullyQualifiedName();

								for (SimpleName name : (List<SimpleName>) arguments) {
									String efqn = name.getFullyQualifiedName();
									if (efqn.equals(varfqn)) {
										CryptSLObject cryptSLObject = variableDefinitions.get(var);
										retObj = cryptSLObject;
										break;
									}
								}
							}
						} else if ("addParameter".equals(calledMethodName)) {
							for (SimpleName var : variableDefinitions.keySet()) {
								String varfqn = var.getFullyQualifiedName();
								SimpleName name = (SimpleName) arguments.get(0);
								String efqn = name.getFullyQualifiedName();
								if (efqn.equals(varfqn)) {
									pars.add(new CodeGenCrySLObject(variableDefinitions.get(var), (String) ((StringLiteral) arguments.get(1)).resolveConstantExpressionValue()));
									break;
								}
							}
						} else if ("includeClass".equals(calledMethodName)) {
							String rule = Utils.filterQuotes(arguments.get(0).toString());
								String simpleRuleName = rule.substring(rule.lastIndexOf(".") + 1);
								try {
									CryptSLRule cryptSLRule = Utils.getCryptSLRule(simpleRuleName);
									for (CodeGenCrySLObject o : pars) {
										for (TransitionEdge edge: cryptSLRule.getUsagePattern().getEdges()) {
											for (CryptSLMethod method : edge.getLabel()) {
												List<Entry<String, String>> parameters = method.getParameters();
												for (int i = 0; i < parameters.size(); i++) {
													if (parameters.get(i).getKey().equals(o.getCrySLVariable())) {
														o.setMethod(method.getShortMethodName(), i);
													}
												}
											}
										}
									}
									rules.add(new CodeGenCrySLRule(cryptSLRule, pars, retObj));
								} catch (MalformedURLException e) {
									Activator.getDefault().logError(e);
								}
								retObj = null;
								pars = new ArrayList<>();
								
						} else if ("generate".equals(calledMethodName)) {
							methLims.put(1, node.getStartPosition() + node.getLength());
						} else if ("getInstance".equals(calledMethodName)) {
							methLims.put(0, node.getStartPosition() - "CrySLCodeGenerator.".length());
						}
						return super.visit(node);
					}

					@Override
					public void postVisit(ASTNode node) {
						if (node.getLocationInParent() != null && "thrownExceptionTypes".equals(node.getLocationInParent().getId())) {
							curMethod.addException(node.toString());
						}
						super.postVisit(node);
					}

					@Override
					public boolean visit(VariableDeclarationStatement node) {
						SimpleName varName = ((VariableDeclarationFragment) ((VariableDeclarationStatement) node).fragments().get(0)).getName();
						variableDefinitions.put(varName, new CryptSLObject(varName.getFullyQualifiedName(), ((VariableDeclarationStatement) node).getType().toString()));
						return super.visit(node);
					}

					@SuppressWarnings("unchecked")
					@Override
					public boolean visit(MethodDeclaration node) {
						curMethod = new GeneratorMethod();
						curMethod.setName(node.getName().getFullyQualifiedName());
						curMethod.setReturnType(node.getReturnType2().toString());
						curMethod.setModifier("public");

						for (Statement s : (List<Statement>) node.getBody().statements()) {
							curMethod.addStatementToBody(s.toString());
						}

						for (SingleVariableDeclaration svd : (List<SingleVariableDeclaration>) node.parameters()) {
							variableDefinitions.put(svd.getName(), new CryptSLObject(svd.getName().getFullyQualifiedName(), svd.getType().toString()));
							curMethod.addParameter(new SimpleEntry<String, String>(svd.getName().getFullyQualifiedName(), svd.getType().toString()));
						}
						curMethod.setNumberOfVariablesInTemplate(curMethod.getDeclaredVariables().size());
						templateClass.addMethod(curMethod);
						return super.visit(node);
					}

					@Override
					public void endVisit(MethodDeclaration node) {
						Collections.reverse(rules);
						curMethod.setRules(new ArrayList<>(rules));
						rules.clear();
						super.visit(node);
					}

					@Override
					public boolean visit(TypeDeclaration node) {
						templateClass.setClassName(node.getName().getFullyQualifiedName());
						return super.visit(node);
					}

					@Override
					public boolean visit(ImportDeclaration node) {
						String importedClass = node.getName().getFullyQualifiedName();
						if (!"de.cognicrypt.codegenerator.crysl.CrySLCodeGenerator".equals(importedClass)) {
							templateClass.addImport(importedClass);
						}
						return super.visit(node);
					}

				};
				cu.accept(astVisitor);

				try {
					codeGenerator = new CrySLBasedCodeGenerator(selectedFile);

					Map<CodeGenCrySLRule, ?> constraints = new HashMap<CodeGenCrySLRule, Object>();
					chosenConfig = new CrySLConfiguration(null, constraints, resFileOSPath, templateClass);

				} catch (Exception e) {
					Activator.getDefault().logError(e);
					return false;
				}
				break;
			case XSL:

				this.constraints = (this.constraints != null) ? this.constraints : new HashMap<>();
				final InstanceGenerator instanceGenerator = new InstanceGenerator(CodeGenUtils.getResourceFromWithin(selectedTask.getModelFile())
					.getAbsolutePath(), "c0_" + taskName, selectedTask.getDescription());

				instanceGenerator.generateInstances(this.constraints);
				final Map<String, InstanceClafer> instances = instanceGenerator.getInstances();
				final InstanceClafer instance = instances.values().iterator().next();

				// Initialize Code Generation
				codeGenerator = new XSLBasedGenerator(selectedFile, selectedTask.getXslFile());
				final DeveloperProject developerProject = codeGenerator.getDeveloperProject();

				// Generate code template
				chosenConfig = new XSLConfiguration(instance, this.constraints, developerProject
					.getProjectPath() + Constants.innerFileSeparator + Constants.pathToClaferInstanceFile);
				break;
			default:
				ret = false;
		}

		ret = codeGenerator.generateCodeTemplates(chosenConfig, additionalResources);

		waitingDialog.setVisible(false);

		waitingDialog.dispose();
		return ret;
	}

	public HashMap<Question, Answer> getConstraints() {
		return this.constraints;
	}

}
