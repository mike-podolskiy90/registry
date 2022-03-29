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
package org.gbif.registry.service.collections;

import org.gbif.api.model.collections.Address;
import org.gbif.api.model.collections.Collection;
import org.gbif.api.model.collections.Contact;
import org.gbif.api.model.collections.Contactable;
import org.gbif.api.model.collections.Institution;
import org.gbif.api.model.collections.MasterSourceMetadata;
import org.gbif.api.model.collections.OccurrenceMappeable;
import org.gbif.api.model.collections.OccurrenceMapping;
import org.gbif.api.model.collections.Person;
import org.gbif.api.model.collections.PrimaryCollectionEntity;
import org.gbif.api.model.collections.UserId;
import org.gbif.api.model.registry.Commentable;
import org.gbif.api.model.registry.Dataset;
import org.gbif.api.model.registry.Identifiable;
import org.gbif.api.model.registry.MachineTaggable;
import org.gbif.api.model.registry.NetworkEntity;
import org.gbif.api.model.registry.Organization;
import org.gbif.api.model.registry.PostPersist;
import org.gbif.api.model.registry.PrePersist;
import org.gbif.api.model.registry.Taggable;
import org.gbif.api.service.collections.PrimaryCollectionEntityService;
import org.gbif.api.util.validators.identifierschemes.IdentifierSchemeValidator;
import org.gbif.api.vocabulary.collections.MasterSourceType;
import org.gbif.api.vocabulary.collections.Source;
import org.gbif.registry.events.EventManager;
import org.gbif.registry.events.collections.CreateCollectionEntityEvent;
import org.gbif.registry.events.collections.EventType;
import org.gbif.registry.events.collections.MasterSourceMetadataAddedEvent;
import org.gbif.registry.events.collections.ReplaceEntityEvent;
import org.gbif.registry.events.collections.SubEntityCollectionEvent;
import org.gbif.registry.events.collections.UpdateCollectionEntityEvent;
import org.gbif.registry.persistence.mapper.CommentMapper;
import org.gbif.registry.persistence.mapper.DatasetMapper;
import org.gbif.registry.persistence.mapper.IdentifierMapper;
import org.gbif.registry.persistence.mapper.MachineTagMapper;
import org.gbif.registry.persistence.mapper.NetworkEntityMapper;
import org.gbif.registry.persistence.mapper.OrganizationMapper;
import org.gbif.registry.persistence.mapper.TagMapper;
import org.gbif.registry.persistence.mapper.collections.AddressMapper;
import org.gbif.registry.persistence.mapper.collections.CollectionContactMapper;
import org.gbif.registry.persistence.mapper.collections.MasterSourceSyncMetadataMapper;
import org.gbif.registry.persistence.mapper.collections.OccurrenceMappingMapper;
import org.gbif.registry.persistence.mapper.collections.PrimaryEntityMapper;
import org.gbif.registry.security.SecurityContextCheck;
import org.gbif.registry.service.WithMyBatis;
import org.gbif.registry.service.collections.utils.IdentifierValidatorUtils;
import org.gbif.registry.service.collections.utils.MasterSourceUtils;
import org.gbif.ws.WebApplicationException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;

import static com.google.common.base.Preconditions.checkArgument;
import static org.gbif.registry.security.UserRoles.GRSCICOLL_ADMIN_ROLE;
import static org.gbif.registry.security.UserRoles.GRSCICOLL_EDITOR_ROLE;
import static org.gbif.registry.security.UserRoles.GRSCICOLL_MEDIATOR_ROLE;
import static org.gbif.registry.service.collections.utils.MasterSourceUtils.*;
import static org.gbif.registry.service.collections.utils.MasterSourceUtils.CONTACTS_FIELD_NAME;
import static org.gbif.registry.service.collections.utils.MasterSourceUtils.hasExternalMasterSource;
import static org.gbif.registry.service.collections.utils.MasterSourceUtils.isSourceableField;

