import java.io.FileWriter;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Main
{
    private static final int THREAD_COUNT = 8;
    private static final int MAX_PRIME_NUMBER = 100000000;

    public static void main(String[] args)
    {
        PrimeFinder primeFinder = new PrimeFinder(MAX_PRIME_NUMBER);
        primeFinder.findPrimes(THREAD_COUNT);
    }
}

class PrimeFinder
{
    private static final int NUMBER_OF_PRIMES_TO_WRITE = 10;
    private final PriorityQueue<Integer> primeNumbers;
    private final AtomicInteger primeCount;
    private final AtomicLong primeSum;
    private final int maxPrimeNumber;
    private int counter;

    public PrimeFinder(int maxPrimeNumber)
    {
        this.primeNumbers = new PriorityQueue<>();
        this.maxPrimeNumber = maxPrimeNumber;
        this.counter = 2;
        this.primeCount = new AtomicInteger();
        this.primeSum = new AtomicLong();
    }

    public void addPrimeNumber(int number)
    {
        synchronized (primeNumbers)
        {
            primeNumbers.add(number);

            if (primeNumbers.size() > NUMBER_OF_PRIMES_TO_WRITE)
            {
                primeNumbers.poll();
            }
        }
    }

    public int getMaxPrimeNumber()
    {
        return maxPrimeNumber;
    }

    public synchronized int getAndIncrementCounter()
    {
        if (counter <= maxPrimeNumber)
        {
            return counter++;
        }

        return counter;
    }

    public void incrementPrimeCount()
    {
        primeCount.getAndIncrement();
    }

    public void incrementPrimeSum(int number)
    {
        primeSum.getAndAdd(number);
    }

    public void findPrimes(int threadCount)
    {
        long startTimeNanoseconds = System.nanoTime();

        runThreads(threadCount);

        long endTimeNanoseconds = System.nanoTime();

        long runTimeMilliseconds = (endTimeNanoseconds - startTimeNanoseconds) / 1000000;

        writeToFile(runTimeMilliseconds);
    }

    private void runThreads(int threadCount)
    {
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; ++i)
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

    private void writeToFile(long runtime)
    {
        try (FileWriter primesFileWriter = new FileWriter("primes.txt"))
        {
            primesFileWriter.write("Runtime: " + runtime + " ms\n");

            primesFileWriter.write("Prime count: " + primeCount + "\n");

            primesFileWriter.write("Prime sum: " + primeSum + "\n");

            writeLargestPrimes(primesFileWriter);
        } catch (Exception exception)
        {
            System.out.println("File error has occurred");
        }
    }

    private void writeLargestPrimes(FileWriter primesFileWriter)
    {
        for (int i = NUMBER_OF_PRIMES_TO_WRITE; !primeNumbers.isEmpty() && i > 0; --i)
        {
            try
            {
                primesFileWriter.write(primeNumbers.remove() + "\n");
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
        int number = primeFinder.getAndIncrementCounter(), maxPrimeNumber = primeFinder.getMaxPrimeNumber();
        while (number <= maxPrimeNumber)
        {
            if (isPrime(number))
            {
                primeFinder.addPrimeNumber(number);
                primeFinder.incrementPrimeCount();
                primeFinder.incrementPrimeSum(number);
            }

            number = primeFinder.getAndIncrementCounter();
        }
    }

    private boolean isPrime(int number)
    {
        // Even numbers
        if (number % 2 == 0)
        {
            return number == 2;
        }

        // Odd Numbers
        int maxPrimeFactor = (int) Math.sqrt(number);
        for (int i = 3; i <= maxPrimeFactor; i += 2)
        {
            if (number % i == 0)
            {
                return false;
            }
        }
        return true;
    }
}