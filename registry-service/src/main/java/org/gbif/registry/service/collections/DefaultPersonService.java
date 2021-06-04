package org.gbif.registry.service.collections;

import org.gbif.api.model.collections.Person;
import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingRequest;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.registry.PrePersist;
import org.gbif.api.model.registry.search.collections.PersonSuggestResult;
import org.gbif.api.service.collections.PersonService;
import org.gbif.registry.events.EventManager;
import org.gbif.registry.events.collections.CreateCollectionEntityEvent;
import org.gbif.registry.events.collections.UpdateCollectionEntityEvent;
import org.gbif.registry.persistence.mapper.CommentMapper;
import org.gbif.registry.persistence.mapper.IdentifierMapper;
import org.gbif.registry.persistence.mapper.MachineTagMapper;
import org.gbif.registry.persistence.mapper.TagMapper;
import org.gbif.registry.persistence.mapper.collections.AddressMapper;
import org.gbif.registry.persistence.mapper.collections.PersonMapper;
import org.gbif.registry.service.WithMyBatis;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.validation.groups.Default;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.checkArgument;
import static org.gbif.registry.security.UserRoles.GRSCICOLL_ADMIN_ROLE;
import static org.gbif.registry.security.UserRoles.GRSCICOLL_EDITOR_ROLE;
import static org.gbif.registry.security.UserRoles.GRSCICOLL_MEDIATOR_ROLE;

@Validated
@Service
public class DefaultPersonService extends BaseCollectionEntityService<Person>
    implements PersonService {

  private final PersonMapper personMapper;
  private final AddressMapper addressMapper;

  @Autowired
  protected DefaultPersonService(
      PersonMapper personMapper,
      AddressMapper addressMapper,
      IdentifierMapper identifierMapper,
      TagMapper tagMapper,
      MachineTagMapper machineTagMapper,
      CommentMapper commentMapper,
      EventManager eventManager,
      WithMyBatis withMyBatis) {
    super(
        personMapper,
        tagMapper,
        machineTagMapper,
        identifierMapper,
        commentMapper,
        Person.class,
        eventManager,
        withMyBatis);
    this.addressMapper = addressMapper;
    this.personMapper = personMapper;
  }

  @Secured({GRSCICOLL_ADMIN_ROLE, GRSCICOLL_EDITOR_ROLE, GRSCICOLL_MEDIATOR_ROLE})
  @Transactional
  @Validated({PrePersist.class, Default.class})
  @Override
  public UUID create(Person person) {
    checkArgument(person.getKey() == null, "Unable to create an entity which already has a key");
    preCreate(person);

    if (person.getMailingAddress() != null) {
      addressMapper.create(person.getMailingAddress());
    }

    person.setKey(UUID.randomUUID());
    personMapper.create(person);

    eventManager.post(CreateCollectionEntityEvent.newInstance(person));
    return person.getKey();
  }

  @Secured({GRSCICOLL_ADMIN_ROLE, GRSCICOLL_EDITOR_ROLE, GRSCICOLL_MEDIATOR_ROLE})
  @Transactional
  @Override
  public void update(Person person) {
    preUpdate(person);
    Person oldPerson = get(person.getKey());
    checkArgument(oldPerson != null, "Entity doesn't exist");

    if (oldPerson.getDeleted() != null) {
      // if it's deleted we only allow to update it if we undelete it
      checkArgument(
          person.getDeleted() == null,
          "Unable to update a previously deleted entity unless you clear the deletion timestamp");
    } else {
      // not allowed to delete when updating
      checkArgument(person.getDeleted() == null, "Can't delete an entity when updating");
    }

    // update mailing address
    if (person.getMailingAddress() != null) {
      if (oldPerson.getMailingAddress() == null) {
        checkArgument(
            person.getMailingAddress().getKey() == null,
            "Unable to create an address which already has a key");
        addressMapper.create(person.getMailingAddress());
      } else {
        addressMapper.update(person.getMailingAddress());
      }
    }

    // update entity
    personMapper.update(person);

    // check if we have to delete the mailing address
    if (person.getMailingAddress() == null && oldPerson.getMailingAddress() != null) {
      addressMapper.delete(oldPerson.getMailingAddress().getKey());
    }

    // check if we have to delete the address
    Person newPerson = get(person.getKey());
    eventManager.post(UpdateCollectionEntityEvent.newInstance(newPerson, oldPerson));
  }

  @Override
  public PagingResponse<Person> list(
      @Nullable String query,
      @Nullable UUID institutionKey,
      @Nullable UUID collectionKey,
      @Nullable Pageable page) {
    page = page == null ? new PagingRequest() : page;
    query = query != null ? Strings.emptyToNull(CharMatcher.WHITESPACE.trimFrom(query)) : query;
    long total = personMapper.count(institutionKey, collectionKey, query);
    return new PagingResponse<>(
        page, total, personMapper.list(institutionKey, collectionKey, query, page));
  }

  @Override
  public PagingResponse<Person> listDeleted(@Nullable Pageable page) {
    page = page == null ? new PagingRequest() : page;
    return new PagingResponse<>(page, personMapper.countDeleted(), personMapper.deleted(page));
  }

  @Override
  public List<PersonSuggestResult> suggest(String q) {
    return personMapper.suggest(q);
  }
}
