/*******************************************************************************
 * Copyright (c) 2014 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.fx.tools;

import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import org.eclipse.gef4.mvc.fx.parts.FXPartUtils;
import org.eclipse.gef4.mvc.fx.policies.AbstractFXHoverPolicy;
import org.eclipse.gef4.mvc.parts.IVisualPart;
import org.eclipse.gef4.mvc.tools.AbstractTool;
import org.eclipse.gef4.mvc.viewer.IViewer;

public class FXHoverTool extends AbstractTool<Node> {

	public static final Class<AbstractFXHoverPolicy> TOOL_POLICY_KEY = AbstractFXHoverPolicy.class;

	private final EventHandler<MouseEvent> hoverFilter = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			EventTarget target = event.getTarget();
			if (!(target instanceof Node)) {
				return;
			}

			Node targetNode = (Node) target;
			IVisualPart<Node> targetPart = FXPartUtils.getTargetPart(
					getDomain().getViewers(), targetNode, null);
			if (targetPart == null) {
				return;
			}

			AbstractFXHoverPolicy policy = getToolPolicy(targetPart);
			if (policy != null) {
				policy.hover(event);
			}
		}
	};

	protected AbstractFXHoverPolicy getToolPolicy(IVisualPart<Node> targetPart) {
		return targetPart.getAdapter(TOOL_POLICY_KEY);
	}

	@Override
	protected void registerListeners() {
		for (IViewer<Node> viewer : getDomain().getViewers()) {
			viewer.getRootPart().getVisual().getScene()
					.addEventFilter(MouseEvent.MOUSE_MOVED, hoverFilter);
		}
	}

	@Override
	protected void unregisterListeners() {
		for (IViewer<Node> viewer : getDomain().getViewers()) {
			viewer.getRootPart().getVisual().getScene()
					.removeEventFilter(MouseEvent.MOUSE_MOVED, hoverFilter);
		}
	}

}
