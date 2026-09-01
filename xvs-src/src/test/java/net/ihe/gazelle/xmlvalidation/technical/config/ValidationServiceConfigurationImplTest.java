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
package net.ihe.gazelle.xmlvalidation.technical.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidationServiceConfigurationImplTest {

   @Test
   void exposesConfiguredValues() throws Exception {
      ValidationServiceConfigurationImpl impl = new ValidationServiceConfigurationImpl();
      setField(impl);

      assertEquals("7.0.0", impl.getSchematronEngineVersion());
      assertEquals("ph-schematron", impl.getSchematronEngineName());
   }

   private static void setField(Object target) throws Exception {
      Field field = target.getClass().getDeclaredField("schematronEngineVersion");
      field.setAccessible(true);
      field.set(target, (Object) "7.0.0");
   }
}
