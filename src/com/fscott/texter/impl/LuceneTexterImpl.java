package com.fscott.texter.impl;

import java.io.IOException;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;


// adapted from http://lucene.apache.org/core/7_5_0/demo/overview-summary.html#overview_description
public class LuceneTexterImpl {
    
	
//	void IndexFiles() {
//		
//		Date start = new Date();
//		try {
//		  System.out.println("Indexing to directory '" + indexPath + "'...");
//		
//		  Directory dir = FSDirectory.open(Paths.get(indexPath));
//		  Analyzer analyzer = new StandardAnalyzer();
//		  IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
//		
//		  if (create) {
//		    // Create a new index in the directory, removing any
//		    // previously indexed documents:
//		    iwc.setOpenMode(OpenMode.CREATE);
//		  } else {
//		    // Add new documents to an existing index:
//		    iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
//		  }
//		
//		  // Optional: for better indexing performance, if you
//		  // are indexing many documents, increase the RAM
//		  // buffer.  But if you do this, increase the max heap
//		  // size to the JVM (eg add -Xmx512m or -Xmx1g):
//		  //
//		  // iwc.setRAMBufferSizeMB(256.0);
//		
//		  IndexWriter writer = new IndexWriter(dir, iwc);
//		  indexDocs(writer, docDir);
//		
//		  // NOTE: if you want to maximize search performance,
//		  // you can optionally call forceMerge here.  This can be
//		  // a terribly costly operation, so generally it's only
//		  // worth it when your index is relatively static (ie
//		  // you're done adding documents to it):
//		  //
//		  // writer.forceMerge(1);
//		
//		  writer.close();
//		
//		  Date end = new Date();
//		  System.out.println(end.getTime() - start.getTime() + " total milliseconds");
//		
//		} catch (IOException e) {
//		  System.out.println(" caught a " + e.getClass() +
//		   "\n with message: " + e.getMessage());
//		}
//	}
}