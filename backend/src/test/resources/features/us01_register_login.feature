@epic-1 @US-01 @wip
Feature: Register and log in with a @ua.pt email
  As a student
  I want to create an account with my university email and log in
  So that I can use the e-bike service

  Scenario: Register with a university email
    Given no account exists for "tiago.mendes@ua.pt"
    When a person registers with email "tiago.mendes@ua.pt", a password and profile "STUDENT"
    Then the account is created with role "USER" and profile "STUDENT"

  Scenario Outline: Only @ua.pt emails are accepted
    When a person registers with email "<email>"
    Then the registration is "<result>"

    Examples:
      | email                   | result   |
      | tiago.mendes@ua.pt      | accepted |
      | TIAGO.MENDES@UA.PT      | accepted |
      | tiago.mendes@gmail.com  | rejected |
      | tiago.mendes@rianova.pt | rejected |
      | tiago@ua.pt.com         | rejected |
      | tiago@xua.pt            | rejected |

  Scenario: The same email cannot be registered twice
    Given "tiago.mendes@ua.pt" has an active account
    When a person registers with email "tiago.mendes@ua.pt"
    Then the registration is rejected with message "Email already registered"

  Scenario: Log in with valid credentials
    Given "tiago.mendes@ua.pt" has an active account
    When "tiago.mendes@ua.pt" logs in with the correct password
    Then a JWT token is returned

  Scenario: Log in with wrong credentials
    Given "tiago.mendes@ua.pt" has an active account
    When "tiago.mendes@ua.pt" logs in with a wrong password
    Then the error message "Invalid email or password" is shown
    And no token is returned
