/*
 * Author: Apostolos Chasiotis
 */

import java.math.BigInteger;


public class Factorizer extends Thread{
	BigInteger min;
	BigInteger max;
	BigInteger product;
	
	public static volatile boolean running = true;
	
	public static volatile int threads_alive;
	
	static BigInteger factor1;
	static BigInteger factor2;
	static long execution_time;
	static BigInteger second_factor1;
	static BigInteger second_factor2;
	static int total_secondary_solutions=0;
	
		
	public static synchronized void removeThread() { // synchronized here ensures one thread has access
		threads_alive--;
		if (threads_alive==0) {
			running = false;
		}
	}
	
	public static synchronized void deploySolution(int solution, BigInteger factor1, BigInteger factor2, long execution_time) {
		if (solution == 0) { //main solution
			if (Factorizer.factor1 != null) { // a guard case to not overwrite results
				return;
			}
			Factorizer.factor1 = factor1;
			Factorizer.factor2 = factor2;	
			Factorizer.running = false; // stop when we find a prime solution
		} else if (solution==1) {
			Factorizer.second_factor1 = factor1;
			Factorizer.second_factor2 = factor2;
			Factorizer.total_secondary_solutions++;
			// here dont stop the threads. We will wait for the last result. We have to, because we may find the 2 primes
		}
		Factorizer.execution_time = execution_time;
		try {
			/*
			 * Main thread will check for solution every 200 ms.
			 * And no other thread will have access for the rest 550 ms.
			 * So when a thread enters here and publishes the results, main thread will
			 * have enough time to notice it and keep the first result found. 
			 * All the other threads will be notified.
			 */
			Thread.sleep(550); 
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public Factorizer(BigInteger product, int threads_num) throws InterruptedException {	
		BigInteger cmin = BigInteger.valueOf(2);
		BigInteger step = product.sqrt().divide(BigInteger.valueOf(threads_num));
		BigInteger cmax = step;
		Factorizer.threads_alive = threads_num;
		System.out.println("Product: "+product);
		System.out.println("Min: "+cmin);
		System.out.println("Max: "+product.sqrt());
		System.out.println("Step: "+step);
		
		for (int i=0; i<threads_num; i++) {
			System.out.println("\n----------------------\n[+] Spawning Thread "+i);
			Factorizer new_factorizer = new Factorizer(cmin, cmax, product);
			Thread new_thread = new Thread(new_factorizer);
			new_thread.start();
			
			
			cmin = cmax.add(BigInteger.valueOf(1));
			cmax = (cmin.add(step).compareTo(product.sqrt())>0) ? product.sqrt() : cmin.add(step);
			
		}
		System.out.println("\n[!] Threads spawned\n");
		long main_t0 = System.currentTimeMillis();
		while (true) {
			synchronized (Factorizer.class) {
				if (running == false) {
					if (Factorizer.factor1==null) { // only way to get here is when all threads have died without giving any result
						if (product.isProbablePrime(1)) {
							System.out.println("\n-+-+-+-+-+-+-+-+-+-+-\nFinished Factorization");
							System.out.println("\nNo factorization prossible");
							System.out.println("\nExecution Time: "+(System.currentTimeMillis() - main_t0)/1000 + " sec.");
							System.exit(0);
						} else {
							System.out.println("\n-+-+-+-+-+-+-+-+-+-+-\n\n[~] Found secondary solution with not guaranteed prime numbers:");
							System.out.println("\nFactor1: " + second_factor1);
							System.out.println("\nFactor2: " + second_factor2);
							System.out.println("\nExecution Time: "+execution_time/1000 + " sec.");
							System.out.println("\n[~] Total solutions: "+total_secondary_solutions);
						}
						
					} else {
						System.out.println("\n-+-+-+-+-+-+-+-+-+-+-\n\n[+] Found solution:");
						System.out.println("\nFactor 1: "+factor1);
						System.out.println("\nFactor 2: "+factor2);
						System.out.println("\nExecution Time: "+execution_time/1000 + " sec.");
					}
					
					long t0 = System.currentTimeMillis();
					// Now sleep until all threads have returned or more than 5 secs have passed
					while (Factorizer.threads_alive>0 && ( (System.currentTimeMillis() - t0) <5000) ) {
						Thread.sleep(200);
					}
					System.exit(0);
				}
				
			}
			Thread.sleep(200);
		}
		
		
	}
	
	public Factorizer(BigInteger min, BigInteger max, BigInteger product) {
		this.min = min;
		this.max = max;
		this.product = product;
	}
	
	public void run() {
		BigInteger number = this.min;
		BigInteger factor1, factor2;
		BigInteger current_second_factor1 = null;
		BigInteger current_second_factor2 = null; 
		
		
		long t0 = System.currentTimeMillis();
		
		while (number.compareTo(this.max) <= 0) {			
			synchronized (Factorizer.class) {
			    if (!running) {
			        removeThread();
			        return;
			    }
			}
			
			if (number.isProbablePrime(1) && this.product.remainder(number).compareTo(BigInteger.ZERO) == 0) {
				if (product.divide(number).isProbablePrime(1)) {
					factor1 = number;
					factor2 = product.divide(factor1);
					
					long t1 = System.currentTimeMillis();
					deploySolution(0, factor1, factor2, (t1-t0));
					removeThread();
					return;
				}
				
			} else if(this.product.remainder(number).compareTo(BigInteger.ZERO) == 0) { 
				// Here we will keep the non prime numbers in case no primes will be found. 
				// That way, we will save the last solution found on eacch iteration, but that is ok.
				// I prefer a more in-depth solution rather than 2*x
				current_second_factor1 = number;
				current_second_factor2 = product.divide(current_second_factor1);
			}
    		number = number.add(BigInteger.valueOf(1));
		}
		
		/*
		 * Here we will check if running is true. Running becomes false only when a solution is found and a thread deploys a solution.
		 * In case no thread found a solution, they will reach this point with running still to true.
		 * In that case we will deploy the second solution for the 2 second factors that may not be primes.
		 * And that only if the thread was able to find a pair. deploySolution may not be called on some threads.
		 */
		synchronized (Factorizer.class) { 
		    if (running) {
		    	if (current_second_factor1 != null) {
		    		long t1 = System.currentTimeMillis();
					deploySolution(1, current_second_factor1, current_second_factor2, (t1-t0));
		    	}
		    }
		}
		
		removeThread(); // remove the thread from thread counting.
		return;
	}

}

/*
 * For product 2554353453543325322 and number of threads 60000, we have the following results:
 * 
 * 
-+-+-+-+-+-+-+-+-+-+-

[~] Found secondary solution with not guaranteed prime numbers:

Factor1: 75655223

Factor2: 33763081414

Execution Time: 400 sec.

[~] Total solutions: 8
 
 *
 *

 * For the prime number 1000009541 and number of threads 600 we have:
 * 
 * 
-+-+-+-+-+-+-+-+-+-+-
Finished Factorization

No factorization prossible

Execution Time: 0 sec.

 */
