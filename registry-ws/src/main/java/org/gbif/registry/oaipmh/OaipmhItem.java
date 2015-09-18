package org.gbif.registry.oaipmh;

import org.gbif.api.model.registry.Dataset;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.lyncode.builder.ListBuilder;
import org.dspace.xoai.dataprovider.model.Item;
import org.dspace.xoai.dataprovider.model.Set;
import org.dspace.xoai.model.oaipmh.About;
import org.dspace.xoai.model.oaipmh.Metadata;

public class OaipmhItem implements Item {

  private Dataset dataset;
  private Metadata metadata;

  public OaipmhItem(Dataset dataset, String metadata) {
    this.dataset = dataset;
    this.metadata = new Metadata(metadata);
  }

  @Override
  public List<About> getAbout() {
    return new ArrayList<>();
  }

  @Override
  public Metadata getMetadata() {
    return metadata;
  }

  @Override
  public String getIdentifier() {
    return dataset.getKey().toString();
  }

  @Override
  public Date getDatestamp() {
    dataset.getCreated();
    dataset.getModified();
    dataset.getDeleted();
    return dataset.getModified();
  }

  @Override
  public List<Set> getSets() {
    return new ArrayList<>();
  }

  @Override
  public boolean isDeleted() {
    return dataset.getDeleted() != null;
  }
}
