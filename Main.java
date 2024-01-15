public class Main
{
    private static final int threadCount = 8;

    public static void main(String[] args)
    {
        long startTime = System.nanoTime();

        PrimeFinder primeFinder = new PrimeFinder(threadCount);
        primeFinder.FindPrimes();

        long endTime = System.nanoTime();

        long runTimeSeconds = (endTime - startTime) / 1000000;
        System.out.print(runTimeSeconds + "ms " + primeFinder.getPrimeCount() + " " + primeFinder.getPrimeSum());

        System.out.println();
    }
}

class PrimeFinder
{
    private final int threadCount;
    private final Thread[] threads;
    private int primeCount;
    private long primeSum;

    public PrimeFinder(int threadCount)
    {
        this.threadCount = threadCount;
        threads = new Thread[threadCount];
        primeCount = 0;
        primeSum = 0;
    }

    public int getThreadCount()
    {
        return threadCount;
    }

    public int getPrimeCount()
    {
        return primeCount;
    }

    public long getPrimeSum()
    {
        return primeSum;
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
        for (int i = 0; i < threadCount; ++i)
        {
            threads[i] = new Thread(new PrimeFinderThread(this, i + 1));
            threads[i].start();
        }

        for (int i = 0; i < threadCount; ++i)
        {
            try
            {
                threads[i].join();
            } catch (Exception exception)
            {
                System.out.println("Unknown Error has occurred");
            }
        }
    }
}

class PrimeFinderThread implements Runnable
{
    private static final int maxNumber = 100000000;
    PrimeFinder primeFinder;
    private final int threadNumber;

    public PrimeFinderThread(PrimeFinder primeFinder, int threadNumber)
    {
        this.primeFinder = primeFinder;
        this.threadNumber = threadNumber;
    }

    @Override
    public void run()
    {
        int startNumber = threadNumber;

        if (startNumber == 1)
        {
            startNumber += primeFinder.getThreadCount();
        }

        for (int i = startNumber; i < maxNumber; i += primeFinder.getThreadCount())
        {
            boolean isPrime = checkPrime(i);

            if (isPrime)
            {
                primeFinder.incrementPrimeCount();
                primeFinder.incrementPrimeSum(i);
            }
        }
    }

    private boolean checkPrime(int number)
    {
        int maxFactor = (int) Math.sqrt(number);
        for (int factor = 2; factor <= maxFactor; ++factor)
        {
            if (number % factor == 0)
            {
                return false;
            }
        }

        return true;
    }
}