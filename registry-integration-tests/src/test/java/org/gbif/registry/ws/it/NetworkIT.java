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
package org.gbif.registry.ws.it;

import org.gbif.api.model.common.paging.PagingRequest;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.registry.Dataset;
import org.gbif.api.model.registry.Installation;
import org.gbif.api.model.registry.Network;
import org.gbif.api.model.registry.Organization;
import org.gbif.api.service.registry.DatasetService;
import org.gbif.api.service.registry.NetworkService;
import org.gbif.registry.identity.service.IdentityService;
import org.gbif.registry.search.test.EsManageServer;
import org.gbif.registry.test.TestDataFactory;
import org.gbif.registry.ws.client.NetworkClient;
import org.gbif.registry.ws.it.fixtures.RequestTestFixture;
import org.gbif.registry.ws.it.fixtures.TestConstants;
import org.gbif.ws.WebApplicationException;
import org.gbif.ws.client.filter.SimplePrincipalProvider;
import org.gbif.ws.security.KeyStore;

import java.util.UUID;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This is parameterized to run the same test routines for the following:
 *
 * <ol>
 *   <li>The persistence layer
 *   <li>The WS service layer
 *   <li>The WS service client layer
 * </ol>
 */
public class NetworkIT extends NetworkEntityIT<Network> {

  private final TestDataFactory testDataFactory;
  private final NetworkService networkResource;
  private final RequestTestFixture requestTestFixture;
  private final IdentityService identityService;
  private final DatasetService datasetResource;

  @Autowired
  public NetworkIT(
      RequestTestFixture requestTestFixture,
      NetworkService service,
      @Nullable SimplePrincipalProvider principalProvider,
      TestDataFactory testDataFactory,
      EsManageServer esServer,
      @LocalServerPort int localServerPort,
      KeyStore keyStore,
      IdentityService identityService,
      DatasetService datasetResource) {
    super(
        service,
        localServerPort,
        keyStore,
        NetworkClient.class,
        principalProvider,
        testDataFactory,
        esServer);
    this.networkResource = service;
    this.testDataFactory = testDataFactory;
    this.requestTestFixture = requestTestFixture;
    this.identityService = identityService;
    this.datasetResource = datasetResource;
  }

  @Test
  public void testAddAndDeleteConstituentAsEditor() throws Exception {
    // create a network and a dataset
    Dataset dataset = testDataFactory.newPersistedDataset(TestConstants.TEST_DOI);
    Network network = testDataFactory.newPersistedNetwork();

    // check number of constituents before
    int numConstituentsBefore = network.getNumConstituents();
    assertEquals(0, numConstituentsBefore);

    // User has a editor role, but not allowed to modify network
    // Use requestTestFixture to make sure filters are involved
    requestTestFixture
        .postRequest(
            TestConstants.TEST_EDITOR,
            "/network/" + network.getKey() + "/constituents/" + dataset.getKey())
        .andExpect(status().isForbidden());

    // add editor right on the network
    identityService.addEditorRight(TestConstants.TEST_EDITOR, network.getKey());

    // try again
    requestTestFixture
        .postRequest(
            TestConstants.TEST_EDITOR,
            "/network/" + network.getKey() + "/constituents/" + dataset.getKey())
        .andExpect(status().isNoContent());

    // check number of constituents after
    Network updatedNetwork = networkResource.get(network.getKey());
    int numConstituentsAfter = updatedNetwork.getNumConstituents();
    assertEquals(1, numConstituentsAfter);

    Dataset datasetFromDb = datasetResource.get(dataset.getKey());

    assertTrue(
        datasetFromDb.getNetworkKeys().contains(network.getKey()),
        "Network key not present in dataset networkKeys");

    // delete constituent
    requestTestFixture
        .deleteRequest(
            TestConstants.TEST_EDITOR,
            "/network/" + network.getKey() + "/constituents/" + dataset.getKey())
        .andExpect(status().isNoContent());

    // check number of constituents after
    updatedNetwork = networkResource.get(network.getKey());
    numConstituentsAfter = updatedNetwork.getNumConstituents();
    assertEquals(0, numConstituentsAfter);
  }

