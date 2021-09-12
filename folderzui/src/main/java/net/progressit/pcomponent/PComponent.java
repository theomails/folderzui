package net.progressit.pcomponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import com.google.common.eventbus.EventBus;

import lombok.Data;

/**
 * Very incomplete. Just getting started.
 * @author theo
 *
 * @param <T>
 */
public abstract class PComponent<T> {
	public static class PComponentException extends RuntimeException{
		private static final long serialVersionUID = 1L;
		public PComponentException(String message) {
			super(message);
		}
		public PComponentException(String message, Throwable t) {
			super(message, t);
		}
	}
	
	public static interface PEventListener{}
	public static interface PDataHandler <T>{
		public Set<Object> grabSelfData(T data);
		public Set<Object> grabChildrenData(T data);
	}
	public static interface PRenderHandler <T>{
		public JComponent getUiComponent();
		public void placeUiComponent(JComponent component);
		public void removeUiComponent(JComponent component);
		public void renderSelf(T data);
		public PChildrenPlan renderChildrenPlan(T data);
	}
	public static interface PLifecycleHandler {
		public void prePlacement();
		public void postPlacement();
		public void preData();
		public void postData();
		public void preRemove();
		public void postRemove();
	}
	public static class PSimpleLifecycleHandler implements PLifecycleHandler{
		@Override
		public void prePlacement() {
		}
		@Override
		public void postPlacement() {
		}
		@Override
		public void preData() {
		}
		@Override
		public void postData() {
		}
		@Override
		public void preRemove() {
		}
		@Override
		public void postRemove() {
		}
	}
	
	@Data
	public static class PChildKey{
		private final String path;
		private final Object data;
	}
	
	@Data
	public static class PChildPlan<U>{
		public final PComponent<U> component;
		public final U data;
		public final PEventListener listener;
	}
	
	@Data
	public static class PChildrenPlan{
		private final List<PChildPlan<Object>> childrenPlan = new ArrayList<>();
		public void addChildPlan(PChildPlan<Object> childPlan) {
			
		}
	}
	
	private final EventBus bus = new EventBus();
	//private final PComponent<?> parent;
	private PEventListener listener = null;
	private T renderedData = null;
	private Set<Object> renderedSelfData = null;
	private Set<Object> renderedChildrenData = null;
	private PChildrenPlan renderedPlan = null;
	private final List<PComponent<?>> renderedComponents = new ArrayList<>();
	
	public PComponent() {
		//this.parent = parent;
		if(getDataHandler()==null) throw new PComponentException("DataHandler is mandatory");
		if(getRenderHandler()==null) throw new PComponentException("RenderHandler is mandatory");
	}
	
	/**
	 * Feel free to set data always. This component will check and re-render only if necessary.
	 * @param inData
	 */
	public void setData(T inData) {
		getLifecycleHandler().preData();
		if(inData.equals(renderedData)) {
			getLifecycleHandler().postData();
			return;
		}
		//Some change is there
		Set<Object> selfData = getDataHandler().grabSelfData(inData);
		Set<Object> childrenData = getDataHandler().grabChildrenData(inData);
		if(!selfData.equals(renderedSelfData)) {
			getRenderHandler().renderSelf(inData);
			renderedSelfData = selfData;
		}
		if(!childrenData.equals(renderedChildrenData)) {
			PChildrenPlan childrenPlan = getRenderHandler().renderChildrenPlan(inData);
			renderedChildrenData = childrenData; //Data has been processed into plan
			diffAndRenderPlan(childrenPlan);
			renderedPlan = childrenPlan; //Plan has been rendered
		}
		getLifecycleHandler().postData();
	}
	
	public void clearListener() {
		if(listener!=null) {
			bus.unregister(listener);
			listener = null;
		}
	}
	public void setListener(PEventListener listener) {
		this.listener = listener;
		bus.register(listener);
	}
	
	protected abstract PDataHandler<T> getDataHandler();
	protected abstract PRenderHandler<T> getRenderHandler();
	protected abstract PLifecycleHandler getLifecycleHandler();

	
	@SuppressWarnings("unchecked")
	private void diffAndRenderPlan(PChildrenPlan childrenPlan) {
		int oldSize = renderedPlan.getChildrenPlan().size();
		int newSize = childrenPlan.getChildrenPlan().size();
		int commonSize = Math.min(oldSize, newSize);
		int matchedCount = 0;
		for(int i=0;i<commonSize;i++) {
			Class<?> oldType = renderedPlan.getChildrenPlan().get(i).getComponent().getClass();
			Class<?> newType = childrenPlan.getChildrenPlan().get(i).getComponent().getClass();
			if(oldType.equals(newType)) {
				matchedCount++;
			}
		}
		if(matchedCount>0) {
			for(int i=0;i<matchedCount;i++) {
				//Swap out info, so that same component is re-used, possibly for different data/listener
				PChildPlan<Object> newPlan = childrenPlan.getChildrenPlan().get(i);
				PComponent<Object> renderedComponent = (PComponent<Object>) renderedComponents.get(i);
				renderedComponent.clearListener();
				renderedComponent.setListener( newPlan.getListener() );
				renderedComponent.setData(newPlan.getData());
			}
		} 
		if(oldSize!=newSize) {
			if(oldSize > matchedCount) {
				//Remove old comps
				for(int i=matchedCount;i<oldSize;i++) {
					PComponent<Object> oldComponent = (PComponent<Object>) renderedComponents.get(oldSize); //Get next available
					JComponent uiComponent = oldComponent.getRenderHandler().getUiComponent();
					oldComponent.getLifecycleHandler().preRemove();
					oldComponent.getRenderHandler().removeUiComponent(uiComponent);
					oldComponent.clearListener();
					oldComponent.getLifecycleHandler().postRemove();
					renderedComponents.remove(oldSize); //Remove the picked one
				}
			}
			if(newSize>matchedCount) {
				//Add new comps
				for(int i=matchedCount;i<newSize;i++) {
					PChildPlan<Object> newPlan = childrenPlan.getChildrenPlan().get(i);
					PComponent<Object> newComponent = newPlan.getComponent(); //Get next available
					JComponent uiComponent = newComponent.getRenderHandler().getUiComponent();
					newComponent.getLifecycleHandler().prePlacement();
					newComponent.getRenderHandler().placeUiComponent(uiComponent);
					newComponent.setListener(newPlan.getListener());
					newComponent.getLifecycleHandler().postPlacement();
					newComponent.setData(newPlan.getData());
				}
			}
		}
	}
}
