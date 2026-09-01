/*
 * Copyright 2026 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.itb.gateway.business;

import net.ihe.gazelle.itb.gateway.business.reporting.ItbReporting;
import net.ihe.gazelle.itb.gateway.business.reporting.ItbResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Enriches ITB reporting with parsed result, PDF, and logs.
 */
public class ItbReportingEnrichmentService {

   private static final Logger LOG = LoggerFactory.getLogger(ItbReportingEnrichmentService.class);
   private final ItbClient itbClient;

   /**
    * Creates the enrichment service.
    *
    * @param itbClient ITB client used to fetch additional artifacts
    */
   public ItbReportingEnrichmentService(ItbClient itbClient) {
      this.itbClient = itbClient;
   }

   /**
    * Enriches report with parsed status, PDF, and logs.
    *
    * @param itbReporting raw ITB reporting
    * @return enriched reporting
    */
   public ItbReporting enrich(ItbReporting itbReporting) {
      String sessionId = itbReporting.getTestSession().getTestSessionIdentifier();
      String testCaseIdentifier = itbReporting.getTestSession().getTestCaseIdentifier();
      itbReporting.setResult(parseResult(itbReporting.getTestReport()));
      itbReporting.setPdfReport(getPdfReport(sessionId, testCaseIdentifier));
      if (itbReporting.getLogs() == null) {
         itbReporting.setLogs(getLogs(sessionId));
      }
      return itbReporting;
   }

   private byte[] getPdfReport(final String sessionId, final String testCaseIdentifier) {
      try {
         return itbClient.requestPDFReport(sessionId, testCaseIdentifier);
      } catch (Exception e) {
         LOG.atWarn().setCause(e).log("Could not request ITB PDF for session {}", sessionId);
         return new byte[0];
      }
   }

   private String getLogs(final String sessionID) {
      try {
         return itbClient.getTestLogs(sessionID);
      } catch (Exception e) {
         LOG.atWarn().setCause(e).log("Could not retrieve ITB logs for session {}", sessionID);
         return null;
      }
   }

   ItbResult parseResult(String itbReport) {
      if (itbReport == null || itbReport.isBlank()) {
         return ItbResult.UNDEFINED;
      }
      try {
         DocumentBuilder builder = getDocumentBuilder();
         try (InputStream is = new ByteArrayInputStream(itbReport.getBytes(StandardCharsets.UTF_8))) {
            Document docReport = builder.parse(is);
            NodeList element = docReport.getDocumentElement().getChildNodes();
            return getResult(element);
         }
      } catch (ParserConfigurationException | IOException | SAXException e) {
         LOG.error("Unable to parse ITB report result", e);
         return ItbResult.UNDEFINED;
      }
   }

   private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      // Harden XML parsing against XXE and external entity expansion
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);
      // Forbids any external access during parsing
      optionalEntityEscape(factory);
      return factory.newDocumentBuilder();
   }

   private static void optionalEntityEscape(DocumentBuilderFactory factory) {
      try {
         factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
         factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      } catch (IllegalArgumentException ignored) {
         // Some XML parsers may not support these attributes; ignore if unsupported
      }
   }

   private static ItbResult getResult(NodeList element) {
      if (element == null || element.getLength() < 8 || element.item(7) == null) {
         throw new CatchItbReportServiceException("Status is missing in the ITB report.");
      }
      String status = element.item(7).getTextContent();
      if (status == null || status.isEmpty()) {
         throw new CatchItbReportServiceException("Status is null or empty in the ITB report.");
      }
      if (status.equalsIgnoreCase("SUCCESS")) {
         return ItbResult.SUCCESS;
      } else if (status.equalsIgnoreCase("FAILURE")) {
         return ItbResult.FAILURE;
      } else {
         return ItbResult.UNDEFINED;
      }
   }
}
