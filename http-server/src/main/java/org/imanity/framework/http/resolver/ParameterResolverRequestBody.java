/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.http.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.imanity.framework.Autowired;
import org.imanity.framework.FrameworkMisc;
import org.imanity.framework.JacksonService;
import org.imanity.framework.http.annotation.RequestBody;
import org.imanity.framework.http.entity.MethodDetail;

import java.lang.reflect.Parameter;

public class ParameterResolverRequestBody implements ParameterResolver {

    @Autowired
    private static JacksonService JACKSON_SERVICE;

    @Override
    public Object resolve(MethodDetail methodDetail, Parameter parameter) {
        Object param = null;
        RequestBody requestBody = parameter.getDeclaredAnnotation(RequestBody.class);
        if (requestBody != null) {
            try {
                param = JACKSON_SERVICE.getMainMapper().readValue(methodDetail.getJson(), parameter.getType());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return param;
    }
}
