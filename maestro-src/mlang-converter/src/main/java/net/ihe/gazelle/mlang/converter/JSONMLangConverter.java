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

package net.ihe.gazelle.mlang.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kereval.mlang.api.model.*;
import com.kereval.mlang.converter.ConverterException;
import com.kereval.mlang.converter.ParserException;
import com.kereval.mlang.converter.ToMLangConverter;
import net.ihe.gazelle.model.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONMLangConverter extends ToMLangConverter {
    private Stack<Transaction> transactions = new Stack<>();

    @Override
    public MTest convert(InputStreamReader inputStreamReader) throws ConverterException, ParserException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            TestCaseDTO testCase = mapper.readValue(inputStreamReader, TestCaseDTO.class);
            return buildTest(testCase);
        } catch (IOException e) {
            throw new ConverterException(e.getMessage());
        }
    }


    private MTest buildTest(TestCaseDTO testDTO) throws ConverterException {
        MTest test = new MTest(testDTO.getName());
        test.setDescription(testDTO.getDescription());
        for (var inputDTO : testDTO.getInputs()) {
            convertInputDTO(test, inputDTO);
        }

        for (var stepDTO : testDTO.getSteps()) {
            convertStepDTO(test, stepDTO);
        }

        return test;
    }

    private void convertInputDTO(MTest test, InputDTO inputDTO) throws ConverterException {
        switch (inputDTO.getType()) {
            case "file":
                test.addRequire(buildRequireFile(inputDTO));
                break;
            default:
                throw new ConverterException("Input type " + inputDTO.getType() + " not handle by the converter");
        }
    }

    private void convertStepDTO(MTest test, StepDTO stepDTO) throws ConverterException {
        if (stepDTO instanceof ValidationStepDTO) {
            if (transactions.empty())
                test.addStep(buildAssertValid((ValidationStepDTO) stepDTO));
            else
                transactions.peek().addAction(new MStepTAction((buildAssertValid((ValidationStepDTO) stepDTO))));
        }
        else if (stepDTO instanceof InstructionStepDTO) {
            convertInstruction(test, (InstructionStepDTO) stepDTO);
        }
        else {
            throw new ConverterException("Kind of Step DTO not handle by the converter detected");
        }
    }


    private RequireFile buildRequireFile(InputDTO input) {
        return new RequireFile(input.getKey());
    }

    private AssertMStep buildAssertValid(ValidationStepDTO validationStepDTO) throws ConverterException {
        Optional<PropertyDTO> validationServiceProp = validationStepDTO.getProperties().stream().
                filter(p -> p.getName().equals("validationService")).findFirst();
        if (validationServiceProp.isEmpty())
            throw new ConverterException("No validation service property detected into validation step");
        Optional<PropertyDTO> profileNameProp = validationStepDTO.getProperties().stream().
                filter(p -> p.getName().equals("validationProfile")).findFirst();
        if (profileNameProp.isEmpty())
            throw new ConverterException("No profile name property detected into validation step");
        Optional<PropertyDTO> contentToValidateProp = validationStepDTO.getProperties().stream().
                filter(p -> p.getName().equals("contentToValidate")).findFirst();
        if (contentToValidateProp.isEmpty())
            throw new ConverterException("No content to validate property detected into validation step");

        String contentToValidate = contentToValidateProp.get().getValue();
        if (!contentToValidate.startsWith("${") && !contentToValidate.endsWith("}"))
            throw new ConverterException("Content to validate property of validation step malformed");
        String value = contentToValidate.substring(2, contentToValidate.length() - 1);
        List<Param> params = List.of(new VariableParam(value));

        return new AssertMStep(AssertMStep.Type.VALID,
                params,
                validationServiceProp.get().getValue(),
                profileNameProp.get().getValue()
        );
    }

    private void convertInstruction(MTest test, InstructionStepDTO instructionStepDTO) throws ConverterException {
        if (instructionStepDTO.getName().startsWith("Transaction preparation")) {
            transactions.push(buildTransaction(instructionStepDTO));
            test.addStep(transactions.peek());
        }
        else if (instructionStepDTO.getName().startsWith("Send message")) {
            transactions.peek().addAction(buildSendMessageTAction(instructionStepDTO));
        }
        else if (instructionStepDTO.getName().startsWith("Waiting message")) {
            transactions.peek().addAction(buildReceiveMessageTAction(instructionStepDTO));
        }
        else if (instructionStepDTO.getName().startsWith("Processing data")) {
            if (transactions.empty())
                test.addStep(buildProcessMStep(instructionStepDTO));
            else
                transactions.peek().addAction(new MStepTAction(buildProcessMStep(instructionStepDTO)));
        }
        else {
            throw new ConverterException("Kind of instruction not handle by the converter");
        }
    }

    private Transaction buildTransaction(InstructionStepDTO instructionStepDTO) throws ConverterException {
        Pattern pattern = Pattern.compile("Transaction preparation (\\w+) from (\\w+) to (\\w+)");
        Matcher matcher = pattern.matcher(removeHtmlTags(instructionStepDTO.getDescription()));

        if (matcher.find()) {
            String name = matcher.group(1);
            String from = matcher.group(2);
            String to = matcher.group(3);

            return new Transaction(name, from, to);
        }
        else {
            throw new ConverterException("Instruction which represent a Transaction is malformed");
        }
    }

    private SendMessageTAction buildSendMessageTAction(InstructionStepDTO instructionStepDTO) throws ConverterException {
        Pattern pattern = Pattern.compile(
                "Sending message (\\w+) from (\\w+) to (\\w+)" +
                        "(?: with parameters :\\s*((?:\\S+\\s+as\\s+\\S+\\s*)+))?" +
                        "(?:Message sent stored in (\\w+))?",
                Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(removeHtmlTags(instructionStepDTO.getDescription()));
        if (matcher.find()) {
            String name = matcher.group(1);
            String from = matcher.group(2);
            String to = matcher.group(3);
            String paramsBlock = matcher.group(4);
            String storein = matcher.group(5);

            Map<String, Param> params = new HashMap<>();
            if (paramsBlock != null) {
                Pattern paramPattern = Pattern.compile("(\\S+)\\s+as\\s+(\\S+)");
                Matcher paramMatcher = paramPattern.matcher(paramsBlock);
                while (paramMatcher.find()) {
                    params.put(paramMatcher.group(2), new VariableParam(paramMatcher.group(1)));
                }
            }

            SendMessageTAction send = new SendMessageTAction(name, params);
            if (storein != null) {
                send.setStorein(storein);
            }

            return send;
        }
        else {
            throw new ConverterException("Instruction which represent a Transaction is malformed");
        }
    }

    private ReceiveMessageTAction buildReceiveMessageTAction(InstructionStepDTO instructionStepDTO) throws ConverterException {
        Pattern pattern = Pattern.compile(
                "Waiting message (\\w+) from (\\w+)" +
                        "(?: and stored it in (\\w+))?"
                , Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(removeHtmlTags(instructionStepDTO.getDescription()));
        if (matcher.find()) {
            String name = matcher.group(1);
            String from = matcher.group(2);
            String storein = matcher.group(3);

            Map<String, Param> params = new HashMap<>();
            ReceiveMessageTAction receive = new ReceiveMessageTAction(name, params);
            if (storein != null) {
                receive.setStorein(storein);
            }

            return receive;
        } else {
            throw new ConverterException("Instruction which represent a Transaction is malformed");
        }
    }

    private ProcessMStep buildProcessMStep(InstructionStepDTO instructionStepDTO) throws ConverterException {
        Pattern pattern = Pattern.compile(
                "Processing data (\\w+) with processing service (\\w+)" +
                        "(?: with parameters :\\s*((?:\\S+\\s+as\\s+\\S+\\s*)+))?" +
                        "(?:Result stored it in (\\w+))?"
                , Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(removeHtmlTags(instructionStepDTO.getDescription()));
        if (matcher.find()) {
            String data = matcher.group(1);
            String with = matcher.group(2);
            String paramsBlock = matcher.group(3);
            String storein = matcher.group(4);

            Map<String, Param> params = new HashMap<>();
            if (paramsBlock != null) {
                Pattern paramPattern = Pattern.compile("(\\S+)\\s+as\\s+(\\S+)");
                Matcher paramMatcher = paramPattern.matcher(paramsBlock);
                while (paramMatcher.find()) {
                    params.put(paramMatcher.group(2), new VariableParam(paramMatcher.group(1)));
                }
            }

            ProcessMStep step = new ProcessMStep(data, with, params);
            if (storein != null) {
                step.setStorein(storein);
            }
            return step;
        } else {
            throw new ConverterException("Instruction which represent a Transaction is malformed");
        }
    }

    private static String removeHtmlTags(String input) {
        if (input == null) return null;
        return input.replaceAll("<br/>", " ").replaceAll("<[^>]*>", "").replaceAll("&[^;]+;", " ");
    }
 }
