/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.client.ClientConfiguredServices;
import uk.gov.gchq.palisade.config.service.ConfigUtils;
import uk.gov.gchq.palisade.config.service.ConfigurationService;
import uk.gov.gchq.palisade.example.client.ExampleSimpleClient;
import uk.gov.gchq.palisade.example.common.ExampleUsers;
import uk.gov.gchq.palisade.example.common.Purpose;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.util.StreamUtil;

import java.io.InputStream;
import java.util.stream.Stream;


public class RestExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestExample.class);

    public static void main(final String[] args) throws Exception {
        if (args.length < 1) {
            System.out.printf("Usage: %s file\n", RestExample.class.getTypeName());
            System.out.println("\nfile\tfile containing serialised Employee instances to read");
            System.exit(1);
        }

        String sourceFile = args[0];
        new RestExample().run(sourceFile);
    }

    public void run(final String sourceFile) throws Exception {
        final InputStream stream = StreamUtil.openStream(this.getClass(), System.getProperty(ConfigUtils.CONFIG_SERVICE_PATH));
        ConfigurationService configService = JSONSerialiser.deserialise(stream, ConfigurationService.class);

        ClientConfiguredServices configuredServices = new ClientConfiguredServices(configService);

        PalisadeService palisade = configuredServices.getPalisadeService();

        final ExampleSimpleClient client = new ExampleSimpleClient(palisade);

        final User alice = ExampleUsers.getAlice();
        final User bob = ExampleUsers.getBob();
        final User eve = ExampleUsers.getEve();

        LOGGER.info("");
        LOGGER.info("Alice [ " + alice.toString() + " } is reading the Employee file with a purpose of SALARY...");
        final Stream<Employee> aliceResults = client.read(sourceFile, "Alice", Purpose.SALARY.name());
        LOGGER.info("Alice got back: ");
        aliceResults.map(Object::toString).forEach(LOGGER::info);

        LOGGER.info("");
        LOGGER.info("Alice [ " + alice.toString() + " } is reading the Employee file with a purpose of DUTY_OF_CARE...");
        final Stream<Employee> aliceResults2 = client.read(sourceFile, "Alice", Purpose.DUTY_OF_CARE.name());
        LOGGER.info("Alice got back: ");
        aliceResults2.map(Object::toString).forEach(LOGGER::info);

        LOGGER.info("");
        LOGGER.info("Alice [ " + alice.toString() + " } is reading the Employee file with a purpose of STAFF_REPORT...");
        final Stream<Employee> aliceResults3 = client.read(sourceFile, "Alice", Purpose.STAFF_REPORT.name());
        LOGGER.info("Alice got back: ");
        aliceResults3.map(Object::toString).forEach(LOGGER::info);

        LOGGER.info("");
        LOGGER.info("Bob [ " + bob.toString() + " } is reading the Employee file with a purpose of DUTY_OF_CARE...");
        final Stream<Employee> bobResults1 = client.read(sourceFile, "Bob", Purpose.DUTY_OF_CARE.name());
        LOGGER.info("Bob got back: ");
        bobResults1.map(Object::toString).forEach(LOGGER::info);

        LOGGER.info("");
        LOGGER.info("Bob [ " + bob.toString() + " } is reading the Employee file with a purpose that is empty...");
        final Stream<Employee> bobResults2 = client.read(sourceFile, "Bob", "");
        LOGGER.info("Bob got back: ");
        bobResults2.map(Object::toString).forEach(LOGGER::info);

        LOGGER.info("");
        LOGGER.info("Eve [ " + eve.toString() + " } is reading the Employee file with a purpose that is empty...");
        final Stream<Employee> eveResults1 = client.read(sourceFile, "Eve", "");
        LOGGER.info("Eve got back: ");
        eveResults1.map(Object::toString).forEach(LOGGER::info);
    }
}
