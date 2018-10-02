package com.fscott.texter.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import com.fscott.texter.api.Texter;
import com.fscott.texter.model.Result;
import com.fscott.texter.util.TexterUtils;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;


// adapted from http://lucene.apache.org/core/7_5_0/demo/overview-summary.html#overview_description
public class LuceneTexterImpl implements Texter<IndexSearcher,Query>{

    private Path indexPath = null;
    private boolean brandNewIndex = true;
    
    public LuceneTexterImpl(final Path indexPath, final boolean brandNewIndex) {
        if (!indexPath.toFile().exists()) {
            Preconditions.checkArgument(indexPath.toFile().mkdirs(), "index directory must exist");
        }
        this.indexPath = indexPath;
        this.brandNewIndex = brandNewIndex;
    }

    @Override
    public void prepareDocs(final Path documentDir, boolean doPreProcess)
            throws FileNotFoundException, IOException {

        Preconditions.checkArgument(documentDir.toFile().exists(), "documentDir must exist");
        
        try {
          System.out.println("Indexing to directory '" + indexPath + "'...");
      
          Directory dir = FSDirectory.open(indexPath);
          Analyzer analyzer = new StandardAnalyzer();
          IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
      
          if (this.brandNewIndex) {
            // Create a new index in the directory, removing any
            // previously indexed documents:
            iwc.setOpenMode(OpenMode.CREATE);
          } else {
            // Add new documents to an existing index:
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
          }
      
          // Optional: for better indexing performance, if you
          // are indexing many documents, increase the RAM
          // buffer.  But if you do this, increase the max heap
          // size to the JVM (eg add -Xmx512m or -Xmx1g):
          //
          // iwc.setRAMBufferSizeMB(256.0);
      
          IndexWriter writer = new IndexWriter(dir, iwc);
          indexDocs(writer, documentDir);
      
          // NOTE: if you want to maximize search performance,
          // you can optionally call forceMerge here.  This can be
          // a terribly costly operation, so generally it's only
          // worth it when your index is relatively static (ie
          // you're done adding documents to it):
          //
          // writer.forceMerge(1);
      
          writer.close();
          dir.close();
      
        } catch (IOException e) {
          System.out.println(" caught a " + e.getClass() +
          "\n with message: " + e.getMessage());
        }
    }

    @Override
    public void searchDocs(List<String> searchTerms) {
        Preconditions.checkNotNull(searchTerms, "searchTerms cannot be null");
        
        try (IndexReader reader = DirectoryReader.open(FSDirectory.open(this.indexPath))) {
        
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new StandardAnalyzer();
            
            final String searchField = "contents";
            final String statsField = "freqs";
            final String pathField = "path";
            
            AtomicInteger trial = new AtomicInteger(1);
            searchTerms.stream().forEach(searchTerm -> {
                
                System.out.println("(Trial " + trial.get() + ")The target is: " + searchTerm);
                trial.incrementAndGet();
                List<Result> results = new ArrayList<>();
                Query query = null;
                
                try {
                    query = new QueryParser(searchField, analyzer).parse(searchTerm);
                } catch (ParseException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                                
                Preconditions.checkNotNull(query, "query cannot be null");
                try {
                    TopDocs topDocs = searcher.search(query, reader.maxDoc());
                    // System.out.println(topDocs.totalHits);
                    ScoreDoc[] scoreDocs = topDocs.scoreDocs;
                    for (ScoreDoc d : scoreDocs) {
                        final String docName = searcher.doc(d.doc).get(pathField);  
                        int hits = 0;

                        Terms terms = reader.getTermVector(d.doc, statsField);
                        TermsEnum termsEnumIterator = terms.iterator();

                        if (termsEnumIterator.seekExact(new BytesRef(searchTerm))) {
                            PostingsEnum postings = termsEnumIterator.postings(null);
                            postings.nextDoc();
                            
                            hits = postings.freq();
                        }
                        results.add(Result.create(docName, new AtomicInteger(hits)));    
                    }
                    Collections.sort(results, Collections.reverseOrder());
                    TexterUtils.printResults(results);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });   
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    @Override
    public int getHits(IndexSearcher text, Query target) {
        Preconditions.checkNotNull(text, "text cannot be null");
        Preconditions.checkNotNull(target, "target cannot be null");
        
        return 0;
    }
    
    // only index files ending in .txt
    static void indexDocs(final IndexWriter writer, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        if (file.toFile().getName().endsWith(".txt"))
                            indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
                    } catch (IOException ignore) {
                        // don't index files that can't be read.
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            if (path.toFile().getName().endsWith(".txt"))
                indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
        }
    }

    /** Indexes a single document */
    static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            // make a new, empty document
            Document doc = new Document();
      
            // Add the path of the file as a field named "path".  Use a
            // field that is indexed (i.e. searchable), but don't tokenize 
            // the field into separate words and don't index term frequency
            // or positional information:
            Field pathField = new StringField("path", file.toString(), Field.Store.YES);
            doc.add(pathField);
              
            // Add the last modified date of the file a field named "modified".
            // Use a LongPoint that is indexed (i.e. efficiently filterable with
            // PointRangeQuery).  This indexes to milli-second resolution, which
            // is often too fine.  You could instead create a number based on
            // year/month/day/hour/minutes/seconds, down the resolution you require.
            // For example the long value 2011021714 would mean
            // February 17, 2011, 2-3 PM.
            doc.add(new LongPoint("modified", lastModified));
          
            // Add the contents of the file to a field named "contents".  Specify a Reader,
            // so that the text of the file is tokenized and indexed, but not stored.
            // Note that FileReader expects the file to be in UTF-8 encoding.
            // If that's not the case searching for special characters will fail.
            doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
          
            String text = com.google.common.io.Files.asCharSource(file.toFile(), Charsets.UTF_8).read();        
            FieldType myType = new FieldType();
            myType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
            myType.setStored(true);
            myType.setStoreTermVectors(true);
            myType.setTokenized(true);
            
            Field myfield = new Field("freqs", text, myType);
            
            doc.add(myfield);
            
            if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                // New index, so we just add the document (no old document can be there):
                System.out.println("adding " + file);
                writer.addDocument(doc);
            } else {
                // Existing index (an old copy of this document may have been indexed) so 
                // we use updateDocument instead to replace the old one matching the exact 
                // path, if present:
                System.out.println("updating " + file);
                writer.updateDocument(new Term("path", file.toString()), doc);
            }
        }
    }       
}