/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2024 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
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

package org.wildfly.testing.tools.xml;

import java.io.OutputStream;
import java.io.Writer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * A simple {@link XMLStreamWriter} which also implements {@link AutoCloseable}.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface CloseableXMLStreamWriter extends AutoCloseable, XMLStreamWriter {

    /**
     * Creates an {@link XMLStreamWriter} which writes to the output stream.
     *
     * @param out the output stream to write to
     *
     * @return a closable XML stream writer
     *
     * @throws XMLStreamException if an error occurs creating the writer
     */
    static CloseableXMLStreamWriter of(final OutputStream out) throws XMLStreamException {
        final XMLOutputFactory factory = XMLOutputFactory.newInstance();
        return new IndentingXmlWriter(factory.createXMLStreamWriter(out, "utf-8"));
    }

    /**
     * Creates an {@link XMLStreamWriter} which writes to the output stream.
     *
     * @param writer the writer to write to
     *
     * @return a closable XML stream writer
     *
     * @throws XMLStreamException if an error occurs creating the writer
     */
    static CloseableXMLStreamWriter of(final Writer writer) throws XMLStreamException {
        final XMLOutputFactory factory = XMLOutputFactory.newInstance();
        return new IndentingXmlWriter(factory.createXMLStreamWriter(writer));
    }
}
