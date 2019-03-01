/*******************************************************************************
 * Copyright (c) 2010, 2019 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabian Steeg    - intial Xtext generation (see bug #277380)
 *     Alexander Nyßen - initial implementation
 *     Tamas Miklossy (itemis AG) - Add quickfix support for all dot attributes (bug #513196)
 *     Zoey Gerrit Prigge (itemis AG) - quickfix to remove redundant attributes (bug #540330)
 *
 *******************************************************************************/
package org.eclipse.gef.dot.internal.ui.language.quickfix;

import static org.eclipse.gef.dot.internal.language.validation.DotJavaValidator.INVALID_EDGE_OPERATOR;
import static org.eclipse.gef.dot.internal.language.validation.DotJavaValidator.REDUNDANT_ATTRIBUTE;

import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.dot.internal.DotAttributes;
import org.eclipse.gef.dot.internal.language.arrowtype.DeprecatedShape;
import org.eclipse.gef.dot.internal.language.clustermode.ClusterMode;
import org.eclipse.gef.dot.internal.language.color.DotColors;
import org.eclipse.gef.dot.internal.language.dir.DirType;
import org.eclipse.gef.dot.internal.language.dot.AttrList;
import org.eclipse.gef.dot.internal.language.dot.Attribute;
import org.eclipse.gef.dot.internal.language.dot.DotFactory;
import org.eclipse.gef.dot.internal.language.dot.EdgeOp;
import org.eclipse.gef.dot.internal.language.dot.EdgeRhsNode;
import org.eclipse.gef.dot.internal.language.dot.EdgeRhsSubgraph;
import org.eclipse.gef.dot.internal.language.dot.Subgraph;
import org.eclipse.gef.dot.internal.language.layout.Layout;
import org.eclipse.gef.dot.internal.language.outputmode.OutputMode;
import org.eclipse.gef.dot.internal.language.pagedir.Pagedir;
import org.eclipse.gef.dot.internal.language.rankdir.Rankdir;
import org.eclipse.gef.dot.internal.language.ranktype.RankType;
import org.eclipse.gef.dot.internal.language.shape.PolygonBasedNodeShape;
import org.eclipse.gef.dot.internal.language.shape.RecordBasedNodeShape;
import org.eclipse.gef.dot.internal.language.style.ClusterStyle;
import org.eclipse.gef.dot.internal.language.style.EdgeStyle;
import org.eclipse.gef.dot.internal.language.style.NodeStyle;
import org.eclipse.gef.dot.internal.language.terminals.ID;
import org.eclipse.gef.dot.internal.language.terminals.ID.Type;
import org.eclipse.gef.dot.internal.language.validation.DotArrowTypeJavaValidator;
import org.eclipse.gef.dot.internal.language.validation.DotStyleJavaValidator;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.model.edit.ISemanticModification;
import org.eclipse.xtext.ui.editor.quickfix.DefaultQuickfixProvider;
import org.eclipse.xtext.ui.editor.quickfix.Fix;
import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionAcceptor;
import org.eclipse.xtext.validation.Issue;

/**
 * A quick-fix provider for Dot.
 * 
 * @author anyssen
 */
public class DotQuickfixProvider extends DefaultQuickfixProvider {

	private static final String DELETE_IMAGE = "delete.png"; //$NON-NLS-1$

	@Fix(INVALID_EDGE_OPERATOR)
	public void fixInvalidEdgeOperator(final Issue issue,
			IssueResolutionAcceptor acceptor) {
		String[] issueData = issue.getData();
		if (issueData != null && issueData.length > 0) {
			String invalidEdgeOperator = issueData[0];
			EdgeOp validEdgeOperator = invalidEdgeOperator
					.equals(EdgeOp.DIRECTED.toString()) ? EdgeOp.UNDIRECTED
							: EdgeOp.DIRECTED;
			provideQuickfix(invalidEdgeOperator, validEdgeOperator.toString(),
					"edge operator", //$NON-NLS-1$
					issue, acceptor, new ISemanticModification() {

						@Override
						public void apply(EObject element,
								IModificationContext context) {
							if (element instanceof EdgeRhsNode) {
								EdgeRhsNode edge = (EdgeRhsNode) element;
								edge.setOp(validEdgeOperator);
							}
							if (element instanceof EdgeRhsSubgraph) {
								EdgeRhsSubgraph edge = (EdgeRhsSubgraph) element;
								edge.setOp(validEdgeOperator);
							}
						}
					});
		}
	}

