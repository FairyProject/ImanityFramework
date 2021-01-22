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

package org.imanity.framework.http.entity;

import com.google.common.collect.Multimap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Getter
public class HttpEntity<T> {
    public static final HttpEntity<?> EMPTY = new HttpEntity<>();
    private final HttpHeaders headers;
    @Nullable
    private final T body;

    protected HttpEntity() {
        this(null, (HttpHeaders) null);
    }

    public HttpEntity(T body) {
        this(body, (HttpHeaders) null);
    }

    public HttpEntity(Multimap<String, String> headers) {
        this(null, headers);
    }

    public HttpEntity(@Nullable T body, @Nullable Multimap<String, String> headers) {
        this.body = body;
        HttpHeaders tempHeaders = new HttpHeaders();
        if (headers != null) {
            tempHeaders.putAll(headers);
        }

        this.headers = HttpHeaders.readOnly(tempHeaders);
    }

    public HttpEntity(@Nullable T body, @Nullable HttpHeaders headers) {
        this.body = body;
        HttpHeaders tempHeaders = new HttpHeaders();
        if (headers != null) {
            tempHeaders.putAll(headers);
        }

        this.headers = HttpHeaders.readOnly(tempHeaders);
    }

    public boolean hasBody() {
        return this.body != null;
    }

    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        } else if (other != null && other.getClass() == this.getClass()) {
            HttpEntity<?> otherEntity = (HttpEntity<?>) other;
            return Objects.equals(this.headers, otherEntity.headers) && Objects.equals(this.body, otherEntity.body);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(this.headers) * 29 + Objects.hash(this.body);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("<");
        if (this.body != null) {
            builder.append(this.body);
            builder.append(',');
        }

        builder.append(this.headers);
        builder.append('>');
        return builder.toString();
    }
}
