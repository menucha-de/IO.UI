package havis.net.ui.io.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface StateChangeEventHandler extends EventHandler {
	void onStateChange(StateChangeEvent event);
}
