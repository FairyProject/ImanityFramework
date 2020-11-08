package org.imanity.framework.cache;

import java.util.Optional;

public interface Unless {

    boolean unless(CacheableAspect.Tunnel tunnel);

    public class ResultIsNull implements Unless {

        @Override
        public boolean unless(CacheableAspect.Tunnel tunnel) {
            return !tunnel.hasResult();
        }
    }

    public class ResultOptionalIsNull implements Unless {

        @Override
        public boolean unless(CacheableAspect.Tunnel tunnel) {
            return !tunnel.hasResult() || (tunnel.cached().get() instanceof Optional && !((Optional<?>) tunnel.cached().get()).isPresent());
        }
    }

}
