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
import com.kereval.mlang.converter.ConverterException;
import com.kereval.mlang.converter.FromMLangConverter;
import com.kereval.mlang.converter.ParserException;
import com.kereval.mlang.parser.MLANGParser;
import com.kereval.mlang.parser.MLANGParserBaseVisitor;
import net.ihe.gazelle.model.*;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class MLangJSONConverter extends FromMLangConverter {
    private final String HTML_TAB = "&nbsp;&nbsp;&nbsp;";

    @Override
    public void convert(MLANGParser.TestFileContext testFileContext, OutputStreamWriter outputStreamWriter) throws ConverterException, ParserException {
        MLangJSONVisitor visitor = new MLangJSONVisitor();
        TestCaseDTO testCase = visitor.build(testFileContext);

        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(outputStreamWriter, testCase);
        } catch (IOException e) {
            throw new ConverterException(e.getMessage());
        }
    }

    private class MLangJSONVisitor extends MLANGParserBaseVisitor<Object> {
        private TestCaseDTO m_testCase;
        private int stepId = 1;

        private int getStepId() { return stepId++; }

        private MLANGParser.Transaction_stmtContext currentTransaction = null;


        public TestCaseDTO build(MLANGParser.TestFileContext testFile) {
            m_testCase = new TestCaseDTO();

            testFile.accept(this);

            return m_testCase;
        }

        @Override
        public Object visitTest(MLANGParser.TestContext ctx) {
            m_testCase.setName(ctx.ID().getText());

            return super.visitTest(ctx);
        }

        @Override
        public Object visitTest_comments(MLANGParser.Test_commentsContext ctx) {
            StringBuilder builder = new StringBuilder();
            if (!ctx.comments().isEmpty()) {
                String str = ctx.comments(0).STRING().getText();
                if (str.length() > 2) {
                    builder.append(str, 1, str.length() - 1);
                }
            }
            m_testCase.setDescription(builder.toString());
            return super.visitTest_comments(ctx);
        }

        @Override
        public Object visitRequire_file(MLANGParser.Require_fileContext ctx) {
            InputDTO fileInput = new InputDTO();
            fileInput.setKey(ctx.ID().getText());
            fileInput.setLabel(ctx.ID().getText());
            fileInput.setFormat("JSON");
            fileInput.setType("file");
            fileInput.setRequired(true);

            m_testCase.addInputs(fileInput);
            return super.visitRequire_file(ctx);
        }

        @Override
        public Object visitTransaction_stmt(MLANGParser.Transaction_stmtContext ctx) {
            InstructionStepDTO step = new InstructionStepDTO();
            step.setName("Transaction preparation");
            step.setId(Integer.toString(getStepId()));

            String description = "Transaction preparation " + boldify(ctx.ID().getText()) +
                    " from " + boldify(ctx.from().ID().getText()) +
                    " to " + boldify(ctx.to().ID().getText());
            step.setDescription(description);
            m_testCase.addStep(step);
            MLANGParser.Transaction_stmtContext save = this.currentTransaction;
            this.currentTransaction = ctx;
            Object ret = super.visitTransaction_stmt(ctx);
            this.currentTransaction = save;
            return ret;
        }

        @Override
        public Object visitSend_message(MLANGParser.Send_messageContext ctx) {
            InstructionStepDTO step = new InstructionStepDTO();
            step.setName("Send message");
            step.setId(Integer.toString(getStepId()));
            StringBuilder builder = new StringBuilder();

            builder.append("Sending message ").append(boldify(ctx.ID().getText()))
                    .append(" from ").append(boldify(this.currentTransaction.from().ID().getText()))
                    .append(" to ").append(boldify(this.currentTransaction.to().ID().getText()));
            if (ctx.send_message_param() != null) {
                builder.append(" with parameters :<br/>");
                for (int i = 0 ; i < ctx.send_message_param().value_name.size() ; i++) {
                    String name = ctx.send_message_param().value_name.get(i).getText();
                    String value = ctx.send_message_param().value.get(i).getText();
                    if (value.length() <= 2) {
                        builder.append(HTML_TAB)
                                .append(boldify("UNDEFINEDVALUE"))
                                .append(" as ")
                                .append(boldify(name));
                    }
                    else {
                        builder.append(HTML_TAB)
                                .append(boldify(value))
                                .append(" as ")
                                .append(boldify(name));
                    }

                    if (i + 1 < ctx.send_message_param().value_name.size()) {
                        builder.append("<br/>");
                    }
                }

                for (int i = 0 ; i < ctx.send_message_param().variable_name.size() ; i++) {
                    String name = ctx.send_message_param().variable_name.get(i).getText();
                    String value = ctx.send_message_param().variable.get(i).getText();
                    if (value.isEmpty()) {
                        builder.append(HTML_TAB)
                                .append(boldify("UNDEFINEDVALUE"))
                                .append(" as ")
                                .append(boldify(name));
                    }
                    else {
                        builder.append(HTML_TAB)
                                .append(boldify(value))
                                .append(" as ")
                                .append(boldify(name));
                    }

                    if (i + 1 < ctx.send_message_param().variable_name.size()) {
                        builder.append("<br/>");
                    }
                }
            }

            if (ctx.storein() != null) {
                builder.append("<br/>");
                builder.append("Message sent stored in ")
                        .append(boldify(ctx.storein().name.getText()));
            }

            step.setDescription(builder.toString());
            m_testCase.addStep(step);
            return super.visitSend_message(ctx);
        }

        @Override
        public Object visitReceive_message(MLANGParser.Receive_messageContext ctx) {
            InstructionStepDTO step = new InstructionStepDTO();
            step.setName("Waiting message");
            step.setId(Integer.toString(getStepId()));
            StringBuilder builder = new StringBuilder();

            builder.append("Waiting message ").append(boldify(ctx.ID().getText()))
                    .append(" from ").append(boldify(this.currentTransaction.from().ID().getText()));
            if (ctx.storein() != null) {
                builder.append(" and stored it in ").append(boldify(ctx.storein().name.getText()));
            }

            step.setDescription(builder.toString());
            m_testCase.addStep(step);
            return super.visitReceive_message(ctx);
        }

        @Override
        public Object visitAssertValid(MLANGParser.AssertValidContext ctx) {
            ValidationStepDTO step = new ValidationStepDTO();
            step.setName("Assertion valid");
            step.setId(Integer.toString(getStepId()));

            step.addProperty(buildPropertyDTO("validationService","string", ctx.serviceName.getText()));
            step.addProperty(buildPropertyDTO("validationProfile","string", deleteMLangStringQuote(ctx.profileName.getText())));

            StringBuilder description = new StringBuilder("This step will validate");
            for (var param : ctx.assertParams) {
                description.append(" ").append(boldify(param.ID().getText()));
                step.addProperty(buildPropertyDTO("contentToValidate","bytearray", "${" + param.ID().getText() + "}"));

            }
            description.append(" against the validator ")
                    .append(boldify(ctx.serviceName.getText()))
                    .append(" with the profile ")
                    .append(boldify(deleteMLangStringQuote(ctx.profileName.getText())));
            step.setDescription(description.toString());

            m_testCase.addStep(step);
            return super.visitAssertValid(ctx);
        }

        @Override
        public Object visitProcess_stmt(MLANGParser.Process_stmtContext ctx) {
            InstructionStepDTO step = new InstructionStepDTO();
            step.setName("Processing data");
            step.setId(Integer.toString(getStepId()));

            StringBuilder builder = new StringBuilder();
            builder.append("Processing data ")
                    .append(boldify(ctx.data.getText()))
                    .append(" with processing service ")
                    .append(boldify(ctx.processingService.getText()));


            if (ctx.send_message_param() != null) {
                builder.append(" with parameters :<br/>");
                for (int i = 0 ; i < ctx.send_message_param().value_name.size() ; i++) {
                    String name = ctx.send_message_param().value_name.get(i).getText();
                    String value = ctx.send_message_param().value.get(i).getText();
                    if (value.length() <= 2) {
                        builder.append(HTML_TAB)
                                .append(boldify("UNDEFINEDVALUE"))
                                .append(" as ")
                                .append(boldify(name));
                    }
                    else {
                        builder.append(HTML_TAB)
                                .append(boldify(value))
                                .append(" as ")
                                .append(name);
                    }

                    if (i + 1 < ctx.send_message_param().value_name.size()) {
                        builder.append("<br/>");
                    }
                }

                for (int i = 0 ; i < ctx.send_message_param().variable_name.size() ; i++) {
                    String name = ctx.send_message_param().variable_name.get(i).getText();
                    String value = ctx.send_message_param().variable.get(i).getText();
                    if (value.isEmpty()) {
                        builder.append(HTML_TAB)
                                .append(boldify("UNDEFINEDVALUE"))
                                .append(" as ")
                                .append(boldify(name));
                    }
                    else {
                        builder.append(HTML_TAB)
                                .append(boldify(value))
                                .append(" as ")
                                .append(boldify(name));
                    }

                    if (i + 1 < ctx.send_message_param().variable_name.size()) {
                        builder.append("<br/>");
                    }
                }
            }

            if (ctx.storein() != null) {
                builder.append("<br/>");
                builder.append("Result stored it in ").append(boldify(ctx.storein().name.getText()));
            }
            step.setDescription(builder.toString());
            m_testCase.addStep(step);
            return super.visitProcess_stmt(ctx);
        }

        private static PropertyDTO buildPropertyDTO(String name, String type, String value) {
            PropertyDTO property = new PropertyDTO();
            property.setName(name);
            property.setType(type);
            property.setValue(value);
            return property;
        }

        private static String deleteMLangStringQuote(String str) {
            if (str.startsWith("\"") && str.endsWith("\"")) {
                return str.substring(1, str.length() - 1);
            }
            else {
                return str;
            }
        }
    }

    private String boldify(String value) {
        return "<b>" + value + "</b>";
    }
}
