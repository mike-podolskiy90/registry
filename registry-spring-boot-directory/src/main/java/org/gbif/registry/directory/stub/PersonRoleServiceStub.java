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
package org.gbif.registry.directory.stub;

import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.directory.PersonRole;
import org.gbif.api.service.directory.PersonRoleService;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class PersonRoleServiceStub implements PersonRoleService {

  @Override
  public PersonRole create(@NotNull PersonRole personRole) {
    return null;
  }

  @Override
  public PersonRole get(@NotNull Integer integer) {
    return null;
  }

  @Override
  public void update(@NotNull PersonRole personRole) {}

  @Override
  public void delete(@NotNull Integer integer) {}

  @Override
  public PagingResponse<PersonRole> list(@Nullable String s, @Nullable Pageable pageable) {
    return null;
  }
}
