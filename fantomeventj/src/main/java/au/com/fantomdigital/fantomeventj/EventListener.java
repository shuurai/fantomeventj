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

/**
 * A listener that can map the target and method. This is to allow parsing of methods through functions
 * and call backs like AS3.
 *
 * <p>This class only verifies the suitability of the method and event type if something fails.  Callers are expected t
 * verify their uses of this class.
 *
 * <p>Two EventHandlers are equivalent when they refer to the same method on the same object (not class).   This
 * property is used to ensure that no handler method is registered more than once.
 *
 * Project: fantomeventj
 *
 * @author sfdi
 * @date 15/08/15
 */
public class EventListener {

  /** Object sporting the handler method. */
  private final Object _target;
  /** Handler method. */
  private final Method _method;
  /** Event reference. */
  private final IBaseEvent _event;
  /** Object hash code. */
  private final int _hashCode;
  /** Should this handler receive events? */
  private boolean _valid = true;

  public EventListener(IBaseEvent event, Object target, String methodName) throws NoSuchMethodException {
    if (target == null) {
      throw new NullPointerException("EventHandler target cannot be null.");
    }
    if (methodName == null) {
      throw new NullPointerException("EventHandler method name cannot be null.");
    }
    if (event == null) {
      throw new NullPointerException("EventHandler event cannot be null.");
    }

    this._event = event;
    this._target = target;

    Class targetClass = target.getClass();

    Class[] params = new Class[1];
    params[0] = IBaseEvent.class;

    Method method = null;
    try {
        method = targetClass.getDeclaredMethod(methodName, params);

        this._method = method;
        method.setAccessible(true);

        // Compute hash code eagerly since we know it will be used frequently and we cannot estimate the runtime of the
        // target's hashCode call.
        final int prime = 31;
        _hashCode = (prime + method.hashCode()) * prime + _target.hashCode();
    } catch (NoSuchMethodException e) {
      throw(e);
    }
  }

  public boolean isValid() {
    return _valid;
  }

  /**
   * If invalidated, will subsequently refuse to handle events.
   *
   * Should be called when the wrapped object is unregistered from the Bus.
   */
  public void invalidate() {
      _valid = false;
  }

  /**
   * Invokes the wrapped handler method to handle {@code event}.
   *
   * @param event  event to handle
   * @throws java.lang.IllegalStateException  if previously invalidated.
   * @throws java.lang.reflect.InvocationTargetException  if the method throws any {@link Throwable} that is not
   *     an {@link Error} ({@code Error}s are propagated as it is).
   */
  public void handleEvent(Object event) throws InvocationTargetException {
    if (!_valid) {
      throw new IllegalStateException(toString() + " has been invalidated and can no longer handle events.");
    }
    try {
        _method.invoke(_target, event);
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof Error) {
        throw (Error) e.getCause();
      }
      throw e;
    }
  }

  @Override public String toString() {
    return "[EventHandler " + _method + "]";
  }

  @Override public int hashCode() {
    return _hashCode;
  }

  @Override public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final EventListener other = (EventListener) obj;

    return _method.equals(other.getMethod()) && _target == other.getTarget();
  }

    public Object getTarget() {
        return _target;
    }

    public Method getMethod() {
        return _method;
    }

    public IBaseEvent getEvent() {
        return _event;
    }
}
