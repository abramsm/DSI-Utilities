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

import it.unimi.dsi.Util;
import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.fastutil.longs.LongBigList;

import java.io.Serializable;

/**
 * An array of approximate sets each represented using a HyperLogLog counter.
 * 
 * <p>HyperLogLog counters represent the number of elements of a set in an approximate way. They have been
 * introduced by Philippe Flajolet, &Eacute;ric Fusy, Olivier Gandouet, and Fre&eacute;de&eacute;ric Meunier in
 * &ldquo;HyperLogLog: the analysis of a near-optimal cardinality estimation algorithm&rdquo;,
 * <em>Proceedings of the 13th conference on analysis of algorithm (AofA 07)</em>, pages
 * 127&minus;146, 2007. They are an improvement over the basic idea of <em>loglog counting</em>, introduced by
 * Marianne Durand and Philippe Flajolet in &ldquo;Loglog counting of large cardinalities&rdquo;,
 * <i>ESA 2003, 11th Annual European Symposium</i>, volume 2832 of Lecture Notes in Computer Science, pages 605&minus;617, Springer, 2003.
 * 
 * <p>Each counter is composed by {@link #m} registers, and each register is made of {@link #registerSize} bits.
 * The first number depends on the desired relative standard deviation, and its logarithm can be computed using {@link #log2NumberOfRegisters(double)},
 * whereas the second number depends on an upper bound on the number of distinct elements to be counted, and it can be computed
 * using {@link #registerSize(long)}.
 * 
 * <p>Actually, this class implements an <em>array</em> of counters. Each counter is completely independent, but they all use the same hash function. 
 * The reason for this design is that in our intended applications hundred of millions of counters are common, and the JVM overhead to create such a number of objects
 * would be unbearable. This class allocates an array of {@link LongArrayBitVector}s, each containing {@link #CHUNK_SIZE} registers,
 * and can thus handle billions of billions of registers efficiently (in turn, this means being able to
 * handle an array of millions of billions of high-precision counters).
 * 
 * <p>When creating an instance, you can choose the size of the array (i.e., the number of counters) and the desired relative standard deviation 
 * (either {@linkplain #IntHyperLogLogCounterArray(int, long, double) explicitly} or
 * {@linkplain #IntHyperLogLogCounterArray(int, long, int) choosing the number of registers per counter}).
 * Then, you can {@linkplain #add(int, int) add an element to a counter}. At any time, you can
 * {@linkplain #count(int) count} count (approximately) the number of distinct elements that have been added to a counter.
 * 
 * <p>If you need to reuse this class multiple times, you can {@linkplain #clear() clear all registers}, possibly {@linkplain #clear(long) setting a new seed}.
 * The seed is used to compute the hash function used by the HyperLogLog counters.
 * 
 * @author Paolo Boldi
 * @author Sebastiano Vigna
 */

