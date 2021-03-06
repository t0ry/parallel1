package edu.coursera.parallel;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Class wrapping methods for implementing reciprocal array sum in parallel.
 */
public final class ReciprocalArraySumTwoTasks {

	/**
	 * Default constructor.
	 */
	private ReciprocalArraySumTwoTasks() {
	}

	/**
	 * Sequentially compute the sum of the reciprocal values for a given array.
	 *
	 * @param input
	 *            Input array
	 * @return The sum of the reciprocals of the array input
	 */
	protected static double seqArraySum(final double[] input) {
		double sum = 0;

		// Compute sum of reciprocals of array elements
		for (int i = 0; i < input.length; i++) {
			sum += 1 / input[i];
		}

		return sum;
	}

	/**
	 * Computes the size of each chunk, given the number of chunks to create across
	 * a given number of elements.
	 *
	 * @param nChunks
	 *            The number of chunks to create
	 * @param nElements
	 *            The number of elements to chunk across
	 * @return The default chunk size
	 */
	private static int getChunkSize(final int nChunks, final int nElements) {
		// Integer ceil
		return (nElements + nChunks - 1) / nChunks;
	}

	/**
	 * Computes the inclusive element index that the provided chunk starts at, given
	 * there are a certain number of chunks.
	 *
	 * @param chunk
	 *            The chunk to compute the start of
	 * @param nChunks
	 *            The number of chunks created
	 * @param nElements
	 *            The number of elements to chunk across
	 * @return The inclusive index that this chunk starts at in the set of nElements
	 */
	private static int getChunkStartInclusive(final int chunk, final int nChunks, final int nElements) {
		final int chunkSize = getChunkSize(nChunks, nElements);
		return chunk * chunkSize;
	}

	/**
	 * Computes the exclusive element index that the provided chunk ends at, given
	 * there are a certain number of chunks.
	 *
	 * @param chunk
	 *            The chunk to compute the end of
	 * @param nChunks
	 *            The number of chunks created
	 * @param nElements
	 *            The number of elements to chunk across
	 * @return The exclusive end index for this chunk
	 */
	private static int getChunkEndExclusive(final int chunk, final int nChunks, final int nElements) {
		final int chunkSize = getChunkSize(nChunks, nElements);
		final int end = (chunk + 1) * chunkSize;
		if (end > nElements) {
			return nElements;
		} else {
			return end;
		}
	}

	/**
	 * This class stub can be filled in to implement the body of each task created
	 * to perform reciprocal array sum in parallel.
	 */
	private static class ReciprocalArraySumTask extends RecursiveAction {
		static final int THRESHOLD = 1000;
		/**
		 * Starting index for traversal done by this task.
		 */
		private final int startIndexInclusive;
		/**
		 * Ending index for traversal done by this task.
		 */
		private final int endIndexExclusive;
		/**
		 * Input array to reciprocal sum.
		 */
		private final double[] input;
		/**
		 * Intermediate value produced by this task.
		 */
		private double value;

		/**
		 * Constructor.
		 * 
		 * @param setStartIndexInclusive
		 *            Set the starting index to begin parallel traversal at.
		 * @param setEndIndexExclusive
		 *            Set ending index for parallel traversal.
		 * @param setInput
		 *            Input values
		 */
		ReciprocalArraySumTask(final int setStartIndexInclusive, final int setEndIndexExclusive,
				final double[] setInput) {
			this.startIndexInclusive = setStartIndexInclusive;
			this.endIndexExclusive = setEndIndexExclusive;
			this.input = setInput;
		}

		/**
		 * Minimal atomic step of recursion
		 * 
		 * @param hi
		 * @param low
		 * @return
		 */
		private double computeSequential(int low, int hi) {
			double sum = 0.0;
			for (int i = low; i < hi; i++) {
				sum += 1 / input[i];
			}

			return sum;
		}

		/**
		 * Getter for the value produced by this task.
		 * 
		 * @return Value produced by this task
		 */
		public double getValue() {
			return value;
		}

		@Override
		protected void compute() {

			if (this.endIndexExclusive - this.startIndexInclusive < THRESHOLD) {
				this.value = this.computeSequential(this.startIndexInclusive, this.endIndexExclusive);
			} else {
				
				

				int mid = this.startIndexInclusive + ((this.endIndexExclusive - this.startIndexInclusive) >>> 1);
				ReciprocalArraySumTask asyncTaskLo = new ReciprocalArraySumTask(this.startIndexInclusive, mid,
						this.input);
				ReciprocalArraySumTask asyncTaskHi = new ReciprocalArraySumTask(mid, this.endIndexExclusive,
						this.input);

				asyncTaskLo.fork();
				asyncTaskHi.compute();
				asyncTaskLo.join();

				this.value = asyncTaskLo.getValue() + asyncTaskHi.getValue();

				// invokeAll(new ReciprocalArraySumTask(this.startIndexInclusive, mid,
				// this.input),
				// new ReciprocalArraySumTask(mid, this.endIndexExclusive, this.input));

			}
		}
	}

	/**
	 * TODO: Modify this method to compute the same reciprocal sum as seqArraySum,
	 * but use two tasks running in parallel under the Java Fork Join framework. You
	 * may assume that the length of the input array is evenly divisible by 2.
	 *
	 * @param input
	 *            Input array
	 * @return The sum of the reciprocals of the array input
	 */
	protected static double parArraySum(final double[] input) {
		assert input.length % 2 == 0;

		// Compute sum of reciprocals of array elements
		ReciprocalArraySumTask task = new ReciprocalArraySumTask(0, input.length, input);
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
				String.valueOf(Runtime.getRuntime().availableProcessors()));
		ForkJoinPool.commonPool().invoke(task);

		return task.getValue();
	}

	/**
	 * TODO: Extend the work you did to implement parArraySum to use a set number of
	 * tasks to compute the reciprocal array sum. You may find the above utilities
	 * getChunkStartInclusive and getChunkEndExclusive helpful in computing the
	 * range of element indices that belong to each chunk.
	 *
	 * @param input
	 *            Input array
	 * @param numTasks
	 *            The number of tasks to create
	 * @return The sum of the reciprocals of the array input
	 */
	protected static double parManyTaskArraySum(final double[] input, final int numTasks) {
		double sum = 0;
		
		
		
		
		// Compute sum of reciprocals of array elements
		List<ReciprocalArraySumTask> tasks = new LinkedList<> ();
		for (int i = 0; i < numTasks; i++) {
			int start = getChunkStartInclusive(i, numTasks, input.length);
			int end = getChunkEndExclusive(i, numTasks, input.length);
			System.out.println(start + ";" + end); 
			
			ReciprocalArraySumTask task = new ReciprocalArraySumTask(start, end, input);
			tasks.add(task);
		}
		return sum;
	}
}
