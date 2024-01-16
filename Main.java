import java.util.Arrays;

public class Main
{
    private static final int THREAD_COUNT = 8;
    private static final int MAX_PRIME_NUMBER = 100000000;

    public static void main(String[] args)
    {
        long startTimeNanoseconds = System.nanoTime();

        PrimeFinder primeFinder = new PrimeFinder(THREAD_COUNT, MAX_PRIME_NUMBER);
        primeFinder.FindPrimes();

        long endTimeNanoseconds = System.nanoTime();

        long runTimeMilliseconds = (endTimeNanoseconds - startTimeNanoseconds) / 1000000;
        System.out.println(runTimeMilliseconds + "ms " + primeFinder.getPrimeCount() + " " + primeFinder.getPrimeSum());
    }
}

class PrimeFinder
{
    private Thread[] threads;
    private boolean[] isPrime;
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

    public int getMaxPrimeNumber()
    {
        return isPrime.length - 1;
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

    public void FindPrimes()
    {
        this.isPrime[0] = false;
        this.isPrime[1] = false;

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
                System.out.println("Thread Error has occurred");
            }
        }

        for(int i = 0; i < isPrime.length ; ++i)
        {
            if(isPrime[i])
            {
                ++primeCount;
                primeSum += i;
            }
        }
    }

    public void WriteToFile()
    {

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
            primeFinder.setIsPrime(i, false);
        }
    }
}