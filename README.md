All programming is in Java.
Completed by Tyler Knoop.

The prime finding is all parallel, and the work is approximately the same for each thread. Each thread takes a number from counter (which is synchronized) and sees if it is prime, updating whatever is necessary if it is. This process does not require any work from other threads. Any info it updates is either atomic or synchronized. Since there is no shared variables that are not synchronized/atomic, it follows all of the rules for parallel processing. The counter makes it so all of the threads get around the same amount of work since they each have to update around the same size number everytime. The time from testing was around 8 seconds with 8 concurrent threads calculating all 10^8 numbers.
