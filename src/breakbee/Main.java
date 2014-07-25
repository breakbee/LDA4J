package breakbee;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

	public static void main(String[] args) throws IOException {
		
		ArrayList<String[]> corpus = new ArrayList<String[]>();
		
		String filename = "test.txt";
		int k = 2;
		int iter = 100;
		
		String summaryPath = "summary.txt";
		
		String pathTheta = "theta.csv";
		String pathPhi = "phi.csv";
		
		if(args.length == 0){
			System.out.println("Please input file name!");
			//return;
		}
		
		if(args.length > 0){
			filename = args[0];
		}
		
		if(args.length > 1){
			k = Integer.parseInt(args[1]);
		}
		
		if(args.length > 2){
			iter = Integer.parseInt(args[2]);
		}
		
		if(args.length > 3){
			summaryPath = args[3];
		}
		
		if(args.length > 4){
			pathTheta = args[4];
		}
		
		if(args.length > 5){
			pathPhi = args[5];
		}
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
		String line = null;
		while((line=reader.readLine())!=null){
			String[] words = line.split(" ");
			corpus.add(words);
		}
		reader.close();
		
		LDA lda = new LDA(corpus, k);
		lda.fit(iter, true);
		lda.summarize(0.1, 10, summaryPath);
		lda.saveParameters(pathTheta, pathPhi);
		System.out.println("LDA completed.");
	}
}
