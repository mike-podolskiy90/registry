package org.gbif.registry.domain.ws;

import org.gbif.api.model.common.DOI;

import java.net.URI;

public class CitationResponse {

  private DOI assignedDOI;

  private String citation;

  private String title;

  private URI target;

  public DOI getAssignedDOI() {
    return assignedDOI;
  }

  public void setAssignedDOI(DOI assignedDOI) {
    this.assignedDOI = assignedDOI;
  }

  public String getCitation() {
    return citation;
  }

  public void setCitation(String citation) {
    this.citation = citation;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public URI getTarget() {
    return target;
  }

  public void setTarget(URI target) {
    this.target = target;
  }
}
