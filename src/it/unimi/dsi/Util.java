package it.unimi.dsi;

/*		 
 * DSI utilities
 *
 * Copyright (C) 2002-2012 Sebastiano Vigna 
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

import it.unimi.dsi.fastutil.BigArrays;
import it.unimi.dsi.fastutil.longs.LongBigArrays;

import java.util.Enumeration;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/** All-purpose static-method container class.
 *
 * @author Sebastiano Vigna
 * @since 0.1
 */

public final class Util {
	private Util() {}

	/** A reasonable format for real numbers. */
	private static final java.text.NumberFormat FORMAT_DOUBLE = new java.text.DecimalFormat( "#,##0.00" );
	
	/** Formats a number.
	 *
	 * <P>This method formats a double separating thousands and printing just two fractional digits.
	 * @param d a number.
	 * @return a string containing a pretty print of the number.
	 */
	public static String format( final double d ) {
		final StringBuffer s = new StringBuffer();
		return FORMAT_DOUBLE.format( d, s, new java.text.FieldPosition( 0 ) ).toString();
	}
	
	/** A reasonable format for integers. */
	private static final java.text.NumberFormat FORMAT_LONG = new java.text.DecimalFormat( "#,###" );
	
	/** Formats a number.
	 *
	 * <P>This method formats a long separating thousands.
	 * @param l a number.
	 * @return a string containing a pretty print of the number.
	 */
	public static String format( final long l ) {
		final StringBuffer s = new StringBuffer();
		return FORMAT_LONG.format( l, s, new java.text.FieldPosition( 0 ) ).toString();
	}

	/** Formats a size.
	 *
	 * <P>This method formats a long using suitable unit multipliers (e.g., <samp>K</samp>, <samp>M</samp>, <samp>G</samp>, and <samp>T</samp>)
	 * and printing just two fractional digits.
	 * @param l a number, representing a size (e.g., memory).
	 * @return a string containing a pretty print of the number using unit multipliers.
	 */
	public static String formatSize( final long l ) {
		if ( l >= 1000000000000L ) return format( l / 1000000000000.0 ) + "T";
		if ( l >= 1000000000L ) return format( l / 1000000000.0 ) + "G";
		if ( l >= 1000000L ) return format( l / 1000000.0 ) + "M";
		if ( l >= 1000L ) return format( l / 1000.0 ) + "K";
		return Long.toString( l );
	}

	/** Formats a binary size.
	 *
	 * <P>This method formats a long using suitable unit binary multipliers (e.g., <samp>Ki</samp>, <samp>Mi</samp>, <samp>Gi</samp>, and <samp>Ti</samp>)
	 * and printing <em>no</em> fractional digits. The argument must be a power of 2.
	 * @param l a number, representing a binary size (e.g., memory); must be a power of 2.
	 * @return a string containing a pretty print of the number using binary unit multipliers.
	 */
	public static String formatBinarySize( final long l ) {
		if ( ( l & -l ) != l ) throw new IllegalArgumentException( "Not a power of 2: " + l );
		if ( l >= ( 1L << 40 ) ) return format( l >> 40 ) + "Ti";
		if ( l >= ( 1L << 30 ) ) return format( l >> 30 ) + "Gi";
		if ( l >= ( 1L << 20 ) ) return format( l >> 20 ) + "Mi";
		if ( l >= ( 1L << 10 ) ) return format( l >> 10 ) + "Ki";
		return Long.toString( l );
	}

	/** Formats a size.
	 *
	 * <P>This method formats a long using suitable binary
	 * unit multipliers (e.g., <samp>Ki</samp>, <samp>Mi</samp>, <samp>Gi</samp>, and <samp>Ti</samp>)
	 * and printing just two fractional digits.
	 * @param l a number, representing a size (e.g., memory).
	 * @return a string containing a pretty print of the number using binary unit multipliers.
	 */
	public static String formatSize2( final long l ) {
		if ( l >= 1L << 40 ) return format( (double)l / ( 1L << 40 ) ) + "Ti";
		if ( l >= 1L << 30 ) return format( (double)l / ( 1L << 30 ) ) + "Gi";
		if ( l >= 1L << 20 ) return format( (double)l / ( 1L << 20 ) ) + "Mi";
		if ( l >= 1L << 10 ) return format( (double)l / ( 1L << 10 ) ) + "Ki";
		return Long.toString( l );
	}

	
	/** Checks whether Log4J is properly configuring by searching for appenders in all loggers.
	 * 
	 * @return whether Log4J is configured (or, at least, an educated guess).
	 */
	
