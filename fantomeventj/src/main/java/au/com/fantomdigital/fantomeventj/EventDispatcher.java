/*
 * Copyright (C) 2015 Fantom Digital Pty. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package au.com.fantomdigital.fantomeventj;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/*
 * EventSatellite: A event dispatcher satellite that can attach itself
 * to any class targets using the main looper.
 *
 * <p>This class is hopefully safe for concurrent use.</p>
 *
 * Project: fantomeventj
 *
 * @author sfdi
 * @date 15/08/15
 */
public class EventDispatcher implements IEventDispatcher {
    public static final String DEFAULT_IDENTIFIER = "default";

    /** Identifier used to differentiate the event satellite instance. */
    private final String _id;

    /** Looper binder for register, unregister, and posting events. */
    private final LooperBinder _binder;

    /** Target of satellite attachment. */
    private final Object _target;

    /** All registered event handlers, indexed by event class. */
    private final ConcurrentMap<Class<?>, Set<EventListener>> _listeners =
            new ConcurrentHashMap<Class<?>, Set<EventListener>>();

    /** Queues of event with handler pairs on the binded looper */
    private final ThreadLocal<ConcurrentLinkedQueue<EventWithHandler>> _queue =
            new ThreadLocal<ConcurrentLinkedQueue<EventWithHandler>>() {
                @Override protected ConcurrentLinkedQueue<EventWithHandler> initialValue() {
                    return new ConcurrentLinkedQueue<EventWithHandler>();
                }
            };

    /** Check if satellite is in the process of dispatching. */
    private final ThreadLocal<Boolean> _processingQueue = new ThreadLocal<Boolean>() {
        @Override protected Boolean initialValue() {
            return false;
        }
    };

    /**
     * Creates a new EventSatellite with the given {@code id} and {@code target} that enforces actions on the main looper.
     *
     * @param id a brief name for this satellite, for debugging purposes.  Should be a valid Java identifier.
     */
    public EventDispatcher(String id, Object target) {
        this(LooperBinder.MAIN, id, target);
    }

    /**
     * Creates a new EventSatellite named "default" with the given {@code id} and {@code target} for actions.
     *
     * @param binder Looper Binder for register, unregister, and post actions.
     */
    public EventDispatcher(LooperBinder binder, Object target) {
        this(binder, DEFAULT_IDENTIFIER, target);
    }

    /**
     * Creates a new EventSatellite with the given {@code binder} for actions and the given {@code id} and {@code target}.
     *
     * @param binder Looper Binder for register, unregister, and post actions.
     * @param id A brief name for this satellite, for debug needs. Should be a valid Java id.
     */
    public EventDispatcher(LooperBinder binder, String id, Object target) {
        _id = id;
        _binder = binder;
        _target = target;
    }

    /**
     * Add event listener {@code event} to receive events and listeners.
     *
     * <p>Listeners will be listening and waiting for dispatchments.</p>
     *
     * <p>There can only be multiple event handlers from one target to the dispatcher.</p>
     *
     * <p>Event argument is really not needed but it is just there to feel like AS3 for
     * people that are used to the flex ways.</p>
     *
     * @param event whose handler methods should be registered.
     * @throws NullPointerException if the object is null.
     */
    public void addEventListener(IBaseEvent event, EventListener listener) {
        if (_target == null) {
            throw new IllegalStateException("Event dispatcher target " + _target + " can not be null.");
        }

        // binds the looper
        _binder.bind(this);

        Class type = event.getClass();
        // find the listener set of this event
        Set<EventListener> setListeners = _listeners.get(type);
        if (setListeners == null) {
            // concurrent init set
            Set<EventListener> newSet = new CopyOnWriteArraySet<EventListener>();
            setListeners = _listeners.putIfAbsent(type, newSet);
            if (setListeners == null) {
                setListeners = newSet;
            }
        }

        Boolean found = false;

        // check if same one exist by event name and event class
        for (Iterator<EventListener> its = setListeners.iterator(); its.hasNext();){
            EventListener foundListener = its.next();
            //Object foundTarget = foundListener.getTarget();
            //Method foundMethod = foundListener.getMethod();
            IBaseEvent foundEvent = foundListener.getEvent();

            // hash code should be the same
            if(event.getName().equals(foundEvent.getName()) && listener.hashCode() == foundListener.hashCode()) {
                // if class is the same, target must be correct and if method is the same, ignore
                found = true;
                break;
            }
        }

        if(!found) {
            setListeners.add(listener);
        }
    }

    /**
     * Removes a listener by event type and listener information
     *
     * @param event
     * @param listener
     */
    public void removeEventListener(IBaseEvent event, EventListener listener) {
        if (_target == null) {
            throw new IllegalStateException("Event dispatcher target " + _target + " can not be null.");
        }

        // binds the looper
        _binder.bind(this);

        Class type = event.getClass();
        // find the listener set of this event
        Set<EventListener> setListeners = _listeners.get(type);
        if (setListeners != null) {
            EventListener foundListener = null;
            for (Iterator<EventListener> its = setListeners.iterator(); its.hasNext();){
                foundListener = its.next();
                //Object foundTarget = foundListener.getTarget();
                //Method foundMethod = foundListener.getMethod();
                IBaseEvent foundEvent = foundListener.getEvent();

                if(event.getName().equals(foundEvent.getName()) && listener.hashCode() == foundListener.hashCode()) {
                    // found
                    break;
                }
            }

            if(foundListener != null) {
                foundListener.invalidate();
                setListeners.remove(foundListener);
            }
        }
    }

