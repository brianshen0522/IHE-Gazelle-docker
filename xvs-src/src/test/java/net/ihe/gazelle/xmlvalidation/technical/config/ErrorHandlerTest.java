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

import static org.junit.jupiter.api.Assertions.*;

public class ErrorHandlerTest {

    @Test
    public void testAssertCollectableErrors(){
        ErrorHandler errorHandler = new ErrorHandler();
        errorHandler.addError(new Exception("Error Test"));
        assertTrue(errorHandler.hasErrors());
        assertEquals(1, errorHandler.getErrors().size());
        assertEquals("Error Test", errorHandler.getErrors().get(0));
    }

    @Test
    public void testNoErrors(){
        assertFalse(new ErrorHandler().hasErrors());
    }

    @Test
    public void constructErrorMessageWithNullArgumentTest(){
        assertEquals("", ErrorHandler.constructErrorMessage(null));
    }


    @Test
    public void constructErrorsMessageWithoutCauseTest(){
        Throwable e = new Throwable("Error Test");
        assertEquals("Error Test", ErrorHandler.constructErrorMessage(e));
    }

    @Test
    public void constructErrorsMessageWithCauseTest(){
        Throwable e = new Throwable("Error Test", new Throwable("Error Cause"));
        assertEquals("Error Test\nCaused by: Error Cause", ErrorHandler.constructErrorMessage(e));
    }

    @Test
    public void constructErrorsMessageWithPackagesTest(){
        Throwable e = new Throwable("net.ihe.gazelle.validation.ValidationServiceImpl: net.ihe.gazelle.schematron.XsdValidationError: Invalid schema");
        assertEquals("Invalid schema", ErrorHandler.constructErrorMessage(e));
    }

    @Test
    public void constructErrorsMessageWithFalsePackagesTest(){
        Throwable e = new Throwable("cda.xml.sch Path not found");
        assertEquals("cda.xml.sch Path not found", ErrorHandler.constructErrorMessage(e));
    }
}
