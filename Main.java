import java.io.FileWriter;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main
{
    private static final int THREAD_COUNT = 8;
    private static final int MAX_PRIME_NUMBER = 100000000;
    private static final int NUMBER_OF_PRIMES_TO_WRITE = 10;

    public static void main(String[] args)
    {
        long startTimeNanoseconds = System.nanoTime();

        PrimeFinder primeFinder = new PrimeFinder(THREAD_COUNT, MAX_PRIME_NUMBER);
        primeFinder.findPrimes();

        long endTimeNanoseconds = System.nanoTime();

        long runTimeMilliseconds = (endTimeNanoseconds - startTimeNanoseconds) / 1000000;

        primeFinder.writeToFile(runTimeMilliseconds, NUMBER_OF_PRIMES_TO_WRITE);
    }
}

class PrimeFinder
{
    private final Thread[] threads;
    private final AtomicBoolean[] isPrime;
    private int counter;
    private int primeCount;
    private long primeSum;

    public PrimeFinder(int threadCount, int maxPrimeNumber)
    {
        threads = new Thread[threadCount];
        isPrime = new AtomicBoolean[maxPrimeNumber + 1];
        for (int i = 0; i < isPrime.length; ++i)
        {
            isPrime[i] = new AtomicBoolean();
            isPrime[i].set(true);
        }
        counter = 2;
        primeCount = 0;
        primeSum = 0;
    }

    public int getMaxPrimeNumber()
    {
        return isPrime.length - 1;
    }

    public synchronized int getAndIncrementCounter()
    {
        int maxPrimeNumber = getMaxPrimeNumber();
        if (counter <= maxPrimeNumber)
        {
            while (counter <= maxPrimeNumber && !isPrime[counter].get())
            {
                ++counter;
            }

            if (counter <= maxPrimeNumber)
            {
                return counter++;
            }
        }
        return counter;
    }

    public void setIsPrime(int number, boolean expectedValue, boolean changedValue)
    {
        this.isPrime[number].compareAndSet(expectedValue, changedValue);
    }

    public void findPrimes()
    {
        this.isPrime[0].set(false);
        this.isPrime[1].set(false);

        runThreads();

        for (int i = 0; i < isPrime.length; ++i)
        {
            if (isPrime[i].get())
            {
                ++primeCount;
                primeSum += i;
            }
        }
    }

    private void runThreads()
    {
        for (int i = 0; i < threads.length; ++i)
        {
            threads[i] = new Thread(new PrimeFinderThread(this));
            threads[i].start();
        }

        for (Thread thread : threads)
        {
            try
            {
                thread.join();
            } catch (Exception exception)
            {
                System.out.println("Thread error has occurred");
            }
        }
    }

    public void writeToFile(long runtime, int numberOfPrimesToWrite)
    {
        try (FileWriter primesFileWriter = new FileWriter("primes.txt"))
        {
            primesFileWriter.write("Runtime: " + runtime + " ms\n");

            primesFileWriter.write("Prime count: " + primeCount + "\n");

            primesFileWriter.write("Prime sum: " + primeSum + "\n");

            writeLargestPrimes(primesFileWriter, numberOfPrimesToWrite);
        } catch (Exception exception)
        {
            System.out.println("File error has occurred");
        }
    }

    private void writeLargestPrimes(FileWriter primesFileWriter, int numberOfPrimesToWrite)
    {
        int[] primesToWrite = new int[numberOfPrimesToWrite];
        for (int i = getMaxPrimeNumber(); i >= 0; --i)
        {
            if (numberOfPrimesToWrite == 0)
            {
                break;
            }

            if (isPrime[i].get())
            {
                primesToWrite[--numberOfPrimesToWrite] = i;
            }
        }
        for (int prime : primesToWrite)
        {
            try
            {
                primesFileWriter.write(prime + "\n");
            } catch (Exception exception)
            {
                System.out.println("File error has occurred");
            }
        }
    }
}

class PrimeFinderThread implements Runnable
{
    PrimeFinder primeFinder;

    public PrimeFinderThread(PrimeFinder primeFinder)
    {
        this.primeFinder = primeFinder;
    }

    @Override
    public void run()
    {
        int numberToCheck = primeFinder.getAndIncrementCounter(), maxPrimeFactor = (int) Math.sqrt(primeFinder.getMaxPrimeNumber());
        while (numberToCheck <= maxPrimeFactor)
        {
            checkPrimeMultiples(numberToCheck, primeFinder.getMaxPrimeNumber());
            numberToCheck = primeFinder.getAndIncrementCounter();
        }
    }

    private void checkPrimeMultiples(int number, int maxPrimeNumber)
    {
        for (int i = number * number; i <= maxPrimeNumber; i += number)
        {
            primeFinder.setIsPrime(i, true, false);
        }
    }
}