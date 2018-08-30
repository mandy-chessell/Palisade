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
package uk.gov.gchq.palisade.cache.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.cache.service.CacheService;
import uk.gov.gchq.palisade.cache.service.request.AddCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.CacheRequest;
import uk.gov.gchq.palisade.cache.service.request.GetCacheRequest;
import uk.gov.gchq.palisade.cache.service.request.ListCacheRequest;
import uk.gov.gchq.palisade.service.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class BasicCacheService implements CacheService {

    Logger LOGGER = LoggerFactory.getLogger(BasicCacheService.class);

    private BackingStore store;

    public BasicCacheService() {
    }

    public BasicCacheService backingStore(final BackingStore store) {
        Objects.requireNonNull(store, "store");
        this.store = store;
        return this;
    }

    public void setBackingStore(final BackingStore store) {
        backingStore(store);
    }

    public BackingStore getBackingStore() {
        Objects.requireNonNull(store, "store must be initialised");
        return store;
    }

    @Override
    public <K, V> CompletableFuture<Boolean> add(final AddCacheRequest<K, V> request) {
        Objects.requireNonNull(request, "request");
        //make the final key name
        String baseKey = request.makeBaseName();
        LOGGER.debug("Got request to store item with key " + baseKey);

        V value = request.getValue();
        Class<? extends Object> valueClass = request.getValue().getClass();
        Optional<Duration> timeToLive = request.getTimeToLive();

        if (timeToLive.isPresent() && !getBackingStore().supportsTimeToLive()) {
            throw new UnsupportedOperationException("Cache backing store doesn't support time to live");
        }

        //encode value
        byte[] encodedValue = request.getValueEncoder().apply(value);

        //send to store
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("Requesting backing store to store " + baseKey);
            boolean result = getBackingStore().store(baseKey, valueClass, encodedValue, timeToLive);
            LOGGER.debug("Backing store has stored " + baseKey + " with result " + result);
            return result;
        });
    }

    @Override
    public <K,V> CompletableFuture<Optional<V>> get(final GetCacheRequest<K,V> request) {
        Objects.requireNonNull(request, "request");
        //make final key name
        String baseKey = request.makeBaseName();
        LOGGER.debug("Got request to retrieve item " + baseKey);

        //get from store
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("Requesting backing store to retrieve " + baseKey);
            BasicCacheObject<byte[], V> result = getBackingStore().retrieve(baseKey);
            if (result.getValue().isPresent()) {
                LOGGER.debug("Backing store successfully retrieved " + baseKey);
            } else {
                LOGGER.debug("Backing store failed to retrieve " + baseKey);
            }

            //assign so Javac can infer the generic type
            BiFunction<byte[], Class<V>, V> decode = request.getValueDecoder();

            return result.getValue().map(x -> decode.apply(x, result.getValueClass()));
        });
    }

    @Override
    public <K> CompletableFuture<Collection<String>> list(final ListCacheRequest<K> request) {
        Objects.requireNonNull(request, "request");

        //make final key name
        String baseKey = request.makeBaseName();
        LOGGER.debug("Got request to list items with prefix  " + baseKey);

        //retrieve from store
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("Sending list request to store " + baseKey);
            Collection<String> ret = getBackingStore().list(baseKey);
            LOGGER.debug("Store list returned for " + baseKey);
            return ret;
        });
    }
}
