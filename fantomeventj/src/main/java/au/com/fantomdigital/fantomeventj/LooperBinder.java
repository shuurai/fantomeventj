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

import android.os.Looper;

/**
 * LooperBinder: Enforces a looper (thread) bind policy for methods on a particular event satellite.
 * Project: fantomeventj
 *
 * @author sfdi
 * @date 15/08/15
 */
public interface LooperBinder {

  /**
   * Enforce a valid looper for the given {@code satellite}. Implementations may throw any runtime exception.
   *
   * @param satellite Event satellite instance on which an action is being performed.
   */
  void bind(EventDispatcher satellite);


  /** A {@link LooperBinder} that does no verification. */
  LooperBinder ANY = new LooperBinder() {
    //@Override
    public void bind(EventDispatcher satellite) {
      // Allow any looper.
    }
  };

  /** A {@link LooperBinder} that confines {@link EventDispatcher} methods to the main looper. */
  LooperBinder MAIN = new LooperBinder() {
    //@Override
    public void bind(EventDispatcher satellite) {
      if (Looper.myLooper() != Looper.getMainLooper()) {
        throw new IllegalStateException("Event satellite " + satellite + " accessed from main looper " + Looper.myLooper());
      }
    }
  };

}
