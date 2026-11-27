@epic-5 @US-20
Feature: Telemetry from the fleet simulator
  As an operator
  I want the e-bike state to be updated automatically from telemetry
  So that I have reliable information without manual checks

  Scenario: Telemetry updates the e-bike
    Given the e-bike "EB-001" has battery 80% at hub "Residências"
    When the telemetry source publishes for "EB-001":
      | battery | hub        | lock   | faultCode |
      | 55      | Biblioteca | LOCKED |           |
    Then the e-bike "EB-001" has battery 55% at hub "Biblioteca"
    And the last telemetry time of "EB-001" is updated

  Scenario: An invalid telemetry message is ignored
    Given the e-bike "EB-001" has battery 80%
    When the telemetry source publishes an invalid message for "EB-001"
    And the telemetry source publishes battery 70% for "EB-002"
    Then the e-bike "EB-001" still has battery 80%
    And the invalid message is logged
    And the e-bike "EB-002" has battery 70%

  @integration
  Scenario: The MQTT adapter reads messages from the broker
    Given a Mosquitto broker is running
    And the MqttTelemetryAdapter is subscribed to "pedalpoint/bikes/+/telemetry"
    When a message with battery 42 is published to "pedalpoint/bikes/EB-001/telemetry"
    Then the e-bike "EB-001" has battery 42%
