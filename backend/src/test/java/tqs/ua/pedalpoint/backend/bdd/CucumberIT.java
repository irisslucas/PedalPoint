package tqs.ua.pedalpoint.backend.bdd;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Runs every .feature file under src/test/resources/features.
 * Named *IT so that failsafe runs it in the verify phase, with Testcontainers, like the other integration tests.
 * The JSON report (target/cucumber-reports/cucumber.json) can be imported into Xray.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "tqs.ua.pedalpoint.backend.bdd")
// Features tagged @wip have their Gherkin written but no step definitions yet.
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @wip")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/cucumber-reports/cucumber.html, json:target/cucumber-reports/cucumber.json")
class CucumberIT {
}
