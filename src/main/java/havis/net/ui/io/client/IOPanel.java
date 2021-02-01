package havis.net.ui.io.client;

import havis.device.io.Direction;
import havis.device.io.IOConfiguration;
import havis.device.io.State;
import havis.net.ui.io.client.event.DirectionChangeEvent;
import havis.net.ui.io.client.event.DirectionChangeEventHandler;
import havis.net.ui.io.client.event.HasDirectionChangeEventHandlers;
import havis.net.ui.io.client.event.HasStateChangeEventHandlers;
import havis.net.ui.io.client.event.StateChangeEvent;
import havis.net.ui.io.client.event.StateChangeEventHandler;
import havis.net.ui.shared.client.widgets.ThreeStateSwitch;
import havis.net.ui.shared.client.widgets.ThreeStateSwitch.Position;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;

public class IOPanel extends Composite
		implements HasEnabled, HasDirectionChangeEventHandlers, HasStateChangeEventHandlers {

	@UiField
	Label label;
	@UiField(provided = true)
	ValueListBox<Direction> direction = new ValueListBox<Direction>(new Renderer<Direction>() {
		@Override
		public String render(Direction object) {
			return object.toString();
		};

		@Override
		public void render(Direction object, Appendable appendable) throws java.io.IOException {
			String s = render(object);
			appendable.append(s);
		};
	});
	@UiField
	ToggleButton state;

	@UiField
	ThreeStateSwitch initial;

	private boolean allowDirectionChange;
	private boolean allowStateChange;
	private boolean allowInitialStateChange;
	private boolean enabled;
	private short ioId;

	private static IOPanelUiBinder uiBinder = GWT.create(IOPanelUiBinder.class);

	interface IOPanelUiBinder extends UiBinder<Widget, IOPanel> {
	}

	public IOPanel(IOConfiguration ioConf, String pinName, boolean allowDirectionChange, boolean allowStateChange, boolean allowInitialStateChange) {
		this();

		this.allowDirectionChange = allowDirectionChange;
		this.allowStateChange = allowStateChange;
		this.allowInitialStateChange = allowInitialStateChange;

		setConfiguration(ioConf, pinName, false);
	}

	private Position toPosition(State state) {
		if (state == null)
			return Position.MIDDLE;

		switch (state) {
		case HIGH:
			return Position.RIGHT;
		case LOW:
			return Position.LEFT;
		case UNKNOWN:
			return Position.MIDDLE;
		default:
			return Position.MIDDLE;
		}
	}

	private boolean toBoolean(State state) {
		switch (state) {
		case HIGH:
			return true;
		case LOW:
			return false;
		case UNKNOWN:
			return false;
		default:
			return false;
		}
	}

	private State fromPosition(Position position) {
		switch (position) {
		case LEFT:
			return State.LOW;
		case RIGHT:
			return State.HIGH;
		case MIDDLE:
			return null;
		default:
			return null;
		}
	}

	private State fromBoolean(boolean state) {
		return state ? State.HIGH : State.LOW;
	}

	private boolean stateEnabled() {
		return direction.getValue().equals(Direction.OUTPUT) && allowStateChange;
	}

	private boolean directionEnabled() {
		return enabled && allowDirectionChange;
	}
	
	private boolean initialStateEnabled() {
		return direction.getValue().equals(Direction.OUTPUT) && allowInitialStateChange;
	}

	public void setConfiguration(IOConfiguration ioConf, String pinName, boolean observe) {
		ioId = ioConf.getId();

		label.setText(pinName);

		direction.setValue(ioConf.getDirection());
		direction.setAcceptableValues(Arrays.asList(Direction.values()));
		direction.setEnabled(directionEnabled());

		state.setEnabled(stateEnabled());
		if (!observe || getIoDirection().equals(Direction.INPUT)) {
			state.setValue(toBoolean(ioConf.getState()));
		}

		initial.setEnabled(initialStateEnabled());

		initial.setValue(toPosition(ioConf.getInitialState()));
	}

	public State getInitialState() {
		return fromPosition(initial.getValue());
	}

	public void setInitialState(State ioState) {
		initial.setValue(toPosition(ioState));
		initial.setEnabled(initialStateEnabled());
	}

	public State getIoState() {
		return fromBoolean(state.getValue());
	}

	public void setIoState(State ioState) {
		state.setValue(toBoolean(ioState));
		state.setEnabled(stateEnabled());
	}

	public Direction getIoDirection() {
		return direction.getValue();
	}

	public void setIoDirection(Direction ioDirection) {
		direction.setValue(ioDirection);
		direction.setEnabled(directionEnabled());
	}

	public short getIoId() {
		return ioId;
	}

	public void setIoId(short ioId) {
		this.ioId = ioId;
	}

	public IOPanel() {
		initWidget(uiBinder.createAndBindUi(this));

	}

	public void addDirectionChangeHandler(ValueChangeHandler<Direction> handler) {
		direction.addValueChangeHandler(handler);
	}

	public void addStateChangeHandler(ValueChangeHandler<Boolean> handler) {
		state.addValueChangeHandler(handler);
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		direction.setEnabled(directionEnabled());
		state.setEnabled(stateEnabled());
		initial.setEnabled(initialStateEnabled());
	}

	@UiHandler("direction")
	void directionChange(ValueChangeEvent<Direction> e) {
		fireEvent(new DirectionChangeEvent(ioId, getIoDirection()));
		state.setEnabled(stateEnabled());
		initial.setEnabled(initialStateEnabled());
	}

	@UiHandler("state")
	void stateChange(ValueChangeEvent<Boolean> e) {
		fireEvent(new StateChangeEvent(ioId, StateChangeEvent.StateType.CURRENT, getIoState()));
	}

	@UiHandler("initial")
	void initialChange(ValueChangeEvent<Position> e) {
		fireEvent(new StateChangeEvent(ioId, StateChangeEvent.StateType.INITIAL, getInitialState()));
	}

	@Override
	public HandlerRegistration addStateChangeEventHandler(StateChangeEventHandler handler) {
		return addHandler(handler, StateChangeEvent.getType());
	}

	@Override
	public HandlerRegistration addDirectionChangeEventHandler(DirectionChangeEventHandler handler) {
		return addHandler(handler, DirectionChangeEvent.getType());
	}
}
