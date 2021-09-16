package net.progressit.pcomponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javax.swing.JComponent;

import com.google.common.eventbus.EventBus;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

/**
 * <code>PComponent</code> is the Progressive Swing framework core class, which defines a Reactive Visual Component.
 * <p>Any Progressive component is created as a sub-class of this, with two template type specifications. A PComponent is defined around a single Swing JComponent
 * which it manages (however that component could be a container, and more nested JComponents could be handled by the PComponent itself). Each PComponent could have 
 * several PComponents as children. The framework takes care of rendering the children which are PComponents.
 * <p>The PComponent is reactive in the sense that the when-to-redraw decision is taken automatically by the framework, whenever data is updated.
 * Also, rendering of own Swing component(s) and rendering of child PComponents are triggered by the framework.
 * <p>PComponent provides several lifecycle hooks to enable the component to be well defined. Using these, the PComponent can manage the Swing components that it wraps.
 * <p>These are the steps of defining the PComponent tree to build an app:
 * <ol>
 * <li>Sub-class PComponent to build each custom component which wraps one or a few Swing components.
 * <li>Define two data structures for each component - the format of the props passed from parent, and the format of the state/data maintained in the component
 * <li>Parent can pass in the whole data of the component, making the child completely in sync with the parent, or, the parent can just provide the context while the
 * child component takes more ownership of the data as well as more responsibility in terms of functionality.
 * <li>Child should never edit the props passed in. However, child can manage its own data. Child should keep the framework informed about changes in its data. In fact, it is 
 * recommended that the child not store any data in local variables, and rather just pass the data directly to <code>setData</code> method. Note that the props have to be merged
 * into the data, and passed on to the framework. This it because the framework just hands over the props and does not monitor or react to the props.
 * <li>Parent has to provide placement logic through a <code>PPlacementHandler</code> for each child, and the lambda function will be used by framework when the child's Swing component needs to be placed.
 * <li>PComponent subclass itself has to provide a <code>PDataHandler</code> which will define which part of the data field affects its own Swing components
 * vs which part of the data affects its PComponent children.
 * <li>PComponent also needs to provide lambdas via <code>PRenderHandler</code> to render the data into (1) its own Swing components and (2) to generate a render plan of PComponent children
 * <li>PComponent also needs to provide a <code>PSimpleLifecycleHandler</code> to hook into its own lifecycle.
 * <li>Having setup all the above, at the time of using the PComponent, a <code>PEventListener</code> has to be provided along with the props.
 * <li>Most of the PComponent tree of the application will be used inside <code>childrenPlanRenderer</code> lambda of the parent PComponent's <code>PRenderHandler</code>
 * <li>However, the top-most one (or few) PComponents can be placed manually by calling the <code>PComponent.place</code> static method. 
 * <li>Once all the lambdas and handlers are setup for a PComponent, the rendering only starts on the first <code>setData</code> call. However, <code>setData</code> is never called
 * by framework. Framework only calls <code>setProps</code> to trigger the component. The component has to merge it with/pass it on as <code>setData</code>.
 * </ol>
 * <p>Some tips for defining the generic types for PComponents:
 * <ol>
 * <li><code>T</code> represents the type of the data, while <code>U</code> represents the type of the props.
 * <li>If the component uses no props or data (highly unlikely to be useful), both T and U can be left as Object or some such class.
 * <li>If the component uses no props and uses only data, U can be left as Object, etc.
 * <li>If the component has a prop, at least that prop has to be part of the data (as the framework reacts only to data not props).
 * <li>In the above case, the prop can be a scalar data class like String or Long. Or, if there are more props, it can be a custom type.
 * <li>Likewise, the data can also be just a scalar type matching the prop, or it can be a bigger type to hold more internal state of the component.
 * <li>The <code>data</code> has to be an immutable data structure. This is because we need the old data tree to be intact to compare it to the new tree. This is because
 * mutating the data object will also change the "old" copy which is retained by the framework. Using some other mechanism to overcome this limitation, like cloning or serialization, would also be sub-optimal.
 * <li>For derieving updated versions of the immutable data class, the recommended way is to use the <code>Lombok</code> <code>@Builder</code> annotation with the <code>toBuilder</code> flag enabled.
 * This allows for converting an existing object back to a builder, and from there creating a derived copy, with just selected fields getting new values.
 * <li>All listeners and hooks are called on Swing Event dispatcher threads. You may start other custom threads, but, get back on Swing event dispatcher thread to interact with the F/w.
 * @author theo
 *
 * @param <T>
 */
public abstract class PComponent<T,U> {
	private static final Logger LOGGER = Logger.getLogger( PComponent.class.getName() );
	
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
		/**
		 * Will be called after placed, and props assigned.
		 */
		public void postPlacement();
		public void preProps();
		public void postProps();
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
		public void preProps() {
		}
		@Override
		public void postProps() {
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
		public final PComponent<?,?> component;
		public final Object props;
		public final Optional<PEventListener> listener;
	}
	
