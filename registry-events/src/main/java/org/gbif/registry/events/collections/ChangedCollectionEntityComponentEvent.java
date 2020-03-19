/*
 * Copyright 2020 Global Biodiversity Information Facility (GBIF)
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
package org.gbif.registry.events.collections;

import java.util.UUID;

import com.google.common.base.Preconditions;

/**
 * This event is fired after collection entity components such as contacts, identifiers or tags have
 * been successfully updated, added or removed.
 */
public class ChangedCollectionEntityComponentEvent {

  private final UUID targetEntityKey;
  private final Class targetClass;
  private final Class componentClass;

  public static ChangedCollectionEntityComponentEvent newInstance(
      UUID targetEntityKey, Class targetClass, Class componentClass) {
    return new ChangedCollectionEntityComponentEvent(targetEntityKey, targetClass, componentClass);
  }

  private ChangedCollectionEntityComponentEvent(
      UUID targetEntityKey, Class targetClass, Class componentClass) {
    this.targetEntityKey = Preconditions.checkNotNull(targetEntityKey);
    this.targetClass = Preconditions.checkNotNull(targetClass);
    this.componentClass = Preconditions.checkNotNull(componentClass);
  }

  public UUID getTargetEntityKey() {
    return targetEntityKey;
  }

  public Class getTargetClass() {
    return targetClass;
  }

  public Class getComponentClass() {
    return componentClass;
  }
}
