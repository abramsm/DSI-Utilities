package it.unimi.dsi.util;

/*		 
 * DSI utilities
 *
 * Copyright (C) 2010-2012 Paolo Boldi and Sebastiano Vigna 
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */

import static org.junit.Assert.assertTrue;
import it.unimi.dsi.logging.ProgressLogger;

import org.junit.Ignore;
import org.junit.Test;

public class IntHyperLogLogCounterArrayTest {
	
	@Test
	public void testSingle() {
		final int numTrials = 10;

		for( int size: new int[] { 1, 10, 100, 1000, 100000 } ) {
			for( int log2m: new int[] { 6, 8, 12 } ) {
				final double rsd = IntHyperLogLogCounterArray.relativeStandardDeviation( log2m );
				int correct = 0;
				for ( int trial = 0; trial < numTrials; trial++ ) {
					IntHyperLogLogCounterArray a = new IntHyperLogLogCounterArray( 1, size, log2m, trial );
					final int incr = (int)( ( 1L << 32 ) / size );
					int x = Integer.MIN_VALUE;
					for( int i = 0; i < size; i++ ) {
						a.add( 0, x );
						x += incr;
					}

					System.err.println( "Trial " + trial + ", size " + size + ", error: " + ( size - a.count( 0 ) ) / size + " " + ( Math.abs( size - a.count( 0 ) ) < 2 * rsd * size ? "(+)" : "(-)" ) );
					if ( Math.abs( size - a.count( 0 ) ) / size < 2 * rsd ) correct++;
				}

				System.err.println( "Correct trials for size " + size + ", rsd " + rsd + ": " + correct );
				assertTrue( correct + " < " + 9, correct >= 9 );
			}
		}
	}

	@Test
	public void testMerge() {
		int size = 10000;
		IntHyperLogLogCounterArray a = new IntHyperLogLogCounterArray( 1, size, .005);
		IntHyperLogLogCounterArray b = new IntHyperLogLogCounterArray( 1, size, .005);
//		final double rsd = IntHyperLogLogCounterArray.relativeStandardDeviation(  );
		final int incr1 = (int)( ( 1L << 32 ) / size );
		final int incr2 = (int)( ( 1L << 32 ) / size ) * 3;
		int x1 = Integer.MIN_VALUE;
		int x2 = Integer.MIN_VALUE;
		for( int i = 0; i < size; i++ ) {
			a.add( 0, x1 );
			b.add(0, x2);
			x1 += incr1;
			x2 += incr2;
		}

		System.out.println(IntHyperLogLogCounterArray.registerSize(10));
		System.out.println(a.count(0));
		System.out.println(b.count(0));
		
		System.out.println(a.mergeCount(a, b));
//		assertTrue(Math.abs( size - a.count( 0 ) ) / size < 2 * rsd );
//		assertTrue(Math.abs( size - b.count( 0 ) ) / size < 2 * rsd );
	}

	@Test
	public void testDouble() {
		final int numTrials = 10;

		for( int size: new int[] { 1, 10, 100, 1000, 100000 } ) {
			for( int log2m: new int[] { 4, 6, 8, 12 } ) {
				final double rsd = IntHyperLogLogCounterArray.relativeStandardDeviation( log2m );
				int correct0 = 0, correct1 = 0;
				for ( int trial = 0; trial < numTrials; trial++ ) {
					IntHyperLogLogCounterArray a = new IntHyperLogLogCounterArray( 2, size, log2m, trial );
					final int incr = (int)( ( 1L << 32 ) / size );
					int x = Integer.MIN_VALUE;
					for( int i = 0; i < size; i++ ) {
						a.add( 0, x );
						a.add( 1, x );
						x += incr;
					}

					System.err.println( "Trial " + trial + " (0), size " + size + ", error: " + ( size - a.count( 0 ) ) / size + " " + ( Math.abs( size - a.count( 0 ) ) < 2 * rsd * size ? "(+)" : "(-)" ) );
					System.err.println( "Trial " + trial + " (1), size " + size + ", error: " + ( size - a.count( 1 ) ) / size + " " + ( Math.abs( size - a.count( 1 ) ) < 2 * rsd * size ? "(+)" : "(-)" ) );
					if ( Math.abs( size - a.count( 0 ) ) / size < 2 * rsd ) correct0++;
					if ( Math.abs( size - a.count( 1 ) ) / size < 2 * rsd ) correct1++;
				}

				System.err.println( "Correct trials (0) for size " + size + ", rsd " + rsd + ": " + correct0 );
				System.err.println( "Correct trials (1) for size " + size + ", rsd " + rsd + ": " + correct1 );
				assertTrue( correct0 + " < " + 9, correct0 >= 9 );
				assertTrue( correct1 + " < " + 9, correct1 >= 9 );
			}
		}
	}

