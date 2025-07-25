/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.protocol.jmx;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.container.test.spi.client.deployment.ProtocolArchiveProcessor;
import org.jboss.arquillian.protocol.jmx.AbstractJMXProtocol;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.Authentication;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.arquillian.container.NetworkUtils;
import org.jboss.as.arquillian.protocol.jmx.ExtendedJMXProtocol.ServiceArchiveHolder;
import org.jboss.as.arquillian.service.ArquillianService;
import org.jboss.as.arquillian.service.DependenciesProvider;
import org.jboss.as.arquillian.service.JMXProtocolEndpointExtension;
import org.jboss.logging.Logger;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.asset.UrlAsset;
import org.jboss.shrinkwrap.api.container.ManifestContainer;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.se.manifest.ManifestDescriptor;

/**
 * A {@link DeploymentPackager} for the JMXProtocol.
 *
 * It dynamically generates the arquillian-archive, which is deployed to the
 * target container before the test run.
 *
 * @see ArquillianServiceDeployer
 *
 * @author Thomas.Diesler@jboss.com
 * @since 01-Jul-2011
 */
public class JMXProtocolPackager implements DeploymentPackager {

    private static final List<String> defaultDependencies = new ArrayList<String>();

    private static final Set<String> optionalDeps = new HashSet<>();
    private static final Set<String> exportedDeps = Set.of("org.jboss.as.controller-client", "org.jboss.dmr");

    private static final Collection<String> services = new HashSet<>();

    static {
        defaultDependencies.add("deployment.arquillian-service");
        defaultDependencies.add("org.jboss.modules");
        defaultDependencies.add("org.jboss.msc");
        defaultDependencies.add("org.wildfly.security.manager");
        optionalDeps.add("java.logging");
        // JUnit 5 uses a Java service to obtain the engine implementation
        services.add("deployment.arquillian-service");
    }

    private static final Logger log = Logger.getLogger(JMXProtocolPackager.class);

    private ServiceArchiveHolder archiveHolder;

    JMXProtocolPackager(ServiceArchiveHolder archiveHolder) {
        this.archiveHolder = archiveHolder;
    }

    @Override
    public Archive<?> generateDeployment(TestDeployment testDeployment,
            Collection<ProtocolArchiveProcessor> protocolProcessors) {
        final Archive<?> appArchive = testDeployment.getApplicationArchive();
        if (archiveHolder.getArchive() == null) {
            try {
                Collection<Archive<?>> auxArchives = testDeployment.getAuxiliaryArchives();
                JavaArchive archive = generateArquillianServiceArchive(auxArchives);

                for (ProtocolArchiveProcessor processor : protocolProcessors) {
                    processor.process(testDeployment, archive);
                }

                archiveHolder.setArchive(archive);
            } catch (Exception ex) {
                throw new IllegalStateException("Cannot generate arquillian service", ex);
            }
        }
        addModulesManifestDependencies(appArchive);
        TestDescription.addTestDescription(testDeployment);
        archiveHolder.addPreparedDeployment(testDeployment.getDeploymentName());
        return appArchive;
    }

    private JavaArchive generateArquillianServiceArchive(Collection<Archive<?>> auxArchives) throws Exception {

        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "arquillian-service");
        log.debugf("Generating: %s", archive.getName());

        archive.addPackage(ArquillianService.class.getPackage());
        archive.addPackage(AbstractJMXProtocol.class.getPackage());
        // add the classes required for server setup
        archive.addClasses(ServerSetup.class, ServerSetupTask.class, ManagementClient.class, Authentication.class,
                NetworkUtils.class, TestDescription.class);

        final Set<ModuleIdentifier> archiveDependencies = new LinkedHashSet<ModuleIdentifier>();
        archiveDependencies.add(ModuleIdentifier.create("org.jboss.as.jmx"));
        archiveDependencies.add(ModuleIdentifier.create("org.jboss.as.server"));
        archiveDependencies.add(ModuleIdentifier.create("org.jboss.as.controller-client"));
        archiveDependencies.add(ModuleIdentifier.create("org.jboss.jandex"));
        archiveDependencies.add(ModuleIdentifier.create("org.jboss.logging"));
        archiveDependencies.add(ModuleIdentifier.create("org.jboss.modules"));
        archiveDependencies.add(ModuleIdentifier.create("org.jboss.dmr"));
        archiveDependencies.add(ModuleIdentifier.create("org.jboss.msc"));
        archiveDependencies.add(ModuleIdentifier.create("org.wildfly.security.manager"));
        archiveDependencies.add(ModuleIdentifier.create("org.wildfly.common"));
        archiveDependencies.add(ModuleIdentifier.create("org.wildfly.security.elytron"));
        archiveDependencies.add(ModuleIdentifier.create("java.logging"));

