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

import java.util.UUID;

/*
 * BaseEvent: The basic event for the traversal.
 * Every custom event must have its own class. The name must match the class name.
 *
 * Project: fantomeventj
 *
 * @author sfdi
 * @date 16/08/15
 */
public class BaseEvent implements IBaseEvent {
    /** Object sporting the handler method. */
    final private Object _target;
    /** Event name */
    final private String _name;

    /**
     * Creates a new base event.
     * Variables can not be changed once instantiated for concurrency compatibility.
     *
     * @param eventName the name of the event
     * @param target object broadcasting the BaseEvent.
     */
    public BaseEvent(String eventName, Object target) {
        this._name = eventName;
        this._target = target;
    }

    /**
     * Simple Event for base loading
     * @param eventName
     */
    public BaseEvent(String eventName) {
        this._name = eventName;
        this._target = null;
    }

    /** String representation */
    @Override public String toString() {
        return "[BaseEvent " + _name + "]";
    }

    /** Get targets */
    public Object getTarget() {
        return _target;
    }

    /**
     * Get name
     *
     * @return
     */
    public String getName() {
        return _name;
    }
}
