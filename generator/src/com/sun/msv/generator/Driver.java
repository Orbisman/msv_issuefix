/*
 * @(#)$Id$
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tranquilo.generator;

import javax.xml.parsers.*;
import java.io.*;
import org.xml.sax.*;
import java.util.*;
import com.sun.tranquilo.grammar.trex.*;
import com.sun.tranquilo.grammar.relax.*;
import com.sun.tranquilo.grammar.*;
import com.sun.tranquilo.relaxns.grammar.RELAXGrammar;
import com.sun.tranquilo.driver.textui.DebugController;
import com.sun.tranquilo.reader.util.GrammarLoader;
import org.apache.xml.serialize.*;

/**
 * command line driver.
 * 
 * @author <a href="mailto:kohsuke.kawaguchi@eng.sun.com">Kohsuke KAWAGUCHI</a>
 */
public class Driver
{
	protected void usage()
	{
		System.out.println(
			"Sun XMLGenerator\n"+
			"----------------\n"+
			"Usage: XMLGenerator <options> <schema> [<output name>]\n"+
			"Options:\n"+
			"  -relax    : assume RELAX as a schema\n"+
			"  -trex     : assume TREX as a schema\n"+
			"  -ascii    : use ASCII character only\n"+
			"  -seed <n> : set random seed\n"+
			"  -depth <n>: set cut back depth\n"+
			"  -width <n>: maximum number of times '*'/'+' are repeated\n" +
			"  -n <n>    : # of files to be generated\n" +
			"  -encoding <str>: output encoding (Java name)\n"+
			"  -error <n>/<m>: error ratio. generate n errors per m elemnts (average).\n"+
			"                  to control error generation, see manual for details.\n"+
			"  -nocomment: suppress insertion of comments that indicate generated errors.\n"+
			"\n"+
			"  <output name> must include one '$'. '$' will be replaced by number.\n"+
			"  e.g., test.$.xml -> test.1.xml test.2.xml test.3.xml ...\n"+
			"  if omitted, generated file will be sent to stdout.\n"
			);
	}

	public static void main( String[] args ) throws Exception
	{
		new Driver().run(args);
	}
	
	protected double getRatio( String s )
	{
		int idx = s.indexOf('/');
		double n = Double.parseDouble(s.substring(0,idx));
		double m = Double.parseDouble(s.substring(idx+1));
						
		double ratio = n/m;
						
		if( ratio<=0 || ratio>1 )
		{
			System.out.println("error ratio out of range");
			usage();
			System.exit(-1);
		}
		return ratio;
	}
	