	@Data
	public static class PChildrenPlan{
		private final List<PChildPlan> childrenPlan = new ArrayList<>();
		public void addChildPlan(PChildPlan childPlan) {
			childrenPlan.add(childPlan);
		}
	}
	
	public static <V> void place(PComponent<?,V> newComponent, PEventListener listener, V props){
		LOGGER.info(string("Place"));
		JComponent uiComponent = newComponent.getRenderHandler().uiComponentMaker.get();
		//uiComponent.setBorder(BorderFactory.createLineBorder(Color.red));
		newComponent.getLifecycleHandler().prePlacement();
		newComponent.getPlacementHandler().placer.accept(uiComponent);
		newComponent.setListener(listener);
		newComponent.setProps(props);
		newComponent.getLifecycleHandler().postPlacement();
	}
	
	private final EventBus bus = new EventBus();
	//private final PComponent<?> parent;
	@Getter(value = AccessLevel.PROTECTED)
	private final PPlacementHandler placementHandler;
	private PEventListener listener = null;
	private T renderedData = null;
	private U renderedProps = null;
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
	protected void setProps(U props) {
		LOGGER.info(string("setProps", props));
		this.getLifecycleHandler().preProps();
		this.renderedProps = props;
		this.getLifecycleHandler().postProps();
	}
	protected U getProps() {
		return renderedProps;
	}
	protected T getData() {
		return renderedData;
	}
	/**
	 * Feel free to set data always. This component will check and re-render only if necessary.
	 * @param inData
	 */
	protected void setData(T inData) {
		LOGGER.info(string("setData", inData));
		
		if((inData==null && renderedData==null) || inData.equals(renderedData)) {
			return;
		}
		//Some change is there
		Set<Object> selfData = getDataHandler().selfDataGetter.apply(inData);
		Set<Object> childrenData = getDataHandler().childrenDataGetter.apply(inData);
		if(!selfData.equals(renderedSelfData)) {
			getRenderHandler().selfRenderer.accept(inData);
			renderedSelfData = selfData;
		}
		renderedData = inData; //Set before going to children.
		if(!childrenData.equals(renderedChildrenData)) {
			PChildrenPlan childrenPlan = getRenderHandler().childrenPlanRenderer.apply(inData);
			renderedChildrenData = childrenData; //Data has been processed into plan
			diffAndRenderPlan(childrenPlan);
			renderedPlan = childrenPlan; //Plan has been rendered
		}
	}
	
	/**
	 * Just to make sure that next time we don't register the same listener again during setListener.
	 */
	public void clearListener() {
		if(listener!=null) {
			bus.unregister(listener);
			listener = null;
		}
	}
	/**
	 * Set listener has to be called each time when a component is placed (used), because, each time
	 * the same component could be used for a completely different purpose.
	 * @param listener
	 */
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
		LOGGER.info(string("post", event));
		bus.post(event);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void diffAndRenderPlan(PChildrenPlan childrenPlan) {
		LOGGER.info(string("diffAndRenderPlan", "children size", childrenPlan.getChildrenPlan().size()));
		int oldSize = renderedPlan.getChildrenPlan().size();
		int newSize = childrenPlan.getChildrenPlan().size();
		int commonSize = Math.min(oldSize, newSize);
		int matchedCount = 0;
		for(int i=0;i<commonSize;i++) {
			Class<?> oldType = renderedPlan.getChildrenPlan().get(i).getComponent().getClass();
			Class<?> newType = childrenPlan.getChildrenPlan().get(i).getComponent().getClass();
			if(oldType.equals(newType)) { //The user implemented sub class should not be parameterised.
				matchedCount++;
			}
		}
		if(matchedCount>0) {
			for(int i=0;i<matchedCount;i++) {
				//Swap out info, so that same component is re-used, possibly for different data/listener
				PChildPlan newPlan = childrenPlan.getChildrenPlan().get(i);
				PComponent<Object, Object> renderedComponent = renderedComponents.get(i);
				renderedComponent.clearListener();
				renderedComponent.setListener( newPlan.getListener().orElse(null) );
				renderedComponent.getLifecycleHandler().preProps();
				renderedComponent.setProps(newPlan.getProps());
				renderedComponent.getLifecycleHandler().postProps();
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
					renderedComponents.add(newComponent);
					newComponent.getLifecycleHandler().prePlacement();
					newComponent.getPlacementHandler().placer.accept(uiComponent);
					newComponent.setListener(newPlan.getListener().orElse(null));
					newComponent.getLifecycleHandler().postPlacement();
					newComponent.getLifecycleHandler().preProps();
					newComponent.setProps(newPlan.getProps());
					newComponent.getLifecycleHandler().postProps();
				}
			}
		}
	}
	
	private static String string(Object...objects) {
		StringBuilder builder = new StringBuilder(1000);
		for(Object o:objects) {
			builder.append(o==null?"*null*":o.toString()).append(" ");
		}
		return builder.toString();
	}
}