@Validated
public abstract class BasePrimaryCollectionEntityService<
        T extends
            PrimaryCollectionEntity & Taggable & Identifiable & MachineTaggable & Contactable
                & Commentable & OccurrenceMappeable>
    extends BaseCollectionEntityService<T> implements PrimaryCollectionEntityService<T> {

  private final OccurrenceMappingMapper occurrenceMappingMapper;
  private final AddressMapper addressMapper;
  private final PrimaryEntityMapper<T> primaryEntityMapper;
  private final CollectionContactMapper contactMapper;
  private final MasterSourceSyncMetadataMapper masterSourceSyncMetadataMapper;
  private final DatasetMapper datasetMapper;
  private final OrganizationMapper organizationMapper;

  protected BasePrimaryCollectionEntityService(
      Class<T> objectClass,
      PrimaryEntityMapper<T> primaryEntityMapper,
      AddressMapper addressMapper,
      MachineTagMapper machineTagMapper,
      TagMapper tagMapper,
      IdentifierMapper identifierMapper,
      CommentMapper commentMapper,
      OccurrenceMappingMapper occurrenceMappingMapper,
      CollectionContactMapper contactMapper,
      MasterSourceSyncMetadataMapper masterSourceSyncMetadataMapper,
      DatasetMapper datasetMapper,
      OrganizationMapper organizationMapper,
      EventManager eventManager,
      WithMyBatis withMyBatis) {
    super(
        primaryEntityMapper,
        tagMapper,
        machineTagMapper,
        identifierMapper,
        commentMapper,
        objectClass,
        eventManager,
        withMyBatis);
    this.addressMapper = addressMapper;
    this.occurrenceMappingMapper = occurrenceMappingMapper;
    this.primaryEntityMapper = primaryEntityMapper;
    this.contactMapper = contactMapper;
    this.masterSourceSyncMetadataMapper = masterSourceSyncMetadataMapper;
    this.datasetMapper = datasetMapper;
    this.organizationMapper = organizationMapper;
  }

  @Secured({GRSCICOLL_ADMIN_ROLE, GRSCICOLL_EDITOR_ROLE, GRSCICOLL_MEDIATOR_ROLE})
  @Transactional
  @Validated({PrePersist.class, Default.class})
  @Override
  public UUID create(T entity) {
    checkArgument(entity.getKey() == null, "Unable to create an entity which already has a key");
    preCreate(entity);

    if (entity.getAddress() != null) {
      addressMapper.create(entity.getAddress());
    }

    if (entity.getMailingAddress() != null) {
      addressMapper.create(entity.getMailingAddress());
    }

    entity.setMasterSource(MasterSourceType.GRSCICOLL);
    entity.setKey(UUID.randomUUID());
    baseMapper.create(entity);

    eventManager.post(CreateCollectionEntityEvent.newInstance(entity));

    return entity.getKey();
  }

  @Validated({PostPersist.class, Default.class})
  @Secured({GRSCICOLL_ADMIN_ROLE, GRSCICOLL_EDITOR_ROLE, GRSCICOLL_MEDIATOR_ROLE})
  @Transactional
  @Override
  public void update(T entity) {
    update(entity, true);
  }

  @Validated({PostPersist.class, Default.class})
  @Secured({GRSCICOLL_ADMIN_ROLE, GRSCICOLL_EDITOR_ROLE, GRSCICOLL_MEDIATOR_ROLE})
  @Transactional
  @Override
  public void update(@NotNull @Valid T entity, boolean lockFields) {
    preUpdate(entity);
    T entityOld = get(entity.getKey());
    checkArgument(entityOld != null, "Entity doesn't exist");
    checkCodeUpdate(entity, entityOld);
    checkReplacedEntitiesUpdate(entity, entityOld);

    if (entityOld.getDeleted() != null) {
      // if it's deleted we only allow to update it if we undelete it
      checkArgument(
          entity.getDeleted() == null,
          "Unable to update a previously deleted entity unless you clear the deletion timestamp");
    } else {
      // not allowed to delete when updating
      checkArgument(entity.getDeleted() == null, "Can't delete an entity when updating");
    }

    // lock fields
    if (lockFields && isLockableEntity(entityOld)) {
      lockFields(entityOld, entity);
    }

    // update mailing address
    updateAddress(entity.getMailingAddress(), entityOld.getMailingAddress());

    // update address
    updateAddress(entity.getAddress(), entityOld.getAddress());

    // update entity
    baseMapper.update(entity);

    // check if we can delete the mailing address
    if (entity.getMailingAddress() == null && entityOld.getMailingAddress() != null) {
      addressMapper.delete(entityOld.getMailingAddress().getKey());
    }

    // check if we can delete the address
    if (entity.getAddress() == null && entityOld.getAddress() != null) {
      addressMapper.delete(entityOld.getAddress().getKey());
    }

    T newEntity = get(entity.getKey());

    eventManager.post(UpdateCollectionEntityEvent.newInstance(newEntity, entityOld));
  }

  public T lockFields(T entityOld, T entityNew) {
    List<LockableField> fieldsToLock = new ArrayList<>();
    if (entityOld instanceof Institution) {
      fieldsToLock = INSTITUTION_LOCKABLE_FIELDS.get(entityOld.getMasterSource());
    } else if (entityOld instanceof Collection) {
      fieldsToLock = COLLECTION_LOCKABLE_FIELDS.get(entityOld.getMasterSource());
    }

    fieldsToLock.forEach(
        f -> {
          try {
            f.getSetter().invoke(entityNew, f.getGetter().invoke(entityOld));
          } catch (Exception e) {
            throw new IllegalStateException("Could not lock field", e);
          }
        });

    return entityNew;
  }

  private void updateAddress(Address newAddress, Address oldAddress) {
    if (newAddress != null) {
      if (oldAddress == null) {
        checkArgument(
            newAddress.getKey() == null, "Unable to create an address which already has a key");
        addressMapper.create(newAddress);
      } else {
        addressMapper.update(newAddress);
      }
    }
  }

  @Secured({GRSCICOLL_ADMIN_ROLE, GRSCICOLL_MEDIATOR_ROLE})
  @Transactional
  @Override
  public void delete(UUID key) {
    T entityToDelete = get(key);
    if (hasExternalMasterSource(entityToDelete)) {
      throw new IllegalArgumentException(
          "Cannot delete an entity whose master source is not GRSciColl");
    }

    super.delete(entityToDelete);
  }

  @Deprecated
  @Secured({GRSCICOLL_ADMIN_ROLE, GRSCICOLL_EDITOR_ROLE, GRSCICOLL_MEDIATOR_ROLE})
  @Transactional
  @Override
  public void addContact(@NotNull UUID entityKey, @NotNull UUID personKey) {
    // check if the contact exists
    List<Person> contacts = primaryEntityMapper.listContacts(entityKey);

    if (contacts != null && contacts.stream().anyMatch(p -> p.getKey().equals(personKey))) {
      throw new WebApplicationException("Duplicate contact", HttpStatus.CONFLICT);
    }

    primaryEntityMapper.addContact(entityKey, personKey);
    eventManager.post(
        SubEntityCollectionEvent.newInstance(
            entityKey, objectClass, Person.class, personKey, EventType.LINK));
  }

  @Deprecated
  @Secured({GRSCICOLL_ADMIN_ROLE, GRSCICOLL_EDITOR_ROLE, GRSCICOLL_MEDIATOR_ROLE})
  @Transactional
  @Override
  public void removeContact(@NotNull UUID entityKey, @NotNull UUID personKey) {
    primaryEntityMapper.removeContact(entityKey, personKey);
    eventManager.post(
        SubEntityCollectionEvent.newInstance(
            entityKey, objectClass, Person.class, personKey, EventType.UNLINK));
  }

  @Deprecated
  @Override
  public List<Person> listContacts(@PathVariable UUID key) {
    return primaryEntityMapper.listContacts(key);
  }

  @Override
  public List<Contact> listContactPersons(@NotNull UUID entityKey) {
    return primaryEntityMapper.listContactPersons(entityKey);
  }

  @Validated({PrePersist.class, Default.class})
  @Secured({GRSCICOLL_ADMIN_ROLE, GRSCICOLL_EDITOR_ROLE, GRSCICOLL_MEDIATOR_ROLE})
  @Transactional
  @Override
  public int addContactPerson(@NotNull UUID entityKey, @NotNull @Valid Contact contact) {
    checkArgument(contact.getKey() == null, "Cannot create a contact that already has a key");

    T entity = get(entityKey);
    if (isLockableEntity(entity) && isSourceableField(objectClass, CONTACTS_FIELD_NAME)) {
      throw new IllegalArgumentException(
          "Cannot add contacts to an entity whose master source is not GRSciColl");
    }

    return addContactPersonToEntity(entityKey, contact);
  }

  @Secured({GRSCICOLL_ADMIN_ROLE, GRSCICOLL_EDITOR_ROLE, GRSCICOLL_MEDIATOR_ROLE})
  @Transactional
  private int addContactPersonToEntity(@NotNull UUID entityKey, @NotNull Contact contact) {
    checkArgument(contact.getKey() == null, "Cannot create a contact that already has a key");

    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    final String username = authentication.getName();
    contact.setCreatedBy(username);
    contact.setModifiedBy(username);

    validateUserIds(contact);

    contactMapper.createContact(contact);
    primaryEntityMapper.addContactPerson(entityKey, contact.getKey());
    eventManager.post(
        SubEntityCollectionEvent.newInstance(
            entityKey, objectClass, contact, contact.getKey(), EventType.CREATE));

    return contact.getKey();
  }

  @Validated({PostPersist.class, Default.class})
  @Secured({GRSCICOLL_ADMIN_ROLE, GRSCICOLL_EDITOR_ROLE, GRSCICOLL_MEDIATOR_ROLE})
  @Transactional
  @Override
  public void updateContactPerson(@NotNull UUID entityKey, @NotNull @Valid Contact contact) {
    checkArgument(contact.getKey() != null, "Unable to update a contact with no key");

    T entity = get(entityKey);
    if (isLockableEntity(entity) && isSourceableField(objectClass, CONTACTS_FIELD_NAME)) {
      throw new IllegalArgumentException(
          "Cannot update contacts from an entity whose master source is not GRSciColl");
    }

    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    final String username = authentication.getName();
    contact.setModifiedBy(username);

    validateUserIds(contact);

    contactMapper.updateContact(contact);

    eventManager.post(
        SubEntityCollectionEvent.newInstance(
            entityKey, objectClass, contact, contact.getKey(), EventType.UPDATE));
  }

  private void validateUserIds(Contact contact) {
    // validate userIds
    if (contact.getUserIds() != null && !contact.getUserIds().isEmpty()) {
      for (UserId userId : contact.getUserIds()) {
        if (userId.getType() == null) {
          throw new IllegalArgumentException("UserId type cannot be null");
        }

        IdentifierSchemeValidator validator =
            IdentifierValidatorUtils.getValidatorByIdType(userId.getType());

        if (validator != null && !validator.isValid(userId.getId())) {
          throw new IllegalArgumentException(
              "Invalid user ID with type " + userId.getType() + " and ID " + userId.getId());
        }
      }
    }
  }

  @Secured({GRSCICOLL_ADMIN_ROLE, GRSCICOLL_EDITOR_ROLE, GRSCICOLL_MEDIATOR_ROLE})
  @Transactional
  @Override
  public void removeContactPerson(@NotNull UUID entityKey, @NotNull int contactKey) {
    Contact contactToRemove = contactMapper.getContact(contactKey);
    checkArgument(contactToRemove != null, "Contact to delete doesn't exist");

    T entity = get(entityKey);
    if (isLockableEntity(entity) && isSourceableField(objectClass, CONTACTS_FIELD_NAME)) {
      throw new IllegalArgumentException(
          "Cannot remove contacts from an entity whose master source is not GRSciColl");
    }

    primaryEntityMapper.removeContactPerson(entityKey, contactKey);
    eventManager.post(
        SubEntityCollectionEvent.newInstance(
            entityKey, objectClass, contactToRemove, contactKey, EventType.DELETE));
  }

  @Validated({PrePersist.class, Default.class})
  @Secured({GRSCICOLL_ADMIN_ROLE, GRSCICOLL_EDITOR_ROLE, GRSCICOLL_MEDIATOR_ROLE})
  @Transactional
  @Override
  public void replaceContactPersons(
      @NotNull UUID entityKey, List<@Valid Contact> newContactPersons) {
    checkArgument(entityKey != null);

    List<Contact> contacts = primaryEntityMapper.listContactPersons(entityKey);
    primaryEntityMapper.removeAllContactPersons(entityKey);

    contacts.forEach(
        c ->
            eventManager.post(
                SubEntityCollectionEvent.newInstance(
                    entityKey, objectClass, c, c.getKey(), EventType.DELETE)));

    if (newContactPersons != null) {
      newContactPersons.forEach(c -> addContactPersonToEntity(entityKey, c));
    }
  }

  @Secured({GRSCICOLL_ADMIN_ROLE, GRSCICOLL_EDITOR_ROLE, GRSCICOLL_MEDIATOR_ROLE})
  @Transactional
  @Validated({PrePersist.class, Default.class})
  @Override
  public int addOccurrenceMapping(UUID entityKey, OccurrenceMapping occurrenceMapping) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    occurrenceMapping.setCreatedBy(authentication.getName());
    checkArgument(
        occurrenceMapping.getKey() == null, "Unable to create an entity which already has a key");
    occurrenceMappingMapper.createOccurrenceMapping(occurrenceMapping);
    primaryEntityMapper.addOccurrenceMapping(entityKey, occurrenceMapping.getKey());

    eventManager.post(
        SubEntityCollectionEvent.newInstance(
            entityKey,
            objectClass,
            occurrenceMapping,
            occurrenceMapping.getKey(),
            EventType.CREATE));

    return occurrenceMapping.getKey();
  }

  @Override
  public List<OccurrenceMapping> listOccurrenceMappings(@NotNull UUID uuid) {
    return primaryEntityMapper.listOccurrenceMappings(uuid);
  }

  @Secured({GRSCICOLL_ADMIN_ROLE, GRSCICOLL_EDITOR_ROLE, GRSCICOLL_MEDIATOR_ROLE})
  @Transactional
  @Override
  public void deleteOccurrenceMapping(UUID entityKey, int occurrenceMappingKey) {
    OccurrenceMapping occurrenceMappingToDelete = occurrenceMappingMapper.get(occurrenceMappingKey);
    checkArgument(occurrenceMappingToDelete != null, "Occurrence Mapping to delete doesn't exist");

    primaryEntityMapper.deleteOccurrenceMapping(entityKey, occurrenceMappingKey);
    eventManager.post(
        SubEntityCollectionEvent.newInstance(
            entityKey,
            objectClass,
            occurrenceMappingToDelete,
            occurrenceMappingKey,
            EventType.DELETE));
  }

  @Secured({GRSCICOLL_ADMIN_ROLE, GRSCICOLL_MEDIATOR_ROLE})
  @Transactional
  @Override
  public void replace(UUID targetEntityKey, UUID replacementKey) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    primaryEntityMapper.replace(targetEntityKey, replacementKey, authentication.getName());
    eventManager.post(
        ReplaceEntityEvent.newInstance(
            objectClass, targetEntityKey, replacementKey, EventType.REPLACE));
  }

  @Validated({PrePersist.class, Default.class})
  @Secured({GRSCICOLL_ADMIN_ROLE, GRSCICOLL_EDITOR_ROLE, GRSCICOLL_MEDIATOR_ROLE})
  @Transactional
  @Override
  public int addMasterSourceMetadata(
      UUID targetEntityKey, MasterSourceMetadata masterSourceMetadata) {
    checkArgument(targetEntityKey != null);
    checkArgument(masterSourceMetadata != null);
    checkArgument(
        masterSourceMetadata.getKey() == null, "Cannot create a metadata that already has a key");

    T entity = primaryEntityMapper.get(targetEntityKey);
    checkArgument(entity != null, "The entity doesn't exist");

    if (entity.getMasterSourceMetadata() != null) {
      throw new IllegalArgumentException(
          "The entity already has a master source. You have to delete the existing one before adding a new one");
    }

    // check preconditions for datasets
    if (masterSourceMetadata.getSource() == Source.DATASET) {
      if (!(entity instanceof Collection)) {
        throw new IllegalArgumentException("Dataset sources can be set only to collections");
      }
      checkExistsNetworkEntity(datasetMapper, masterSourceMetadata.getSourceId(), Dataset.class);
    }

    // check preconditions for organizations
    if (masterSourceMetadata.getSource() == Source.ORGANIZATION) {
      if (!(entity instanceof Institution)) {
        throw new IllegalArgumentException("Organization sources can be set only to institutions");
      }
      checkExistsNetworkEntity(
          organizationMapper, masterSourceMetadata.getSourceId(), Organization.class);
    }

    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    masterSourceMetadata.setCreatedBy(authentication.getName());

    masterSourceSyncMetadataMapper.create(masterSourceMetadata);
    MasterSourceType masterSourceType =
        masterSourceMetadata.getSource() == Source.IH_IRN
            ? MasterSourceType.IH
            : MasterSourceType.GBIF_REGISTRY;

    primaryEntityMapper.addMasterSourceMetadata(
        targetEntityKey, masterSourceMetadata.getKey(), masterSourceType);

    // event to sync the entity
    eventManager.post(
        MasterSourceMetadataAddedEvent.newInstance(targetEntityKey, masterSourceMetadata));

    return masterSourceMetadata.getKey();
  }

  @Override
  public void deleteMasterSourceMetadata(UUID targetEntityKey) {
    checkArgument(targetEntityKey != null);

    MasterSourceMetadata metadata =
        primaryEntityMapper.getEntityMasterSourceMetadata(targetEntityKey);

    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!SecurityContextCheck.checkUserInRole(authentication, GRSCICOLL_ADMIN_ROLE)
        && metadata.getSource() == Source.IH_IRN) {
      // non-admins cannot remove a IH metadata if this entity is the only one connected to IH - if
      // it were deleted the next sync would create a new entity for this IH IRN
      int activeEntities =
          masterSourceSyncMetadataMapper.countActiveEntitiesForMasterSource(
              Source.IH_IRN, metadata.getSourceId());

      if (activeEntities <= 1) {
        throw new IllegalArgumentException(
            "This is the only entity connected to IH for the IRN "
                + metadata.getSourceId()
                + ". Only GRSciColl admins can perform this operation");
      }
    }

    primaryEntityMapper.removeMasterSourceMetadata(targetEntityKey);
  }

  @Override
  public MasterSourceMetadata getMasterSourceMetadata(@NotNull UUID targetEntityKey) {
    return primaryEntityMapper.getEntityMasterSourceMetadata(targetEntityKey);
  }

  @Override
  public List<T> findByMasterSource(Source source, String sourceId) {
    return primaryEntityMapper.findByMasterSource(source, sourceId);
  }

  /**
   * Some iDigBio collections and institutions don't have code and we allow that in the DB but not
   * in the API.
   */
  protected void checkCodeUpdate(T newEntity, T oldEntity) {
    if (newEntity.getCode() == null && oldEntity.getCode() != null) {
      throw new IllegalArgumentException("Not allowed to delete the code of a primary entity");
    }
  }

  /**
   * Replaced and converted entities cannot be updated or restored. Also, they can't be replaced or
   * converted in an update
   */
  protected void checkReplacedEntitiesUpdate(T newEntity, T oldEntity) {
    if (oldEntity.getReplacedBy() != null) {
      throw new IllegalArgumentException("Not allowed to update a replaced entity");
    } else if (newEntity.getReplacedBy() != null) {
      throw new IllegalArgumentException("Not allowed to replace an entity while updating");
    }

    if (newEntity instanceof Institution) {
      Institution newInstitution = (Institution) newEntity;
      Institution oldInstitution = (Institution) oldEntity;

      if (oldInstitution.getConvertedToCollection() != null) {
        throw new IllegalArgumentException(
            "Not allowed to update a replaced or converted institution");
      } else if (newInstitution.getConvertedToCollection() != null) {
        throw new IllegalArgumentException(
            "Not allowed to replace or convert an institution while updating");
      }
    }
  }

  private <N extends NetworkEntity> void checkExistsNetworkEntity(
      NetworkEntityMapper<N> mapper, String key, Class<N> entityClass) {
    N networkEntity = mapper.get(UUID.fromString(key));
    if (networkEntity == null) {
      throw new IllegalArgumentException(
          entityClass.getSimpleName() + " not found with key " + key);
    }
    if (networkEntity.getDeleted() != null) {
      throw new IllegalArgumentException(
          "Cannot set a deleted " + entityClass.getSimpleName() + " as master source");
    }
  }
}
