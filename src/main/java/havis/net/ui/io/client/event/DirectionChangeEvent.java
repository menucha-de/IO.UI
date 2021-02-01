package havis.net.ui.io.client.event;

import havis.device.io.Direction;

import com.google.gwt.event.shared.GwtEvent;

public class DirectionChangeEvent extends GwtEvent<DirectionChangeEventHandler> {

	private static final Type<DirectionChangeEventHandler> TYPE = new Type<>();
	
	private short id;
	private Direction direction;
	
	public DirectionChangeEvent(short id, Direction direction) {
		this.id = id;
		this.direction = direction;
	}
	
	public Direction getDirection() {
		return direction;
	}
	
	public short getId() {
		return id;
	}

	@Override
	public Type<DirectionChangeEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(DirectionChangeEventHandler handler) {
		handler.onDirectionChange(this);
	}
	
	public static Type<DirectionChangeEventHandler> getType() {
		return TYPE;
	}
}