	protected void run( String[] args ) throws Exception
	{
		String grammarName=null;
		String outputName=null;
		String encoding="UTF-8";
		
		int number = 1;

		GeneratorOption opt = new GeneratorOption();
		opt.random = new Random();
		
		opt.dtGenerator = new DataTypeGeneratorImpl();

		// parse options
		//===========================================
		try
		{
			for( int i=0; i<args.length; i++ )
			{
				if( args[i].equalsIgnoreCase("-ascii") )
					((DataTypeGeneratorImpl)opt.dtGenerator).asciiOnly = true;
				else
				if( args[i].equalsIgnoreCase("-nocomment") )
					opt.insertComment = false;
				else
				if( args[i].equalsIgnoreCase("-depth") )
					opt.cutBackDepth = new Integer(args[++i]).intValue();
				else
				if( args[i].equalsIgnoreCase("-width") )
					opt.width = new Rand.UniformRand( opt.random, new Integer(args[++i]).intValue() );
				else
				if( args[i].equalsIgnoreCase("-n") )
				{
					number = new Integer(args[++i]).intValue();
					if( number<1 )	number=1;
				}
				else
				if( args[i].equalsIgnoreCase("-encoding") )
					encoding = args[++i];
				else
				if( args[i].equalsIgnoreCase("-seed") )
					opt.random.setSeed( new Long(args[++i]).longValue() );
				else
				if( args[i].equalsIgnoreCase("-error") )
				{
					opt.probGreedyChoiceError=
					opt.probMissingAttrError=
					opt.probMissingElemError=
					opt.probMutatedAttrError=
					opt.probMutatedElemError=
					opt.probSeqError=
					opt.probSlipInAttrError=
					opt.probSlipInElemError=
					opt.probMissingPlus=
					opt.probAttrNameTypo=
					opt.probElemNameTypo=
						getRatio(args[++i]);
				}
				else
				if( args[i].equalsIgnoreCase("-error-greedyChoice") )
					opt.probGreedyChoiceError	= getRatio(args[++i]);
				else
				if( args[i].equalsIgnoreCase("-error-missingAttribute") )
					opt.probMissingAttrError	= getRatio(args[++i]);
				else
				if( args[i].equalsIgnoreCase("-error-missingElement") )
					opt.probMissingElemError	= getRatio(args[++i]);
				else
				if( args[i].equalsIgnoreCase("-error-mutatedAttribute") )
					opt.probMutatedAttrError	= getRatio(args[++i]);
				else
				if( args[i].equalsIgnoreCase("-error-mutatedElement") )
					opt.probMutatedElemError	= getRatio(args[++i]);
				else
				if( args[i].equalsIgnoreCase("-error-sequenceError") )
					opt.probSeqError			= getRatio(args[++i]);
				else
				if( args[i].equalsIgnoreCase("-error-slipInAttribute") )
					opt.probSlipInAttrError		= getRatio(args[++i]);
				else
				if( args[i].equalsIgnoreCase("-error-slipInElement") )
					opt.probSlipInElemError		= getRatio(args[++i]);
				else
				if( args[i].equalsIgnoreCase("-error-missingPlus") )
					opt.probMissingPlus			= getRatio(args[++i]);
				else
				if( args[i].equalsIgnoreCase("-error-attributeNameTypo") )
					opt.probAttrNameTypo		= getRatio(args[++i]);
				else
				if( args[i].equalsIgnoreCase("-error-attributeNameTypo") )
						opt.probElemNameTypo	= getRatio(args[++i]);
				else
				{
					if( args[i].charAt(0)=='-' )
					{
						System.err.println("unrecognized option :" + args[i]);
						usage();
						return;
					}
					
					if( grammarName==null )	grammarName = args[i];
					else
					if( outputName==null ) outputName = args[i];
					else
					{
						System.err.println("too many parameters");
						usage();
						return;
					}
				}
			}
		}
		catch(Exception e)
		{
			usage();
			return;
		}
		
		if( grammarName==null )
		{
			usage();
			return;
		}
		
		
			
		
		SAXParserFactory factory = new org.apache.xerces.jaxp.SAXParserFactoryImpl();
		DocumentBuilderFactory domFactory = new org.apache.xerces.jaxp.DocumentBuilderFactoryImpl();
	
		factory.setNamespaceAware(true);
		factory.setValidating(false);
		
		try
		{
			factory.setFeature("http://xml.org/sax/features/validation",false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
		}
		catch(Exception e) { ;	}
		
		Expression topLevel;
		
		System.err.println("parsing a grammar");
		
		
		// load a schema
		//===========================================
		Grammar grammar = GrammarLoader.loadSchema(
				grammarName, new DebugController(false), factory );
		
		topLevel = grammar.getTopLevel();
		
		if( grammar instanceof RELAXGrammar
		||  grammar instanceof RELAXModule )
			topLevel = topLevel.visit( new NoneTypeRemover(grammar.getPool()) );
	
		topLevel = topLevel.visit( new RefExpRemover(grammar.getPool()) );
		opt.pool = (TREXPatternPool)grammar.getPool();
		
		// generate instances
		//===========================================
		for( int i=0; i<number; i++ )
		{
			if( number<10 )		System.err.println("generating a document #"+(i+1));
			else				System.err.print('.');
			
			org.w3c.dom.Document dom = domFactory.newDocumentBuilder().newDocument();
			
			Generator.generate(topLevel,dom,opt);
		
			// serialize it
			OutputStream os;
			if( outputName==null )		os = System.out;	// in case no output file name is specified
			else
			{
				int idx = outputName.indexOf('$');
				if( idx!=-1 )
				{
					String s = Integer.toString(i);
					for( int j=s.length(); j<Integer.toString(number-1).length(); j++ )
						s = "0"+s;
					
					os = new FileOutputStream( outputName.substring(0,idx)+s+outputName.substring(idx+1) );
				}
				else
					os = new FileOutputStream( outputName );
			}
			
			// write generated instance.
			XMLSerializer2 s = new XMLSerializer2( os, new OutputFormat("XML",encoding,true) );
			s.serialize(dom);
			
			if( os!=System.out )	os.close();
			else
			{
				System.in.read();
			}
		}
	}
	
	private static InputSource getInputSource( String fileOrURL )
	{
		try
		{// try it as a file
			InputSource is = new InputSource(new java.io.FileInputStream(fileOrURL));
			is.setSystemId(new File(fileOrURL).getCanonicalPath());
			return is;
		}
		catch( Exception e )
		{// try it as an URL
			return new InputSource(fileOrURL);
		}
	}
}
