package org.imanity.framework.bukkit.impl.test;

import lombok.NonNull;
import org.imanity.framework.bukkit.impl.annotation.ProviderTestImpl;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImplementationFactory {

    private static final Map<Class<? extends ImplementationTest>, TestResult> TESTS = new ConcurrentHashMap<>();

    public static TestResult test(@Nullable ProviderTestImpl testAnnotation) {
        if (testAnnotation == null) {
            return TestResult.NO_PROVIDER;
        }

        Class<? extends ImplementationTest> type = testAnnotation.value();
        if (TESTS.containsKey(type)) {
            return TESTS.get(type);
        }

        try {
            ImplementationTest test = type.newInstance();

            TestResult result = test.test() ? TestResult.SUCCESS : TestResult.FAILURE;
            TESTS.put(type, result);

            return result;
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public enum TestResult {

        SUCCESS,
        FAILURE,
        NO_PROVIDER

    }

}
