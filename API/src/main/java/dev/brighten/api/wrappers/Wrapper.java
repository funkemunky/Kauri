package dev.brighten.api.wrappers;

import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.reflections.types.WrappedField;
import cc.funkemunky.api.reflections.types.WrappedMethod;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Wrapper {

    public final WrappedClass wrappedClass;
    public Object object;

    public Wrapper(WrappedClass wrappedClass) {
        this.wrappedClass = wrappedClass;
    }

    public Wrapper(WrappedClass wrappedClass, Object object) {
        this.wrappedClass = wrappedClass;
        this.object = object;
    }

    private static final Map<String, WrappedField> cachedFields = new ConcurrentHashMap<>();
    private static final Map<String, WrappedMethod> cachedMethods = new ConcurrentHashMap<>();

    public <T> T fetchField(String fieldName) {
        assert object != null;

        return cachedFields.compute(wrappedClass.getParent().getName() + ";;;" + fieldName, (key, field) -> {
            if(field != null) return field;

            return wrappedClass.getFieldByName(fieldName);
        }).get(object);
    }

    public <T> T fetchMethod(String methodName, Object... parameters) {
        assert object != null;

        return cachedMethods.compute(wrappedClass.getParent().getName() + ";;;" + methodName + ";;;"
                + Arrays.stream(parameters).map(ob -> ob.getClass().getSimpleName())
                        .collect(Collectors.joining(",")), (key, field) -> {
            if(field != null) return field;

            return wrappedClass.getMethod(methodName);
        }).invoke(object, parameters);
    }
}
