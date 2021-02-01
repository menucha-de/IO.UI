package havis.net.ui.io.client;

import org.fusesource.restygwt.client.Defaults;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

import havis.net.ui.shared.client.ErrorPanel;
import havis.net.ui.shared.resourcebundle.ResourceBundle;

public class IOHardware extends Composite implements EntryPoint, IOHardwarePresenter.View {
	
	@UiField FlowPanel ioPanels;
	@UiField ToggleButton observeButton;
	@UiField Button refreshButton;
	
	private static IOHardwareUiBinder uiBinder = GWT
			.create(IOHardwareUiBinder.class);

	interface IOHardwareUiBinder extends UiBinder<Widget, IOHardware> {
	}

	private IOHardwarePresenter presenter;

	public IOHardware() {
		initWidget(uiBinder.createAndBindUi(this));
		Defaults.setDateFormat(null);
		ResourceBundle.INSTANCE.css().ensureInjected();
		new IOHardwarePresenter(this);
	}

	@UiHandler("observeButton")
	void onObserveClick(ValueChangeEvent<Boolean> e) {
		presenter.onObserve();
	}

	@UiHandler("refreshButton")
	void onRefresh(ClickEvent e) {
		presenter.onRefresh();
	}

	@Override
	public void setPresenter(IOHardwarePresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public FlowPanel getIOPanels() {
		return ioPanels;
	}

	@Override
	public HasValue<Boolean> getObserveButton() {
		return observeButton;
	}

	@Override
	public HasVisibility getRefreshButton() {
		return refreshButton;
	}

	@Override
	public ErrorPanel getErrorPanel() {
		return new ErrorPanel(0, 0);
	}

	@Override
	public void onModuleLoad() {
		RootLayoutPanel.get().add(this);
	}
	
}
