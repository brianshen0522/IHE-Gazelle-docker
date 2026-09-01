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

import io.quarkus.runtime.annotations.RegisterForReflection;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import net.ihe.gazelle.validation.v2.api.business.report.*;
import net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest;
import net.ihe.gazelle.validation.v2.api.technical.ws.ValidationInterfaceV2;

@RegisterForReflection(targets = {
        Service.class,
        ValidationInterfaceV2.class,
        ValidationProfile.class,
        HttpRestBinding.class,
        ValidationReport.class,
        ValidationSubReport.class,
        AssertionReport.class,
        ValidationMethod.class,
        Metadata.class,
        UnexpectedError.class,
        ValidationCounters.class,
        ValidationRequest.class,
        ValidationRequest.class,
})
public class ReflectionConfig {
}
