/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.arquillian.domain;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchiveEventHandler;
import org.jboss.shrinkwrap.api.ArchiveFormat;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Assignable;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.IllegalArchivePathException;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.NamedAsset;
import org.jboss.shrinkwrap.api.exporter.StreamExporter;
import org.jboss.shrinkwrap.api.formatter.Formatter;

/**
 * Allows an archive to be wrapped and return the server group names this archive should be associated with.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ServerGroupArchive<T extends Archive<T>> implements Archive<T> {
    private final Archive<T> delegate;
    private final Set<String> serverGroups;

    ServerGroupArchive(final Archive<T> delegate, final Set<String> serverGroups) {
        this.delegate = delegate;
        this.serverGroups = serverGroups;
    }

    /**
     * THe server groups this archive should be associated with.
     *
     * @return a set of server groups
     */
    public Set<String> getServerGroups() {
        return serverGroups;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public T add(final Asset asset, final ArchivePath target) throws IllegalArgumentException {
        return delegate.add(asset, target);
    }

    @Override
    public T add(final Asset asset, final ArchivePath target, final String name) throws IllegalArgumentException {
        return delegate.add(asset, target, name);
    }

    @Override
    public T add(final Asset asset, final String target, final String name) throws IllegalArgumentException {
        return delegate.add(asset, target, name);
    }

    @Override
    public T add(final NamedAsset namedAsset) throws IllegalArgumentException {
        return delegate.add(namedAsset);
    }

    @Override
    public T add(final Asset asset, final String target) throws IllegalArgumentException {
        return delegate.add(asset, target);
    }

    @Override
    public T addAsDirectory(final String path) throws IllegalArgumentException {
        return delegate.addAsDirectory(path);
    }

    @Override
    public T addAsDirectories(final String... paths) throws IllegalArgumentException {
        return delegate.addAsDirectories(paths);
    }

    @Override
    public T addAsDirectory(final ArchivePath path) throws IllegalArgumentException {
        return delegate.addAsDirectory(path);
    }

    @Override
    public T addAsDirectories(final ArchivePath... paths) throws IllegalArgumentException {
        return delegate.addAsDirectories(paths);
    }

    @Override
    public T addHandlers(final ArchiveEventHandler... handlers) {
        return delegate.addHandlers(handlers);
    }

    @Override
    public Node get(final ArchivePath path) throws IllegalArgumentException {
        return delegate.get(path);
    }

    @Override
    public Node get(final String path) throws IllegalArgumentException {
        return delegate.get(path);
    }

    @Override
    public <X extends Archive<X>> X getAsType(final Class<X> type, final String path) {
        return delegate.getAsType(type, path);
    }

    @Override
    public <X extends Archive<X>> X getAsType(final Class<X> type, final ArchivePath path) {
        return delegate.getAsType(type, path);
    }

    @Override
    public <X extends Archive<X>> Collection<X> getAsType(final Class<X> type, final Filter<ArchivePath> filter) {
        return delegate.getAsType(type, filter);
    }

    @Override
    public <X extends Archive<X>> X getAsType(final Class<X> type, final String path, final ArchiveFormat archiveFormat) {
        return delegate.getAsType(type, path, archiveFormat);
    }

    @Override
    public <X extends Archive<X>> X getAsType(final Class<X> type, final ArchivePath path, final ArchiveFormat archiveFormat) {
        return delegate.getAsType(type, path, archiveFormat);
    }

    @Override
    public <X extends Archive<X>> Collection<X> getAsType(final Class<X> type, final Filter<ArchivePath> filter, final ArchiveFormat archiveFormat) {
        return delegate.getAsType(type, filter, archiveFormat);
    }

    @Override
    public boolean contains(final ArchivePath path) throws IllegalArgumentException {
        return delegate.contains(path);
    }

    @Override
    public boolean contains(final String path) throws IllegalArgumentException {
        return delegate.contains(path);
    }

    @Override
    public Node delete(final ArchivePath path) throws IllegalArgumentException {
        return delegate.delete(path);
    }

    @Override
    public Node delete(final String archivePath) throws IllegalArgumentException {
        return delegate.delete(archivePath);
    }

    @Override
    public Map<ArchivePath, Node> getContent() {
        return delegate.getContent();
    }

    @Override
    public Map<ArchivePath, Node> getContent(final Filter<ArchivePath> filter) {
        return delegate.getContent(filter);
    }

    @Override
    public T add(final Archive<?> archive, final ArchivePath path, final Class<? extends StreamExporter> exporter) throws IllegalArgumentException {
        return delegate.add(archive, path, exporter);
    }

    @Override
    public T add(final Archive<?> archive, final String path, final Class<? extends StreamExporter> exporter) throws IllegalArgumentException {
        return delegate.add(archive, path, exporter);
    }

    @Override
    public T merge(final Archive<?> source) throws IllegalArgumentException {
        return delegate.merge(source);
    }

    @Override
    public T merge(final Archive<?> source, final Filter<ArchivePath> filter) throws IllegalArgumentException {
        return delegate.merge(source, filter);
    }

    @Override
    public T merge(final Archive<?> source, final ArchivePath path) throws IllegalArgumentException {
        return delegate.merge(source, path);
    }

    @Override
    public T merge(final Archive<?> source, final String path) throws IllegalArgumentException {
        return delegate.merge(source, path);
    }

    @Override
    public T merge(final Archive<?> source, final ArchivePath path, final Filter<ArchivePath> filter) throws IllegalArgumentException {
        return delegate.merge(source, path, filter);
    }

    @Override
    public T merge(final Archive<?> source, final String path, final Filter<ArchivePath> filter) throws IllegalArgumentException {
        return delegate.merge(source, path, filter);
    }

    @Override
    public T move(final ArchivePath source, final ArchivePath target) throws IllegalArgumentException, IllegalArchivePathException {
        return delegate.move(source, target);
    }

    @Override
    public T move(final String source, final String target) throws IllegalArgumentException, IllegalArchivePathException {
        return delegate.move(source, target);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public String toString(final boolean verbose) {
        return delegate.toString(verbose);
    }

    @Override
    public String toString(final Formatter formatter) throws IllegalArgumentException {
        return delegate.toString(formatter);
    }

    @Override
    public void writeTo(final OutputStream outputStream, final Formatter formatter) throws IllegalArgumentException {
        delegate.writeTo(outputStream, formatter);
    }

    @Override
    public Archive<T> shallowCopy() {
        return delegate.shallowCopy();
    }

    @Override
    public <TYPE extends Assignable> TYPE as(final Class<TYPE> clazz) {
        return delegate.as(clazz);
    }
}
