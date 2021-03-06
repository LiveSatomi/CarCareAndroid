/*
 * Copyright 2017, Team LTT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teamltt.carcare.database;

import android.os.Bundle;

/**
 * In the Observer pattern, An observable object (subject) maintains a list of its observers.
 * The subject notifies the observers when the state changes.
 * See more at https://en.wikipedia.org/wiki/Observer_pattern
 */
public interface IObserver {
    /**
     * A method for the Observable subject to deliver its state changes.
     *
     * @param o    the subject
     * @param args the state changes
     */
    void update(IObservable o, Bundle args);
}