public class IntHyperLogLogCounterArray implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final boolean ASSERTS = false;
	private static final boolean DEBUG = false;

	/** The logarithm of the maximum size in registers of a bit vector. */
	public final static int CHUNK_SHIFT = 30;
	/** The maximum size in registers of a bit vector. */
	public final static long CHUNK_SIZE = 1L << CHUNK_SHIFT;
	/** The mask used to obtain an register offset in a chunk. */
	public final static long CHUNK_MASK = CHUNK_SIZE - 1;
	
	/** An array of bit vectors containing all registers. */
	protected final LongArrayBitVector bitVector[];

	/** {@link #registerSize}-bit views of {@link #bitVector}. */
	protected final LongBigList registers[];

	/** The logarithm of the number of registers per counter. */
	protected final int log2m;

	/** The number of registers per counter. */
	protected final int m;

	/** The number of registers minus one. */
	protected final int mMinus1;

	/** The size in bits of each register. */
	protected final int registerSize;

	/** The size in bits of each counter ({@link #registerSize} <code>*</code> {@link #m}). */
	protected final int counterSize;
	
	/** The shift that selects the chunk corresponding to a counter. */  
	protected final int counterShift;

	/** A seed for hashing. */
	protected long seed;
	
	/** The correct value for &alpha;, multiplied by {@link #m}<sup>2</sup> (see the paper). */
	private double alphaMM;
	
	/** The mask OR'd with the output of the hash function so that {@link Fast#leastSignificantBit(long)} does not return too large a value. */
	private long sentinelMask;

	/**
	 * Returns the logarithm of the number of registers per counter that are necessary to attain a
	 * given relative standard deviation.
	 * 
	 * @param rsd the relative standard deviation to be attained.
	 * @return the logarithm of the number of registers that are necessary to attain relative standard deviation <code>rsd</code>.
	 */
	public static int log2NumberOfRegisters( final double rsd ) {
		// 1.106 is valid for 16 registers or more.
		return (int)Math.ceil( Fast.log2( ( 1.106 / rsd ) * ( 1.106 / rsd ) ) );
	}


	/**
	 * Returns the relative standard deviation corresponding to a given logarithm of the number of registers per counter.
	 * 
	 * @param log2m the logarithm of the number of registers.
	 * @return the resulting relative standard deviation.
	 */
	public static double relativeStandardDeviation( final int log2m ) {
		return ( log2m == 4 ? 1.106 : log2m == 5 ? 1.070 : log2m == 6 ? 1.054 : log2m == 7 ? 1.046 : 1.04 ) / Math.sqrt( 1 << log2m );
	}

	/**
	 * Returns the register size in bits, given an upper bound on the number of distinct elements.
	 * 
	 * @param n an upper bound on the number of distinct elements.
	 * @return the register size in bits.
	 */

	public static int registerSize( final long n ) {
		return Math.max( 5, (int)Math.ceil( Math.log( Math.log( n ) / Math.log( 2 ) ) / Math.log( 2 ) ) );
	}

	/** Returns the chunk of a given counter.
	 * 
	 * @param counter a counter.
	 * @return its chunk.
	 */
	protected int chunk( final int counter ) {
		return counter >> counterShift;
	}
	
	/** Returns the bit offset of a given counter in its chunk.
	 * 
	 * @param counter a counter.
	 * @return the starting bit of the given counter in its chunk.
	 */
	protected long offset( final int counter ) {
		return ( (long)counter << log2m & CHUNK_MASK ) * registerSize;
	}
	
	/**
	 * Creates a new array of counters.
	 * 
	 * @param arraySize the number of counters. 
	 * @param n the expected number of elements.
	 * @param rsd the relative standard deviation.
	 */
	public IntHyperLogLogCounterArray( final int arraySize, final long n, final double rsd ) {
		this( arraySize, n, log2NumberOfRegisters( rsd ) );
	}

	/**
	 * Creates a new array of counters.
	 * 
	 * @param arraySize the number of counters. 
	 * @param n the expected number of elements.
	 * @param log2m the logarithm of the number of registers per counter.
	 */
	public IntHyperLogLogCounterArray( final int arraySize, final long n, final int log2m ) {
		this( arraySize, n, log2m, Util.randomSeed() );
	}
	
	/**
	 * Creates a new array of counters.
	 * 
	 * @param arraySize the number of counters. 
	 * @param n the expected number of elements.
	 * @param log2m the logarithm of the number of registers per counter.
	 * @param seed the seed used to compute the hash function.
	 */
	public IntHyperLogLogCounterArray( final int arraySize, final long n, final int log2m, final long seed ) {
		this.m = 1 << ( this.log2m = log2m );
		this.mMinus1 = m - 1;
		this.registerSize = registerSize( n );
		this.counterSize = registerSize << log2m;

		counterShift = CHUNK_SHIFT - log2m;
		sentinelMask = 1L << ( 1 << registerSize ) - 2;
		// System.err.println( arraySize + " " + m + " " + registerSize);
		final long sizeInRegisters = (long)arraySize * m;
		final int numVectors = (int)( ( sizeInRegisters + CHUNK_MASK ) >>> CHUNK_SHIFT );
		
		bitVector = new LongArrayBitVector[ numVectors ];
		registers = new LongBigList[ numVectors ];
		for( int i = 0; i < numVectors; i++ ) {
			this.bitVector[ i ] = LongArrayBitVector.ofLength( registerSize * Math.min( CHUNK_SIZE, sizeInRegisters - ( (long)i << CHUNK_SHIFT ) ) );
			this.registers[ i ] = bitVector[ i ].asLongBigList( registerSize );
		}
		this.seed = seed;
		if ( DEBUG ) System.err.println( "Register size: " + registerSize + " log2m (b): " + log2m + " m: " + m );
		// See the paper.
		switch ( log2m ) {
		case 4:
			alphaMM = 0.673 * m * m; break;
		case 5:
			alphaMM = 0.697 * m * m; break;
		case 6:
			alphaMM = 0.709 * m * m; break;
		default:
			alphaMM = ( 0.7213 / ( 1 + 1.079 / m ) ) * m * m;
		}
	}
	
	/** Clears all registers and sets a new seed (e.g., using {@link Util#randomSeed()}).
	 * 
	 * @param seed the new seed used to compute the hash function
	 */
	public void clear( final long seed ) {
		clear();
		this.seed = seed;
	}
	
	/** Clears all registers. */
	public void clear() {
		for( LongArrayBitVector bv: bitVector ) bv.fill( false );
	}
	
	private final static long jenkins( final long x, final long seed ) {
		long a, b, c;

		/* Set up the internal state */
		a = seed + x;
		b = seed;
		c = 0x9e3779b97f4a7c13L; /* the golden ratio; an arbitrary value */

		a -= b; a -= c; a ^= (c >>> 43);
		b -= c; b -= a; b ^= (a << 9);
		c -= a; c -= b; c ^= (b >>> 8);
		a -= b; a -= c; a ^= (c >>> 38);
		b -= c; b -= a; b ^= (a << 23);
		c -= a; c -= b; c ^= (b >>> 5);
		a -= b; a -= c; a ^= (c >>> 35);
		b -= c; b -= a; b ^= (a << 49);
		c -= a; c -= b; c ^= (b >>> 11);
		a -= b; a -= c; a ^= (c >>> 12);
		b -= c; b -= a; b ^= (a << 18);
		c -= a; c -= b; c ^= (b >>> 22);

		return c;
	}

	/** Adds an element to a counter.
	 * 
	 * @param k the index of the counter.
	 * @param v the element to be added.
	 */
	public void add( int k, int v ) {
		final long x = jenkins( v, seed );
		final int j = (int)( x & mMinus1 );
		final int r = Fast.leastSignificantBit( x >>> log2m | sentinelMask );
		if ( ASSERTS ) assert r < ( 1 << registerSize ) - 1;
		if ( ASSERTS ) assert r >= 0;
		final LongBigList l = registers[ k >>> counterShift ];
		final long offset = ( ( (long)k << log2m ) + j ) & CHUNK_MASK;
		l.set( offset, Math.max( r + 1, l.getLong( offset ) ) );
	}

	public double mergeCount(IntHyperLogLogCounterArray... estimators)
	{
		LongArrayBitVector mergedBytes = mergeBytes(estimators);
		return count(mergedBytes.bits(), offset(0));

	}

	public static LongArrayBitVector mergeBytes(IntHyperLogLogCounterArray... estimators)
	{
		LongArrayBitVector mergedBytes = null;
		int numEsitimators = (estimators == null) ? 0 : estimators.length;
		if (numEsitimators > 0)
		{
			mergedBytes =  LongArrayBitVector.ofLength( estimators[0].bitVector[0].length());
			
			for (int e = 0; e < numEsitimators; e++)
			{
				mergedBytes.or(estimators[e].bitVector[0]);
			}
		}
		return mergedBytes;
	}

	/** Returns the array of big lists of registers underlying this array of counters.
	 * 
	 * <p>The main purpose of this method is debugging, as it makes comparing
	 * the evolution of the state of two implementations easy.
	 * 
	 * @return the array of big lists of registers underlying this array of counters.
	 */
	
	public LongBigList[] registers() {
		return registers;
	}


	/** Estimates the number of distinct elements that have been added to a given counter so far.
	 * 
	 * @param bits the bit array containing the counter.
	 * @param offset the starting bit position of the counter in <code>bits</code>.
	 * @return an approximation of the number of distinct elements that have been added to counter so far.
	 */	
	protected double count( final long[] bits, final long offset ) {
		int remaining = (int)( Long.SIZE - offset % Long.SIZE ); 
		int word = (int)( offset / Long.SIZE );
		long curr = bits[ word ] >>> offset % Long.SIZE;

		final int registerSize = this.registerSize;
		final int mask = ( 1 << registerSize ) - 1;

		double s = 0;
		int zeroes = 0;
		long r;

		for ( int j = m; j-- != 0; ) {
			if ( remaining >= registerSize ) {
				r = curr & mask;
				curr >>>= registerSize;
				remaining -= registerSize;
			}
			else {
				r = ( curr | bits[ ++word ] << remaining ) & mask;
				curr = bits[ word ] >>> registerSize - remaining;
				remaining += Long.SIZE - registerSize;
			}

			// if ( ASSERTS ) assert r == registers[ chunk( k ) ].getLong( offset( k ) + j ) : "[" + j + "] " + r + "!=" + registers[ chunk( k ) ].getLong( offset( k ) + j );

			if ( r == 0 ) zeroes++;
			s += 1. / ( 1L << r );
		}

		s = alphaMM / s;
		if ( DEBUG ) System.err.println( "Zeroes: " + zeroes );
		if ( zeroes != 0 && s < 5. * m / 2 ) {
			if ( DEBUG ) System.err.println( "Small range correction" );
			return m * Math.log( (double)m / zeroes );
		}
		else return s;
	}

	/** Estimates the number of distinct elements that have been added to a given counter so far.
	 * 
	 * @param k the index of the counter.
	 * @return an approximation of the number of distinct elements that have been added to counter <code>k</code> so far.
	 */	
	public double count( final int k ) {
		return count( bitVector[ chunk( k ) ].bits(), offset( k ) );
	}
}
