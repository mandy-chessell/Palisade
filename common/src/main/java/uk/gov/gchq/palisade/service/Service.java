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

package uk.gov.gchq.palisade.service;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import uk.gov.gchq.palisade.exception.NoConfigException;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.concurrent.CompletableFuture;

/**
 * This class defines the top level services API.
 * <p>
 * The only requirement is that there is a process method, used to process all requests that are passed to a service.
 * <p>
 * The {@link Service#applyConfigFrom(ServiceState)} method should implemented so that the class can be configured with an
 * initial configuration.
 */
@JsonPropertyOrder(value = {"class"}, alphabetic = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = As.EXISTING_PROPERTY,
        property = "class"
)
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public interface Service {
    default CompletableFuture<?> process(final Request request) {
        throw new IllegalArgumentException("Request type was not recognised: " + request.getClass().getName());
    }

    /**
     * Gives the initial configuration to a service to allow it to configure itself. This method is called by the
     * internal system to configure a service.
     * <p>
     * Subclasses should override this implementation which does nothing.
     * <p>
     * Subclasses should ONLY throws {@link NoConfigException} if a required configuration key is not present
     *
     * @param config the configuration
     * @throws NoConfigException if some required element of the configuration is not present
     */
    default void applyConfigFrom(final ServiceState config) throws NoConfigException {
        //does nothing by default
    }

    /**
     * Tells a service to save its configuration to the given configuration object. This method is called by the
     * internal system to save a configuration.
     * <p>
     * Subclasses should override this implementation which does nothing.
     *
     * @param config the configuration to place initial configuration information into
     */
    default void recordCurrentConfigTo(final ServiceState config) {
        //does nothing by default
    }

    @JsonGetter("class")
    default String _getClass() {
        return getClass().getName();
    }

    @JsonSetter("class")
    default void _setClass(final String className) {
        // do nothing.
    }
}
