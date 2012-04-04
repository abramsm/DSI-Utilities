package it.unimi.dsi.util;

/*		 
 * DSI utilities
 *
 * Copyright (C) 2010-2012 Sebastiano Vigna 
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Random;

import org.junit.Test;

public class BloomFilterTest {
	
	@Test
	public void testAdd() {
		BloomFilter bloomFilter = new BloomFilter( 10, 20, new byte[ 8 ] ); // High precision 
		assertTrue( bloomFilter.add( "test" ) );
		assertFalse( bloomFilter.add( "test" ) );
		assertTrue( bloomFilter.add( "foo" ) );
		assertTrue( bloomFilter.add( "bar" ) );
		assertEquals( 3, bloomFilter.size() );
		
		bloomFilter.clear();
		assertTrue( bloomFilter.add( new int[] { 0, 1 } ) );
		assertFalse( bloomFilter.add( new int[] { 0, 1 } ) );
		assertTrue( bloomFilter.add( new int[] { 1, 2 } ) );
		assertTrue( bloomFilter.add( new int[] { 1, 0 } ) );
		assertEquals( 3, bloomFilter.size() );
	}

	@Test
	public void testConflicts() {
		BloomFilter bloomFilter = new BloomFilter( 1000, 11, new byte[ 8 ] ); // Low precision
		LongOpenHashSet longs = new LongOpenHashSet();
		Random random = new XorShiftStarRandom( 1 );
		
		for( int i = 1000; i-- != 0; ) {
			final long l = random.nextLong();
			longs.add( l );
			bloomFilter.add( Long.toBinaryString( l ) );
		}
		
		assertEquals( longs.size(), bloomFilter.size() );
	}
}
