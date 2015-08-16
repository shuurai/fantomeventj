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

package au.com.fantomdigital.fantomeventj.sample;

import au.com.fantomdigital.fantomeventj.*;

/**
 * Project: Satellite for the sample.
 * Progressive update of colors using event. Shows how the threads
 * are handled seperately from the UI Thread.
 */
public final class SatelliteProvider {
  private static final EventDispatcher SATELLITE = new EventDispatcher();

  public static EventDispatcher getInstance() {
    return SATELLITE;
  }

  private SatelliteProvider() {
    // No instances.
  }
}
