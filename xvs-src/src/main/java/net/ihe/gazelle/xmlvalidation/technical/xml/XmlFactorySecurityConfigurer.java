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
package net.ihe.gazelle.xmlvalidation.technical.xml;

import org.slf4j.Logger;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;

public final class XmlFactorySecurityConfigurer {
   private static final String ALLOWED_EXTERNAL_PROTOCOLS = "all";

   private XmlFactorySecurityConfigurer() {
      // Utility class
   }

   public static SchemaFactory newSecureSchemaFactory(String schemaLanguage, Logger logger) {
      SchemaFactory factory = SchemaFactory.newInstance(schemaLanguage);
      secure(factory, logger);
      return factory;
   }

   public static TransformerFactory newSecureTransformerFactory(Logger logger) {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      secure(transformerFactory, logger);
      return transformerFactory;
   }

   public static void secure(SchemaFactory factory, Logger logger) {
      try {
         factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
         logger.debug("SchemaFactory does not support secure processing feature", e);
      }
      try {
         factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ALLOWED_EXTERNAL_PROTOCOLS);
      } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
         logger.debug("SchemaFactory does not support external DTD access property", e);
      }
      try {
         factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ALLOWED_EXTERNAL_PROTOCOLS);
      } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
         logger.debug("SchemaFactory does not support external schema access property", e);
      }
   }

   public static void secure(TransformerFactory transformerFactory, Logger logger) {
      try {
         transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      } catch (TransformerException e) {
         logger.debug("TransformerFactory does not support secure processing feature", e);
      }
      try {
         transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ALLOWED_EXTERNAL_PROTOCOLS);
      } catch (IllegalArgumentException e) {
         logger.debug("TransformerFactory does not support external DTD access attribute", e);
      }
      try {
         transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, ALLOWED_EXTERNAL_PROTOCOLS);
      } catch (IllegalArgumentException e) {
         logger.debug("TransformerFactory does not support external stylesheet access attribute", e);
      }
   }
}
