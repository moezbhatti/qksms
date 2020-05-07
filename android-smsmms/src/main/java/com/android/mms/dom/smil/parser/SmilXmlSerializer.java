/*
 * Copyright 2014 Jacob Klinker
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

package com.android.mms.dom.smil.parser;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.smil.SMILDocument;
import org.w3c.dom.smil.SMILElement;
import timber.log.Timber;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class SmilXmlSerializer {

    public static void serialize(SMILDocument smilDoc, OutputStream out) {
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"), 2048);

            writeElement(writer, smilDoc.getDocumentElement());
            writer.flush();
        } catch (UnsupportedEncodingException e) {
            Timber.e(e, "exception thrown");
        } catch (IOException e) {
            Timber.e(e, "exception thrown");
        }
    }

    private static void writeElement(Writer writer, Element element)
            throws IOException {
        writer.write('<');
        writer.write(element.getTagName());

        if (element.hasAttributes()) {
            NamedNodeMap attributes = element.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr)attributes.item(i);
                writer.write(" " + attribute.getName());
                writer.write("=\"" + attribute.getValue() + "\"");
            }
        }

        // FIXME: Might throw ClassCastException
        SMILElement childElement = (SMILElement) element.getFirstChild();

        if (childElement != null) {
            writer.write('>');

            do {
                writeElement(writer, childElement);
                childElement = (SMILElement) childElement.getNextSibling();
            } while (childElement != null);

            writer.write("</");
            writer.write(element.getTagName());
            writer.write('>');
        } else {
            writer.write("/>");
        }
    }
}

