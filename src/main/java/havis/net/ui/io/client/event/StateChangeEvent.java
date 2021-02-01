package havis.net.ui.io.client.event;

import havis.device.io.State;

import com.google.gwt.event.shared.GwtEvent;

public class StateChangeEvent extends GwtEvent<StateChangeEventHandler> {

	private static final Type<StateChangeEventHandler> TYPE = new Type<>();
	
	public static enum StateType {
		CURRENT, INITIAL
	}
	
	private short id;
	private State state;
	private StateType stateType;
	
	public StateChangeEvent(short id, StateType stateType, State state) {
		this.id = id;
		this.stateType = stateType;
		this.state = state;
	}
	
	public State getState() {
		return state;
	}
	
	public short getId() {
		return id;
	}

	public StateType getStateType() {
		return stateType;
	}
	
	@Override
	public Type<StateChangeEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(StateChangeEventHandler handler) {
		handler.onStateChange(this);
	}
	
	public static Type<StateChangeEventHandler> getType() {
		return TYPE;
	}
}
