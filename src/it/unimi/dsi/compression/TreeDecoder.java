package it.unimi.dsi.compression;

/*		 
 * DSI utilities
 *
 * Copyright (C) 2005-2012 Sebastiano Vigna 
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

import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.fastutil.booleans.BooleanIterator;
import it.unimi.dsi.io.InputBitStream;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

/** A decoder that follows 0/1 labelled paths in a tree.
 * 
 *  <p>Additional, the {@link #buildCodes()} method returns a vector
 *  of codewords corresponding to the paths of an instance of this class. Conversely,
 *  the {@linkplain #TreeDecoder(BitVector[], int[]) codeword-based constructor} builds
 *  a tree out of the codewords generated by root-to-leaf paths.
 */

public final class TreeDecoder implements Decoder, Serializable {
	private static final long serialVersionUID = 2L;
	private static final boolean DEBUG = false;

	/** A internal node of the decoding tree. */
	public static class Node implements Serializable {
		private static final long serialVersionUID = 1L;
		public Node left, right;
	}

	/** A leaf node of the decoding tree. */
	public static class LeafNode extends Node {
		private static final long serialVersionUID = 1L;
		public final int symbol;
		
		/** Creates a leaf node. 
		 * @param symbol the symbol for this node.
		 */
		public LeafNode( final int symbol ) {
			this.symbol = symbol;
		}
	}
	
	/** The root of the decoding tree. */
	private final Node root;
	/** The number of symbolds in this decoder. */
	private final int n;
	
	/** Creates a new codeword-based decoder using the given tree. It
	 * is responsability of the caller that the tree is well-formed,
	 * that is, that all internal nodes are instances of {@link TreeDecoder.Node}
	 * and all leaf nodes are instances of  {@link TreeDecoder.LeafNode}.
	 * 
	 * @param root the root of a decoding tree.
	 * @param n the number of leaves (symbols).
	 */
	public TreeDecoder( final Node root, final int n ) {
		this.root = root;
		this.n = n;
	}

	/** Creates a new codeword-based decoder starting from a set
	 * of complete, lexicographically ordered codewords. It
	 * is responsability of the caller that the tree is well-formed,
	 * that is, that the provided codewords are exactly the root-to-leaf
	 * paths of such a tree.
	 * 
	 * @param lexSortedCodeWord a vector of lexically sorted codeWords.
	 * @param symbol a mapping from codewords to symbols.
	 */
	public TreeDecoder( BitVector[] lexSortedCodeWord, int[] symbol ) {
		this( buildTree( lexSortedCodeWord, symbol, 0, 0, lexSortedCodeWord.length ), lexSortedCodeWord.length );
	}
	
	
	private static Node buildTree( BitVector lexSortedCodeWords[], final int[] symbol, int prefix, int offset, int length ) {
		if ( DEBUG ) {
			System.err.println( "****** " + offset + " " + length ); 
			System.err.println( Arrays.toString( lexSortedCodeWords ) );
			for( int i = 0; i < length; i++ ) {
				System.err.print( lexSortedCodeWords[ offset + i ].size() + "\t" );
				for( int j = 0; j < lexSortedCodeWords[ offset + i ].size(); j++ ) System.err.print( lexSortedCodeWords[ offset + i ].getBoolean( j ) ? 1 : 0 );
				System.err.println();
			}
		}
		
		if ( length == 1 ) return new LeafNode( symbol[ offset ] );
		for( int i = length - 1; i-- != 0; ) 
			if ( lexSortedCodeWords[ offset + i ].get( prefix ) != lexSortedCodeWords[ offset + i + 1 ].get( prefix ) ) {
				final Node node = new Node();
				node.left = buildTree( lexSortedCodeWords, symbol, prefix + 1, offset, i + 1 );
				node.right = buildTree( lexSortedCodeWords, symbol, prefix + 1, offset + i + 1, length - i - 1 );
				return node;
			}
		
		throw new IllegalStateException();
	}
	
	
	public int decode( final BooleanIterator iterator ) {
		Node n = root;
		while( ! ( n instanceof LeafNode ) ) 
			n = iterator.nextBoolean() ? n.right : n.left;
		return ((LeafNode)n).symbol;
	}
	
	public int decode( final InputBitStream ibs ) throws IOException {
		Node n = root;
		while( ! ( n instanceof LeafNode ) ) 
			n = ibs.readBit() == 0 ? n.left : n.right;
		return ((LeafNode)n).symbol;
	}

	/** Populates the codeword vector by scanning recursively 
	 * the decoding tree.
	 * 
	 * @param node a subtree of the decoding tree.
	 * @param prefix the path leading to <code>n</code>.
	 */
	private void buildCodes( final BitVector[] codeWord, final TreeDecoder.Node node, final BitVector prefix ) {

		if ( node instanceof TreeDecoder.LeafNode ) {
			codeWord[ ((TreeDecoder.LeafNode)node).symbol ] = prefix; 
			return;
		}
		
		BitVector bitVector = prefix.copy();
		bitVector.length( bitVector.length() + 1 );
		buildCodes( codeWord, node.left, bitVector );

		bitVector = prefix.copy();
		bitVector.length( bitVector.length() + 1 );
		bitVector.set( bitVector.size() - 1 );

		buildCodes( codeWord, node.right, bitVector );
	}
	
	/** Generate the codewords corresponding to this tree decoder.
	 * 
	 * @return a vector of codewords for this decoder.
	 */
	public BitVector[] buildCodes() {
		final BitVector[] codeWord = new BitVector[ n ];
		buildCodes( codeWord, root, LongArrayBitVector.getInstance() );
		return codeWord;
	}
}
