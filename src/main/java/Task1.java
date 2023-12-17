import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Retention(RetentionPolicy.RUNTIME)
@interface NotNull {}

@Retention(RetentionPolicy.RUNTIME)
@interface Min {
    int value();
}

@Retention(RetentionPolicy.RUNTIME)
@interface Max {
    int value();
}

class RandomObjectGenerator {
    private final Map<Class<?>, Object> defaultValues = new HashMap<>();
    private final Random random = new Random();

    public <T> T nextObject(Class<T> clazz, String factoryMethod) throws Exception {
        if (defaultValues.containsKey(clazz)) {
            return (T) defaultValues.get(clazz);
        }

        T instance = null;

        if (factoryMethod != null && !factoryMethod.isEmpty()) {
            Method method = clazz.getMethod(factoryMethod);
            instance = (T) method.invoke(null);
        } else {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            instance = constructor.newInstance();
        }

        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(NotNull.class)) {
                setValue(instance, field, generateNotNullValue(field.getType()));
            } else if (field.isAnnotationPresent(Min.class)) {
                setValue(instance, field, generateMinValue(field.getAnnotation(Min.class).value()));
            } else if (field.isAnnotationPresent(Max.class)) {
                setValue(instance, field, generateMaxValue(field.getAnnotation(Max.class).value()));
            }
        }

        return instance;
    }

    private void setValue(Object instance, java.lang.reflect.Field field, Object value) throws Exception {
        field.setAccessible(true);
        field.set(instance, value);
    }

    private Object generateNotNullValue(Class<?> type) throws Exception {
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return random.nextInt();
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            return random.nextDouble();
        } else if (type.equals(String.class)) {
            return "RandomString";
        }

        return null;
    }

    private int generateMinValue(int min) {
        return random.nextInt(Integer.MAX_VALUE - min) + min;
    }

    private int generateMaxValue(int max) {
        return random.nextInt(max + 1);
    }
}

class MyClass {
    private final int intValue;

    public MyClass(int intValue) {
        this.intValue = intValue;
    }

    @NotNull
    public static MyClass create() {
        return new MyClass(42);
    }

    public int getIntValue() {
        return intValue;
    }
}

class MyRecord {
    @Min(10)
    @Max(100)
    private final int intValue;

    public MyRecord() {
        // Default constructor for reflection
        this.intValue = 0; // Set some default value
    }

    public MyRecord(int intValue) {
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }
}

public class Task1 {
    public static void main(String[] args) throws Exception {
        RandomObjectGenerator rog = new RandomObjectGenerator();

        MyClass myClass = rog.nextObject(MyClass.class, "create");
        System.out.println(myClass.getIntValue());

        MyRecord myRecord = rog.nextObject(MyRecord.class, null);
        System.out.println(myRecord.getIntValue());
    }
}
