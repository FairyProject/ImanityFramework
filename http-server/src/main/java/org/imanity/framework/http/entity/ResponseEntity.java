package org.imanity.framework.http.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class ResponseEntity<T> extends HttpEntity<T> {
    private final Object status;

    public ResponseEntity(HttpResponseStatus status) {
        this(null, (HttpHeaders) null, status);
    }

    public ResponseEntity(@Nullable T body, HttpResponseStatus status) {
        this(body, (HttpHeaders) null, status);
    }

    public ResponseEntity(Multimap<String, String> headers, HttpResponseStatus status) {
        this(null, headers, status);
    }

    public ResponseEntity(@Nullable T body, @Nullable HttpHeaders headers, HttpResponseStatus status) {
        super(body, headers);
        Preconditions.checkNotNull(status, "HttpStatus must not be null");
        this.status = status;
    }

    private ResponseEntity(@Nullable T body, @Nullable HttpHeaders headers, Object status) {
        super(body, headers);
        Preconditions.checkNotNull(status, "HttpStatus must not be null");
        this.status = status;
    }

    private ResponseEntity(@Nullable T body, @Nullable Multimap<String, String> headers, Object status) {
        super(body, headers);
        Preconditions.checkNotNull(status, "HttpStatus must not be null");
        this.status = status;
    }

    public HttpResponseStatus getStatusCode() {
        return this.status instanceof HttpResponseStatus ? (HttpResponseStatus)this.status : HttpResponseStatus.valueOf((int) this.status);
    }

    public int getStatusCodeValue() {
        return this.status instanceof HttpResponseStatus ? ((HttpResponseStatus)this.status).code() : (int) this.status;
    }

    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        } else if (!super.equals(other)) {
            return false;
        } else {
            ResponseEntity<?> otherEntity = (ResponseEntity)other;
            return Objects.equals(this.status, otherEntity.status);
        }
    }

    public int hashCode() {
        return 29 * super.hashCode() + Objects.hash(this.status);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("<");
        builder.append(this.status.toString());
        if (this.status instanceof HttpResponseStatus) {
            builder.append(' ');
            builder.append(((HttpResponseStatus)this.status).reasonPhrase());
        }

        builder.append(',');
        T body = this.getBody();
        HttpHeaders headers = this.getHeaders();
        if (body != null) {
            builder.append(body);
            builder.append(',');
        }

        builder.append(headers);
        builder.append('>');
        return builder.toString();
    }

    public static ResponseEntity.BodyBuilder status(HttpResponseStatus status) {
        Preconditions.checkNotNull(status, "HttpStatus must not be null");
        return new ResponseEntity.DefaultBuilder(status);
    }

    public static ResponseEntity.BodyBuilder status(int status) {
        return new ResponseEntity.DefaultBuilder(status);
    }

    public static ResponseEntity.BodyBuilder ok() {
        return status(HttpResponseStatus.OK);
    }

    public static <T> ResponseEntity<T> ok(T body) {
        return ok().body(body);
    }

    public static <T> ResponseEntity<T> of(Optional<T> body) {
        Preconditions.checkNotNull(body, "Body must not be null");
        return body.map(ResponseEntity::ok).orElseGet(() -> notFound().build());
    }

    public static ResponseEntity.BodyBuilder accepted() {
        return status(HttpResponseStatus.ACCEPTED);
    }

    public static ResponseEntity.HeadersBuilder<?> noContent() {
        return status(HttpResponseStatus.NO_CONTENT);
    }

    public static ResponseEntity.BodyBuilder badRequest() {
        return status(HttpResponseStatus.BAD_REQUEST);
    }

    public static ResponseEntity.HeadersBuilder<?> notFound() {
        return status(HttpResponseStatus.NOT_FOUND);
    }

    public static ResponseEntity.BodyBuilder unprocessableEntity() {
        return status(HttpResponseStatus.UNPROCESSABLE_ENTITY);
    }

    private static class DefaultBuilder implements ResponseEntity.BodyBuilder {
        private final Object statusCode;
        private final HttpHeaders headers = new HttpHeaders();

        public DefaultBuilder(Object statusCode) {
            this.statusCode = statusCode;
        }

        public ResponseEntity.BodyBuilder header(String headerName, String... headerValues) {
            String[] var3 = headerValues;
            int var4 = headerValues.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String headerValue = var3[var5];
                this.headers.set(headerName, headerValue);
            }

            return this;
        }

        public ResponseEntity.BodyBuilder headers(@Nullable HttpHeaders headers) {
            if (headers != null) {
                this.headers.putAll(headers);
            }

            return this;
        }

        public ResponseEntity.BodyBuilder headers(Consumer<HttpHeaders> headersConsumer) {
            headersConsumer.accept(this.headers);
            return this;
        }

        public ResponseEntity.BodyBuilder contentLength(long contentLength) {
            this.headers.set(HttpHeaders.CONTENT_LENGTH, contentLength);
            return this;
        }

        public ResponseEntity.BodyBuilder contentType(String contentType) {
            this.headers.set(HttpHeaders.CONTENT_TYPE, contentType);
            return this;
        }

        public <T> ResponseEntity<T> build() {
            return this.body(null);
        }

        public <T> ResponseEntity<T> body(@Nullable T body) {
            return new ResponseEntity<>(body, this.headers, this.statusCode);
        }
    }

    public interface BodyBuilder extends ResponseEntity.HeadersBuilder<ResponseEntity.BodyBuilder> {
        ResponseEntity.BodyBuilder contentLength(long var1);

        ResponseEntity.BodyBuilder contentType(String var1);

        <T> ResponseEntity<T> body(@Nullable T var1);
    }

    public interface HeadersBuilder<B extends ResponseEntity.HeadersBuilder<B>> {
        B header(String var1, String... var2);

        B headers(@Nullable HttpHeaders var1);

        B headers(Consumer<HttpHeaders> var1);

        <T> ResponseEntity<T> build();
    }
}
