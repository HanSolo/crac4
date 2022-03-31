package eu.hansolo.crac4;

import jdk.crac.Context;
import jdk.crac.Core;
import jdk.crac.Resource;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * The Main class will run through a loop from 1 - END_VALUE (default 100_000)
 * and will calculate if the number is a prime. Before it calculates the value
 * it will check the primeCache if the number is in the cache and if yes it will
 * return the value from the cache instead of calculation it.
 * The loop that runs from 1 - END_VALUE will be called every 5 seconds.
 * It will take around 10-15 runs before the cache is fully loaded with values
 * because the cache will also remove values that have not been read within a given
 * period of time.
 * If you create the checkpoint after 10-15 runs and restore it after some time
 * you should see that the access times are still fast because the cache was also
 * restored.
 * To make that work correctly you will find some code in the afterRestore() method
 * in the GenericCache that takes the time between the checkpoint and the restore into
 * account.
 */
public class Main implements Resource {
    private static final Random                      RND        = new Random();
    private static final int                         END_VALUE  = 100_000;
    private static final int                         INTERVAL   = 3;
    private final        GenericCache<Long, Boolean> primeCache = new GenericCache<>(10);
    private              int                         counter;
    private              Runnable                    task;
    private              ScheduledExecutorService    executorService;


    // ******************** Constructor ***************************************
    public Main(final Runtime runtime) {
        runtime.addShutdownHook(new Thread(() -> System.out.println("App stopped in shutdown hook")));
        counter         = 1;
        task            = () -> checkForPrimes();
        executorService = Executors.newSingleThreadScheduledExecutor();

        // Register this class as resource in the global context of CRaC
        Core.getGlobalContext().register(Main.this);

        System.out.println("Running on CRaC (PID " + ProcessHandle.current().pid() + ")");

        executorService.scheduleAtFixedRate(task, 0, INTERVAL, TimeUnit.SECONDS);
    }


    // ******************** Methods *******************************************
    @Override public void beforeCheckpoint(Context<? extends Resource> context) throws Exception {
        System.out.println("beforeCheckpoint() called in Main");
        // Shutdown services
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        executorService = null;
    }

    @Override public void afterRestore(Context<? extends Resource> context) throws Exception {
        System.out.println("afterRestore() called in Main");
        // Restart services
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(task, 0, INTERVAL, TimeUnit.SECONDS);
    }

    private void checkForPrimes() {
        long start = System.nanoTime();
        for (long i = 1 ; i <= END_VALUE ; i++) {
            isPrime(RND.nextInt(END_VALUE));
        }
        System.out.println(counter + ". Run: " + ((System.nanoTime() - start) / 1_000_000 + " ms + (" + primeCache.size() + " elements in cache)"));
        counter++;
    }

    private boolean isPrime(final long number) {
        if (primeCache.containsKey(number)) { return primeCache.get(number).get(); }
        boolean isPrime = true;
        for (long n = number ; n > 0 ; n--) {
            if (n != number && n != 1 && number % n == 0) {
                isPrime = false;
                break;
            }
        }
        primeCache.put(number, isPrime);
        return isPrime;
    }


    public static void main(String[] args) {
        Runtime runtime = Runtime.getRuntime();
        Main main = new Main(runtime);

        try {
            while (true) { Thread.sleep(1000); }
        } catch (InterruptedException e) { }
    }
}
