package it.unimi.dsi.io;

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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

public class InputBitStreamTest {

	@Test
	public void testReadAligned() throws IOException {
		byte[] a = { 1 }, A = new byte[ 1 ];
		new InputBitStream( a ).read( A, 8 );
		assertTrue( Arrays.toString( a ) + " != " + Arrays.toString( A ), Arrays.equals( a, A ) );
		byte[] b = { 1, 2 }, B = new byte[ 2 ];
		new InputBitStream( b ).read( B, 16 );
		assertTrue( Arrays.toString( b ) + " != " + Arrays.toString( B ), Arrays.equals( b, B ) );
		byte[] c = { 1, 2, 3 }, C = new byte[ 3 ];
		new InputBitStream( c ).read( C, 24 );
		assertTrue( Arrays.toString( c ) + " != " + Arrays.toString( C ), Arrays.equals( c, C ) );
	}
	
	@Test
	public void testOverflow() throws IOException {
		InputBitStream ibs = new InputBitStream( new byte[ 0 ] );
		ibs.readInt( 0 );
	}
}
