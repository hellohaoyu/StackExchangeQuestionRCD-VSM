package analyzer;

//LoadXML with SAC
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class DocAnalyzer {

	//a list of stopwords
	HashSet<String> stopwords;
	
	//Store the Top 3 reviews(with the highest similarity)
	HashMap <String, Double> Top_Questions;
	
	
	HashMap <String, Integer> TTF;
	
	//Store the IDF of training set
	HashMap <String, Double> IDF;
	
	// Comparation doc's TF-IDF
	HashMap <String, Double> TF_IDF_Compare;

	
	// Define global tokenizer
	Tokenizer tokenizer;
	
	//Number of Docs
	int numberDocs;
	
	public DocAnalyzer() {

		Top_Questions = new HashMap <String, Double>();
		
		TTF =  new HashMap <String, Integer> ();

		IDF =  new HashMap <String, Double> ();
		
		TF_IDF_Compare = new HashMap<String, Double>();
		
		try {
		tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("/Users/haoyuchen/Dropbox/2015-Spring/Text-Mining/Project/StackExchangeQuestionRCD-VSM/Model/en-token.bin")));
		}
		catch (IOException e) {
			  e.printStackTrace();
			}
	}
	

public double cosineSimilarity( HashMap <String, Double> TF_IDF_A, HashMap <String, Double> TF_IDF_B ){
	double similarity;
	
	double muplicationVector_AB = 0;
	double Length_A = 0;
	double Length_B = 0;
	
	for (HashMap.Entry<String, Double> entry : TF_IDF_A.entrySet()) {
	    //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
	    if(TF_IDF_B.containsKey(entry.getKey())){
	    	// Muplication of Vector A and B
	    	muplicationVector_AB = muplicationVector_AB + TF_IDF_B.get(entry.getKey())*TF_IDF_A.get(entry.getKey()) ;
	    }
	    Length_A = Length_A + entry.getValue()*entry.getValue();
	}
	// Vector A's distance
	Length_A = Math.sqrt(Length_A);
	
	for (HashMap.Entry<String, Double> entry : TF_IDF_B.entrySet()) {
		Length_B = Length_B + entry.getValue()*entry.getValue();
	}
	
	// Vector B's distance
	Length_B = Math.sqrt(Length_B);
	
	similarity = muplicationVector_AB/(Length_B * Length_A);
	
	return similarity;
	
}

public  HashMap <String, Double>  getTD_IDF( HashMap <String, Integer> oneDocTF ){
	
	//Vector for a single doc
	HashMap <String, Double> TF_IDF_Doc = new HashMap<String, Double>();
	
	for (HashMap.Entry<String, Integer> entry : oneDocTF.entrySet()) {
	    //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
	    if(oneDocTF.containsKey(entry.getKey())){
	    	// Apply Sub-linear TF scaling
	    	double scaled_TF = 1 + Math.log(entry.getValue());
	    	// Apply TF-IDF
	    	double idf_value = scaled_TF * oneDocTF.get(entry.getKey());
	    	TF_IDF_Doc.put(entry.getKey(), idf_value);
	    }
		
//		if(m_stats.containsKey(entry.getKey())){
//	    	m_stats.put(entry.getKey(), (m_stats.get(entry.getKey())+ 1));	
//	    	//m_stats_TTF.put(entry.getKey(), (m_stats_TTF.get(entry.getKey())+ entry.getValue()));
//	    } else {
//	    	m_stats.put(entry.getKey(), 1);	
//	    	//m_stats_TTF.put(entry.getKey(), entry.getValue()); 	
//	    }
	 }
		
	return TF_IDF_Doc;
}
	
