package org.openmrs.module.openhmis.eventbasedbilling.api.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.openmrs.OpenmrsObject;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.event.Event;
import org.openmrs.event.Event.Action;
import org.openmrs.module.openhmis.billableobjects.api.IBillableObjectsService;
import org.openmrs.module.openhmis.billableobjects.api.IBillingHandlerService;
import org.openmrs.module.openhmis.billableobjects.api.model.IBillingHandler;
import org.openmrs.module.openhmis.billableobjects.api.type.IBillableObject;
import org.openmrs.module.openhmis.eventbasedbilling.api.EventListenerFactory;
import org.openmrs.module.openhmis.eventbasedbilling.api.IBillingEventService;
import org.openmrs.module.openhmis.eventbasedbilling.api.util.BillableObjectEventListener;
import org.openmrs.module.openhmis.eventbasedbilling.api.util.HandlerChangeListener;

public class BillingEventServiceImpl extends BaseOpenmrsService implements IBillingEventService {
	/**
	 * This set is used to track billable object classes that have events bound
	 * to the event listener.
	 */
	private static Set<Class<? extends IBillableObject>> handledBillabledObjectClasses =
			new HashSet<Class<? extends IBillableObject>>();
	
	private static IBillableObjectsService billableObjectsService;
	private static IBillingHandlerService billingHandlerService;
	private static BillableObjectEventListener<OpenmrsObject> objectEventListener;
	private static HandlerChangeListener handlerChangeListener;
	
	@Override
	public void rebindListenerForAllHandlers() {
		unbindListenerForAllHandlers();
		for (Class<?> handledClass : Context.getService(IBillingHandlerService.class).getActivelyHandledClasses()) {
			Set<IBillingHandler<?>> handlers = getBillingHandlerService().getHandlersForClassName(handledClass.getName());
			for (IBillingHandler handler : handlers) {
				Class<? extends IBillableObject> billableObjectType = getBillableObjectsService().getBillableObjectClassForClassName(handledClass.getName());
				if (billableObjectType == null)
					throw new APIException("Couldn't find a class extending " + IBillableObject.class.getSimpleName() + " for " + handledClass.getSimpleName() + ".");
				getObjectEventListener().registerHandler(billableObjectType.getName(), handler);
				handledBillabledObjectClasses.add(billableObjectType);
				Event.subscribe(
						billableObjectType,
						Action.CREATED.toString(),
						objectEventListener
				);
			}
		}
	}
	
	@Override
	public void unbindListenerForAllHandlers() {
		Class<?>[] registeredClasses = new Class<?>[handledBillabledObjectClasses.size()];
		handledBillabledObjectClasses.toArray(registeredClasses);
		for (Class<?> cls : registeredClasses) {
			getObjectEventListener().unregisterHandler(cls.getName());
			Event.unsubscribe(
					cls,
					Action.CREATED,
					getObjectEventListener()
			);
			handledBillabledObjectClasses.remove(cls);
		}
	}

	private static final Set<Action> handlerChangeEvents = new HashSet<Action>(Arrays.asList(
		Action.CREATED,
		Action.RETIRED,
		Action.UNRETIRED,
		Action.PURGED
	));
	
	@Override
	public void bindBillingHandlerChangeListener() {
		for (Class<? extends IBillingHandler> cls : getBillingHandlerService().getBillingHandlerClasses()) {
			for (Action action : handlerChangeEvents) 
				Event.subscribe(cls, action.toString(), getHandlerChangeListener());
		}
	}
	
	@Override
	public void unbindBillingHandlerChangeListener() {
		for (Class<? extends IBillingHandler> cls : getBillingHandlerService().getBillingHandlerClasses()) {
			for (Action action : handlerChangeEvents) 
				Event.unsubscribe(cls, action, getHandlerChangeListener());
		}
	}

	/* Getters & setters */
	
	@Override
	public IBillingHandlerService getBillingHandlerService() {
		if (billingHandlerService == null)
			billingHandlerService = Context.getService(IBillingHandlerService.class);
		return billingHandlerService;
	}
	@Override
	public void setBillingHandlerService(IBillingHandlerService billingHandlerService) {
		BillingEventServiceImpl.billingHandlerService = billingHandlerService;
	}
	@Override
	public IBillableObjectsService getBillableObjectsService() {
		if (billableObjectsService == null)
			billableObjectsService = Context.getService(IBillableObjectsService.class);
		return billableObjectsService;
	}
	@Override
	public void setBillableObjectsService(IBillableObjectsService billableObjectsService) {
		BillingEventServiceImpl.billableObjectsService = billableObjectsService;
	}
	@Override
	public BillableObjectEventListener<OpenmrsObject> getObjectEventListener() {
		if (objectEventListener == null)
			objectEventListener = EventListenerFactory.getInstance(BillableObjectEventListener.class);
		return objectEventListener;
	}
	@Override
	public  void setObjectEventListener(BillableObjectEventListener<OpenmrsObject> eventListener) {
		BillingEventServiceImpl.objectEventListener = eventListener;
	}
	@Override
	public HandlerChangeListener getHandlerChangeListener() {
		if (handlerChangeListener == null)
			handlerChangeListener = EventListenerFactory.getInstance(HandlerChangeListener.class);
		return handlerChangeListener;
	}
	@Override
	public void setHandlerChangeListener(HandlerChangeListener handlerChangeListener) {
		BillingEventServiceImpl.handlerChangeListener = handlerChangeListener;
	}
	
}
