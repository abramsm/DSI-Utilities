package it.unimi.dsi.io;

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

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

import org.junit.Test;

public class OutputBitStreamTest {

	@Test
	public void testPositionWrapped() throws IOException {
		final byte[] a = new byte[ 2 ];
		OutputBitStream obs = new OutputBitStream( a );
		obs.position( 8 );
		obs.writeInt( 1, 8 );
		assertArrayEquals( new byte[] { 0, 1 }, a );
		obs.position( 0 );
		obs.writeInt( 1, 1 );
		obs.flush();
		assertArrayEquals( new byte[] { -128, 1 }, a );
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPositionUnaligned() throws IOException {
		final byte[] a = new byte[ 2 ];
		OutputBitStream obs = new OutputBitStream( a );
		obs.position( 1 );
	}
}
