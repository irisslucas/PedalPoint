# Iteration 2 user stories

Checkpoint: 27/11. Total: 18 story points.
The acceptance criteria of each core story are written as Gherkin in `backend/src/test/resources/features` (one `.feature` per story, tagged `@US-<n>`).
A feature tagged `@wip` has its Gherkin written but no step definitions yet; the developer of the story removes the tag when the steps are implemented.

| Story | Summary | Epic | SP | Owner | Gherkin |
|---|---|---|---|---|---|
| US-01 | Register and log in with a @ua.pt email | 1 · Accounts and roles | 3 | Team Coordinator | `us01_register_login.feature` |
| US-04 | See the available e-bikes at a hub, by type | 2 · Find and use an e-bike | 3 | Product Owner | `us04_available_ebikes.feature` |
| US-20 | Simulator and MQTT telemetry with a replaceable adapter | 5 · Availability based on battery | 5 | DevOps Master | `us20_telemetry.feature` |
| US-21 | Charging and minimum battery (CHARGING state) | 5 · Availability based on battery | 5 | DevOps Master | `us21_charging.feature` |
| — | Set up Cucumber with the first scenario running (technical story) | — | 2 | QA Engineer | — |

## US-01 — Register and log in with a @ua.pt email (3 SP)

As a student, I want to create an account with my university email and log in, so that I can use the e-bike service.

- Only @ua.pt emails are accepted (case-insensitive), so that every trip belongs to an identifiable person.
- A new account gets the role USER and the profile STUDENT or STAFF.
- Two accounts cannot have the same email.
- Login returns a JWT token.
- Wrong credentials show an error message and give no access.

## US-04 — See the available e-bikes at a hub, by type (3 SP)

As a student, I want to see the e-bikes available at each hub, by type, so that I can quickly find an e-bike near me.

- Only e-bikes in state AVAILABLE with battery at or above the minimum (20%) are listed.
- RESERVED, CHARGING, MAINTENANCE and RETIRED e-bikes are not listed.
- The list can be filtered by type: E_BIKE or CARGO_E_BIKE.
- The e-bikes are ordered by battery, highest first.
- An unknown hub returns HTTP 404; an unknown type returns HTTP 400.
- Out of scope in I2: restricting cargo e-bikes to staff (depends on login, US-01).
- This story is the AI4 story: the Gherkin was written first and the code was generated from it.

## US-20 — Simulator and MQTT telemetry with a replaceable adapter (5 SP)

As an operator, I want the e-bike state to be updated automatically from telemetry, so that I have reliable information without manual checks.

- The simulator runs in its own container and publishes, over MQTT, the battery, hub, lock state and fault code of each e-bike.
- The backend reads telemetry only through the `TelemetrySource` interface; the `MqttTelemetryAdapter` can be replaced without changing the business logic.
- The battery, hub and last telemetry time of each e-bike are updated.
- Invalid messages are ignored and logged, and the other messages are still processed.
- Integration tests use Mosquitto in Testcontainers.
- Uses the `Bike` entity and the `bike` table created in US-04 (do not create another one).

## US-21 — Charging and minimum battery (5 SP)

As a student, I want e-bikes with low battery to charge at the hubs until they can be used again, so that the e-bikes offered to me have enough battery for a trip.

- Every hub has chargers: an e-bike locked at a hub charges at 1% per minute, up to 100%.
- An e-bike at a hub with less than 20% goes to CHARGING.
- At 20% it goes back to AVAILABLE automatically (at most 20 minutes for an empty e-bike).
- Time in CHARGING does not count as unavailability for the SLA.

Each feature covers one story only. Rules about CHARGING that belong to other stories are tested in those stories:
- A CHARGING e-bike is not listed: US-04 (`us04_available_ebikes.feature`).
- A CHARGING e-bike cannot be booked or unlocked: US-05 and US-06 (I3).
- A trip continues when the battery drops below 20%, and the e-bike goes to CHARGING when the trip ends at a hub: US-07 (I3).

## Technical story — Set up Cucumber (2 SP)

As the QA Engineer, I want the Gherkin scenarios to run with Cucumber in `./mvnw verify`, so that the acceptance criteria of the core stories are tested automatically.

- The `.feature` files are in `backend/src/test/resources/features`, one per story, tagged `@US-<n>`.
- They run in `verify` (failsafe), with Testcontainers, and CI fails if a scenario fails.
- Features tagged `@wip` are excluded from the run.
- The JSON report (`target/cucumber-reports/cucumber.json`) can be imported into Xray.
