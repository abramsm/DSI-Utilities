package it.unimi.dsi.stat;

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

import it.unimi.dsi.fastutil.Size64;
import it.unimi.dsi.fastutil.doubles.DoubleList;

/** A simple class digesting a stream of numbers and providing basic statistics about the stream.
 * You just have to create an instance, call {@link #add(double)} or {@link #add(double[])} to
 * add elements.
 * 
 * @author Sebastiano Vigna
 */

public class SummaryStats implements Size64 {
	/** Mean */
	private double a;
	/** A statistics used to compute the variance (see <a href="http://en.wikipedia.org/wiki/Standard_deviation#Rapid_calculation_methods">here</a>). */
	private double q;
	/** The minimum value in the stream. */
	private double min = Double.POSITIVE_INFINITY;
	/** The maximum value in the stream. */
	private double max = Double.NEGATIVE_INFINITY;
	/** The number of elements in the stream. */
	private long size;
	
	/** Adds a value to the stream.
	 * 
	 * @param x the new value.
	 */
	public void add( double x ) {
		final double oldA = a;
		a += ( x - a ) / ++size;
		q += ( x - a ) * ( x - oldA );
		min = Math.min( min, x );
		max = Math.max( max, x );
	}

	/** Adds values to the stream.
	 * 
	 * @param a an array of new values.
	 * @deprecated Use {@link #addAll(double[])}.
	 */
	@Deprecated
	public void add( double[] a ) {
		for( double x: a ) add( x );
	}

	/** Adds values to the stream.
	 * 
	 * @param a an array of new values.
	 */
	public void addAll( double[] a ) {
		for( double x: a ) add( x );
	}

	/** Adds values to the stream.
	 * 
	 * @param l a list of new values.
	 */
	public void addAll( DoubleList l ) {
		for( double x: l ) add( x );
	}

	/** Returns the mean of the values added so far.
	 * 
	 * @return the mean of the values added so far.
	 */
	public double mean() {
		return a;
	}

	/** Returns the sum of the values added so far.
	 * 
	 * @return the sum of the values added so far.
	 */
	public double sum() {
		return a * size;
	}

	/** Returns the <em>sample</em> variance of the values added so far.
	 * 
	 * @return the sample variance of the values added so far.
	 * @see #variance()
	 */
	public double sampleVariance() {
		return q / ( size - 1 );
	}

	/** Returns the variance of the values added so far.
	 * 
	 * @return the variance of the values added so far.
	 * @see #sampleVariance()
	 */
	public double variance() {
		return q / size;
	}

	/** Returns the <em>sample</em> standard deviation of the values added so far.
	 * 
	 * @return the sample standard deviation of the values added so far.
	 * @see #standardDeviation()
	 */
	public double sampleStandardDeviation() {
		return Math.sqrt( sampleVariance() );
	}

	/** Returns the standard deviation of the values added so far.
	 * 
	 * @return the standard deviation of the values added so far.
	 * @see #sampleStandardDeviation()
	 */
	public double standardDeviation() {
		return Math.sqrt( variance() );
	}
	
	/** Returns the <em>sample</em> relative standard deviation of the values added so far.
	 * 
	 * @return the sample relative standard deviation of the values added so far.
	 * @see #relativeStandardDeviation()
	 */
	public double sampleRelativeStandardDeviation() {
		return Math.sqrt( sampleVariance() ) / mean();
	}

	/** Returns the relative standard deviation of the values added so far.
	 * 
	 * @return the relative standard deviation of the values added so far.
	 * @see #sampleRelativeStandardDeviation()
	 */
	public double relativeStandardDeviation() {
		return Math.sqrt( variance() ) / mean();
	}
	
	/** Returns the minimum of the values added so far.
	 * 
	 * @return the minimum of the values added so far.
	 */
	public double min() {
		return min;
	}
	
	/** Returns the maximum of the values added so far.
	 * 
	 * @return the maximum of the values added so far.
	 */
	public double max() {
		return max;
	}
	
	/** Returns the number of values added so far.
	 * 
	 * @return the number of values added so far.
	 */
	public long size64() {
		return size;
	}
	
	@Deprecated
	public int size() {
		throw new UnsupportedOperationException();
	}
}
