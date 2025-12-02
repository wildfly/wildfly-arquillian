/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.tools.modules;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A module dependency used for the {@link ModuleBuilder}.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @deprecated use the new WildFly Testing Tools project
 */
@Deprecated(forRemoval = true, since = "6.0")
public class ModuleDependency implements Comparable<ModuleDependency> {

    private final org.wildfly.testing.tools.module.ModuleDependency delegate;

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
            return new NewToOldFilter(path, include);
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

    protected ModuleDependency(final org.wildfly.testing.tools.module.ModuleDependency delegate) {
        this.delegate = delegate;
    }

    /**
     * Creates a new dependency builder.
     *
     * @param name the name of the dependency
     *
     * @return the new dependency builder
     */
    public static Builder builder(final String name) {
        return new Builder(org.wildfly.testing.tools.module.ModuleDependency.builder(name));
    }

    /**
     * Returns the module dependency name.
     *
     * @return the dependency name
     */
    public String name() {
        return delegate.name();
    }

    /**
     * Indicates if the dependency should be exported.
     *
     * @return whether the dependency should be exported
     */
    public boolean isExport() {
        return delegate.isExport();
    }

    /**
     * Indicates if the dependency is optional.
     *
     * @return whether the dependency is optional
     */
    public boolean isOptional() {
        return delegate.isOptional();
    }

    /**
     * The import filters for the dependency.
     *
     * @return the import filters or an empty set
     */
    public Set<Filter> imports() {
        return NewToOldFilter.map(delegate.imports());
    }

    /**
     * The export filters for the dependency.
     *
     * @return the export filters or an empty set
     */
    public Set<Filter> exports() {
        return NewToOldFilter.map(delegate.exports());
    }

    /**
     * Returns the services value, if defined.
     *
     * @return the services value
     */
    public Optional<Services> services() {
        final var found = delegate.services();
        return found.flatMap(services -> switch (services) {
            case IMPORT -> Optional.of(Services.IMPORT);
            case EXPORT -> Optional.of(Services.EXPORT);
            default -> Optional.of(Services.NONE);
        });
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof final ModuleDependency other)) {
            return false;
        }
        return Objects.equals(delegate, other.delegate);
    }

    @Override
    public String toString() {
        return "ModuleDependency[name=" + name() + ", export=" + isExport() + ", optional=" + isOptional() + ", services="
                + services()
                + ", imports=" + imports() + ", exports=" + exports() + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate);
    }

    @Override
    public int compareTo(final ModuleDependency o) {
        return delegate.compareTo(o.delegate);
    }

    org.wildfly.testing.tools.module.ModuleDependency delegate() {
        return delegate;
    }

    static Collection<org.wildfly.testing.tools.module.ModuleDependency> map(
            final Collection<ModuleDependency> moduleDependencies) {
        return moduleDependencies.stream().map(ModuleDependency::delegate).collect(Collectors.toList());
    }

    /**
     * Builds a module dependency.
     *
     * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
     */
    @SuppressWarnings("unused")
    public static class Builder {
        private final org.wildfly.testing.tools.module.ModuleDependency.Builder delegate;

        private Builder(final org.wildfly.testing.tools.module.ModuleDependency.Builder delegate) {
            this.delegate = delegate;
        }

        /**
         * Whether the dependency should be optional or not.
         *
         * @param optional {@code true} if the dependency should be optional
         *
         * @return this builder
         */
        public Builder optional(final boolean optional) {
            delegate.optional(optional);
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
            delegate.export(export);
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
            final org.wildfly.testing.tools.module.ModuleDependency.Services s = switch (services) {
                case IMPORT -> org.wildfly.testing.tools.module.ModuleDependency.Services.IMPORT;
                case EXPORT -> org.wildfly.testing.tools.module.ModuleDependency.Services.EXPORT;
                default -> org.wildfly.testing.tools.module.ModuleDependency.Services.NONE;
            };
            delegate.services(s);
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
            delegate.addImportFilter(new OldToNewFilter(filter));
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
            delegate.addImportFilters(OldToNewFilter.map(List.of(filters)));
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
            delegate.addImportFilters(OldToNewFilter.map(filters));
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
            delegate.addExportFilter(new OldToNewFilter(filter));
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
            delegate.addExportFilters(OldToNewFilter.map(filters));
            return this;
        }

        /**
         * Creates the module dependency.
         *
         * @return the module dependency
         */
        public ModuleDependency build() {
            return new ModuleDependency(delegate.build());
        }
    }

    private record NewToOldFilter(org.wildfly.testing.tools.module.ModuleDependency.Filter delegate) implements Filter {
        private NewToOldFilter(final String path, final boolean include) {
            this(org.wildfly.testing.tools.module.ModuleDependency.Filter.of(path, include));
        }

        static Set<Filter> map(final Collection<org.wildfly.testing.tools.module.ModuleDependency.Filter> filters) {
            return filters.stream().map(NewToOldFilter::new).collect(Collectors.toCollection(LinkedHashSet::new));
        }

        @Override
        public String path() {
            return delegate.path();
        }

        @Override
        public boolean include() {
            return delegate.include();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof final NewToOldFilter other)) {
                return false;
            }
            return Objects.equals(delegate, other.delegate);
        }

        @Override
        public String toString() {
            return "Filter[path=" + delegate.path() + ", include=" + delegate.include() + "]";
        }

        @Override
        public int compareTo(final Filter o) {
            int result = path().compareTo(o.path());
            if (result == 0) {
                result = Boolean.compare(include(), o.include());
            }
            return result;
        }
    }

    private record OldToNewFilter(Filter delegate) implements org.wildfly.testing.tools.module.ModuleDependency.Filter {

        static Set<org.wildfly.testing.tools.module.ModuleDependency.Filter> map(final Collection<Filter> filters) {
            return filters.stream().map(OldToNewFilter::new).collect(Collectors.toCollection(LinkedHashSet::new));
        }

        @Override
        public String path() {
            return delegate.path();
        }

        @Override
        public boolean include() {
            return delegate.include();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof final OldToNewFilter other)) {
                return false;
            }
            return Objects.equals(delegate, other.delegate);
        }

        @Override
        public String toString() {
            return "Filter[path=" + delegate.path() + ", include=" + delegate.include() + "]";
        }

        @Override
        public int compareTo(final org.wildfly.testing.tools.module.ModuleDependency.Filter o) {
            int result = path().compareTo(o.path());
            if (result == 0) {
                result = Boolean.compare(include(), o.include());
            }
            return result;
        }
    }
}
