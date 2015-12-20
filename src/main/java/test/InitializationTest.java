/**
 *	@file InitializationTest.java
 *	@brief Test the \see InitializationDriver Job.
 *  @author Federico Conte (draxent)
 *  
 *	Copyright 2015 Federico Conte
 *	https://github.com/Draxent/ConnectedComponents
 * 
 *	Licensed under the Apache License, Version 2.0 (the "License"); 
 *	you may not use this file except in compliance with the License. 
 *	You may obtain a copy of the License at 
 * 
 *	http://www.apache.org/licenses/LICENSE-2.0 
 *  
 *	Unless required by applicable law or agreed to in writing, software 
 *	distributed under the License is distributed on an "AS IS" BASIS, 
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *	See the License for the specific language governing permissions and 
 *	limitations under the License. 
 */

package test;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import pad.InitializationDriver;
import test.TranslatorDriver.DirectionTranslation;

/**	Test the \see InitializationDriver Job. */
public class InitializationTest
{
	static FileSystem fs;
	static Path input;
	
	public static void exit( String suffix ) throws IllegalArgumentException, IOException
	{
		fs.delete( input.suffix( suffix ), true  );
		System.exit( 1 );
	}
	
	public static void main( String[] args ) throws Exception 
	{
		if ( args.length != 1 )
		{
			System.out.println( "Usage: InitializationTest <graph_input>" );
			System.exit(1);
		}
		fs = FileSystem.get( new Configuration() );
		input = new Path( FilenameUtils.removeExtension( args[0] ) + "_0" );
		
		System.out.println( "Start InitializationDriver. " );
		InitializationDriver init = new InitializationDriver( args[0], true );
		if ( init.run( null ) != 0 ) exit( "" );
		System.out.println( "End InitializationDriver." );
		
		System.out.println( "Start TranslatorDriver Pair2Text. " );
		TranslatorDriver trans = new TranslatorDriver( input, DirectionTranslation.Pair2Text );
		if ( trans.run( null ) != 0 ) exit( "_transl" );
		System.out.println( "End TranslatorDriver Pair2Text." );
		
		// Delete previous output and rename result
		fs.delete( input, true  );
		fs.rename( input.suffix( "_transl" ), input );

		System.exit( 0 );
	}
}