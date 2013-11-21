package org.eclipse.gef4.mvc.javafx;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import org.eclipse.gef4.mvc.aspects.selection.AbstractSelectionTool;
import org.eclipse.gef4.mvc.parts.IEditPart;
import org.eclipse.gef4.mvc.parts.IRootEditPart;

public class FXSelectionTool extends AbstractSelectionTool<Node> {

	private EventHandler<MouseEvent> pressedHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			if (select(getTargetPart(event), event.isControlDown())) {
				event.consume();
			}
		}
	};

	protected IEditPart<Node> getTargetPart(MouseEvent event) {
		IEditPart<Node> newSelection = getDomain().getViewer().getVisualPartMap().get(
				((Node) event.getTarget()));
		return newSelection;
	}

	@Override
	public void activate() {
		((FXViewer) getDomain().getViewer()).getCanvas().getScene()
				.addEventFilter(MouseEvent.MOUSE_PRESSED, pressedHandler);
	}

	@Override
	public void deactivate() {
		((FXViewer) getDomain().getViewer()).getCanvas().getScene()
				.removeEventFilter(MouseEvent.MOUSE_PRESSED, pressedHandler);
	}

}
