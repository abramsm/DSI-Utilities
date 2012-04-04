package it.unimi.dsi.util;

/*		 
 * DSI utilities
 *
 * Copyright (C) 2011-2012 Sebastiano Vigna 
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

import java.util.Random;

/** An unbelievably fast, high-quality pseudorandom number generator that combines George Marsaglia's Xorshift
 * generators (described in <a href="http://www.jstatsoft.org/v08/i14/paper/">&ldquo;Xorshift RNGs&rdquo;</a>,
 * <i>Journal of Statistical Software</i>, 8:1&minus;6, 2003) with a multiplication.
 * Note that this is <strong>not</strong> a cryptographic-strength
 * pseudorandom number generator, but its quality is significantly higher than {@link Random}'s (e.g., its cycle length is 2<sup>64</sup>&nbsp;&minus;&nbsp;1).
 * 
 * <p>On an Intel i5, calls to {@link #nextLong()}/{@link #nextDouble()} are one order of magnitude faster than {@link Random}'s, 
 * calls to {@link #nextInt()} are four times faster than {@link Random}'s, and calls
 * to {@link #nextInt(int)} are almost three times faster than {@link Random}'s (and several times faster if the 
 * parameter is close to 2<sup>31</sup>).
 * 
 * <p>This class extends {@link Random}, overriding (as usual) the {@link Random#next(int)} method. Nonetheless,
 * since the generator is inherently 64-bit also {@link Random#nextInt()}, {@link Random#nextInt(int)},
 * {@link Random#nextLong()} and {@link Random#nextDouble()} have been overridden for speed (preserving, of course, {@link Random}'s semantics).
 * 
 * <h3>Parameter choice</h3>
 * 
 * <p>There are five parameters to choose in a pseudorandom number generator of this kind: the three shift values,
 * the type of shift, and the multiplier. <i>Numerical Recipes</i> (third edition, Cambridge University Press, 2007) 
 * suggests a choice of parameters from
 * which we take only the multiplier (actually, proposed by Pierre L'Ecuyer
 * in &ldquo;Tables of linear congruential generators of different sizes and good lattice structure&rdquo;,
 * <i>Math. Comput.</i>, 68(225):249&minus;260, 1999). The remaining parameters have been set following
 * extensive experimentation on the 2200 possible choices using 
 * <a href="http://www.iro.umontreal.ca/~simardr/testu01/tu01.html">TestU01</a> and
 * <a href="http://www.phy.duke.edu/~rgb/General/dieharder.php">Dieharder</a>. More details
 * will appear in a forthcoming paper.
 *  
 */
public class XorShiftStarRandom extends Random {
	private static final long serialVersionUID = 1L;

	/** 2<sup>-53</sup>. */
	private static final double NORM_53 = 1. / ( 1L << 53 );
	/** 2<sup>-24</sup>. */
	private static final double NORM_24 = 1. / ( 1L << 24 );

	/** The internal state (and last returned value) of the algorithm. */
	private long x;

	public XorShiftStarRandom() {
		this( Util.randomSeed() );
	}

	/** Creates a new generator using a given seed.
	 * 
	 * @param seed a nonzero seed for the generator (if zero, the generator will be seeded with -1).
	 */
	public XorShiftStarRandom( final long seed ) {
		x = seed == 0 ? -1 : seed;
		nextLong(); // Warm-up.
	}

	@Override
	protected int next( int bits ) {
		return (int)( nextLong() & ( 1L << bits ) - 1 );
	}

	@Override
	public long nextLong() {
		x ^= x << 23;
		x ^= x >>> 52;
		return 2685821657736338717L * ( x ^= ( x >>> 17 ) );
	}

	@Override
	public int nextInt() {
		return (int)nextLong();
	}
	
	@Override
	public int nextInt( final int n ) {
        if ( n <= 0 ) throw new IllegalArgumentException();
		// No special provision for n power of two: all our bits are good.
		if ( ( 1 << 30 ) < n && n < ( ( 1 << 30 ) + ( 1 << 29 ) ) ) {
			// In this range, the expected value of 32-bit trials is between 5/4 and 2.
			if ( n <= 0 ) throw new IllegalArgumentException();
			long bits, value;
			for(;;) {
				bits = nextLong() >>> 1;
				value = bits % n;
				if ( bits - value + ( n - 1 ) >= 0 ) return (int)value;
			}
		}
		
        int bits;
        int value;
		for(;;) {
			bits = (int)(nextLong() >>> 33);
			value = ( bits % n );
			if ( bits - value + ( n - 1 ) >= 0 ) return value;
		}
	}
	
	public long nextLong( final long n ) {
        if ( n <= 0 ) throw new IllegalArgumentException();
		// No special provision for n power of two: all our bits are good.
        long bits, value;
		for(;;) {
			bits = nextLong() >>> 1;
			value = bits % n;
			if ( bits - value + ( n - 1 ) >= 0 ) return value;
		}
	}
	
	@Override
	 public double nextDouble() {
		return ( nextLong() >>> 11 ) * NORM_53;
	}
	
	@Override
	public float nextFloat() {
		return (float)( ( nextLong() >>> 40 ) * NORM_24 );
	}

	@Override
	public boolean nextBoolean() {
		return ( nextLong() & 1 ) != 0;
	}
	
	@Override
	public void nextBytes( final byte[] bytes ) {
		int i = bytes.length, n = 0;
		while( i != 0 ) {
			n = Math.min( i, 8 );
			for ( long bits = nextLong(); n-- != 0; bits >>= 8 ) bytes[ --i ] = (byte)bits;
		}
	}
		

	@Override
	public void setSeed( final long seed ) {
		x = seed == 0 ? -1 : seed;
	}
}
