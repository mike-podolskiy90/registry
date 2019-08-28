package org.gbif.registry.ws.resources.collections;

import org.gbif.api.model.collections.CollectionEntity;
import org.gbif.api.service.collections.CrudService;
import org.gbif.registry.events.EventManager;
import org.gbif.registry.events.collections.DeleteCollectionEntityEvent;
import org.gbif.registry.persistence.mapper.collections.CrudMapper;
import org.gbif.registry.ws.Trim;
import org.gbif.ws.server.interceptor.NullToNotFound;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static org.gbif.registry.ws.security.UserRoles.ADMIN_ROLE;
import static org.gbif.registry.ws.security.UserRoles.GRSCICOLL_ADMIN_ROLE;

/**
 * Base class to implement the CRUD methods of a {@link CollectionEntity}.
 */
public abstract class BaseCrudResource<T extends CollectionEntity> implements CrudService<T> {

  private final CrudMapper<T> crudMapper;
  private final EventManager eventManager;
  private final Class<T> objectClass;

  protected BaseCrudResource(
      CrudMapper<T> crudMapper,
      EventManager eventManager,
      Class<T> objectClass) {
    this.crudMapper = crudMapper;
    this.eventManager = eventManager;
    this.objectClass = objectClass;
  }

  // TODO: 2019-08-21 implement validation

  @RequestMapping(method = RequestMethod.POST)
  @Trim
//  @Validate
  @Transactional
  @Secured({ADMIN_ROLE, GRSCICOLL_ADMIN_ROLE})
  public UUID create(@RequestBody @NotNull T entity, Authentication authentication) {
    final String username = ((UserDetails) authentication.getPrincipal()).getUsername();
    entity.setCreatedBy(username);
    entity.setModifiedBy(username);
    return create(entity);
  }

  @DeleteMapping("{key}")
//  @Validate
  @Transactional
  @Secured({ADMIN_ROLE, GRSCICOLL_ADMIN_ROLE})
  public void delete(@PathVariable @NotNull UUID key, Authentication authentication) {
    T entityToDelete = get(key);
    entityToDelete.setModifiedBy(((UserDetails) authentication.getPrincipal()).getUsername());
    update(entityToDelete);

    delete(key);
  }

  @Transactional
//  @Validate
  @Override
  public void delete(@NotNull UUID key) {
    T objectToDelete = get(key);
    crudMapper.delete(key);
    eventManager.post(DeleteCollectionEntityEvent.newInstance(objectToDelete, objectClass));
  }

  @GetMapping("{key}")
  @Nullable
  @NullToNotFound
//  @Validate(validateReturnedValue = true)
  @Override
  public T get(@PathVariable @NotNull UUID key) {
    return crudMapper.get(key);
  }

  @PutMapping("{key}")
//  @Validate
  @Transactional
  @Secured({ADMIN_ROLE, GRSCICOLL_ADMIN_ROLE})
  public void update(@PathVariable @NotNull UUID key, @RequestBody @NotNull @Trim T entity) {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    checkArgument(
        key.equals(entity.getKey()), "Provided entity must have the same key as the resource URL");
    entity.setModifiedBy(((UserDetails) authentication.getPrincipal()).getUsername());
    update(entity);
  }
}
