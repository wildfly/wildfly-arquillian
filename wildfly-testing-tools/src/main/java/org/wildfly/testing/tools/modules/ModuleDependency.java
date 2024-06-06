/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.tools.modules;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A module dependency used for the {@link ModuleBuilder}.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ModuleDependency implements Comparable<ModuleDependency> {

    private final String name;
    private final boolean export;
    private final boolean optional;
    private final Services services;
    private final Set<Filter> imports;
    private final Set<Filter> exports;

    public enum Services {
        NONE,
        IMPORT,
        EXPORT;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * A simple filter of the dependency to filter paths.
     */
    public interface Filter extends Comparable<Filter> {

        /**
         * Creates a new path filter.
         *
         * @param path    the path to filter
         * @param include whether the filter should be an include or exclude
         *
         * @return a new filter
         */
        static Filter of(final String path, final boolean include) {
            return new PathFilter(path, include);
        }

        /**
         * The relative path of the filter
         *
         * @return the relative path
         */
        String path();

        /**
         * Whether the filter should be an include or exclude.
         *
         * @return {@code true} for an include filter, {@code false} for an exclude filter
         */
        boolean include();
    }

    private ModuleDependency(final String name, final boolean export, final boolean optional, final Services services,
            final Set<Filter> imports, final Set<Filter> exports) {
        this.name = name;
        this.export = export;
        this.optional = optional;
        this.services = services;
        this.imports = imports;
        this.exports = exports;
    }

    /**
     * Creates a new dependency builder.
     *
     * @param name the name of the dependency
     *
     * @return the new dependency builder
     */
    public static Builder builder(final String name) {
        return new Builder(name);
    }

    /**
     * Returns the module dependency name.
     *
     * @return the dependency name
     */
    public String name() {
        return name;
    }

    /**
     * Indicates if the dependency should be exported.
     *
     * @return whether the dependency should be exported
     */
    public boolean isExport() {
        return export;
    }

    /**
     * Indicates if the dependency is optional.
     *
     * @return whether the dependency is optional
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * The import filters for the dependency.
     *
     * @return the import filters or an empty set
     */
    public Set<Filter> imports() {
        return imports;
    }

    /**
     * The export filters for the dependency.
     *
     * @return the export filters or an empty set
     */
    public Set<Filter> exports() {
        return exports;
    }

    /**
     * Returns the services value, if defined.
     *
     * @return the services value
     */
    public Optional<Services> services() {
        return Optional.ofNullable(services);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ModuleDependency)) {
            return false;
        }
        final ModuleDependency other = (ModuleDependency) obj;
        return Objects.equals(name, other.name);
    }

    @Override
    public String toString() {
        return "ModuleDependency[name=" + name + ", export=" + export + ", optional=" + optional + ", services=" + services
                + ", imports=" + imports + ", exports=" + exports + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(final ModuleDependency o) {
        return name.compareTo(o.name);
    }

    /**
     * Builds a module dependency.
     *
     * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
     */
    @SuppressWarnings("unused")
    public static class Builder {
        private final String name;
        private boolean optional;
        private boolean export;
        private Services services;
        private final Set<Filter> imports;
        private final Set<Filter> exports;

        private Builder(final String name) {
            this.name = name;
            imports = new LinkedHashSet<>();
            exports = new LinkedHashSet<>();
        }

        /**
         * Whether the dependency should be optional or not.
         *
         * @param optional {@code true} if the dependency should be optional
         *
         * @return this builder
         */
        public Builder optional(final boolean optional) {
            this.optional = optional;
            return this;
        }

        /**
         * Whether the dependency should be exported or not.
         *
         * @param export {@code true} if the dependency should be exported
         *
         * @return this builder
         */
        public Builder export(final boolean export) {
            this.export = export;
            return this;
        }

        /**
         * Define the value for the dependencies {@code services} attribute.
         *
         * @param services the value for the service attribute
         *
         * @return this builder
         */
        public Builder services(final Services services) {
            this.services = services;
            return this;
        }

        /**
         * Adds an import filter for the dependency.
         *
         * @param filter the import filter
         *
         * @return this builder
         */
        public Builder addImportFilter(final Filter filter) {
            imports.add(filter);
            return this;
        }

        /**
         * Adds an import filter for the dependency.
         *
         * @param path    the path to filter
         * @param include {@code true} if the path should be included, {@code false} if the path should be excluded
         *
         * @return this builder
         */
        public Builder addImportFilter(final String path, final boolean include) {
            return addImportFilter(Filter.of(path, include));
        }

        /**
         * Adds the filters as import filters for the dependency.
         *
         * @param filters the filters to add
         *
         * @return this builder
         */
        public Builder addImportFilters(final Filter... filters) {
            imports.addAll(Set.of(filters));
            return this;
        }

        /**
         * Adds the filters as import filters for the dependency.
         *
         * @param filters the filters to add
         *
         * @return this builder
         */
        public Builder addImportFilters(final Collection<Filter> filters) {
            imports.addAll(filters);
            return this;
        }

        /**
         * Adds an export filter for the dependency.
         *
         * @param filter the export filter
         *
         * @return this builder
         */
        public Builder addExportFilter(final Filter filter) {
            exports.add(filter);
            return this;
        }

        /**
         * Adds an export filter for the dependency.
         *
         * @param path    the path to filter
         * @param include {@code true} if the path should be included, {@code false} if the path should be excluded
         *
         * @return this builder
         */
        public Builder addExportFilter(final String path, final boolean include) {
            return addExportFilter(Filter.of(path, include));
        }

        /**
         * Adds the filters as export filters for the dependency.
         *
         * @param filters the filters to add
         *
         * @return this builder
         */
        public Builder addExportFilters(final Filter... filters) {
            return addExportFilters(Set.of(filters));
        }

        /**
         * Adds the filters as export filters for the dependency.
         *
         * @param filters the filters to add
         *
         * @return this builder
         */
        public Builder addExportFilters(final Collection<Filter> filters) {
            exports.addAll(filters);
            return this;
        }

        /**
         * Creates the module dependency.
         *
         * @return the module dependency
         */
        public ModuleDependency build() {
            return new ModuleDependency(name, export, optional, services, Set.copyOf(imports), Set.copyOf(exports));
        }
    }

    private static class PathFilter implements Filter {
        private final String path;
        private final boolean include;

        private PathFilter(final String path, final boolean include) {
            this.path = path;
            this.include = include;
        }

        @Override
        public String path() {
            return path;
        }

        @Override
        public boolean include() {
            return include;
        }

        @Override
        public int hashCode() {
            return Objects.hash(path, include);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PathFilter)) {
                return false;
            }
            final PathFilter other = (PathFilter) obj;
            return Objects.equals(path, other.path) && Objects.equals(include, other.include);
        }

        @Override
        public String toString() {
            return "Filter[path=" + path + ", include=" + include + "]";
        }

        @Override
        public int compareTo(final Filter o) {
            int result = path().compareTo(o.path());
            if (result == 0) {
                result = Boolean.compare(include, o.include());
            }
            return result;
        }
    }
}
