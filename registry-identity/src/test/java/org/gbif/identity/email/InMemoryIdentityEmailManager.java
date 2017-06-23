package org.gbif.identity.email;

import org.gbif.api.model.common.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of {@link IdentityEmailManager} that keeps information in variable instead of
 * sending real emails.
 * Should only be used for testing.
 */
public class InMemoryIdentityEmailManager implements IdentityEmailManager {

  private final Map<String, UUID> emailToChallengeCode = new HashMap<>();

  @Override
  public void generateAndSendUserCreated(User user, UUID challengeCode) {
    emailToChallengeCode.put(user.getEmail(), challengeCode);
  }

  @Override
  public void generateAndSendPasswordReset(User user, UUID challengeCode) {
    emailToChallengeCode.put(user.getEmail(), challengeCode);
  }

  public UUID getChallengeCode(String email){
    return emailToChallengeCode.get(email);
  }

  public int getNumberOfChallengeCode() {
    return emailToChallengeCode.size();
  }

}
