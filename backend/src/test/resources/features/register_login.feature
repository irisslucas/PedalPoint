@epic-1 @US-01 @wip
Feature: Register and log in with a @ua.pt email
  Scenario: Register with a UA email
    Given no account exists for "ana@ua.pt"
    When I register with email "ana@ua.pt", password "Secret123!" and profile "STUDENT"
    Then the account is created with role "USER" and profile "STUDENT"
  Scenario: Reject an email outside the UA domain
    When I register with email "ana@gmail.com", password "Secret123!" and profile "STUDENT"
    Then the registration is rejected with the message "Only @ua.pt emails can register"
  Scenario: Reject a duplicate email
    Given an account exists for "ana@ua.pt"
    When I register with email "ana@ua.pt", password "Other123!" and profile "STUDENT"
    Then the registration is rejected with the message "Email already registered"
  Scenario: Log in with valid credentials
    Given an account exists for "ana@ua.pt" with password "Secret123!"
    When I log in with "ana@ua.pt" and "Secret123!"
    Then I receive an access token
  Scenario: Reject wrong credentials
    Given an account exists for "ana@ua.pt" with password "Secret123!"
    When I log in with "ana@ua.pt" and "Wrong123!"
    Then the login is rejected
