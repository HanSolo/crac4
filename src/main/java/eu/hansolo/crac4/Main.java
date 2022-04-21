package eu.hansolo.crac4;

import jdk.crac.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;


/**
 * The Main class will run through a loop from 1 - 100_000
 * and will calculate if the number is a prime. Before it calculates the value
 * it will check the primeCache if the number is in the cache and if yes it will
 * return the value from the cache instead of calculation it.
 * The loop that runs from 1 - 100_000 will be called every 5 seconds.
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
    private static final    Random                      RND        = new Random();
    private static final    int                         INTERVAL   = 5;
    private static final    String                      CRAC_FILES = System.getProperty("user.home") + File.separator + "crac-files";
    private static final    DateTimeFormatter           FORMATTER  = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private        final    GenericCache<Long, Boolean> primeCache;
    private                 int                         counter;
    private                 Runnable                    task;
    private                 ScheduledExecutorService    executorService;


    // ******************** Constructor ***************************************
    public Main(final Runtime runtime) {
        if (!Files.exists(Paths.get(CRAC_FILES))) {
            try {
                System.out.println("Creating " + CRAC_FILES);
                Files.createDirectory(Paths.get(CRAC_FILES));
            } catch (IOException e) {
                System.out.println("Error creating /crac-files folder. " + e);
            }
        }

        runtime.addShutdownHook(new Thread(() -> {
            // Clean crac-files folder only if not in automatic checkpoint mode
            cleanCracFilesFolder();
            System.out.println("App stopped in shutdown hook");
        }));

        final long initialCleanDelay = PropertyManager.INSTANCE.getLong(Constants.INITIAL_CACHE_CLEAN_DELAY, 50);
        final long cacheTimeout      = PropertyManager.INSTANCE.getLong(Constants.CACHE_TIMEOUT, 12);

        primeCache      = new GenericCache<>(initialCleanDelay, cacheTimeout);
        counter         = 1;
        task            = () -> checkForPrimes();
        executorService = Executors.newSingleThreadScheduledExecutor();

        // Register this class as resource in the global context of CRaC
        Core.getGlobalContext().register(Main.this);

        executorService.scheduleAtFixedRate(task, 0, INTERVAL, TimeUnit.SECONDS);
    }


    // ******************** Methods *******************************************
    @Override public void beforeCheckpoint(Context<? extends Resource> context) throws Exception {
        System.out.println("beforeCheckpoint() called in Main");
        // Free resources or stop services
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        executorService = null;
    }

    @Override public void afterRestore(Context<? extends Resource> context) throws Exception {
        System.out.println("afterRestore() called in Main");
        // Restore resources or re-start services
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(task, 0, INTERVAL, TimeUnit.SECONDS);
    }

    private void checkForPrimes() {
        long start = System.nanoTime();
        for (long i = 1 ; i <= 100_000 ; i++) {
            isPrime(RND.nextInt(100_000));
        }
        System.out.println(FORMATTER.format(LocalDateTime.now()) + " " + counter + ". Run: " + ((System.nanoTime() - start) / 1_000_000 + " ms (" + primeCache.size() + " elements cached, " + String.format(Locale.US, "%.1f%%", primeCache.size() / 1_000.0) + ")"));
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

    private void cleanCracFilesFolder() {
        if (PropertyManager.INSTANCE.getBoolean(Constants.CLEANUP)) {
            System.out.println("\nCleanup " + CRAC_FILES);
            File cracFiles = new File(CRAC_FILES);
            if (cracFiles.exists() && cracFiles.isDirectory()) {
                Arrays.stream(Objects.requireNonNull(cracFiles.listFiles())).filter(Predicate.not(File::isDirectory)).forEach(File::delete);
            }
        }
    }

    public static void main(String[] args) {
        Runtime runtime = Runtime.getRuntime();
        System.out.println(FORMATTER.format(LocalDateTime.now()) + " Starting application");
        System.out.println("Running on CRaC (PID " + ProcessHandle.current().pid() + ")");
        System.out.println("First run will take up to 30 seconds...");
        Main main = new Main(runtime);

        try {
            while (true) { Thread.sleep(1000); }
        } catch (InterruptedException e) { }
    }
}
