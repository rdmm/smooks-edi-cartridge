/*-
 * ========================LICENSE_START=================================
 * smooks-edi-cartridge
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.cartridges.edi;

import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.ValidationMode;
import org.smooks.cartridges.dfdl.DataProcessorFactory;
import org.smooks.cartridges.dfdl.DfdlSchema;
import org.smooks.cdr.Parameter;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.annotation.AppContext;
import org.smooks.container.ApplicationContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EdiDataProcessorFactory extends DataProcessorFactory {

    @AppContext
    protected ApplicationContext applicationContext;

    @Override
    public DataProcessor createDataProcessor() {
        try {
            final Map<String, String> variables = new HashMap<>();
            variables.put("{http://www.ibm.com/dfdl/EDI/Format}SegmentTerm", smooksResourceConfiguration.getStringParameter("segmentTerminator", "'%NL;%WSP*; '%WSP*;"));
            variables.put("{http://www.ibm.com/dfdl/EDI/Format}FieldSep", smooksResourceConfiguration.getStringParameter("dataElementSeparator", "+"));
            variables.put("{http://www.ibm.com/dfdl/EDI/Format}CompositeSep", smooksResourceConfiguration.getStringParameter("compositeDataElementSeparator", ":"));
            variables.put("{http://www.ibm.com/dfdl/EDI/Format}EscapeChar", smooksResourceConfiguration.getStringParameter("escapeCharacter", "?"));
            variables.put("{http://www.ibm.com/dfdl/EDI/Format}RepeatSep", smooksResourceConfiguration.getStringParameter("repetitionSeparator", "*"));
            variables.put("{http://www.ibm.com/dfdl/EDI/Format}DecimalSep", smooksResourceConfiguration.getStringParameter("decimalSign", "."));
            variables.put("{http://www.ibm.com/dfdl/EDI/Format}GroupingSep", smooksResourceConfiguration.getStringParameter("triadSeparator", ","));

            final List<Parameter> variableParameters = smooksResourceConfiguration.getParameters("variables");
            if (variableParameters != null) {
                for (Parameter variableParameter : variableParameters) {
                    final Map.Entry<String, String> variable = (Map.Entry<String, String>) variableParameter.getObjValue();
                    variables.put(variable.getKey(), variable.getValue());
                }
            }

            return doCreateDataProcessor(variables);
        } catch (Throwable t) {
            throw new SmooksConfigurationException(t);
        }
    }

    protected DataProcessor doCreateDataProcessor(final Map<String, String> variables) throws URISyntaxException {
        final DfdlSchema dfdlSchema = new DfdlSchema(new URI(schemaUri), variables, ValidationMode.valueOf(smooksResourceConfiguration.getStringParameter("validationMode", "Off")), smooksResourceConfiguration.getBoolParameter("cacheOnDisk", false), smooksResourceConfiguration.getBoolParameter("debugging", false));
        return compileOrGet(dfdlSchema);
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