  @ParameterizedTest
  @EnumSource(ServiceType.class)
  public void testListPublishingOrganizations(ServiceType serviceType) {
    NetworkService service = (NetworkService) getService(serviceType);

    // prepare two networks
    Network network1 = testDataFactory.newPersistedNetwork();
    Network network2 = testDataFactory.newPersistedNetwork();

    // prepare organizations & installations
    Organization org1 = testDataFactory.newPersistedOrganization();
    Organization org2 = testDataFactory.newPersistedOrganization();
    Organization org3 = testDataFactory.newPersistedOrganization();
    Installation installation1 = testDataFactory.newPersistedInstallation(org1.getKey());
    Installation installation2 = testDataFactory.newPersistedInstallation(org2.getKey());
    Installation installation3 = testDataFactory.newPersistedInstallation(org3.getKey());

    // prepare 4 datasets: 3 for the first network and 1 for the second one
    Dataset dataset1 = testDataFactory.newPersistedDataset(org1.getKey(), installation1.getKey());
    Dataset dataset2 = testDataFactory.newPersistedDataset(org1.getKey(), installation1.getKey());
    Dataset dataset3 = testDataFactory.newPersistedDataset(org2.getKey(), installation2.getKey());
    Dataset dataset4 = testDataFactory.newPersistedDataset(org3.getKey(), installation3.getKey());

    // add constituents
    service.addConstituent(network1.getKey(), dataset1.getKey());
    service.addConstituent(network1.getKey(), dataset2.getKey());
    service.addConstituent(network1.getKey(), dataset3.getKey());
    service.addConstituent(network2.getKey(), dataset4.getKey());

    // list publishing organizations for networks
    PagingResponse<Organization> network1PublishingOrgs =
        service.publishingOrganizations(network1.getKey(), new PagingRequest());
    PagingResponse<Organization> network2PublishingOrgs =
        service.publishingOrganizations(network2.getKey(), new PagingRequest());
    PagingResponse<Organization> notExistingNetworkPublishingOrgs =
        service.publishingOrganizations(UUID.randomUUID(), new PagingRequest());

    assertEquals(2, network1PublishingOrgs.getCount());
    assertEquals(1, network2PublishingOrgs.getCount());
    assertEquals(0, notExistingNetworkPublishingOrgs.getCount());

    // remove constituent
    service.removeConstituent(network1.getKey(), dataset3.getKey());

    // check again
    network1PublishingOrgs =
        service.publishingOrganizations(network1.getKey(), new PagingRequest());
    assertEquals(1, network1PublishingOrgs.getCount());

    // delete datasets
    datasetResource.delete(dataset1.getKey());
    datasetResource.delete(dataset2.getKey());
    // check again
    network1PublishingOrgs =
        service.publishingOrganizations(network1.getKey(), new PagingRequest());
    assertEquals(0, network1PublishingOrgs.getCount());
  }

  @ParameterizedTest
  @EnumSource(ServiceType.class)
  public void testConstituentsHandlingErrors(ServiceType serviceType) {
    NetworkService service = (NetworkService) getService(serviceType);

    Network network = testDataFactory.newPersistedNetwork();
    Organization organization = testDataFactory.newPersistedOrganization();
    Installation installation = testDataFactory.newPersistedInstallation();
    Dataset dataset = testDataFactory.newPersistedDataset(organization.getKey(), installation.getKey());

    //Simple add
    service.addConstituent(network.getKey(), dataset.getKey());

    //Exception class depends on the service type
    Class<? extends  Throwable> expectedException = serviceType == ServiceType.RESOURCE? WebApplicationException.class : IllegalArgumentException.class;

    //Adding to a non-existing network

    assertThrows(expectedException, () -> service.addConstituent(UUID.randomUUID(), dataset.getKey()));

    //Adding a non-existing dataset
    assertThrows(expectedException, () -> service.addConstituent(network.getKey(), UUID.randomUUID()));

    //Removing from a non-existing network
    assertThrows(expectedException, () -> service.removeConstituent(UUID.randomUUID(), dataset.getKey()));

    //Removing a non-existing dataset
    assertThrows(expectedException, () -> service.removeConstituent(network.getKey(), UUID.randomUUID()));

    //Simple remove
    service.removeConstituent(network.getKey(), dataset.getKey());
  }

  @Override
  protected Network newEntity(ServiceType serviceType) {
    return testDataFactory.newNetwork();
  }

  /**
   * Test doesn't make sense for a network.
   */
  @Override
  public void testCreateAsEditor(ServiceType serviceType) {}

  @Override
  protected Network duplicateForCreateAsEditorTest(Network entity) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected UUID keyForCreateAsEditorTest(Network entity) {
    return null;
  }
}
