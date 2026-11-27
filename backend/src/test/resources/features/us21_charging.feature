@epic-5 @US-21
Feature: Charging and minimum battery
  As a student
  I want e-bikes with low battery to charge at the hubs until they can be used again
  So that the e-bikes offered to me have enough battery for a trip

  Background:
    Given the minimum battery to use an e-bike is 20%
    And every hub charges e-bikes at 1% per minute

  Scenario Outline: An e-bike at a hub below the minimum battery is charging
    Given the e-bike "EB-001" is parked at hub "Biblioteca"
    When the telemetry source publishes battery <battery>% for "EB-001"
    Then the e-bike "EB-001" has state "<state>"

    Examples:
      | battery | state     |
      | 0       | CHARGING  |
      | 19      | CHARGING  |
      | 20      | AVAILABLE |
      | 80      | AVAILABLE |

  Scenario Outline: Charging at 1% per minute
    Given the e-bike "EB-001" arrived at hub "Biblioteca" with battery <start>%
    When <minutes> minutes pass
    Then the e-bike "EB-001" has battery <battery>% and state "<state>"

    Examples:
      | start | minutes | battery | state     |
      | 0     | 19      | 19      | CHARGING  |
      | 0     | 20      | 20      | AVAILABLE |
      | 95    | 10      | 100     | AVAILABLE |