	@Fix(REDUNDANT_ATTRIBUTE)
	public void fixRedundantAttribute(final Issue issue,
			IssueResolutionAcceptor acceptor) {
		if (issue.getData() == null || issue.getData().length == 0) {
			return;
		}

		String attributeName = issue.getData()[0];

		String label = "Remove '" + attributeName + "' attribute."; //$NON-NLS-1$ //$NON-NLS-2$
		String description = "Remove the redundant '" + attributeName //$NON-NLS-1$
				+ "' attribute."; //$NON-NLS-1$
		ISemanticModification semanticModification = (EObject element,
				IModificationContext context) -> EcoreUtil.remove(element);

		acceptor.accept(issue, label, description, DELETE_IMAGE,
				semanticModification);
	}

	@Fix(DotAttributes.ARROWHEAD__E)
	public void fixArrowheadAttributeValue(final Issue issue,
			IssueResolutionAcceptor acceptor) {
		fixArrowType(issue, "edge arrowhead", acceptor); //$NON-NLS-1$
	}

	@Fix(DotAttributes.ARROWTAIL__E)
	public void fixArrowtailAttributeValue(final Issue issue,
			IssueResolutionAcceptor acceptor) {
		fixArrowType(issue, "edge arrowtail", acceptor); //$NON-NLS-1$
	}

	@Fix(DotAttributes.CLUSTERRANK__G)
	public void fixClusterRankAttributeValue(final Issue issue,
			IssueResolutionAcceptor acceptor) {
		String invalidValue = issue.getData()[0];
		provideQuickfixes(invalidValue, ClusterMode.values(),
				"graph clusterMode", issue, //$NON-NLS-1$
				acceptor);
	}

	@Fix(DotAttributes.COLORSCHEME__GCNE)
	public void fixColorschemeAttributeValue(final Issue issue,
			IssueResolutionAcceptor acceptor) {
		// TODO: use "graph colorscheme", "node colorscheme", "edge colorscheme"
		// as suffix.
		String invalidValue = issue.getData()[0];
		provideQuickfixes(invalidValue, DotColors.getColorSchemes(),
				"colorscheme", //$NON-NLS-1$
				issue, acceptor);
	}

	@Fix(DotAttributes.DIR__E)
	public void fixDirAttributeValue(final Issue issue,
			IssueResolutionAcceptor acceptor) {
		String invalidValue = issue.getData()[0];
		provideQuickfixes(invalidValue, DirType.values(), "edge dir", issue, //$NON-NLS-1$
				acceptor);
	}

	@Fix(DotAttributes.LAYOUT__G)
	public void fixLayoutAttributeValue(final Issue issue,
			IssueResolutionAcceptor acceptor) {
		String invalidValue = issue.getData()[0];
		provideQuickfixes(invalidValue, Layout.values(), "graph layout", issue, //$NON-NLS-1$
				acceptor);
	}

	@Fix(DotAttributes.OUTPUTORDER__G)
	public void fixOutputOrderAttributeValue(final Issue issue,
			IssueResolutionAcceptor acceptor) {
		String invalidValue = issue.getData()[0];
		provideQuickfixes(invalidValue, OutputMode.values(), "graph outputMode", //$NON-NLS-1$
				issue, acceptor);
	}

	@Fix(DotAttributes.PAGEDIR__G)
	public void fixPagedirAttributeValue(final Issue issue,
			IssueResolutionAcceptor acceptor) {
		String invalidValue = issue.getData()[0];
		provideQuickfixes(invalidValue, Pagedir.values(), "graph pagedir", //$NON-NLS-1$
				issue, acceptor);
	}

	@Fix(DotAttributes.RANK__S)
	public void fixRankAttributeValue(final Issue issue,
			IssueResolutionAcceptor acceptor) {
		String invalidValue = issue.getData()[0];
		provideQuickfixes(invalidValue, RankType.values(), "subgraph rankType", //$NON-NLS-1$
				issue, acceptor);
	}

	@Fix(DotAttributes.RANKDIR__G)
	public void fixRankdirAttributeValue(final Issue issue,
			IssueResolutionAcceptor acceptor) {
		String invalidValue = issue.getData()[0];
		provideQuickfixes(invalidValue, Rankdir.values(), "graph rankdir", //$NON-NLS-1$
				issue, acceptor);
	}

	@Fix(DotAttributes.SHAPE__N)
	public void fixShapeAttributeValue(final Issue issue,
			IssueResolutionAcceptor acceptor) {
		String invalidValue = issue.getData()[0];
		provideQuickfixes(invalidValue, PolygonBasedNodeShape.VALUES,
				"node shape", issue, //$NON-NLS-1$
				acceptor);
		provideQuickfixes(invalidValue, RecordBasedNodeShape.VALUES,
				"node shape", issue, //$NON-NLS-1$
				acceptor);
	}

