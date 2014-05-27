/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.logging.log4j.core.config.plugins.visitors;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.util.TypeConverters;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.core.util.Assert;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

/**
 * Base class for PluginVisitor implementations. Provides convenience methods as well as all method implementations
 * other than the {@code visit} method.
 *
 * @param <A> the Plugin annotation type.
 */
public abstract class AbstractPluginVisitor<A extends Annotation> implements PluginVisitor<A> {

    protected static final Logger LOGGER = StatusLogger.getLogger();

    protected final Class<A> clazz;
    protected A annotation;
    protected String[] aliases;
    protected Class<?> conversionType;
    protected StrSubstitutor substitutor;

    /**
     * This constructor must be overridden by implementation classes as a no-arg constructor.
     *
     * @param clazz the annotation class this PluginVisitor is for.
     */
    protected AbstractPluginVisitor(final Class<A> clazz) {
        this.clazz = clazz;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PluginVisitor<A> setAnnotation(final Annotation annotation) {
        final Annotation a = Assert.requireNonNull(annotation, "No annotation was provided");
        if (this.clazz.isInstance(annotation)) {
            this.annotation = (A) annotation;
        }
        return this;
    }

    @Override
    public PluginVisitor<A> setAliases(final String... aliases) {
        this.aliases = aliases;
        return this;
    }

    @Override
    public PluginVisitor<A> setConversionType(final Class<?> conversionType) {
        this.conversionType = Assert.requireNonNull(conversionType, "No conversion type class was provided");
        return this;
    }

    @Override
    public PluginVisitor<A> setStrSubstitutor(StrSubstitutor substitutor) {
        this.substitutor = Assert.requireNonNull(substitutor, "No StrSubstitutor was provided");
        return this;
    }

    /**
     * Removes an Entry from a given Map using a key name and aliases for that key. Keys are case-insensitive.
     *
     * @param attributes the Map to remove an Entry from.
     * @param name       the key name to look up.
     * @param aliases    optional aliases of the key name to look up.
     * @return the value corresponding to the given key or {@code null} if nonexistent.
     */
    protected static String removeAttributeValue(final Map<String, String> attributes,
                                                 final String name,
                                                 final String... aliases) {
        for (final Map.Entry<String, String> entry : attributes.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            if (key.equalsIgnoreCase(name)) {
                attributes.remove(key);
                return value;
            }
            if (aliases != null) {
                for (final String alias : aliases) {
                    if (key.equalsIgnoreCase(alias)) {
                        attributes.remove(key);
                        return value;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Converts the given value into the configured type falling back to the provided default value.
     *
     * @param value        the value to convert.
     * @param defaultValue the fallback value to use in case of no value or an error.
     * @return the converted value whether that be based on the given value or the default value.
     */
    protected Object convert(final String value, final Object defaultValue) {
        if (defaultValue instanceof String) {
            return TypeConverters.convert(value, this.conversionType, Strings.trimToNull((String) defaultValue));
        }
        return TypeConverters.convert(value, this.conversionType, defaultValue);
    }
}