    /**
     * Find set of event listeners
     *
     * @param event
     */
    public int countEventListeners(IBaseEvent event) {
        // binds the looper
        _binder.bind(this);

        Class type = event.getClass();
        // find the listener set of this event
        Set<EventListener> resultSet = _listeners.get(type);

        if(resultSet != null) {
            return resultSet.size();
        }

        return 0;
    }

    /**
     * Removes all event listeners of an event type
     *
     * @param event
     */
    public void removeAllEventListener(IBaseEvent event) {
        // binds the looper
        _binder.bind(this);

        Class type = event.getClass();
        Set<EventListener> setListeners = _listeners.remove(type);

        // remove will remove the set for the given type

        // removed totally
        for (Iterator<EventListener> its = setListeners.iterator(); its.hasNext();){
            EventListener removed = its.next();
            removed.invalidate();
        }
    }

    /**
     * Removes all event listeners
     */
    public void removeAllEventListener() {
        // binds the looper
        _binder.bind(this);

        for (Class<?> type : _listeners.keySet()) {
            Set<EventListener> entries = _listeners.get(type);
            for (Iterator<EventListener> its = entries.iterator(); its.hasNext();){
                EventListener removed = its.next();
                removed.invalidate();
            }
        }

        _listeners.clear();
    }


    public void dispatchEvent(IBaseEvent event) {
        _binder.bind(this);

        Class type = event.getClass();
        Set<EventListener> setListeners = _listeners.get(type);

        // boolean dispatched = false;
        if (setListeners != null && !setListeners.isEmpty()) {
            // dispatched = true;
            for (EventListener listener : setListeners) {
                // enqueus the listeners with the same event name
                if(listener.getEvent().getName().equals(event.getName())) {
                    enqueueEvent(event, listener);
                }
            }
        }

        processQueue();
    }

    /**
     * Dispatches an event and enqueues so to check all listeners and
     * execute accordingly, this is called externally
     *
     * sourceTarget is optional
     *
     * @param event
     * @param sourceTarget
     */
    public void dispatchEvent(IBaseEvent event, Object sourceTarget) {
        if (event == null) {
            throw new NullPointerException("Event to dispatchEvent must not be null.");
        }

        if(sourceTarget == _target) {
            dispatchEvent(event);
        }
    }

    /**
     * Queue the {@code event} for dispatch during {@link #dispatchEvent(IBaseEvent, Object)}.
     * FIFO order
     *
     * @param event
     * @param listener
     */
    protected void enqueueEvent(IBaseEvent event, EventListener listener) {
        _queue.get().offer(new EventWithHandler(event, listener));
    }

    /**
     * Dispatches {@code event} to the handler in {@code wrapper}.  This method is an appropriate override point for
     * subclasses that wish to make event delivery asynchronous.
     *
     * @param event event to dispatch.
     * @param listener wrapper that will call the handler.
     */
    protected void process(IBaseEvent event, EventListener listener) {
        try {
            listener.handleEvent(event);
        } catch (InvocationTargetException e) {
            throwRuntimeException("Could not dispatch event: " + event.getClass() + " to listener " + listener, e);
        }
    }

    /**
     * Throw a {@link RuntimeException} with a message and start a {@link
     * InvocationTargetException}. If the specified {@link InvocationTargetException} does not
     * originate properly, then it will not show {@link RuntimeException}.
     */
    private static void throwRuntimeException(String msg, InvocationTargetException e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            throw new RuntimeException(msg + ": " + cause.getMessage(), cause);
        } else {
            throw new RuntimeException(msg + ": " + e.getMessage(), e);
        }
    }

    /**
     * Drain the queue of events to be dispatched. As the queue is being drained, new events may be posted to the end of
     * the queue.
     */
    protected void processQueue() {
        // don't dispatch if we're already dispatching, that would allow reentrancy and out-of-order events. Instead, leave
        // the events to be dispatched after the in-progress dispatch is complete.
        if (_processingQueue.get()) {
            return;
        }

        _processingQueue.set(true);
        try {
            while (true) {
                EventWithHandler eventWithHandler = _queue.get().poll();
                if (eventWithHandler == null) {
                    break;
                }

                if (eventWithHandler.handler.isValid()) {
                    process(eventWithHandler.event, eventWithHandler.handler);
                }
            }
        } finally {
            _processingQueue.set(false);
        }
    }

    /**
     * Returns a string description of the object
     *
     * @return
     */
    @Override public String toString() {
        return "[EventSatellite \"" + _id + "\"]";
    }

    /**
     * Get the current target.
     *
     * @return
     */
    public Object getTarget() {
        return _target;
    }

    /**
     * Destroys and clears the satellite.
     * Must be destroyed to clear up any residues in memory and
     * thread zombies.
     */
    public void destroy() {
        removeAllEventListener();

        _queue.remove();
        _processingQueue.remove();
    }

    /** Simple struct representing an event and its handler. */
    static class EventWithHandler {
        final IBaseEvent event;
        final EventListener handler;

        public EventWithHandler(IBaseEvent event, EventListener handler) {
            this.event = event;
            this.handler = handler;
        }
    }
}
