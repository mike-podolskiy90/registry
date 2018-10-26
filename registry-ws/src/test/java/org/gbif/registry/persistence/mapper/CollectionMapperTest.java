package org.gbif.registry.persistence.mapper;

import org.gbif.api.model.collections.Address;
import org.gbif.api.model.collections.Collection;
import org.gbif.api.model.collections.Institution;
import org.gbif.api.model.collections.vocabulary.AccessionStatus;
import org.gbif.api.model.collections.vocabulary.PreservationType;
import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.registry.Tag;
import org.gbif.registry.database.DatabaseInitializer;
import org.gbif.registry.database.LiquibaseInitializer;
import org.gbif.registry.database.LiquibaseModules;
import org.gbif.registry.guice.RegistryTestModules;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.google.inject.Injector;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CollectionMapperTest {

  private CollectionMapper collectionMapper;
  private AddressMapper addressMapper;

  @ClassRule
  public static LiquibaseInitializer liquibase = new LiquibaseInitializer(LiquibaseModules.database());

  @Rule
  public final DatabaseInitializer databaseRule = new DatabaseInitializer(LiquibaseModules.database());

  @Before
  public void setup() {
    Injector inj = RegistryTestModules.mybatis();
    collectionMapper = inj.getInstance(CollectionMapper.class);
    addressMapper = inj.getInstance(AddressMapper.class);
  }

  @Test
  public void crudTest() {
    UUID key = UUID.randomUUID();

    assertNull(collectionMapper.get(key));

    Collection collection = new Collection();
    collection.setKey(key);
    collection.setAccessionStatus(AccessionStatus.INSTITUTIONAL);
    collection.setCode("CODE");
    collection.setName("NAME");
    collection.setCreatedBy("test");
    collection.setModifiedBy("test");

    List<PreservationType> preservationTypes = new ArrayList<>();
    preservationTypes.add(PreservationType.STORAGE_CONTROLLED_ATMOSPHERE);
    preservationTypes.add(PreservationType.SAMPLE_CRYOPRESERVED);
    collection.setPreservationTypes(preservationTypes);

    Address address = new Address();
    address.setKey(1);
    address.setAddress("dummy address");
    addressMapper.create(address);

    collection.setAddress(address);

    collectionMapper.create(collection);

    Collection collectionStored = collectionMapper.get(key);

    assertEquals("CODE", collectionStored.getCode());
    assertEquals("NAME", collectionStored.getName());
    assertEquals(AccessionStatus.INSTITUTIONAL, collectionStored.getAccessionStatus());
    assertEquals(2, collectionStored.getPreservationTypes().size());

    // assert address
    assertEquals((Integer) 1, collectionStored.getAddress().getKey());
    assertEquals("dummy address", collectionStored.getAddress().getAddress());

    // update entity
    collection.setDescription("dummy description");
    preservationTypes.add(PreservationType.SAMPLE_DRIED);
    collection.setPreservationTypes(preservationTypes);
    collectionMapper.update(collection);
    collectionStored = collectionMapper.get(key);

    assertEquals("CODE", collectionStored.getCode());
    assertEquals("NAME", collectionStored.getName());
    assertEquals("dummy description", collectionStored.getDescription());
    assertEquals(AccessionStatus.INSTITUTIONAL, collectionStored.getAccessionStatus());
    assertEquals(3, collectionStored.getPreservationTypes().size());

    // assert address
    assertEquals((Integer) 1, collectionStored.getAddress().getKey());
    assertEquals("dummy address", collectionStored.getAddress().getAddress());

    // delete address
    collection.setAddress(null);
    collectionMapper.update(collection);
    collectionStored = collectionMapper.get(key);
    assertNull(collectionStored.getAddress());

    // delete entity
    collectionMapper.delete(key);
    collectionStored = collectionMapper.get(key);
    assertNotNull(collectionStored.getDeleted());
  }

  @Test
  public void listTest() {
    Collection col1 = new Collection();
    col1.setKey(UUID.randomUUID());
    col1.setCode("c1");
    col1.setName("n1");
    col1.setCreatedBy("test");
    col1.setModifiedBy("test");

    Collection col2 = new Collection();
    col2.setKey(UUID.randomUUID());
    col2.setCode("c2");
    col2.setName("n2");
    col2.setCreatedBy("test");
    col2.setModifiedBy("test");

    Collection col3 = new Collection();
    col3.setKey(UUID.randomUUID());
    col3.setCode("c3");
    col3.setName("n3");
    col3.setCreatedBy("test");
    col3.setModifiedBy("test");

    collectionMapper.create(col1);
    collectionMapper.create(col2);
    collectionMapper.create(col3);

    Pageable pageable = new Pageable() {
      @Override
      public int getLimit() {
        return 5;
      }

      @Override
      public long getOffset() {
        return 0;
      }
    };

    List<Collection> cols = collectionMapper.list(pageable);
    assertEquals(3, cols.size());

    pageable = new Pageable() {
      @Override
      public int getLimit() {
        return 2;
      }

      @Override
      public long getOffset() {
        return 0;
      }
    };

    cols = collectionMapper.list(pageable);
    assertEquals(2, cols.size());
  }

  @Test
  public void searchTest() {
    Collection col1 = new Collection();
    col1.setKey(UUID.randomUUID());
    col1.setCode("c1");
    col1.setName("n1");
    col1.setCreatedBy("test");
    col1.setModifiedBy("test");

    Address address = new Address();
    address.setKey(1);
    address.setAddress("dummy address");
    addressMapper.create(address);

    col1.setAddress(address);

    Collection col2 = new Collection();
    col2.setKey(UUID.randomUUID());
    col2.setCode("c2");
    col2.setName("n1");
    col2.setCreatedBy("test");
    col2.setModifiedBy("test");

    collectionMapper.create(col1);
    collectionMapper.create(col2);

    Pageable pageable = new Pageable() {
      @Override
      public int getLimit() {
        return 5;
      }

      @Override
      public long getOffset() {
        return 0;
      }
    };

    List<Collection> cols = collectionMapper.search("c1 n1", pageable);
    assertEquals(1, cols.size());
    assertEquals("c1", cols.get(0).getCode());
    assertEquals("n1", cols.get(0).getName());

    cols = collectionMapper.search("c2 c1", pageable);
    assertEquals(0, cols.size());

    cols = collectionMapper.search("c3", pageable);
    assertEquals(0, cols.size());

    cols = collectionMapper.search("n1", pageable);
    assertEquals(2, cols.size());

    cols = collectionMapper.search("dummy address", pageable);
    assertEquals(1, cols.size());
  }

}
