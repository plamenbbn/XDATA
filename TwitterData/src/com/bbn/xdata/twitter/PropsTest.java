package com.bbn.xdata.twitter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PropsTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {

		Properties props = new  Properties();
		
		props.setProperty( "my.test.prop", "12,000" );
		props.setProperty( "twitter.languages.accepted", "French" );
		
		props.load( new FileReader( "TwitterDataParser.props" ) );
		
		props.list( System.out );
		
	}

}
