package it.unimi.dsi.util;

/*		 
 * DSI utilities
 *
 * Copyright (C) 2012 Sebastiano Vigna 
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

import it.unimi.dsi.fastutil.booleans.BooleanArrays;
import it.unimi.dsi.fastutil.longs.AbstractLongBigList;
import it.unimi.dsi.fastutil.longs.LongBigList;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;


/** A bridge between byte {@linkplain ByteBuffer buffers} and {@linkplain LongBigList long big lists}.
 * 
 * <p>Java's {@linkplain FileChannel#map(MapMode, long, long) memory-mapping facilities} have
 * the severe limitation of mapping at most {@link Integer#MAX_VALUE} bytes, as they
 * expose the content of a file using a {@link MappedByteBuffer}. This class can {@linkplain  #map(FileChannel, ByteOrder, MapMode) expose
 * a file of longs of arbitrary length} as a {@linkplain LongBigList}
 * that is actually based on an array of {@link MappedByteBuffer}s, each mapping
 * a <em>chunk</em> of {@link #CHUNK_SIZE} longs.
 * 
 * @author Sebastiano Vigna
 */

public class ByteBufferLongBigList extends AbstractLongBigList {
	private static int CHUNK_SHIFT = 27;
	
	/** The size in longs of a chunk created by {@link #map(FileChannel, ByteOrder, MapMode)}. */
	public static final long CHUNK_SIZE = 1L << CHUNK_SHIFT;

	/** The mask used to compute the offset in the chunk in longs. */
	private static final long CHUNK_MASK = CHUNK_SIZE - 1;

	/** The underlying byte buffers. */
	private final ByteBuffer[] byteBuffer;

	/** An array parallel to {@link #byteBuffer} specifying which buffers do not need to be 
	 * {@linkplain ByteBuffer#duplicate() duplicated} before being used. */
	private final boolean[] readyToUse;

	/** The number of byte buffers. */
	private final int n;
	
	/** The overall size in longs. */
	private final long size;

	/** Creates a new byte-buffer long big list from a single {@link ByteBuffer}.
	 * 
	 * @param byteBuffer the underlying byte buffer.
	 */
	
	public ByteBufferLongBigList( final ByteBuffer byteBuffer ) {
		this( new ByteBuffer[] { byteBuffer }, byteBuffer.capacity(), new boolean[ 1 ] );
	}

	/** Creates a new byte-buffer long big list.
	 * 
	 * @param byteBuffer the underlying byte buffers.
	 * @param size the sum of the {@linkplain ByteBuffer#capacity() capacities} of the byte buffers.
	 * @param readyToUse an array parallel to <code>byteBuffer</code> specifying which buffers do not need to be 
	 * {@linkplain ByteBuffer#duplicate() duplicated} before being used (the process will happen lazily); the array
	 * will be used internally by the newly created byte-buffer long big list.
	 */
	
	protected ByteBufferLongBigList( final ByteBuffer[] byteBuffer, final long size, final boolean[] readyToUse ) {
		this.byteBuffer = byteBuffer;
		this.n = byteBuffer.length;
		this.size = size;
		this.readyToUse = readyToUse;

		for( int i = 0; i < n; i++ ) if ( i < n - 1 && byteBuffer[ i ].capacity() / 8 != CHUNK_SIZE ) throw new IllegalArgumentException();
	}

	/** Creates a new byte-buffer long big list by mapping a given file channel.
	 * 
	 * @param fileChannel the file channel that will be mapped.
	 * @param byteOrder a prescribed byte order.
	 * @param mapMode this must be {@link MapMode#READ_ONLY}.
	 * @return  a new byte-buffer long big list over the contents of <code>fileChannel</code>.
	 * @throws IOException
	 */
	
	public static ByteBufferLongBigList map( final FileChannel fileChannel, final ByteOrder byteOrder, final MapMode mapMode ) throws IOException {
		final long size = fileChannel.size() / 8;
		final int chunks = (int)( ( size + ( CHUNK_SIZE - 1 ) ) / CHUNK_SIZE );
		final ByteBuffer[] byteBuffer = new ByteBuffer[ chunks ];
		for( int i = 0; i < chunks; i++ ) byteBuffer[ i ] = fileChannel.map( mapMode, i * CHUNK_SIZE * 8, Math.min( CHUNK_SIZE, size - i * CHUNK_SIZE ) * 8 ).order( byteOrder );
		final boolean[] readyToUse = new boolean[ chunks ];
		BooleanArrays.fill( readyToUse, true );
		return new ByteBufferLongBigList( byteBuffer, size, readyToUse );
	}

	private ByteBuffer byteBuffer( final int n ) {
		if ( readyToUse[ n ] ) return byteBuffer[ n ];
		readyToUse[ n ] = true;
		return byteBuffer[ n ] = byteBuffer[ n ].duplicate().order( byteBuffer[ n ].order() );
	}	
	
	public ByteBufferLongBigList copy() {
		return new ByteBufferLongBigList( byteBuffer.clone(), size, new boolean[ n ] );
	}

	@Override
	public long getLong( final long index ) {
		return byteBuffer( (int)( index >>> CHUNK_SHIFT ) ).getLong( (int)( index & CHUNK_MASK ) << 3 );
	}

	@Override
	public long size64() {
		return size;
	}
}
