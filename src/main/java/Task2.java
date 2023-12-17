import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
interface FibCalculator {
    @Cache(persist = true)
    long fib(int number);
}

class CacheProxy implements InvocationHandler {
    private final Object target;
    private final String cacheDirectory = "cache";

    private final Map<String, Object> cache = new HashMap<>();

    private CacheProxy(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(Cache.class)) {
            Cache cacheAnnotation = method.getAnnotation(Cache.class);
            if (cacheAnnotation.persist()) {
                String key = method.getName() + args[0];
                if (cache.containsKey(key)) {
                    return cache.get(key);
                } else {
                    Object result = method.invoke(target, args);
                    cache.put(key, result);
                    persistResult(key, result);
                    return result;
                }
            }
        }
        return method.invoke(target, args);
    }

    private void persistResult(String key, Object result) {
        File directory = new File(cacheDirectory);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Directory created: " + cacheDirectory);
            } else {
                System.err.println("Failed to create directory: " + cacheDirectory);
                return;
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(directory.getPath() + File.separator + key))) {
            oos.writeObject(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T create(T target, Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new CacheProxy(target));
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Cache {
    boolean persist() default false;
}

public class Task2 {
    public static void main(String[] args) {
        FibCalculator fibCalculator = new FibCalculatorImpl();
        FibCalculator proxy = CacheProxy.create(fibCalculator, FibCalculator.class);

        System.out.println(proxy.fib(5));
        System.out.println(proxy.fib(5));
    }
}

class FibCalculatorImpl implements FibCalculator {
    @Override
    public long fib(int number) {
        if (number <= 1) {
            return number;
        } else {
            return fib(number - 1) + fib(number - 2);
        }
    }
}
