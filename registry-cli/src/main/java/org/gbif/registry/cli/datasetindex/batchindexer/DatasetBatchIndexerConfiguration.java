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
package org.gbif.registry.cli.datasetindex.batchindexer;

import org.gbif.registry.cli.datasetindex.DatasetIndexConfiguration;
import org.gbif.registry.search.dataset.indexing.es.IndexingConstants;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** A configuration exclusively for DatasetUpdater. */
@Data
@EqualsAndHashCode(callSuper = true)
public class DatasetBatchIndexerConfiguration extends DatasetIndexConfiguration {

  private Map<String, Object> indexingSettings =
      new HashMap<>(IndexingConstants.DEFAULT_INDEXING_SETTINGS);

  private Map<String, String> searchSettings =
      new HashMap<>(IndexingConstants.DEFAULT_SEARCH_SETTINGS);
}