	@Fix(DotAttributes.STYLE__GCNE)
	public void fixStyleAttributeValue(final Issue issue,
			IssueResolutionAcceptor acceptor) {

		String[] issueData = issue.getData();
		if (issueData == null || issueData.length < 2) {
			return;
		}
		String issueCode = issueData[0];

		switch (issueCode) {
		case DotStyleJavaValidator.DEPRECATED_STYLE_ITEM:
			provideQuickfixesForDeprecatedStyleItem(issue, acceptor);
			break;
		case DotStyleJavaValidator.DUPLICATED_STYLE_ITEM:
			provideQuickfixesForDuplicatedStyleItem(issue, acceptor);
			break;
		case DotStyleJavaValidator.INVALID_STYLE_ITEM:
			provideQuickfixesForInvalidStyleItem(issue, acceptor);
		default:
			return;
		}
	}

	private void fixArrowType(Issue issue, String suffix,
			IssueResolutionAcceptor acceptor) {
		String[] issueData = issue.getData();
		if (issueData == null || issueData.length < 2) {
			return;
		}
		String issueCode = issueData[0];

		switch (issueCode) {
		case DotArrowTypeJavaValidator.DEPRECATED_ARROW_SHAPE:
			provideQuickfixesForDeprecatedArrowShape(issue, suffix, acceptor);
			break;
		default:
			return;
		}
	}

	private void provideQuickfixesForDeprecatedArrowShape(Issue issue,
			String suffix, IssueResolutionAcceptor acceptor) {
		String[] issueData = issue.getData();
		String deprecatedShapeString = issueData[1];
		String validArrowShape = getValidArrowShape(deprecatedShapeString);
		if (validArrowShape != null) {
			provideQuickfixForMultipleAttributeValue(deprecatedShapeString,
					validArrowShape, suffix, issue, acceptor);
		}
	}

