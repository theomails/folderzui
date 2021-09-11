package net.progressit.pcomponent;

import java.util.HashMap;
import java.util.Map;

import com.google.common.eventbus.EventBus;

/**
 * Very incomplete. Just getting started.
 * @author theo
 *
 * @param <T>
 */
@SuppressWarnings("unused")
public abstract class PComponent<T> {
	private final EventBus bus = new EventBus();
	private T data;
	private final Map<String, PComponent<Object>> children = new HashMap<>();
	
	public PComponent(T data, PComponent<Object> parent) {
		this.data = data;
		this.bus.register(parent);
	}
	
	public void render() {
		
	}
}
