package org.gbif.registry.service.collections.batch;

import org.gbif.api.model.collections.Batch;
import org.gbif.api.model.common.export.ExportFormat;

public interface BatchHandler {

  void handleBatch(byte[] entitiesFile, byte[] contactsFile, ExportFormat format, Batch batch);
}
