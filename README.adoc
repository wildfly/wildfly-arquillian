= WildFly Arquillian Adapter
:toc:

The WildFly Arquillian Adapter can be used to test your application with
https://wildfly.org[WildFly Application Server] or
https://www.redhat.com/en/technologies/jboss-middleware/application-platform[JBoss EAP]. It works with both managed
and unmanaged standalone and domain servers. As of 3.0.0 there is also a bootable JAR adapter as well.

Versions under 3.0.0 should work with any version of WildFly and JBoss EAP 7.0 and higher. Version 3.0.0 requires a
minimum of WildFly 13 or JBoss EAP 7.2.

Found a bug or want a new feature? Please file a bug on the https://issues.redhat.com/browse/WFARQ[issue tracker].


== Building

The current minimum is Java 8 and Maven 3.6.0. To build execute the following command.

----
mvn clean install
----


== Usage

=== Standalone

The WildFly Arquillian Adapter can manage the container process with the following dependency.

----
<dependency>
  <groupId>org.wildfly.arquillian</groupId>
  <artifactId>wildfly-arquillian-container-managed</artifactId>
  <version>${version.org.wildfly.arquillian}</version>
  <scope>test</scope>
</dependency>
----

If you'd to manage the container process via a different method use the following dependency.

----
<dependency>
  <groupId>org.wildfly.arquillian</groupId>
  <artifactId>wildfly-arquillian-container-remote</artifactId>
  <version>${version.org.wildfly.arquillian}</version>
  <scope>test</scope>
</dependency>
----

=== Domain

The WildFly Arquillian Adapter can manage the container process with the following dependency.

----
<dependency>
  <groupId>org.wildfly.arquillian</groupId>
  <artifactId>wildfly-arquillian-container-domain-managed</artifactId>
  <version>${version.org.wildfly.arquillian}</version>
  <scope>test</scope>
</dependency>
----

If you'd to manage the container process via a different method use the following dependency.

----
<dependency>
  <groupId>org.wildfly.arquillian</groupId>
  <artifactId>wildfly-arquillian-container-domain-remote</artifactId>
  <version>${version.org.wildfly.arquillian}</version>
  <scope>test</scope>
</dependency>
----

=== Bootable JAR

Since version 3.0.0 of the adapter you can now use Arquillian to test your bootable JAR's.

----
<dependency>
  <groupId>org.wildfly.arquillian</groupId>
  <artifactId>wildfly-arquillian-container-bootable</artifactId>
  <version>${version.org.wildfly.arquillian}</version>
  <scope>test</scope>
</dependency>
----