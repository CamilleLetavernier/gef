/*******************************************************************************
 * Copyright (c) 2018 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tamas Miklossy (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef.dot.internal.ui.language.hyperlinking;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.dot.internal.language.DotAstHelper;
import org.eclipse.gef.dot.internal.language.dot.EdgeRhsNode;
import org.eclipse.gef.dot.internal.language.dot.EdgeStmtNode;
import org.eclipse.gef.dot.internal.language.dot.NodeId;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.hyperlinking.HyperlinkHelper;
import org.eclipse.xtext.ui.editor.hyperlinking.IHyperlinkAcceptor;

import com.google.inject.Inject;

public class DotHyperlinkHelper extends HyperlinkHelper {

	@Inject
	private EObjectAtOffsetHelper eObjectAtOffsetHelper;

	public void createHyperlinksByOffset(XtextResource resource, int offset,
			IHyperlinkAcceptor acceptor) {

		EObject eObject = eObjectAtOffsetHelper.resolveElementAt(resource,
				offset);
		if (eObject instanceof NodeId) {
			NodeId nodeId = (NodeId) eObject;
			EObject container = nodeId.eContainer();

			// if the node is either left or right part of an edge
			if (container instanceof EdgeStmtNode
					|| container instanceof EdgeRhsNode) {
				NodeId targetSemanticObject = DotAstHelper.getNodeId(nodeId);

				if (targetSemanticObject != null) {
					INode sourceNode = NodeModelUtils.findActualNodeFor(nodeId);

					createHyperlinksTo(resource, sourceNode,
							targetSemanticObject, acceptor);
				}
			}
		}
	}

}
