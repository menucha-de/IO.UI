package havis.net.ui.io.client.event;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasDirectionChangeEventHandlers extends HasHandlers {
	HandlerRegistration addDirectionChangeEventHandler(DirectionChangeEventHandler handler);
}
