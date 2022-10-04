## CRaC 4 Demo

### Short description
The demo uses a scheduled task to execute the method ```checkForPrimes()``` every 5 seconds.
In this method a loop will check 100_000 times a random number between 1 - 100_000 for prime.
The ```isPrime(long number)``` method will check if the given number exists in a cache and either
return the result from the cache or calculate the result, put it in the cache and return it.
The cache that is used here, keeps it's items only for a time defined by timeout before they will be
removed from the cache automatically. Everytime a key is requested, it's lifetime in the cache will be
extended by the given timeout value. This leads to the fact that keys that will be requested more often will stay
in the cache where keys that are outdated will be removed from the cache.
The cache also uses a scheduled task that calls the ```clean()``` method once a second and starts after a 
given delay.
The cache timeout and the initial delay can be adjusted using properties file named ```crac4.properties``` which
will be stored in your user home folder.
It contains two entries:
```
initial_cache_clean_delay=50
cache_timeout=10
```
You might need to adjust those values depending on your machine settings (e.g. on my M1 Mac the first run just took 2.5s where on my older Intel Mac it took 22s).
You just have to make sure that the cacheTimeout is not shorter than the time it takes to check the 100_000 numbers for prime, otherwise the cache will be cleaned
too often and you don't see an decrease in calculation time.

### We have two methods that will be called:

The <b>checkForPrimes()</b> method will 100_000 times check a random number between 1 - 100_000 for prime.
After checking all numbers it will print out the time it took to check all numbers on the console and
increase a counter.
```java
private void checkForPrimes() {
    long start = System.nanoTime();
    for (long i = 1 ; i <= 100_000 ; i++) {
        isPrime(RND.nextInt(100_000));
    }
    System.out.println(counter + ". Run: " + ((System.nanoTime() - start) / 1_000_000 + " ms (" + primeCache.size() + " elements cached, " + String.format(Locale.US, "%.1f%%", primeCache.size() / 1_000.0) + ")"));
    counter++;
}
```

