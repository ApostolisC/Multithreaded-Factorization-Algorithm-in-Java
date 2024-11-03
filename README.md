# Multithreaded-Factorization-Algorithm-in-Java
This project implements a multithreaded algorithm for factorizing large numbers using Java's BigInteger class. The algorithm leverages parallel processing to efficiently identify prime factors of a given product, making it suitable for handling very large integers that exceed standard data type limits.

Key features include:

* Multithreading: Utilizes Java threads to perform simultaneous factorization tasks, significantly speeding up the computation for large numbers.
* Probabilistic Primality Testing: Incorporates isProbablePrime for efficient checking of prime candidates, allowing the algorithm to quickly eliminate non-prime factors.
* Dynamic Thread Management: Dynamically spawns multiple threads based on the specified number of workers, optimizing resource usage and execution time.
* Secondary Solution Handling: Capable of returning secondary factor pairs even when primary prime factorization is not possible.

This implementation serves as a practical example of concurrency in Java, demonstrating effective synchronization techniques to ensure thread safety while sharing resources. It is an excellent reference for anyone interested in number theory, algorithms, or Java concurrency.
Getting Started

To run the project, clone the repository and execute the main file, changing the parameters for the Factorizer for the number to factorize and the number of threads.
