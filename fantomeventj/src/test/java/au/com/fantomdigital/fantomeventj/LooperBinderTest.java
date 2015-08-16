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

import org.junit.Test;

import java.lang.reflect.Method;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class LooperBinderTest {

  private static class RecordingLooperBinder implements LooperBinder {
    boolean called = false;

    @Override public void bind(EventDispatcher satellite) {
      called = true;
    }
  }

    /**
     * Test Handler
     * @param event
     */
    private void onTestHander(IBaseEvent event) {

    }

  @Test public void enforerCalledForAddEventListener() {
      RecordingLooperBinder enforcer = new RecordingLooperBinder();
      EventDispatcher satellite = new EventDispatcher(enforcer, this);

      Class clz = this.getClass();
      String handlerMethod = "onTestHander";

      Method method = null;
      try {
          method = clz.getDeclaredMethod(handlerMethod);

          BaseEvent event = new BaseEvent("TestEvent", this);
          EventListener listener = new EventListener(event, this, method);

          assertFalse(enforcer.called);
          satellite.addEventListener(event, listener);
          assertTrue(enforcer.called);
      } catch (NoSuchMethodException e) {
          assertFalse(enforcer.called);
      }
  }

  @Test public void enforcerCalledForDispatchEvent() {
      RecordingLooperBinder enforcer = new RecordingLooperBinder();
      EventDispatcher satellite = new EventDispatcher(enforcer, this);

      BaseEvent event = new BaseEvent("TestEvent", this);

    assertFalse(enforcer.called);
      satellite.dispatchEvent(event, this);
    assertTrue(enforcer.called);
  }

  @Test public void enforcerCalledForRemoveEventListener() {
      RecordingLooperBinder enforcer = new RecordingLooperBinder();
      EventDispatcher satellite = new EventDispatcher(enforcer, this);

      Class clz = this.getClass();
      String handlerMethod = "onTestHander";

      Method method = null;
      try {
          method = clz.getDeclaredMethod(handlerMethod);

          BaseEvent event = new BaseEvent("TestEvent", this);
          EventListener listener = new EventListener(event, this, method);

          assertFalse(enforcer.called);
          satellite.removeEventListener(event, listener);
          assertTrue(enforcer.called);
      } catch (NoSuchMethodException e) {
          assertFalse(enforcer.called);
      }
  }

}