	public static boolean log4JIsConfigured() {
		if ( Logger.getRootLogger().getAllAppenders().hasMoreElements() ) return true;
		Enumeration<?> loggers = LogManager.getCurrentLoggers();
		while ( loggers.hasMoreElements() ) {
			Logger logger = (Logger)loggers.nextElement();
			if ( logger.getAllAppenders().hasMoreElements() ) return true;
		}
		return false;
	}
	
	/** Ensures that Log4J is configured, by invoking, if necessary,
	 * {@link org.apache.log4j.BasicConfigurator#configure()}, and
	 * setting the root logger level to {@link Level#INFO}.
	 * 
	 * @param klass the calling class (to be shown to the user). 
	 */
	
	public static void ensureLog4JIsConfigured( final Class<?> klass ) {
		ensureLog4JIsConfigured( klass, Level.INFO );
	}
	
	/** Ensures that Log4J is configured, by invoking, if necessary,
	 * {@link org.apache.log4j.BasicConfigurator#configure()}, and
	 * setting the root logger level to <code>level</code>.
	 * 
	 * @param klass the calling class (to be shown to the user). 
	 * @param level the required logging level.
	 */
	
	public static void ensureLog4JIsConfigured( final Class<?> klass, final Level level ) {
		if ( ! log4JIsConfigured() ) {
			System.err.println( "WARNING: " + ( klass != null ? klass.getSimpleName()  + " is" : "We are" ) + " autoconfiguring Log4J (level: " + level + "). You should configure Log4J properly instead." );
			// This mimics BasicConfigurator.configure(), but logs to stderr.
			BasicConfigurator.configure( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ), ConsoleAppender.SYSTEM_ERR ) );
			LogManager.getRootLogger().setLevel( level );
		}
	}
	
	/** Ensures that Log4J is configured, by invoking, if necessary,
	 * {@link org.apache.log4j.BasicConfigurator#configure()}, and
	 * setting the root logger level to {@link Level#INFO}.
	 */
	
	public static void ensureLog4JIsConfigured() {
		ensureLog4JIsConfigured( null, Level.INFO );
	}
	
	/** Ensures that Log4J is configured, by invoking, if necessary,
	 * {@link org.apache.log4j.BasicConfigurator#configure()}, and
	 * setting the root logger level to a specified logging level.
	 * 
	 * @param level the required logging level.
	 */
	
	public static void ensureLog4JIsConfigured( final Level level ) {
		ensureLog4JIsConfigured( null, level );
	}
	
	/** Calls Log4J's {@link Logger#getLogger(java.lang.Class)} method and then {@link #ensureLog4JIsConfigured()}.
	 * 
	 * @param klass a class that will be passed to {@link Logger#getLogger(java.lang.Class)}.
	 * @return the logger returned by {@link Logger#getLogger(java.lang.Class)}.
	 */
	
	public static Logger getLogger( final Class<?> klass ) {
		Logger logger = Logger.getLogger( klass );
		ensureLog4JIsConfigured( klass );
		return logger;
	}
	
	/** Calls Log4J's {@link Logger#getLogger(java.lang.Class)} method and then {@link #ensureLog4JIsConfigured()} with argument {@link Level#DEBUG}.
	 * 
	 * @param klass a class that will be passed to {@link Logger#getLogger(java.lang.Class)}.
	 * @return the logger returned by {@link Logger#getLogger(java.lang.Class)}.
	 */
	
	public static Logger getDebugLogger( final Class<?> klass ) {
		Logger logger = Logger.getLogger( klass );
		ensureLog4JIsConfigured( klass, Level.DEBUG );
		return logger;
	}
	
	private final static Runtime RUNTIME = Runtime.getRuntime();
	
	/** Returns true if less then 5% of the available memory is free.
	 * 
	 * @return true if less then 5% of the available memory is free.
	 */
	public static boolean memoryIsLow() {
		return availableMemory() * 100 < RUNTIME.totalMemory() * 5; 
	}

	/** Returns the amount of available memory (free memory plus never allocated memory).
	 * 
	 * @return the amount of available memory, in bytes.
	 */
	public static long availableMemory() {
		return RUNTIME.freeMemory() + ( RUNTIME.maxMemory() - RUNTIME.totalMemory() ); 
	}

	/** Returns the percentage of available memory (free memory plus never allocated memory).
	 * 
	 * @return the percentage of available memory.
	 */
	public static int percAvailableMemory() {
		return (int)( ( Util.availableMemory() * 100 ) / Runtime.getRuntime().maxMemory() ); 
	}

	/** Tries to compact memory as much as possible by forcing garbage collection.
	 */
	public static void compactMemory() {
		try {
			final byte[][] unused = new byte[ 128 ][]; 
			for( int i = unused.length; i-- != 0; ) unused[ i ] = new byte[ 2000000000 ];
		}
		catch ( OutOfMemoryError itsWhatWeWanted ) {}
		System.gc();
	}
	
	private static volatile long seedUniquifier = 0x9E3779B97F4A7C16L;	

	/** Returns a random seed generated by calling {@link System#nanoTime()}, adding a unique identifier
	 * and scrambling the result using
	 * the finalisation step of Austin Appleby's 
	 * <a href="http://sites.google.com/site/murmurhash/">MurmurHash3</a>.
	 * 
	 * @return a reasonably good random seed.
	 */
	public static long randomSeed() {
		long seed = ++seedUniquifier + System.nanoTime();

		seed ^= seed >>> 33;
		seed *= 0xff51afd7ed558ccdL;
		seed ^= seed >>> 33;
		seed *= 0xc4ceb9fe1a85ec53L;
		seed ^= seed >>> 33;

		return seed;
	}

	/** Returns a random seed generated by calling {@link System#nanoTime()}, adding a unique identifier
	 * and scrambling the result using
	 * the finalisation step of Austin Appleby's <a href="http://sites.google.com/site/murmurhash/">MurmurHash3</a>,
	 * converted to a byte array.
	 * 
	 * @return a reasonably good random seed.
	 */
	public static byte[] randomSeedBytes() {
		final long seed = Util.randomSeed();
		final byte[] s = new byte[ 8 ];
		for( int i = Long.SIZE / Byte.SIZE; i-- != 0; ) s[ i ] = (byte)( seed >>> i );
		return s;
	}

	/** Computes in place the inverse of a permutation expressed
	 * as an array of <var>n</var> distinct integers in [0&nbsp;..&nbsp;<var>n</var>).
	 * 
	 * <p><strong>Warning</strong>: if <code>perm</code> is not a permutation,
	 * essentially anything can happen.
	 * 
	 * @param perm the permutation to be inverted.
	 * @return <code>perm</code>.
	 */
	
	public static int[] invertPermutationInPlace( int[] perm ) {
		for( int n = perm.length; n-- != 0; ) {
			int i = perm[ n ];
			if ( i < 0 ) perm[ n ] = -i - 1;
			else if ( i != n ) {
				int j, k = n;
				
				for(;;) {
					j = perm[ i ];
					perm[ i ] = -k - 1;
					if ( j == n ) {
						perm[ n ] = i;
						break;
					}
					k = i;
					i = j;
				}
			}
		}
		
		return perm;
	}
	
	/** Computes the inverse of a permutation expressed
	 * as an array of <var>n</var> distinct integers in [0&nbsp;..&nbsp;<var>n</var>).
	 * 
	 * <p><strong>Warning</strong>: if <code>perm</code> is not a permutation,
	 * essentially anything can happen.
	 * 
	 * @param perm the permutation to be inverted.
	 * @param inv the array storing the inverse. 
	 * @return <code>inv</code>.
	 */
	
	public static int[] invertPermutation( int[] perm, int[] inv ) {
		for( int i = perm.length; i-- != 0; ) inv[ perm[ i ] ]  = i;
		return inv;
	}
	
	/** Computes the inverse of a permutation expressed
	 * as an array of <var>n</var> distinct integers in [0&nbsp;..&nbsp;<var>n</var>) 
	 * and stores the result in a new array.
	 * 
	 * <p><strong>Warning</strong>: if <code>perm</code> is not a permutation,
	 * essentially anything can happen.
	 * 
	 * @param perm the permutation to be inverted.
	 * @return a new array containing the inverse permutation.
	 */
	
	public static int[] invertPermutation( int[] perm ) {
		return invertPermutation( perm, new int[ perm.length ] );
	}
	
	/** Stores the identity permutation in an array.
	 * 
	 * @param perm an array of integers.
	 * @return <code>perm</code>, filled with the identity permutation.
	 */
	
	public static int[] identity( int[] perm ) {
		for( int i = perm.length; i-- != 0; ) perm[ i ] = i;
		return perm;
	}

	/** Stores the identity permutation in a new array of given length.
	 * 
	 * @param n the size of the array.
	 * @return a new array of length <code>n</code>, filled with the identity permutation.
	 */
	
	public static int[] identity( int n ) {
		return identity( new int[ n ] );
	}


	/** Computes in place the inverse of a permutation expressed
	 * as a {@linkplain BigArrays big array} of <var>n</var> distinct long integers in [0&nbsp;..&nbsp;<var>n</var>).
	 * 
	 * <p><strong>Warning</strong>: if <code>perm</code> is not a permutation,
	 * essentially anything can happen.
	 * 
	 * @param perm the permutation to be inverted.
	 * @return <code>perm</code>.
	 */
	
	public static long[][] invertPermutationInPlace( long[][] perm ) {
		for( long n = LongBigArrays.length( perm ); n-- != 0; ) {
			long i = LongBigArrays.get( perm, n );
			if ( i < 0 ) LongBigArrays.set( perm, n, -i - 1 );
			else if ( i != n ) {
				long j, k = n;
				
				for(;;) {
					j = LongBigArrays.get( perm, i );
					LongBigArrays.set( perm, i, -k - 1 );
					if ( j == n ) {
						LongBigArrays.set( perm, n, i );
						break;
					}
					k = i;
					i = j;
				}
			}
		}
		
		return perm;
	}
	
	/** Computes the inverse of a permutation expressed
	 * as a {@linkplain BigArrays big array} of <var>n</var> distinct long integers in [0&nbsp;..&nbsp;<var>n</var>).
	 * 
	 * <p><strong>Warning</strong>: if <code>perm</code> is not a permutation,
	 * essentially anything can happen.
	 * 
	 * @param perm the permutation to be inverted.
	 * @param inv the big array storing the inverse. 
	 * @return <code>inv</code>.
	 */
	
	public static long[][] invertPermutation( long[][] perm, long[][] inv ) {
		for( int i = perm.length; i-- != 0; ) {
			final long t[] = perm[ i ];
			for( int d = t.length; d-- != 0; ) LongBigArrays.set( inv, t[ d ], BigArrays.index( i, d ) );
		}
		return inv;
	}
	
	/** Computes the inverse of a permutation expressed
	 * as a {@linkplain BigArrays big array} of <var>n</var> distinct long integers in [0&nbsp;..&nbsp;<var>n</var>) 
	 * and stores the result in a new big array.
	 * 
	 * <p><strong>Warning</strong>: if <code>perm</code> is not a permutation,
	 * essentially anything can happen.
	 * 
	 * @param perm the permutation to be inverted.
	 * @return a new big array containing the inverse permutation.
	 */
	
	public static long[][] invertPermutation( long[][] perm ) {
		return invertPermutation( perm, LongBigArrays.newBigArray( LongBigArrays.length( perm ) ) );
	}
	
	/** Stores the identity permutation in a {@linkplain BigArrays big array}. 
	 * 
	 * @param perm a big array.
	 * @return <code>perm</code>, filled with the identity permutation.
	 */
	
	public static long[][] identity( long[][] perm ) {
		for( int i = perm.length; i-- != 0; ) {
			final long[] t = perm[ i ];  
			for( int d = t.length; d-- != 0; ) t[ d ] = BigArrays.index( i, d );
		}
		return perm;
	}

	/** Stores the identity permutation in a new big array of given length.
	 * 
	 * @param n the size of the array.
	 * @return a new array of length <code>n</code>, filled with the identity permutation.
	 */
	
	public static long[][] identity( long n ) {
		return identity( LongBigArrays.newBigArray( n ) );
	}
}
