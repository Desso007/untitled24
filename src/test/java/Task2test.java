import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class CacheProxyTest {

    @Test
    void testFibonacciCalculationWithCaching() {
        FibCalculator fibCalculator = new FibCalculatorImpl();
        FibCalculator proxy = CacheProxy.create(fibCalculator, FibCalculator.class);

        long result1 = proxy.fib(5);

        long result2 = proxy.fib(5);


        assertEquals(result1, result2);
    }

    @Test
    void testCachingPersistsToFile() {
        FibCalculator fibCalculator = new FibCalculatorImpl();
        FibCalculator proxy = CacheProxy.create(fibCalculator, FibCalculator.class);


        proxy.fib(5);


        assertTrue(new File("cache/fib5").exists());
    }
}