//Return DF without scaling
	public HashMap <String, Integer> preprocessing(String text) {		
			/**
			 * HINT: instead of constructing the Tokenizer instance every time when you perform tokenization,
			 * construct a global Tokenizer instance once and evoke it everytime when you perform tokenization.
			 */
			// Improvement: put Tokenizer as a global variable
		    //Tokenizer tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("./data/Model/en-token.bin")));
		    HashMap <String, Integer> oneDocTF = new HashMap<String, Integer>();	
		    
		    //Pre-processing Step 1: Tokenization
		    String last_token = "";
		    String bigram = "";
		    System.out.println(text);
			for(String token:tokenizer.tokenize(text)){
				//Pre-processing Step 2: Normalization
				token = Normalization(token);
				
				// Eliminate empty string
				if (token.length() == 0){
					continue;
				}
				//Pre-processing Step 3: Stemming
				token = SnowballStemming(token);

				if (stopwords.contains(token)) {
					continue;
				}

				
				
				
				//Update statics for a doc, which would apply for Term Frequency (TF)
				if( oneDocTF.containsKey(token)){
					oneDocTF.put(token, (oneDocTF.get(token)+ 1));
			    } else {
			    	oneDocTF.put(token,  1);
			    }
				
				if( TTF.containsKey(token)){
					TTF.put(token, (TTF.get(token)+ 1));
			    } else {
			    	TTF.put(token,  1);
			    }
				
				// Add bi-gram to control library
				bigram = last_token +"-"+ token;
				
				if( oneDocTF.containsKey(bigram)){
					oneDocTF.put(bigram, (oneDocTF.get(bigram)+ 1));
			    } else {
			    	oneDocTF.put(bigram,  1);
			    }
				
				if( TTF.containsKey(bigram)){
					TTF.put(bigram, (TTF.get(bigram)+ 1));
			    } else {
			    	TTF.put(bigram,  1);
			    }
				
				last_token = token;		
			
			}
			
			// Process Document Frequency
			for (HashMap.Entry<String, Integer> entry : oneDocTF.entrySet()) {
			    if(IDF.containsKey(entry.getKey())){
			    	IDF.put(entry.getKey(), IDF.get(entry.getKey())+ 1);
			    }
			 }
			
			
//			HashMap <String, Double> TD_IDF = getTD_IDF(doc_m_stats);
			
         return oneDocTF;
			
	}
	
      public void LoadStopwords(String filename) {
		
		stopwords = new HashSet<String>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			String line;

			while ((line = reader.readLine()) != null) {
				//it is very important that you perform the same processing operation to the loaded stopwords
				//otherwise it won't be matched in the text content
				line = SnowballStemming(Normalization(line));
				if (!line.isEmpty())
					stopwords.add(line);
			}
			reader.close();
			System.out.format("Loading %d stopwords from %s\n", stopwords.size(), filename);
		} catch(IOException e){
			System.err.format("[Error]Failed to open file %s!!", filename);
		}
	}
	
	//sample code for demonstrating how to perform text normalization
	public String Normalization(String token) {
		// convert to lower case
		token = token.toLowerCase();
		token = token.replaceAll("\\d+star(s)?", "star");// rating by stars
		// Some scales and measures
		token = token.replaceAll("\\d+(oz|lb|lbs|cent|inch|piec)", "SCALE");
		// convert some of the dates/times formats
		token = token.replaceAll("\\d{2}(:\\d{2})?(\\s)?(a|p)m", "TIME"); // 12 hours format
		token = token.replaceAll("\\d{2}:\\d{2}", "TIME"); // 24 hours format
		token = token.replaceAll("\\d{1,2}(th|nd|st|rd)", "DATE");// 1st 2nd 3rd 4th date format
		// convert numbers
		token = token.replaceAll("\\d+.\\d+", "NUM");		
		token = token.replaceAll("\\d+(ish)?", "NUM");
		// tested on "a 123 b 3123 c 235.123 d 0 e 0.3 f 213.231.1321 g +123 h -123.123"
		// remove punctuations
		token = token.replaceAll("\\p{Punct}", ""); 
		//tested on this string:  "This., -/ is #! an <>|~!@#$%^&*()_-+=}{[]\"':;?/>.<, $ % ^ & * example ;: {} of a = -_ string with `~)() punctuation" 
		return token;
	}
	
	//sample code for demonstrating how to use Snowball stemmer
	public String SnowballStemming(String token) {
		SnowballStemmer stemmer = new englishStemmer();
		stemmer.setCurrent(token);
		if (stemmer.stem())
			return stemmer.getCurrent();
		else
			return token;
	}
	
	
	public class SACHandler extends DefaultHandler {
		public void startElement(String nameSpaceURI, String localName, String qName, Attributes atts){

			String title = atts.getValue("Title");
			//String title = atts.getValue("id");
			if(title != null){
				preprocessing(title);
				numberDocs++;
			}
		    
//			preprocessing(atts.getValue("Title"));
			
			
		}
		
		public void endDocument(){
			
			for (HashMap.Entry<String, Double> entry : IDF.entrySet()) {
			  IDF.put(entry.getKey(), 1 + Math.log(numberDocs/entry.getValue()));  
			 }
		   	
		}
	}
	
	
	
	
	public class SACHandler_Similar extends DefaultHandler {
		public void startElement(String nameSpaceURI, String localName, String qName, Attributes atts){
			double similarity = 0;
			HashMap <String, Double> TF_IDF = new HashMap <String, Double>(); 
			
			String title = atts.getValue("Title");
			if(title == null){
				return;
			}
			TF_IDF =getTD_IDF(preprocessing(title));
			similarity = cosineSimilarity(TF_IDF, TF_IDF_Compare);
			// assign review to it as initialization
			String minsimilarityPost = title;
			double minsimilarity = 0; 
			
			// only when it has more than 3 message than I delete one message.
			if(Top_Questions.size() >= 3){
				for (HashMap.Entry<String, Double> entry : Top_Questions.entrySet()) {
					if(minsimilarity ==0){
						minsimilarity = entry.getValue();
						minsimilarityPost = entry.getKey();
					} else if(minsimilarity > entry.getValue() ){
						minsimilarity = entry.getValue();
						minsimilarityPost = entry.getKey();
					}		 
				}
			
				if(minsimilarity < similarity){
					System.out.println("Delete  :" + minsimilarity + "Add :" + similarity );
					Top_Questions.remove(minsimilarityPost);
					Top_Questions.put(title, similarity);
				} 	
			} else {
				Top_Questions.put(title, similarity);
				System.out.println("Empty + Add :" + similarity );
			}			
			
		}
		
		public void endDocument(){
		
			
	}
	}
	
	public void LoadXML(String filename) throws SAXException, IOException {
		XMLReader p = XMLReaderFactory.createXMLReader();
		p.setContentHandler(new SACHandler());	
		//p.parse("/StackOverflow/stackoverflow.com-Posts/Posts.xml");
		p.parse(filename);
		
	}
	
	public void LoadXML_Similar(String filename) throws SAXException, IOException {
		XMLReader p = XMLReaderFactory.createXMLReader();
		p.setContentHandler(new SACHandler_Similar());	
		//p.parse("/StackOverflow/stackoverflow.com-Posts/Posts.xml");
		p.parse(filename);
		
	}
	
	public void searchSimiarQuestions(String filename) throws SAXException, IOException {	
		Scanner scanner = new Scanner(System.in);
		String question = scanner.nextLine();
		
		
	     TF_IDF_Compare = getTD_IDF(preprocessing(question));
	     LoadXML_Similar(filename);
			//LoadDirectory("./data/samples/test", ".json");
			
	        System.out.println("**********The most three similar reviews for review***************************:");
			System.out.println("Review Content :  " + question );
			System.out.println();
			for (HashMap.Entry<String, Double> entry : Top_Questions.entrySet()) {
				System.out.println("**************************************");
				System.out.println("Similar Question :  " + entry.getKey());
			}
			
			Top_Questions.clear();
			TF_IDF_Compare.clear();
			// just run one reviews for test!
			//break;
		//printHash_TTF_DF();
		scanner.close();
}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//String filename = "/Users/haoyuchen/GoogleDrive/Text-Mining-Project/Data/stackoverflow.com-Tags/Tags.xml";
		String filename = "/Users/haoyuchen/GoogleDrive/Text-Mining-Project/Data/stackoverflow.com-Post/FakePost.xml";

		
		DocAnalyzer analyzer = new DocAnalyzer();
		
		analyzer.LoadStopwords("/Users/haoyuchen/Dropbox/2015-Spring/Text-Mining/Project/StackExchangeQuestionRCD-VSM/english.stop");
		
		try {
			analyzer.LoadXML(filename);
			analyzer.searchSimiarQuestions(filename);
			//analyzer.LoadXML_Similar(filename);
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		
		
		
		
	}

}