	@Ignore("too slow")
	@Test
	public void testLarge() {
		final int numTrials = 10;

		for( int size: new int[] { 1000000000, 2000000000 } ) {
			for( int log2m: new int[] { 6, 8, 12 } ) {
				final double rsd = IntHyperLogLogCounterArray.relativeStandardDeviation( log2m );
				int correct = 0;
				for ( int trial = 0; trial < numTrials; trial++ ) {
					IntHyperLogLogCounterArray a = new IntHyperLogLogCounterArray( 1, size, log2m, trial );
					final int incr = (int)( ( 1L << 32 ) / size );
					int x = Integer.MIN_VALUE;
					for( int i = 0; i < size; i++ ) {
						a.add( 0, x );
						x += incr;
					}

					System.err.println( "Trial " + trial + ", size " + size + ", error: " + ( size - a.count( 0 ) ) / size + " " + ( Math.abs( size - a.count( 0 ) ) < 2 * rsd * size ? "(+)" : "(-)" ) );
					if ( Math.abs( size - a.count( 0 ) ) / size < 2 * rsd ) correct++;
				}

				System.err.println( "Correct trials for size " + size + ", rsd " + rsd + ": " + correct );
				assertTrue( correct + " < " + 9, correct >= 9 );
			}
		}
	}

	
	@Ignore("too slow")
	@Test
	public void testExtreme() {
		final int numTrials = 20, mustBeCorrect = 18;
		final ProgressLogger pl = new ProgressLogger();

		for( int log2m: new int[] { 6, 8, 12 } ) {
			final double rsd = IntHyperLogLogCounterArray.relativeStandardDeviation( log2m );
			int correct = 0;
			long size = 3L * 1024 * 1024 * 1024;
			for ( int trial = 0; trial < numTrials; trial++ ) {
				IntHyperLogLogCounterArray a = new IntHyperLogLogCounterArray( 1, size, log2m, trial );
				pl.start();
				for( int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++ ) if ( i % 4 != 0 ) a.add( 0, i );
				pl.stop();
				pl.count = size;
				System.err.println( pl );
				System.err.println( "Trial " + trial + ", error: " + ( size - a.count( 0 ) ) / size + " " + ( Math.abs( size - a.count( 0 ) ) < 2 * rsd * size ? "(+)" : "(-)" ) );
				if ( Math.abs(  size - a.count( 0 ) ) < 2 * rsd * size ) correct++;
			}

			System.err.println( "Correct trials for size " + size + ", rsd " + rsd + ": " + correct );
			assertTrue( correct + " < " + mustBeCorrect, correct >= mustBeCorrect );
		}

		for( int log2m: new int[] { 6, 8, 12 } ) {
			final double rsd = IntHyperLogLogCounterArray.relativeStandardDeviation( log2m );
			int correct = 0;
			long size = 7L * 512 * 1024 * 1024;
			for ( int trial = 0; trial < numTrials; trial++ ) {
				IntHyperLogLogCounterArray a = new IntHyperLogLogCounterArray( 1, size, log2m, trial );
				pl.start();
				for( int i = Integer.MIN_VALUE; i < Integer.MAX_VALUE; i++ ) if ( i % 8 != 0 ) a.add( 0, i );
				pl.stop();
				pl.count = size;
				System.err.println( pl );
				System.err.println( "Trial " + trial + ", error: " + ( size - a.count( 0 ) ) / size + " " + ( Math.abs( size - a.count( 0 ) ) < 2 * rsd * size ? "(+)" : "(-)" ) );
				if ( Math.abs( size - a.count( 0 ) ) / size < 2 * rsd ) correct++;
			}

			System.err.println( "Correct trials for size " + size + ", rsd " + rsd + ": " + correct );
			assertTrue( correct + " < " + mustBeCorrect, mustBeCorrect >= 9 );
		}
	}

	public static void main( String arg[] ) {
		new IntHyperLogLogCounterArrayTest().testExtreme();
	}
	
}