        // Merge the auxiliary archives and collect the loadable extensions
        final Set<String> loadableExtensions = new HashSet<String>();
        final String loadableExtensionsPath = "META-INF/services/" + RemoteLoadableExtension.class.getName();
        for (Archive<?> aux : auxArchives) {
            Node node = aux.get(loadableExtensionsPath);
            if (node != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(node.getAsset().openStream()));
                String line = br.readLine();
                while (line != null) {
                    loadableExtensions.add(line);
                    ClassLoader classLoader = getClass().getClassLoader();
                    Object extension = classLoader.loadClass(line).newInstance();
                    if (extension instanceof DependenciesProvider) {
                        DependenciesProvider provider = (DependenciesProvider) extension;
                        archiveDependencies.addAll(provider.getDependencies());
                    }
                    line = br.readLine();
                }
            }
            log.debugf("Merging archive: %s", aux);
            archive.merge(aux);
        }
        loadableExtensions.add(JMXProtocolEndpointExtension.class.getName());

        // Generate the manifest with it's dependencies
        ManifestDescriptor manifest = Descriptors.create(ManifestDescriptor.class);
        Iterator<ModuleIdentifier> itdep = archiveDependencies.iterator();
        StringBuilder depspec = new StringBuilder();
        while (itdep.hasNext()) {
            ModuleIdentifier dep = itdep.next();
            depspec.append(dep);
            if (optionalDeps.contains(dep.getName())) {
                depspec.append(" optional");
            }
            if (exportedDeps.contains(dep.getName())) {
                depspec.append(" export");
            }
            if (itdep.hasNext()) {
                depspec.append(",");
            }
        }
        manifest.attribute("Dependencies", depspec.toString());
        archive.setManifest(new StringAsset(manifest.exportAsString()));

        // Add the ServiceActivator
        String serviceActivatorPath = "META-INF/services/" + ServiceActivator.class.getName();
        final URL serviceActivatorURL = this.getClass().getClassLoader()
                .getResource("arquillian-service/" + serviceActivatorPath);
        if (serviceActivatorURL == null) {
            throw new RuntimeException("No arquillian-service/" + serviceActivatorPath + " found by classloader: "
                    + this.getClass().getClassLoader());
        }
        archive.addAsResource(new UrlAsset(serviceActivatorURL), serviceActivatorPath);

        // Replace the loadable extensions with the collected set
        archive.delete(ArchivePaths.create(loadableExtensionsPath));
        archive.addAsResource(new Asset() {
            @Override
            public InputStream openStream() {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(baos));
                for (String line : loadableExtensions) {
                    pw.println(line);
                }
                pw.close();
                return new ByteArrayInputStream(baos.toByteArray());
            }
        }, loadableExtensionsPath);

        archive.addAsManifestResource("META-INF/permissions.xml", "permissions.xml");

        log.debugf("Loadable extensions: %s", loadableExtensions);
        log.tracef("Archive content: %s\n%s", archive, archive.toString(true));
        return archive;
    }

    /**
     * Adds the Manifest Attribute "Dependencies" with the required dependencies for JBoss Modules to depend on the Arquillian
     * Service.
     *
     * @param appArchive The Archive to deploy
     */
    private void addModulesManifestDependencies(Archive<?> appArchive) {
        if (appArchive instanceof ManifestContainer<?> == false)
            throw new IllegalArgumentException("ManifestContainer expected " + appArchive);

        final Manifest manifest = ManifestUtils.getOrCreateManifest(appArchive);

        Attributes attributes = manifest.getMainAttributes();
        if (attributes.getValue(Attributes.Name.MANIFEST_VERSION.toString()) == null) {
            attributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
        }
        String value = attributes.getValue("Dependencies");
        StringBuffer moduleDeps = new StringBuffer(value != null && value.trim().length() > 0 ? value : "org.jboss.modules");
        for (String dep : defaultDependencies) {
            if (moduleDeps.indexOf(dep) < 0) {
                moduleDeps.append("," + dep);
            }
            if (optionalDeps.contains(dep)) {
                moduleDeps.append(" optional");
            }
            // Any dependencies that require the services, META-INF/services, to be exported
            if (services.contains(dep)) {
                moduleDeps.append(" services");
            }
        }

        log.debugf("Add dependencies: %s", moduleDeps);
        attributes.putValue("Dependencies", moduleDeps.toString());

        // Add the manifest to the archive
        ArchivePath manifestPath = ArchivePaths.create(JarFile.MANIFEST_NAME);
        appArchive.delete(manifestPath);
        appArchive.add(new Asset() {
            public InputStream openStream() {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    manifest.write(baos);
                    return new ByteArrayInputStream(baos.toByteArray());
                } catch (IOException ex) {
                    throw new IllegalStateException("Cannot write manifest", ex);
                }
            }
        }, manifestPath);
    }
}