	private void provideQuickfixesForDeprecatedStyleItem(Issue issue,
			IssueResolutionAcceptor acceptor) {
		String[] issueData = issue.getData();
		String styleItemName = issueData[1];

		final String penwidthValue = issueData.length > 2 ? issueData[2] : ""; //$NON-NLS-1$

		final String styleItem = styleItemName + (penwidthValue == "" ? "" //$NON-NLS-1$ //$NON-NLS-2$
				: "(" + penwidthValue + ")"); //$NON-NLS-1$ //$NON-NLS-2$

		String label = "Replace '" + styleItem + "' with 'penwidth=" //$NON-NLS-1$ //$NON-NLS-2$
				+ penwidthValue + "'."; //$NON-NLS-1$
		String description = "Use the 'penwidth' attribute instead of the deprecated '" //$NON-NLS-1$
				+ styleItemName + "' style."; //$NON-NLS-1$
		ISemanticModification semanticModification = new ISemanticModification() {
			@Override
			public void apply(EObject element, IModificationContext context) {
				EObject container = element.eContainer();
				Attribute attribute = (Attribute) element;
				ID value = attribute.getValue();
				Type type = value.getType();
				String currentValue = value.toValue();
				String validValue = currentValue.replace(styleItem, "") //$NON-NLS-1$
						.trim();
				// trim the unnecessary ',' if any is left over
				validValue = validValue.replaceAll("^,", "") //$NON-NLS-1$ //$NON-NLS-2$
						.replaceAll(",$", "").replace(", ,", ",").trim(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

				// if the valid style attribute value is empty, remove the
				// entire style attribute
				if (validValue.isEmpty()) {
					EcoreUtil.remove(attribute);
				} else {
					ID validValueAsID = ID.fromValue(validValue, type);
					attribute.setValue(validValueAsID);
				}

				// add a new pendwidth attribute
				Attribute penwidthAttribute = DotFactory.eINSTANCE
						.createAttribute();
				penwidthAttribute
						.setName(ID.fromValue(DotAttributes.PENWIDTH__CNE));
				penwidthAttribute.setValue(
						ID.fromValue(penwidthValue, ID.Type.QUOTED_STRING));

				if (container instanceof AttrList) {
					AttrList attrList = (AttrList) container;
					attrList.getAttributes().add(penwidthAttribute);
				} else if (container instanceof Subgraph) {
					Subgraph subgraph = (Subgraph) container;
					subgraph.getStmts().add(penwidthAttribute);
				}
			}
		};

		acceptor.accept(issue, label, description, null, semanticModification);
	}

	private void provideQuickfixesForDuplicatedStyleItem(Issue issue,
			IssueResolutionAcceptor acceptor) {
		final String styleItem = issue.getData()[1];

		String label = "Remove '" + styleItem + "' style attribute value."; //$NON-NLS-1$ //$NON-NLS-2$
		String description = "Remove the redundant '" + styleItem //$NON-NLS-1$
				+ "' style attribute value."; //$NON-NLS-1$
		ISemanticModification semanticModification = new ISemanticModification() {
			@Override
			public void apply(EObject element, IModificationContext context) {
				Attribute attribute = (Attribute) element;
				ID value = attribute.getValue();
				Type type = value.getType();
				String currentValue = value.toValue();
				String validValue = currentValue
						.replaceFirst(styleItem + ",", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
				ID validValueAsID = ID.fromValue(validValue, type);
				attribute.setValue(validValueAsID);
			}
		};

		acceptor.accept(issue, label, description, DELETE_IMAGE,
				semanticModification);
	}

	private void provideQuickfixesForInvalidStyleItem(Issue issue,
			IssueResolutionAcceptor acceptor) {

		String[] issueData = issue.getData();

		String invalidValue = issueData[1];
		DotAttributes.Context attributeContext = DotAttributes.Context
				.valueOf(issueData[2]);
		switch (attributeContext) {
		case GRAPH:
		case SUBGRAPH:
		case CLUSTER:
			provideQuickfixesForMultipleAttributeValue(invalidValue,
					ClusterStyle.VALUES, "graph style", //$NON-NLS-1$
					issue, acceptor);
			break;
		case NODE:
			provideQuickfixesForMultipleAttributeValue(invalidValue,
					NodeStyle.VALUES, "node style", //$NON-NLS-1$
					issue, acceptor);
			break;
		case EDGE:
			provideQuickfixesForMultipleAttributeValue(invalidValue,
					EdgeStyle.VALUES, "edge style", //$NON-NLS-1$
					issue, acceptor);
			break;
		default:
			break;
		}
	}

	private String getValidArrowShape(String deprecatedShapeString) {
		DeprecatedShape deprecatedShape = DeprecatedShape
				.get(deprecatedShapeString);
		if (deprecatedShape == null) {
			return null;
		}
		switch (deprecatedShape) {
		case EDIAMOND:
			return "odiamond"; //$NON-NLS-1$
		case OPEN:
			return "vee"; //$NON-NLS-1$
		case HALFOPEN:
			return "lvee"; //$NON-NLS-1$
		case EMPTY:
			return "onormal"; //$NON-NLS-1$
		case INVEMPTY:
			return "oinv"; //$NON-NLS-1$
		default:
			return null;
		}
	}

	private void provideQuickfixesForMultipleAttributeValue(String invalidValue,
			List<?> validValues, String suffix, Issue issue,
			IssueResolutionAcceptor acceptor) {
		for (Object validValue : validValues) {
			provideQuickfixForMultipleAttributeValue(invalidValue,
					validValue.toString(), suffix, issue, acceptor);
		}
	}

	private void provideQuickfixForMultipleAttributeValue(String invalidValue,
			String validValue, String suffix, Issue issue,
			IssueResolutionAcceptor acceptor) {

		provideQuickfix(invalidValue, validValue, suffix, issue, acceptor,
				new ISemanticModification() {
					@Override
					public void apply(EObject element,
							IModificationContext context) {
						Attribute attribute = (Attribute) element;
						ID invalidAttributeValue = attribute.getValue();
						String validAttributeValue = invalidAttributeValue
								.toValue().replaceAll(invalidValue, validValue);
						Type attributeValueType = invalidAttributeValue
								.getType();
						ID validValueAsID = ID.fromValue(validAttributeValue,
								attributeValueType);
						attribute.setValue(validValueAsID);
					}
				});
	}

	private void provideQuickfixes(String invalidValue, Object[] validValues,
			String suffix, Issue issue, IssueResolutionAcceptor acceptor) {
		provideQuickfixes(invalidValue, Arrays.asList(validValues), suffix,
				issue, acceptor);
	}

	private void provideQuickfixes(String invalidValue, List<?> validValues,
			String suffix, Issue issue, IssueResolutionAcceptor acceptor) {
		for (Object validValue : validValues) {
			provideQuickfix(invalidValue, validValue.toString(), suffix, issue,
					acceptor);
		}
	}

	private void provideQuickfix(String invalidValue, String validValue,
			String suffix, Issue issue, IssueResolutionAcceptor acceptor) {
		provideQuickfix(invalidValue, validValue, suffix, issue, acceptor,
				new ISemanticModification() {
					@Override
					public void apply(EObject element,
							IModificationContext context) {
						Attribute attribute = (Attribute) element;
						Type type = attribute.getValue().getType();
						ID validValueAsID = ID.fromValue(validValue, type);
						attribute.setValue(validValueAsID);
					}
				});
	}

	private void provideQuickfix(String invalidValue, String validValue,
			String suffix, Issue issue, IssueResolutionAcceptor acceptor,
			ISemanticModification semanticModification) {
		acceptor.accept(issue,
				"Replace '" + invalidValue + "' with '" + validValue //$NON-NLS-1$ //$NON-NLS-2$
						+ "'.", //$NON-NLS-1$
				"Use valid '" + validValue + "' instead of invalid '" //$NON-NLS-1$ //$NON-NLS-2$
						+ invalidValue + "' " + suffix + ".", //$NON-NLS-1$ //$NON-NLS-2$
				null, semanticModification);
	}
}
