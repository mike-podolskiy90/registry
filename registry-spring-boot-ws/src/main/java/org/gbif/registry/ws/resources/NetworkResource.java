package org.gbif.registry.ws.resources;

import com.google.common.base.Strings;
import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.registry.Dataset;
import org.gbif.api.model.registry.Network;
import org.gbif.api.service.registry.NetworkService;
import org.gbif.registry.events.EventManager;
import org.gbif.registry.persistence.WithMyBatis;
import org.gbif.registry.persistence.mapper.CommentMapper;
import org.gbif.registry.persistence.mapper.ContactMapper;
import org.gbif.registry.persistence.mapper.DatasetMapper;
import org.gbif.registry.persistence.mapper.EndpointMapper;
import org.gbif.registry.persistence.mapper.IdentifierMapper;
import org.gbif.registry.persistence.mapper.MachineTagMapper;
import org.gbif.registry.persistence.mapper.NetworkMapper;
import org.gbif.registry.persistence.mapper.TagMapper;
import org.gbif.registry.ws.model.NetworkRequestSearchParams;
import org.gbif.registry.ws.security.EditorAuthorizationService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

import static org.gbif.registry.ws.security.UserRoles.ADMIN_ROLE;

@RestController
@RequestMapping("network")
public class NetworkResource
  extends BaseNetworkEntityResource<Network>
  implements NetworkService {

  private final DatasetMapper datasetMapper;
  private final NetworkMapper networkMapper;

  public NetworkResource(
    NetworkMapper networkMapper,
    ContactMapper contactMapper,
    EndpointMapper endpointMapper,
    IdentifierMapper identifierMapper,
    MachineTagMapper machineTagMapper,
    TagMapper tagMapper,
    CommentMapper commentMapper,
    DatasetMapper datasetMapper,
    EventManager eventManager,
    EditorAuthorizationService userAuthService,
    WithMyBatis withMyBatis) {
    super(networkMapper,
      commentMapper,
      contactMapper,
      endpointMapper,
      identifierMapper,
      machineTagMapper,
      tagMapper,
      Network.class,
      eventManager,
      userAuthService,
      withMyBatis);
    this.datasetMapper = datasetMapper;
    this.networkMapper = networkMapper;
  }

  /**
   * All network entities support simple (!) search with "&q=".
   * This is to support the console user interface, and is in addition to any complex, faceted search that might
   * additionally be supported, such as dataset search.
   */
  @GetMapping
  public PagingResponse<Network> list(@Valid NetworkRequestSearchParams request, Pageable page) {
    if (request.getIdentifierType() != null && request.getIdentifier() != null) {
      return listByIdentifier(request.getIdentifierType(), request.getIdentifier(), page);
    } else if (request.getIdentifier() != null) {
      return listByIdentifier(request.getIdentifier(), page);
    } else if (request.getMachineTagNamespace() != null) {
      return listByMachineTag(request.getMachineTagNamespace(), request.getMachineTagName(),
        request.getMachineTagValue(), page);
    } else if (Strings.isNullOrEmpty(request.getQ())) {
      return list(page);
    } else {
      return search(request.getQ(), page);
    }
  }

  @GetMapping("{key}/constituents")
  @Override
  public PagingResponse<Dataset> listConstituents(@PathVariable("key") UUID networkKey, Pageable page) {
    return pagingResponse(page, (long) networkMapper.countDatasetsInNetwork(networkKey),
      datasetMapper.listDatasetsInNetwork(networkKey, page));
  }

  @PostMapping("{key}/constituents/{datasetKey}")
  @Secured(ADMIN_ROLE)
  @Override
  public void addConstituent(@PathVariable("key") UUID networkKey, @PathVariable UUID datasetKey) {
    networkMapper.addDatasetConstituent(networkKey, datasetKey);
  }

  @DeleteMapping("{key}/constituents/{datasetKey}")
  @Secured(ADMIN_ROLE)
  @Override
  public void removeConstituent(@PathVariable("key") UUID networkKey, @PathVariable UUID datasetKey) {
    networkMapper.deleteDatasetConstituent(networkKey, datasetKey);
  }
}
