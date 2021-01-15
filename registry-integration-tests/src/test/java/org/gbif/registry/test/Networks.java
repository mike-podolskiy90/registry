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
package org.gbif.registry.test;

import org.gbif.api.model.registry.Network;
import org.gbif.api.model.registry.Organization;
import org.gbif.api.service.registry.NetworkService;
import org.gbif.ws.client.filter.SimplePrincipalProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

@Component
public class Networks extends JsonBackedData<Network> {

  private final NetworkService networkService;

  @Autowired
  public Networks(
      NetworkService networkService,
      ObjectMapper objectMapper,
      SimplePrincipalProvider simplePrincipalProvider) {
    super(
        "data/network.json",
        new TypeReference<Network>() {},
        objectMapper,
        simplePrincipalProvider);
    this.networkService = networkService;
  }

  public Network newPersistedInstance() {
    Network network = newInstance();
    UUID networkUuid = networkService.create(network);

    return networkService.get(networkUuid);
  }
}
