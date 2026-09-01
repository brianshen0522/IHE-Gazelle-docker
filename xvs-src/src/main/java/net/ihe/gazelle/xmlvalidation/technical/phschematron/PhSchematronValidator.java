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
package net.ihe.gazelle.xmlvalidation.technical.phschematron;

import com.helger.commons.io.IHasInputStream;
import com.helger.commons.io.resource.FileSystemResource;
import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.io.resource.inmemory.ReadableResourceByteArray;
import com.helger.schematron.saxon.SchematronTransformerFactory;
import com.helger.schematron.sch.SchematronProviderXSLTFromSCH;
import com.helger.schematron.sch.TransformerCustomizerSCH;
import com.helger.schematron.svrl.SVRLMarshaller;
import com.helger.schematron.svrl.jaxb.SchematronOutputType;
import com.helger.xml.XMLFactory;
import com.helger.xml.sax.DefaultEntityResolver;
import com.helger.xml.serialize.read.DOMReader;
import com.helger.xml.serialize.read.DOMReaderSettings;
import com.helger.xml.transform.DefaultTransformURIResolver;
import com.helger.xml.transform.LoggingTransformErrorListener;
import com.helger.xml.transform.TransformSourceFactory;
import com.helger.xml.transform.XMLTransformerFactory;
import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.validation.v2.api.business.report.*;
import net.ihe.gazelle.xmlvalidation.business.config.ProfileConfiguration;
import net.ihe.gazelle.xmlvalidation.business.SchematronValidator;
import net.ihe.gazelle.xmlvalidation.technical.xml.XmlFactorySecurityConfigurer;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class PhSchematronValidator implements SchematronValidator {

   private static final Logger LOG = LoggerFactory.getLogger(PhSchematronValidator.class);

   // TODO Could be converted to a real cache (such as guava or cafeine) to improve the code of the method getXsltTransformer()
   private static final Map<String, Templates> TEMPLATES_MEM_CACHE = new ConcurrentHashMap<>();

   private final PhSchematronReportMapper reportMapper;
   private final ValidatorBuilderFactory validatorBuilderFactory;

   public PhSchematronValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      this.validatorBuilderFactory = validatorBuilderFactory;
      this.reportMapper = new PhSchematronTreeReportMapper(this.validatorBuilderFactory);
   }


   @Timed(name = "phSchematron#validate", description = "Time to perform ph-schematron validation", unit =
         MetricUnits.MILLISECONDS)
   @Override
   public ValidationSubReportBuilder validate(byte[] content, ProfileConfiguration profileConfiguration) {
      ValidationSubReportBuilder subReportBuilder = new ValidationSubReportBuilder(validatorBuilderFactory)
            .setName("Schematron Validation")
              .setStandards(profileConfiguration.getStandards());
      try {
         SchematronOutputType schematronResult = phValidate(profileConfiguration, content);
         reportMapper.aggregateReport(subReportBuilder, schematronResult);
         if(!subReportBuilder.hasContent()) {
            subReportBuilder.addAssertionReport(new AssertionReportBuilder()
                    .setDescription("Success: Your document has been validated without any notifications")
                    .setPriority(RequirementPriority.MANDATORY)
                    .setResult(ValidationTestResult.PASSED)
            );
         }
         return subReportBuilder;
      } catch (Exception e) {
         return subReportBuilder.setUnexpectedErrors(
               List.of(new UnexpectedErrorBuilder().fromException(
                     new SchematronValidationException("Error during Schematron validation: " + e.getMessage(), e)))
         );
      }
   }

   @Timed(name = "phValidate", description = "Time to setup the xslt transformer and apply it", unit =
         MetricUnits.MILLISECONDS)
   SchematronOutputType phValidate(ProfileConfiguration profileConfiguration, byte[] content)
         throws Exception {

      NodeAndBaseURI nodeContent = new NodeAndBaseURI(new ReadableResourceByteArray(content));
      DOMSource contentDOMSource = new DOMSource(nodeContent.getDocument());
      contentDOMSource.setSystemId(nodeContent.getUri());

      Document resultDocument = XMLFactory.newDocument();
      Transformer transformer = getXsltTransformer(profileConfiguration);

      transformer.transform(contentDOMSource, new DOMResult(resultDocument));

      return new SVRLMarshaller(false).read(resultDocument);
   }

   @Timed(name = "getXsltTransformer", description = "Time to get XSLT transformer", unit = MetricUnits.MILLISECONDS)
   static Transformer getXsltTransformer(ProfileConfiguration profileConfiguration) throws TransformerException {
      if (profileConfiguration.isCacheEnabled()) {
         if (!TEMPLATES_MEM_CACHE.containsKey(profileConfiguration.getId())) {
            LOG.debug("Create templates memory cache entry for profile {}", profileConfiguration.getId());
            TEMPLATES_MEM_CACHE.put(profileConfiguration.getId(), buildTemplates(getXslDocument(profileConfiguration)));
         } else {
            LOG.debug("Templates memory cache hit for profile {}", profileConfiguration.getId());
         }
         return TEMPLATES_MEM_CACHE.get(profileConfiguration.getId()).newTransformer();
      } else {
         LOG.debug("Cache disabled for profile {}, build templates and transformer.", profileConfiguration.getId());
         return buildTemplates(getXslDocument(profileConfiguration)).newTransformer();
      }
   }

   @Timed(name = "buildTemplates", description = "Time to build the transformation template from the XSLT", unit =
         MetricUnits.MILLISECONDS)
   static Templates buildTemplates(Document xslDocument) throws TransformerException {
      TransformerFactory transformerFactory = SchematronTransformerFactory.getDefaultSaxonFirst();
      createCustomizer().customize(transformerFactory);

      return Objects.requireNonNull(XMLTransformerFactory.newTemplates(
            transformerFactory,
            TransformSourceFactory.create(xslDocument)
      ));
   }

   @Timed(name = "getXslDocument", description = "Time to get the XSLT document, it may vary if the XSLT has already " +
         "been compiled or not and is available on the disk", unit = MetricUnits.MILLISECONDS)
   static Document getXslDocument(ProfileConfiguration profileConfiguration) throws TransformerException {
      Document xslDocument;
      File xslFile = new File(profileConfiguration.getXsltAbsolutePath());
      if (xslFile.exists()) {
         LOG.debug("Load XSLT from disk for profile {}", profileConfiguration.getId());
         xslDocument = loadCompiledXslt(xslFile);
      } else {
         LOG.debug("Compile schematron as XSLT for profile {}", profileConfiguration.getId());
         xslDocument = compileSchematronAsXslt(profileConfiguration);
         saveCompiledXslt(xslDocument, xslFile);
      }
      return xslDocument;
   }

   private static Document loadCompiledXslt(File xslFile) {
      return DOMReader.readXMLDOM(xslFile,
            NodeAndBaseURI.internalCreateDOMReaderSettings(new FileSystemResource(xslFile)));
   }

   @Timed(name = "compileSchematronAsXslt", description = "Time to compile a schematron as XSLT", unit =
         MetricUnits.MILLISECONDS)
   static Document compileSchematronAsXslt(ProfileConfiguration profileConfiguration)
         throws TransformerException {
      File schematronFile = new File(profileConfiguration.getSchematronAbsolutePath());
      String schematronRootDir = schematronFile.getParent();
      return SchematronProviderXSLTFromSCH.createSchematronXSLT(
            new FileSystemResource(schematronFile),
            createCustomizer(schematronRootDir)
      );
   }

   private static void saveCompiledXslt(Document document, File outputFile) throws TransformerException {
      TransformerFactory transformerFactory = XmlFactorySecurityConfigurer.newSecureTransformerFactory(LOG);
      Transformer transformer = transformerFactory.newTransformer();
      transformer.transform(new DOMSource(document), new StreamResult(outputFile));
   }

   private static TransformerCustomizerSCH createCustomizer() {
      return new TransformerCustomizerSCH()
            .setErrorListener(new LoggingTransformErrorListener(Locale.US))
            .setForceCacheResult(true);
   }

   private static TransformerCustomizerSCH createCustomizer(String baseUri) {
      return createCustomizer()
            .setURIResolver(new DefaultTransformURIResolver().setDefaultBase(baseUri));
   }

   private static final class NodeAndBaseURI {
      private final Document document;
      private final String uri;

      public NodeAndBaseURI(IHasInputStream iHasInputStream) {
         StreamSource source = TransformSourceFactory.create(iHasInputStream);
         InputStream inputStream = source.getInputStream();
         this.document = DOMReader.readXMLDOM(inputStream,
               internalCreateDOMReaderSettings((IReadableResource) iHasInputStream));
         this.uri = source.getSystemId();
      }

      public Document getDocument() {
         return document;
      }

      public String getUri() {
         return uri;
      }

      public static DOMReaderSettings internalCreateDOMReaderSettings(IReadableResource resource) {
         DOMReaderSettings settings = new DOMReaderSettings();
         settings.setEntityResolver(DefaultEntityResolver.createOnDemand(resource));
         return settings;
      }
   }


}
