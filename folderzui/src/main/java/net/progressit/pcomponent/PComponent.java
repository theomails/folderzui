package net.progressit.pcomponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JComponent;

import com.google.common.eventbus.EventBus;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

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
	@Data
	public static class PPlacementHandler{
		private final Consumer<JComponent> placer;
		private final Consumer<JComponent> remover;		
	}
	@Data
	public static class PDataHandler <T>{
		private final Function<T, Set<Object>> selfDataGetter;
		private final Function<T, Set<Object>> childrenDataGetter;
	}
	@Data
	public static class PRenderHandler <T>{
		private final Supplier<JComponent> uiComponentMaker;
		private final Consumer<T> selfRenderer;
		private final Function<T, PChildrenPlan> childrenPlanRenderer;
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
	@Builder
	public static class PChildPlan{
		public final PComponent<? extends Object> component;
		public final Object data;
		public final Optional<PEventListener> listener;
	}
	
	@Data
	public static class PChildrenPlan{
		private final List<PChildPlan> childrenPlan = new ArrayList<>();
		public void addChildPlan(PChildPlan childPlan) {
			childrenPlan.add(childPlan);
		}
	}
	
	public static <U> void place(PComponent<U> newComponent, PEventListener listener, U data){
		JComponent uiComponent = newComponent.getRenderHandler().uiComponentMaker.get();
		//uiComponent.setBorder(BorderFactory.createLineBorder(Color.red));
		newComponent.getLifecycleHandler().prePlacement();
		newComponent.getPlacementHandler().placer.accept(uiComponent);
		newComponent.setListener(listener);
		newComponent.getLifecycleHandler().postPlacement();
		newComponent.setData(data);		
	}
	
	private final EventBus bus = new EventBus();
	//private final PComponent<?> parent;
	@Getter(value = AccessLevel.PROTECTED)
	private final PPlacementHandler placementHandler;
	private PEventListener listener = null;
	private T renderedData = null;
	private Set<Object> renderedSelfData = null;
	private Set<Object> renderedChildrenData = null;
	private PChildrenPlan renderedPlan = new PChildrenPlan();
	@SuppressWarnings("rawtypes")
	private final List<PComponent> renderedComponents = new ArrayList<>();
	
	public PComponent(PPlacementHandler placementHandler) {
		//this.parent = parent;
		this.placementHandler = placementHandler;
		if(getDataHandler()==null) throw new PComponentException("DataHandler is mandatory");
		if(getRenderHandler()==null) throw new PComponentException("RenderHandler is mandatory");
	}
	
	protected T getData() {
		return renderedData;
	}
	/**
	 * Feel free to set data always. This component will check and re-render only if necessary.
	 * @param inData
	 */
	public void setData(T inData) {
		System.out.println("SET DATA: " + getClass().getSimpleName() + ": " + inData);
		
		getLifecycleHandler().preData();
		if(inData.equals(renderedData)) {
			getLifecycleHandler().postData();
			return;
		}
		//Some change is there
		Set<Object> selfData = getDataHandler().selfDataGetter.apply(inData);
		Set<Object> childrenData = getDataHandler().childrenDataGetter.apply(inData);
		if(!selfData.equals(renderedSelfData)) {
			getRenderHandler().selfRenderer.accept(inData);
			renderedSelfData = selfData;
		}
		if(!childrenData.equals(renderedChildrenData)) {
			PChildrenPlan childrenPlan = getRenderHandler().childrenPlanRenderer.apply(inData);
			renderedChildrenData = childrenData; //Data has been processed into plan
			diffAndRenderPlan(childrenPlan);
			renderedPlan = childrenPlan; //Plan has been rendered
		}
		renderedData = inData;
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
		if(listener!=null) {
			bus.register(listener);
		}
	}
	
	protected abstract PDataHandler<T> getDataHandler();
	protected abstract PRenderHandler<T> getRenderHandler();
	protected abstract PLifecycleHandler getLifecycleHandler();

	protected void post(Object event) {
		System.out.println("POST: " + getClass().getSimpleName() + ": " + event);
		bus.post(event);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
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
				PChildPlan newPlan = childrenPlan.getChildrenPlan().get(i);
				PComponent<Object> renderedComponent = renderedComponents.get(i);
				renderedComponent.clearListener();
				renderedComponent.setListener( newPlan.getListener().orElse(null) );
				renderedComponent.setData(newPlan.getData());
			}
		} 
		if(oldSize!=newSize) {
			if(oldSize > matchedCount) {
				//Remove old comps
				for(int i=matchedCount;i<oldSize;i++) {
					PComponent oldComponent = renderedComponents.get(oldSize); //Get next available
					JComponent uiComponent = (JComponent) oldComponent.getRenderHandler().uiComponentMaker.get();
					oldComponent.getLifecycleHandler().preRemove();
					oldComponent.getPlacementHandler().remover.accept(uiComponent);
					oldComponent.clearListener();
					oldComponent.getLifecycleHandler().postRemove();
					renderedComponents.remove(oldSize); //Remove the picked one
				}
			}
			if(newSize>matchedCount) {
				//Add new comps
				for(int i=matchedCount;i<newSize;i++) {
					PChildPlan newPlan = childrenPlan.getChildrenPlan().get(i);
					PComponent newComponent = newPlan.getComponent(); //Get next available
					JComponent uiComponent = (JComponent) newComponent.getRenderHandler().uiComponentMaker.get();
					newComponent.getLifecycleHandler().prePlacement();
					newComponent.getPlacementHandler().placer.accept(uiComponent);
					newComponent.setListener(newPlan.getListener().orElse(null));
					newComponent.getLifecycleHandler().postPlacement();
					newComponent.setData(newPlan.getData());
					renderedComponents.add(newComponent);
				}
			}
		}
	}
}
