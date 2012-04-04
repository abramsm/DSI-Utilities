package it.unimi.dsi.big.util;

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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.fastutil.objects.AbstractObjectIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

public class ImmutableExternalPrefixMapSlowTest {

	public void testBig( final int blockSize ) throws IOException {
		Iterable<String> p = new Iterable<String>() {
			private final static long INCREMENT= ( ( 1L << 62 ) / 3000000000L );
			@Override
			public Iterator<String> iterator() {
				return new AbstractObjectIterator<String>() {
					long curr = 0;
					@Override
					public boolean hasNext() {
						return curr < 3000000000L;
					}

					@Override
					public String next() {
						if ( ! hasNext() ) throw new NoSuchElementException();
						final long v = curr++ * INCREMENT ;
						char[] a = new char[ 4 ];
						a[ 0 ] = (char)( v >>> 48 );
						a[ 1 ] = (char)( v >>> 32 );
						a[ 2 ] = (char)( v >>> 16 );
						a[ 3 ] = (char)v;
						return String.valueOf( a );
					}
				};
			}
		};
		
		ImmutableExternalPrefixMap d = new ImmutableExternalPrefixMap( p, blockSize );
				
		int j = 0;
		for( Iterator<String> i = p.iterator(); i.hasNext(); ) {
			String s = i.next();
			assertTrue( s, d.containsKey( s ) );
			assertEquals( s, d.list().get( j++ ).toString() );
		}
		
		final Iterator<CharSequence> k = d.iterator();
		for( final Iterator<String> i = p.iterator(); i.hasNext(); ) {
			assertTrue( i.hasNext() == k.hasNext() );
			assertEquals( i.next().toString(), k.next().toString() );
		}

		// Test negatives
		for( long i = 1000000000000L; i < 1000000002000L; i++ ) assertEquals( -1, d.getLong( Long.toBinaryString( i ) ) );

	}

	@Test
	public void testBig1024() throws IOException {
		testBig( 1024 );
	}
	
	@Test
	public void testBig16384() throws IOException {
		testBig( 16384 );
	}
}
