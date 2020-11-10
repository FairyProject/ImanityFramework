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
