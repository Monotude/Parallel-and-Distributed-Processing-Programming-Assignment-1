import java.io.FileWriter;
import java.util.Arrays;

public class Main
{
    private static final int THREAD_COUNT = 8;
    private static final int MAX_PRIME_NUMBER = 100000000;

    public static void main(String[] args)
    {
        long startTimeNanoseconds = System.nanoTime();

        PrimeFinder primeFinder = new PrimeFinder(THREAD_COUNT, MAX_PRIME_NUMBER);
        primeFinder.findPrimes();

        long endTimeNanoseconds = System.nanoTime();

        long runTimeMilliseconds = (endTimeNanoseconds - startTimeNanoseconds) / 1000000;
        System.out.println(runTimeMilliseconds + "ms " + primeFinder.getPrimeCount() + " " + primeFinder.getPrimeSum());
    }
}

class PrimeFinder
{
    private final Thread[] threads;
    private final boolean[] isPrime;
    private int counter;
    private int primeCount;
    private long primeSum;

    public PrimeFinder(int threadCount, int maxPrimeNumber)
    {
        threads = new Thread[threadCount];
        isPrime = new boolean[maxPrimeNumber + 1];
        Arrays.fill(isPrime, true);
        counter = 2;
        primeCount = 0;
        primeSum = 0;
    }

    public int getMaxPrimeNumber()
    {
        return isPrime.length - 1;
    }

    public boolean getIsPrime(int number)
    {
        return isPrime[number];
    }

    public synchronized int getAndIncrementCounter()
    {
        if (counter > getMaxPrimeNumber())
        {
            return counter;
        } else
        {
            while (!isPrime[counter])
            {
                ++counter;
            }

            return counter++;
        }
    }

    public int getPrimeCount()
    {
        return primeCount;
    }

    public long getPrimeSum()
    {
        return primeSum;
    }

    public void setIsPrime(int number, boolean isPrime)
    {
        this.isPrime[number] = isPrime;
    }

    public void findPrimes()
    {
        this.isPrime[0] = false;
        this.isPrime[1] = false;

        runThreads();

        for (int i = 0; i < isPrime.length; ++i)
        {
            if (isPrime[i])
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

    public void writeToFile()
    {
        try
        {
            FileWriter primesFileWriter = new FileWriter("primes.txt");
            primesFileWriter.write("A");
        } catch (Exception exception)
        {
            System.out.println("File error has occurred");
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
            checkPrime(numberToCheck, primeFinder.getMaxPrimeNumber());
            numberToCheck = primeFinder.getAndIncrementCounter();
        }
    }

    private void checkPrime(int number, int maxPrimeNumber)
    {
        for (int i = number * number; i <= maxPrimeNumber; i += number)
        {
            if (primeFinder.getIsPrime(i))
            {
                primeFinder.setIsPrime(i, false);
            }
        }
    }
}