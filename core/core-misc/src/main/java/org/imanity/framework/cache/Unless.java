package org.imanity.framework.cache;

public interface Unless {

    boolean unless(CacheableAspect.Tunnel tunnel);

    public class ResultIsNull implements Unless {

        @Override
        public boolean unless(CacheableAspect.Tunnel tunnel) {
            return !tunnel.hasResult();
        }
    }

}
