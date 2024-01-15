import java.util.Arrays;

public class Main
{
    private static final int threadCount = 8;
    private static final int maxPrimeNumber = 100000000;

    public static void main(String[] args)
    {
        long startTime = System.nanoTime();

        PrimeFinder primeFinder = new PrimeFinder(threadCount, maxPrimeNumber);
        primeFinder.FindPrimes();

        long endTime = System.nanoTime();

        long runTimeSeconds = (endTime - startTime) / 1000000;
        System.out.println(runTimeSeconds + "ms " + primeFinder.getPrimeCount() + " " + primeFinder.getPrimeSum());
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

    public synchronized int getAndIncrementCounter()
    {
        if (counter > getMaxPrimeNumber())
        {
            return counter;
        } else
        {
            if (!isPrime[counter])
            {
                ++counter;
                return getAndIncrementCounter();
            }

            return counter++;
        }
    }

    public boolean getIsPrime(int number)
    {
        return isPrime[number];
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

    public synchronized void incrementPrimeCount()
    {
        ++primeCount;
    }

    public synchronized void incrementPrimeSum(int number)
    {
        primeSum += number;
    }

    public void FindPrimes()
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
                System.out.println("Unknown Error has occurred");
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
        primeFinder.setIsPrime(0, false);
        primeFinder.setIsPrime(1, false);

        int i = primeFinder.getAndIncrementCounter(), maxPrimeNumber = primeFinder.getMaxPrimeNumber();
        while (i <= maxPrimeNumber)
        {
            boolean isPrime = checkPrime(i);
            primeFinder.setIsPrime(i, isPrime);

            if (isPrime)
            {
                primeFinder.incrementPrimeCount();
                primeFinder.incrementPrimeSum(i);
            }

            i = primeFinder.getAndIncrementCounter();
        }
    }

    private boolean checkPrime(int number)
    {
        int maxFactor = (int) Math.sqrt(number);
        for (int i = 2; i <= maxFactor; ++i)
        {
            if (primeFinder.getIsPrime(number) && number % i == 0)
            {
                return false;
            }
        }

        int maxPrimeNumber = primeFinder.getMaxPrimeNumber();
        for (int i = number; i <= maxPrimeNumber; i += number)
        {
            if(primeFinder.getIsPrime(i))
            {
                primeFinder.setIsPrime(i, false);
            }
        }
        return true;
    }
}