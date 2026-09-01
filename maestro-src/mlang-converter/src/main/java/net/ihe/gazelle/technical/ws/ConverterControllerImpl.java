/*
 * Copyright 2025-2026 IHE International.
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

package net.ihe.gazelle.technical.ws;

import com.kereval.mlang.converter.Converter;
import com.kereval.mlang.converter.ConverterException;
import com.kereval.mlang.converter.ParserException;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.mlang.converter.JSONMLangConverter;
import net.ihe.gazelle.mlang.converter.MLangJSONConverter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;


public class ConverterControllerImpl implements ConverterController {

    Converter jsonConverter = new MLangJSONConverter();
    Converter mlangConverter = new JSONMLangConverter();

    @Override
    public Response convertToMlang(InputStream inputStream) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(output)) {

            mlangConverter.convert(new java.io.InputStreamReader(inputStream), writer);
            return Response.ok().entity(output.toString()).build();
        } catch (ConverterException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (ParserException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid MLANG format: " + e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred").build();
        }
    }

    @Override
    public Response convertToJson(InputStream inputStream) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(output)) {

            jsonConverter.convert(new java.io.InputStreamReader(inputStream), writer);
            return Response.ok().entity(output.toString()).build();
        } catch (ConverterException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (ParserException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid JSON format: " + e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred").build();
        }
    }

}
