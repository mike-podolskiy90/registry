package org.gbif.registry.directory;

import org.gbif.api.model.directory.NodePerson;
import org.gbif.api.model.directory.Participant;
import org.gbif.api.model.directory.ParticipantPerson;
import org.gbif.api.model.directory.Person;
import org.gbif.api.model.registry.Contact;
import org.gbif.api.model.registry.Identifier;
import org.gbif.api.model.registry.Node;
import org.gbif.api.service.directory.NodeService;
import org.gbif.api.service.directory.ParticipantService;
import org.gbif.api.service.directory.PersonService;
import org.gbif.api.vocabulary.ContactType;
import org.gbif.api.vocabulary.IdentifierType;
import org.gbif.api.vocabulary.directory.NodePersonRole;
import org.gbif.api.vocabulary.directory.ParticipantPersonRole;

import java.net.URI;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DirectoryAugmenterImpl implements Augmenter {

  /**
   * Maps contact types to participant roles.
   */
  private static final ImmutableMap<ParticipantPersonRole,ContactType> PARTICIPANT_ROLE_TO_CONTACT_TYPE =
          ImmutableMap.of(ParticipantPersonRole.ADDITIONAL_DELEGATE, ContactType.ADDITIONAL_DELEGATE,
                  ParticipantPersonRole.HEAD_OF_DELEGATION, ContactType.HEAD_OF_DELEGATION,
                  ParticipantPersonRole.TEMPORARY_DELEGATE, ContactType.TEMPORARY_DELEGATE,
                  ParticipantPersonRole.TEMPORARY_HEAD_OF_DELEGATION, ContactType.TEMPORARY_HEAD_OF_DELEGATION);

  private static final ImmutableMap<NodePersonRole,ContactType> NODE_ROLE_TO_CONTACT_TYPE =
          ImmutableMap.of(NodePersonRole.NODE_MANAGER, ContactType.NODE_MANAGER,
                  NodePersonRole.NODE_STAFF, ContactType.NODE_STAFF);

  private static Logger LOG = LoggerFactory.getLogger(DirectoryAugmenterImpl.class);

  private ParticipantService participantService;
  private NodeService nodeService;
  private PersonService personService;

  @Inject
  public DirectoryAugmenterImpl(ParticipantService participantService, NodeService nodeService, PersonService personService) {
    this.participantService = participantService;
    this.nodeService = nodeService;
    this.personService = personService;
  }

  /**
   * Gets the participantID from the Node.
   */
  private static Integer findParticipantID(Node node) {
    for (Identifier id : node.getIdentifiers()) {
      if (IdentifierType.GBIF_PARTICIPANT == id.getType()) {
        try {
          return Integer.parseInt(id.getIdentifier());
        } catch (NumberFormatException e) {
          LOG.error("IMS Participant ID is no integer: %s", id.getIdentifier());
        }
      }
    }
    return null;
  }

  @Override
  public Node augment(Node node) {
    if (node != null) {
      try {
        Integer participantID = findParticipantID(node);
        if (participantID != null) {
          Participant participant = participantService.get(participantID);
          List<Contact> contacts = Lists.newArrayList();
          if (participant != null) {
            // update node with Directory info if it exists
            List<org.gbif.api.model.directory.Node> participantNodes = getParticipantNodes(participant);
            node.setParticipantTitle(participant.getName());
            contacts.addAll(getContactsForParticipant(participant));

            node.setAbbreviation(participant.getAbbreviatedName());
            node.setDescription(participant.getComments());
            if(participant.getMembershipStart() != null) {
              node.setParticipantSince(getParticipantSinceYear(participant.getMembershipStart()));
            }
            if(!participantNodes.isEmpty()){
              contacts.addAll(getContactsForNode(participantNodes));
              node.setAddress(getNodesAddresses(participantNodes));
              node.setHomepage(getWebUrls(participant,participantNodes));
              node.setEmail(getEmails(participantNodes));
              node.setPhone(getPhones(participantNodes));
            }
            node.setContacts(contacts);
          }
        }
      } catch (Exception e) {
        LOG.error("Failed to augment node %s with Directory information", node.getKey(), e);
      }
    }
    return node;
  }

  /**
   * Gets the year part of the membershipStart field.
   */
  private static Integer getParticipantSinceYear(String membershipStart) {
    String[] membershipStartComponents = membershipStart.split(" ");
    if(membershipStartComponents.length > 1) {
      return Ints.tryParse(membershipStartComponents[1].trim());
    }
    return null;
  }

  /**
   * Gets all the nodes associated to a participant.
   */
  private List<org.gbif.api.model.directory.Node> getParticipantNodes(Participant participant){
    List<org.gbif.api.model.directory.Node> nodes = Lists.newArrayList();
    if(participant.getNodes() != null){
      for(org.gbif.api.model.directory.Node node : participant.getNodes()) {
        nodes.add(nodeService.get(node.getId()));
      }
    }
    return nodes;
  }

  /**
   * Gets all the addresses associated to a participant.
   */
  private static List<String> getNodesAddresses(List<org.gbif.api.model.directory.Node> participantNodes){
    List<String> addresses = Lists.newArrayList();
    for(org.gbif.api.model.directory.Node node : participantNodes) {
      addresses.add(node.getAddress());
    }
    return  addresses;
  }

  /**
   * Gets all the web pages/urls of participant and its nodes.
   */
  private static List<URI> getWebUrls(Participant participant, List<org.gbif.api.model.directory.Node> participantNodes){
    List<URI> addresses = Lists.newArrayList();
    if(participant.getParticipantUrl() != null) {
      addresses.add(URI.create(participant.getParticipantUrl()));
    }
    for(org.gbif.api.model.directory.Node node : participantNodes) {
      if(node.getNodeUrl() != null) {
        addresses.add(URI.create(node.getNodeUrl()));
      }
    }
    return  addresses;
  }

  /**
   * Gets all the emails from the nodes list.
   */
  private static List<String> getEmails(List<org.gbif.api.model.directory.Node> participantNodes){
    List<String> emails = Lists.newArrayList();
    for(org.gbif.api.model.directory.Node node : participantNodes) {
      if(node.getEmail() != null) {
        emails.add(node.getEmail());
      }
    }
    return  emails;
  }

  /**
   * Gets all the phones numbers from the nodes list.
   */
  private static List<String> getPhones(List<org.gbif.api.model.directory.Node> participantNodes){
    List<String> phones = Lists.newArrayList();
    for(org.gbif.api.model.directory.Node node : participantNodes) {
      if(node.getPhone() != null) {
        phones.add(node.getPhone());
      }
    }
    return  phones;
  }

  /**
   * Transforms the persons associated to a participant into a list of contacts.
   */
  private List<Contact> getContactsForParticipant(Participant participant){
    List<Contact> contacts = Lists.newArrayList();
    if(participant.getPeople() != null){
      Person person;
      Contact contact;
      ContactType contactType;
      for(ParticipantPerson participantPerson : participant.getPeople()) {
        person = personService.get(participantPerson.getPersonId());
        contactType = null;
        if( participantPerson.getRole() != null) {
          contactType = PARTICIPANT_ROLE_TO_CONTACT_TYPE.get(participantPerson.getRole());
        }
        contact = personToContact(person, participant.getName(), contactType);
        contacts.add(contact);
      }
    }
    return contacts;
  }

  /**
   * Transforms the persons associated to a participant into a list of contacts.
   * @param nodes it theory it should never be more than one
   */
  private List<Contact> getContactsForNode(List<org.gbif.api.model.directory.Node> nodes){
    List<Contact> contacts = Lists.newArrayList();
    if(nodes != null){
      Person person;
      Contact contact;
      ContactType contactType;
      for(org.gbif.api.model.directory.Node currentNode : nodes) {
        for (NodePerson nodePerson : currentNode.getPeople()) {
          person = personService.get(nodePerson.getPersonId());
          contactType = null;
          if (nodePerson.getRole() != null) {
            contactType = NODE_ROLE_TO_CONTACT_TYPE.get(nodePerson.getRole());
          }
          contact = personToContact(person, currentNode.getName(), contactType);
          contacts.add(contact);
        }
      }
    }
    return contacts;
  }

  /**
   * Transform a Directory Person into a Registry Contact.
   * @param person
   * @param organization
   * @param contactType
   * @return
   */
  private Contact personToContact(Person person, String organization, ContactType contactType){
    Contact contact = new Contact();
    contact.setKey(person.getId());
    contact.addAddress(person.getAddress());
    contact.setCountry(person.getCountryCode());
    if(person.getEmail() != null) {
      contact.addEmail(person.getEmail());
    }
    contact.setFirstName(person.getFirstName());
    contact.setLastName(person.getSurname());
    if(person.getPhone() != null) {
      contact.addPhone(person.getPhone());
    }
    contact.setModified(person.getModified());
    contact.setOrganization(organization);
    if( contactType != null) {
      contact.setType(contactType);
    }
    return contact;
  }
}