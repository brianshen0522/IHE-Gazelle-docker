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

import com.kereval.mlang.converter.ConverterException;
import com.kereval.mlang.converter.ParserException;
import net.ihe.gazelle.mlang.converter.JSONMLangConverter;
import net.ihe.gazelle.mlang.converter.MLangJSONConverter;

import java.io.*;

public class MLangConverterTest {

    public static void main(String[] args) throws IOException, ConverterException, ParserException {
        MLangJSONConverter converter = new MLangJSONConverter();
        InputStreamReader is = new InputStreamReader(new FileInputStream("mlang-converter/example.mlang"));
        OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream("mlang-converter/example.json"));
        converter.convert(is, ow);
        is.close();
        ow.close();

        is = new InputStreamReader(new FileInputStream("mlang-converter/example.json"));
        ow = new OutputStreamWriter(new FileOutputStream("mlang-converter/example_regenerated.mlang"));
        JSONMLangConverter reconverter = new JSONMLangConverter();
        reconverter.convert(is, ow);
        is.close();
        ow.close();
    }
}
