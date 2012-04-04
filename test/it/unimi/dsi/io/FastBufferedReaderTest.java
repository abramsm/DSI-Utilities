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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import it.unimi.dsi.lang.MutableString;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

public class FastBufferedReaderTest {

	@Test
	public void testToSpec() {
		String className = FastBufferedReader.class.getName();
		assertEquals( className, new FastBufferedReader().toSpec() );
		assertEquals( className + "(100)", new FastBufferedReader( 100 ).toSpec() );
		assertEquals( className + "(\"_\")", new FastBufferedReader( "_" ).toSpec() );
		assertEquals( className + "(100,\"_\")", new FastBufferedReader( "100", "_" ).toSpec() );
	}

	@Test
	public void testBufferZero() throws IOException {
		String string = "test\ntest\n";
		FastBufferedReader fbr = new FastBufferedReader( 1 );
		fbr.setReader( new StringReader( string ) );
		assertEquals( new MutableString( "test" ), fbr.readLine( new MutableString() ) );
		assertEquals( 't', fbr.read() );
		assertEquals( 'e', fbr.read() );
		assertEquals( 's', fbr.read() );
		assertEquals( 't', fbr.read() );
		assertEquals( '\n', fbr.read() );
	}
	
	@Test 
	public void testRandom() throws IOException {
		File file = File.createTempFile( FastBufferedReaderTest.class.getSimpleName(), "tmp" );
		String s;
		file.deleteOnExit();
		MutableString ms = new MutableString(), ms2;
		int l = (int)file.length();
		FastBufferedReader br = new FastBufferedReader( new FileReader( file ), 2 );
		BufferedReader test = new BufferedReader( new FileReader( file ) );
		int a1, a2, off, len, pos;
		java.util.Random r = new java.util.Random( 0 );
		
		// Create temp file
		FastBufferedOutputStream fos = new FastBufferedOutputStream( new FileOutputStream( file ) );
		for( int i = 1000000; i-- !=0; ) {
			final int c = r.nextInt( 100 );
			if ( c <= 1 ) {
				if ( c == 0 ) fos.write( '\r' );
				fos.write( '\n' );
			}
			else fos.write( r.nextInt( 95 ) + 32 );
		}
		fos.close();
		
		char b1[] = new char[ 32768 ];
		char b2[] = new char[ 32768 ];

		for( int t = 1000000; t-- != 0; ) {

			switch( r.nextInt( 4 ) ) {

			case 0:
				//System.err.println("read()");
				a1 = br.read();
				a2 = test.read();
				assertTrue( "Buffered read() returned " + a1 + " instead of " + a2 ,  a1 == a2 );
				if ( a1 == -1 ) return;
				break;

			case 1:
				off = r.nextInt( b1.length );
				len = r.nextInt( b1.length - off + 1 );
				a1 = br.read( b1, off, len );
				a2 = test.read( b2, off, len );
				//System.err.println("read(b, " + off + ", " + len + ")");

				assertTrue( "Buffered read(b, " + off + ", " + len + ") returned " + a1 + " instead of " + a2 ,  a1 == a2 );

				for( int i = off; i < off + len; i++ ) assertTrue( "Buffered read(b, " + off + ", " + len + ") has a mismatch at position " + i ,  b1[ i ] == b2[ i ] );
				break;

			case 2:
				pos = r.nextInt( l/2 + 1 );
				a1 = (int)br.skip( pos );
				a2 = (int)test.skip( pos );
				//System.err.println("skip(" + pos + ")" );
				assertTrue( "skip() returned " + a1 + " instead of " + a2 ,  a1 == a2 );
				break;

			case 3:
				ms2 = br.readLine( ms );
				s = test.readLine();
				//System.err.println( "readLine()" );
				assertTrue( "readLine() returned " + ms + " instead of " + s ,  s == null && ms2 == null || ms.equals( s ) );

			}
			
		}
		
		file.delete();

	}
}
