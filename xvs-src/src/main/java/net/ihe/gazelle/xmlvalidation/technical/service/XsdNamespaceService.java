/*
Copyright 2010-2025 IHE International

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package net.ihe.gazelle.xmlvalidation.technical.service;

import jakarta.enterprise.context.ApplicationScoped;
import net.ihe.gazelle.xmlvalidation.business.config.ProfileConfiguration;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extracts and caches XSD targetNamespace values, invalidating the cache when profile configurations are reloaded.
 */
@ApplicationScoped
public class XsdNamespaceService {

   static final String UKNOWN_NAMESPACE = "UNKNOWN";

   private final Map<String, String> namespaceCache = new ConcurrentHashMap<>();

   /**
    * Returns the targetNamespace of the profile's XSD, caching the parsed value. If profiles are reloaded (the
    * configuration list instance changes), the cache is cleared automatically. Returns null when the XSD cannot be
    * parsed or has no targetNamespace attribute.
    */
   public String getTargetNamespace(ProfileConfiguration profileConfiguration) {
      String xsdPath = profileConfiguration.getXsdAbsolutePath();
      if (xsdPath == null || xsdPath.isBlank()) {
         return UKNOWN_NAMESPACE;
      }
      return namespaceCache.computeIfAbsent(xsdPath, this::parseTargetNamespace);
   }

   private String parseTargetNamespace(String xsdPath) {
      File xsdFile = new File(xsdPath);
      if (!xsdFile.exists()) {
         return UKNOWN_NAMESPACE;
      }
      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
         factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
         factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document document = builder.parse(xsdFile);
         if (document.getDocumentElement() == null) {
            return UKNOWN_NAMESPACE;
         }
         String targetNamespace = document.getDocumentElement().getAttribute("targetNamespace");
         return !targetNamespace.isBlank() ? targetNamespace : UKNOWN_NAMESPACE;
      } catch (ParserConfigurationException | SAXException | IOException e) {
         return UKNOWN_NAMESPACE;
      }
   }
}
