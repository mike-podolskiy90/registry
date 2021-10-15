/*
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
package org.gbif.registry.persistence.mapper.collections.external;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IDigBioMapper {

  List<MachineTagDto> getIDigBioMachineTags(
      @Nullable @Param("collectionKeys") Set<UUID> collectionKeys);

  List<IdentifierDto> getIdentifiers(@Param("collectionKeys") Set<UUID> collectionKeys);

  List<IDigBioCollectionDto> getCollections(@Param("collectionKeys") Set<UUID> collectionKeys);

  Set<UUID> findIDigBioCollections(@Nullable @Param("iDigBioUuid") String iDigBioUuid);

  Set<UUID> findCollectionsByCountry(@Param("countryCode") String countryCode);
}
