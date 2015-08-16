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

/*
 * Project: fantomeventj
 * @author sfdi
 * @date 17/08/15
 */
public class TestEvent extends BaseEvent {
    /**
     * Creates a new base event.
     * Variables can not be changed once instantiated for concurrency compatibility.
     *
     * @param eventName the name of the event
     * @param target    object broadcasting the BaseEvent.
     */
    public TestEvent(String eventName, Object target) {
        super(eventName, target);
    }
}
