package havis.net.ui.io.client;

import java.util.HashMap;
import java.util.List;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.IsWidget;

import havis.device.io.Direction;
import havis.device.io.IOConfiguration;
import havis.device.io.State;
import havis.net.rest.io.async.IODeviceServiceAsync;
import havis.net.ui.io.client.event.DirectionChangeEvent;
import havis.net.ui.io.client.event.DirectionChangeEventHandler;
import havis.net.ui.io.client.event.StateChangeEvent;
import havis.net.ui.io.client.event.StateChangeEventHandler;
import havis.net.ui.shared.client.ErrorPanel;
import havis.net.ui.shared.data.HttpMethod;

public class IOHardwarePresenter {

	private static final String UNEXPECTED_ERROR = "An unexpected error occurred.";
	private static final String CONNECTION_ERROR = "No active connection to IO Device.";
	private static final String PARAMETER_ERROR = "Invalid parameter!";
	private static final String LOCKED_ERROR = "IO Device is locked.";
	private static final String UNAVAILABLE_ERROR = "IO Device is currently in use.";
	private static final String[] PIN_NAMES = new String[] { "HS1", "HS2", "HS3", "HS4", "SWS1/SWD1", "SWS2/SWD2",
			"LS1", "LS2" };

	interface View extends IsWidget {
		void setPresenter(IOHardwarePresenter presenter);

		FlowPanel getIOPanels();

		HasValue<Boolean> getObserveButton();

		HasVisibility getRefreshButton();

		ErrorPanel getErrorPanel();
	}

	private View view;
	private IODeviceServiceAsync service = GWT.create(IODeviceServiceAsync.class);
	private Timer timer;
	private HashMap<Short, IOPanel> pinMap = new HashMap<>();

	private class OptionsCallback implements MethodCallback<Void> {
		private boolean allowed = false;

		public boolean isAllowed() {
			return allowed;
		}

		@Override
		public void onFailure(Method method, Throwable exception) {
			allowed = false;
		}

		@Override
		public void onSuccess(Method method, Void response) {
			allowed = HttpMethod.PUT.isAllowed(method.getResponse());
		}
	}

	private OptionsCallback directionChange = new OptionsCallback();
	private OptionsCallback stateChange = new OptionsCallback();
	private OptionsCallback initialStateChange = new OptionsCallback();
	private OptionsCallback keepAliveChange = new OptionsCallback();
	private boolean observePending;

	public IOHardwarePresenter(final View view) {
		this.view = view;
		this.view.setPresenter(this);
		initialize();
	}

	private void showError(int httpStatus) {
		switch (httpStatus) {
		case 400:
			view.getErrorPanel().showErrorMessage(PARAMETER_ERROR);
			break;
		case 423:
			view.getErrorPanel().showErrorMessage(LOCKED_ERROR);
			break;
		case 500:
			view.getErrorPanel().showErrorMessage(UNEXPECTED_ERROR);
			break;
		case 502:
			view.getErrorPanel().showErrorMessage(CONNECTION_ERROR);
			break;
		case 503:
			view.getErrorPanel().showErrorMessage(UNAVAILABLE_ERROR);
			break;
		default:
			break;
		}
	}

	private void renewDirection(final IOPanel row) {
		service.getIODirection(row.getIoId(), new MethodCallback<Direction>() {

			@Override
			public void onSuccess(Method method, Direction response) {
				row.setIoDirection(response);
				renewState(row);
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				showError(method.getResponse().getStatusCode());
			}
		});
	}

	private void renewState(final IOPanel row) {
		service.getIOState(row.getIoId(), new MethodCallback<State>() {

			@Override
			public void onSuccess(Method method, State response) {
				row.setIoState(response);
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				showError(method.getResponse().getStatusCode());
			}
		});
	}

	private void addIOPanel(final IOPanel row) {
		row.addDirectionChangeEventHandler(new DirectionChangeEventHandler() {
			@Override
			public void onDirectionChange(final DirectionChangeEvent event) {
				final Direction dir = row.getIoDirection();
				service.setIODirection(event.getId(), event.getDirection(), new MethodCallback<Void>() {
					@Override
					public void onSuccess(Method method, Void response) {
						renewState(row);
					}

					@Override
					public void onFailure(Method method, Throwable exception) {
						row.setIoDirection(dir);
						showError(method.getResponse().getStatusCode());
					}
				});
			}
		});
		row.addStateChangeEventHandler(new StateChangeEventHandler() {
			@Override
			public void onStateChange(final StateChangeEvent event) {
				if (event.getStateType() == StateChangeEvent.StateType.CURRENT) {
					final State st = row.getIoState();
					service.setIOState(event.getId(), event.getState(), new MethodCallback<Void>() {
						@Override
						public void onSuccess(Method method, Void response) {

						}

						@Override
						public void onFailure(Method method, Throwable exception) {
							row.setIoState(st);
							int status = method.getResponse().getStatusCode();
							showError(status);
							if (status == 400) {
								renewDirection(row);
							}
						}
					});
				} else if (event.getStateType() == StateChangeEvent.StateType.INITIAL) {
					final State st = row.getInitialState();
					service.setIOInitialState(event.getId(), event.getState(), new MethodCallback<Void>() {
						@Override
						public void onSuccess(Method method, Void response) {

						}

						@Override
						public void onFailure(Method method, Throwable exception) {
							row.setInitialState(st);
							int status = method.getResponse().getStatusCode();
							showError(status);
							if (status == 400) {
								renewDirection(row);
							}
						}
					});
					
				}
			}
		});
		view.getIOPanels().add(row);
	}

	public void onRefresh() {
		if (!observePending) {
			observePending = true;
			service.getIOConfigurations(new MethodCallback<List<IOConfiguration>>() {

				@Override
				public void onSuccess(Method method, List<IOConfiguration> response) {
					for (IOConfiguration ioConf : response) {
						short id = ioConf.getId();
						IOPanel row = pinMap.get(id);
						String pinName = "Pin " + id;
						int index = id - 1;
						if (index < PIN_NAMES.length) {
							pinName = PIN_NAMES[index];
						}
						if (row == null) {
							row = new IOPanel(ioConf, pinName, directionChange.isAllowed(), stateChange.isAllowed(), initialStateChange.isAllowed());
							addIOPanel(row);
							pinMap.put(id, row);
						} else {
							row.setConfiguration(ioConf, pinName, view.getObserveButton().getValue());
						}
						row.setEnabled(!view.getObserveButton().getValue());
					}
					observePending = false;
				}

				@Override
				public void onFailure(Method method, Throwable exception) {
					view.getObserveButton().setValue(false, true);
					showError(method.getResponse().getStatusCode());
					observePending = false;
				}
			});
		}
	}

	private void startTimer() {
		onRefresh();
		timer = new Timer() {
			@Override
			public void run() {
				onRefresh();
			}
		};
		timer.scheduleRepeating(1000);
	}

	private void stopTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		setRowsEnabled(!view.getObserveButton().getValue());
	}

	private void initialize() {
		service.optionsKeepAliveConfiguration(keepAliveChange);
		service.optionsIODirection(directionChange);
		service.optionsIOState(stateChange);
		service.optionsIOInitialState(initialStateChange);
		onRefresh();
	}

	private void setRowsEnabled(boolean enabled) {
		for (IOPanel row : pinMap.values()) {
			row.setEnabled(enabled);
		}
	}

	public void onObserve() {
		view.getRefreshButton().setVisible(!view.getObserveButton().getValue());
		if (view.getObserveButton().getValue()) {
			startTimer();
		} else {
			stopTimer();
		}
	}
}
