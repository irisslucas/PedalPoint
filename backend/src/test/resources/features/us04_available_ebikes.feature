@epic-2 @US-04
Feature: See the available e-bikes at a hub
  As a student
  I want to see the e-bikes available at each hub, by type
  So that I can quickly find an e-bike near me

  Background:
    Given the minimum battery to book is 20%
    And the hub "Residências" exists

  Scenario Outline: Only e-bikes that can be used are listed
    Given the e-bike "<bike>" of type "E_BIKE" is at hub "Residências" with state "<state>" and battery <battery>%
    When a user lists the e-bikes at hub "Residências"
    Then the e-bike "<bike>" is <visibility>

    Examples:
      | bike   | state       | battery | visibility |
      | EB-001 | AVAILABLE   | 80      | listed     |
      | EB-002 | AVAILABLE   | 20      | listed     |
      | EB-003 | AVAILABLE   | 19      | not listed |
      | EB-004 | CHARGING    | 15      | not listed |
      | EB-005 | MAINTENANCE | 90      | not listed |
      | EB-006 | RESERVED    | 90      | not listed |
      | EB-007 | RETIRED     | 90      | not listed |

  Scenario: Filter by type
    Given the e-bike "EB-010" of type "E_BIKE" is at hub "Residências" with state "AVAILABLE" and battery 80%
    And the e-bike "CG-010" of type "CARGO_E_BIKE" is at hub "Residências" with state "AVAILABLE" and battery 80%
    When a user lists the e-bikes of type "CARGO_E_BIKE" at hub "Residências"
    Then only the e-bikes "CG-010" are listed

  Scenario: Only the e-bikes of the chosen hub are listed
    Given the hub "Biblioteca" exists
    And the e-bike "EB-020" of type "E_BIKE" is at hub "Residências" with state "AVAILABLE" and battery 80%
    And the e-bike "EB-021" of type "E_BIKE" is at hub "Biblioteca" with state "AVAILABLE" and battery 80%
    When a user lists the e-bikes at hub "Residências"
    Then only the e-bikes "EB-020" are listed

  Scenario: The e-bikes with more battery are listed first
    Given the e-bike "EB-030" of type "E_BIKE" is at hub "Residências" with state "AVAILABLE" and battery 45%
    And the e-bike "EB-031" of type "E_BIKE" is at hub "Residências" with state "AVAILABLE" and battery 95%
    And the e-bike "EB-032" of type "E_BIKE" is at hub "Residências" with state "AVAILABLE" and battery 70%
    When a user lists the e-bikes at hub "Residências"
    Then only the e-bikes "EB-031, EB-032, EB-030" are listed

  Scenario: A hub without available e-bikes returns an empty list
    When a user lists the e-bikes at hub "Residências"
    Then the response status is 200
    And no e-bikes are listed

  Scenario: Unknown hub
    When a user lists the e-bikes at a hub that does not exist
    Then the response status is 404

  Scenario: Unknown e-bike type
    When a user lists the e-bikes of type "SCOOTER" at hub "Residências"
    Then the response status is 400