The <b>isPrime(final long number)</b> method will be called for each number and either directly returns
the result (in case it's already in the cache) or calculates the result and stores it in the cache.
```java
private boolean isPrime(final long number) {
    if (number < 1) { return false; }
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
```

### Install and run the code:
You need a Linux x64 machine to run the pre-build OpenJDK version incl. CRaC (e.g. Ubuntu on an Intel machine).

<br>

#### Download [OpenJDK pre-build incl. CRaC](https://github.com/CRaC/openjdk-builds/releases/):
```wget https://github.com/CRaC/openjdk-builds/releases/download/17-crac%2B3/openjdk-17-crac+3_linux-x64.tar.gz```

<br>

#### Extract the tar.gz file with sudo rights:
Make sure you have a folder
```
/usr/lib/jvm
```
otherwise create it by using
```
sudo mkdir /usr/lib/jvm
```
Now extract the tar.gz file
```shell
sudo tar zxvf openjdk-17-crac+3_linux-x64.tar.gz -C /usr/lib/jvm
```

<br>

#### Set JAVA_HOME and add it to PATH:
```
$ export JAVA_HOME=/usr/lib/jvm/openjdk-17-crac+3_linux-x64
$ export PATH=$JAVA_HOME/bin:$PATH
$ java -version
openjdk version "17-crac" 2021-09-14
OpenJDK Runtime Environment (build 17-crac+3-15)
OpenJDK 64-Bit Server VM (build 17-crac+3-15, mixed mode, sharing)
```

<br>

#### Clone this repository:
Make sure you have git installed on your machine, otherwise install it by using
```
$ sudo apt update
$ sudo apt install git
```
Now clone this repository
```
$ git clone https://github.com/HanSolo/crac4
```

<br>

#### Build the jar
```
$ cd crac
$ ./gradlew clean build
```

<br>

#### Run the example
Make sure you have the folder ```crac-files``` in your home folder, otherwise create it with
```
$ cd ~
$ mkdir crac-files
```

Open a shell (SHELL 1)
```
cd /build/libs
java -XX:CRaCCheckpointTo=/home/YOUR_USER_NAME/crac-files -jar ./crac4-17.0.0.jar
```
Now you should see something as follows on your screen:
```
Running on CRaC (PID 20719)
1. Run 27935 ms (63254 elements in cache)
2. Run 10290 ms (86556 elements in cache)
3. Run 3828 ms (95142 elements in cache)
4. Run 1378 ms (98244 elements in cache)
5. Run 527 ms (99336 elements in cache)
6. Run 214 ms (99775 elements in cache)
7. Run 77 ms (99918 elements in cache)
8. Run 43 ms (99973 elements in cache)
9. Run 26 ms (99992 elements in cache)
10. Run 14 ms (99995 elements in cache)
11. Run 20 ms (99997 elements in cache)
12. Run 14 ms (99999 elements in cache)
13. Run 18 ms (100000 elements in cache)
```

Open another shell (SHELL 2) and execute the following command to create the checkpoint
```
$ cd /CRAC_PROJECT_FOLDER/build/libs
$ jcmd crac4-17.0.0.jar JDK.checkpoint
20719:
Command executed successfully
```

SHELL 1 should now show something as follows:
```
Running on CRaC (PID 20719)
1. Run 27935 ms (63254 elements in cache)
2. Run 10290 ms (86556 elements in cache)
3. Run 3828 ms (95142 elements in cache)
4. Run 1378 ms (98244 elements in cache)
5. Run 527 ms (99336 elements in cache)
6. Run 214 ms (99775 elements in cache)
7. Run 77 ms (99918 elements in cache)
8. Run 43 ms (99973 elements in cache)
9. Run 26 ms (99992 elements in cache)
10. Run 14 ms (99995 elements in cache)
11. Run 20 ms (99997 elements in cache)
12. Run 14 ms (99999 elements in cache)
13. Run 18 ms (100000 elements in cache)
beforeCheckpoint() called in Main
beforeCheckpoint() called in GenericCache
CR: Checkpoint ...
Killed
```

In the first shell (SHELL 1) window where you started the jar file, restore the checkpoint by calling
```
Running on CRaC (PID 20719)
1. Run 27935 ms (63254 elements in cache)
2. Run 10290 ms (86556 elements in cache)
3. Run 3828 ms (95142 elements in cache)
4. Run 1378 ms (98244 elements in cache)
5. Run 527 ms (99336 elements in cache)
6. Run 214 ms (99775 elements in cache)
7. Run 77 ms (99918 elements in cache)
8. Run 43 ms (99973 elements in cache)
9. Run 26 ms (99992 elements in cache)
10. Run 14 ms (99995 elements in cache)
11. Run 20 ms (99997 elements in cache)
12. Run 14 ms (99999 elements in cache)
13. Run 18 ms (100000 elements in cache)
beforeCheckpoint() called in Main
beforeCheckpoint() called in GenericCache
CR: Checkpoint ...
Killed
$ java -XX:CRaCRestoreFrom=/home/YOUR_USER_NAME/crac-files
```

If everything worked as expected you should now see the program continue to run
```
Running on CRaC (PID 20719)
1. Run 27935 ms (63254 elements in cache)
2. Run 10290 ms (86556 elements in cache)
3. Run 3828 ms (95142 elements in cache)
4. Run 1378 ms (98244 elements in cache)
5. Run 527 ms (99336 elements in cache)
6. Run 214 ms (99775 elements in cache)
7. Run 77 ms (99918 elements in cache)
8. Run 43 ms (99973 elements in cache)
9. Run 26 ms (99992 elements in cache)
10. Run 14 ms (99995 elements in cache)
11. Run 20 ms (99997 elements in cache)
12. Run 14 ms (99999 elements in cache)
13. Run 18 ms (100000 elements in cache)
beforeCheckpoint() called in Main
beforeCheckpoint() called in GenericCache
CR: Checkpoint ...
Killed
$ java -XX:CRaCRestoreFrom=/home/YOUR_USER_NAME/crac-files
afterRestore() called in GenericCache
afterRestore() called in Main
14. Run 19 ms (100000 elements in cache)
15. Run 16 ms (100000 elements in cache)
16. Run 19 ms (100000 elements in cache)
17. Run 15 ms (100000 elements in cache)
18. Run 16 ms (100000 elements in cache)
App stopped in shutdown hook
```
Now you can stop the running app by pressing CTRL+C

As you can see the counter continued counting with 14 and did not restart from 1.
And because the cache was also stored when we created the checkpoint, it will directly
be filled after we restore the app from the checkpoint. And with the cache filled, the
call to ```checkForPrimes()``` will return quickly.
You can imagine that this approach drastically reduces the startup times of applications.

</br>

### Running the demo in a docker container (on a Linux x64 machine)
#### 1. Create docker image
1. Open a shell window
2. Change to the crac4 folder
3. Run ``` docker build -t crac4 . ``` to build the docker image

</br>

#### 2. Start the application in a docker container
1. Open a shell window
2. Run ``` docker run -it --privileged --rm --name crac4 crac4 ```
3. In the docker container run</br> 
   ``` 
   cd /opt/app 
   java -XX:CRaCCheckpointTo=/opt/crac-files -jar crac4-17.0.0.jar
   ```
4. Note the PID of the program   

</br>

#### 3. Start a 2nd shell window and create the checkpoint
1. Open a second shell window
2. Run ``` docker exec -it -u root crac4 /bin/bash ```
3. Wait until the program in the first window reaches for example the 17th iteration
4. Take the PID from shell 1 and run ``` jcmd PID JDK.checkpoint```
5. In the first shell window the application should have created the checkpoint 
6. In second shell window run ``` exit ``` to get back to your machine

</br>

#### 4. Commit the current state of the docker container 
1. Now get the CONTAINER_ID from shell window 1 by execute ``` docker ps -a ``` in shell window 2
2. Run ``` docker commit CONTAINER_ID crac4:checkpoint ``` in shell window 2
3. Go back to shell window 1 and execute ```exit``` to stop the container

</br>

#### 5. Run the docker container from the saved state incl. the checkpoint
Now you can start the docker container from the checkpoint by executing
``` docker run -it --privileged --rm --name crac4 crac4:checkpoint java -XX:CRaCRestoreFrom=/opt/crac-files ```

</br>

#### 6. Create a shell script to restore multiple times
1. Open a shell window
2. Create a text file named ```restore_docker.sh```
3. Add
```
#!/bin/bash

echo "docker run -it --privileged --rm --name $1 crac4:checkpoint java -XX:CRaCRestoreFrom=/opt/crac-files"

docker run -it --privileged --rm --name $1 crac4:checkpoint java -XX:CRaCRestoreFrom=/opt/crac-files
```
4. Make the script executable by executing ```chmod +x restore_docker.sh```
5. Now you can start the docker container multiple times executing ```restore_docker.sh NAME_OF_CONTAINER```

If you would like to start the original container without the checkpoint you can still
do that by executing the following command
```
docker run -it --privileged --rm --name crac4 crac4 java -jar /opt/app/crac4-17.0.0.jar
```