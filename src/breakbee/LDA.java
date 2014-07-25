package breakbee;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import static breakbee.Tuple.tuple;

public class LDA {

	public List<String[]> corpus;
	public List<Integer[]> topic;
	
	public HashSet<String> wordset;
	
	private Map<Tuple<String, Integer>, Integer> numWZ;
	private Map<Tuple<Integer, Integer>, Integer> numDZ;
	
	private Map<Integer, Integer> numZ;
	private Map<Integer, Integer> numD;
	
	public int k;
	public double alpha = 2;
	public double beta = 2;
	
	/**
	 * トピック分布のパラメータ
	 */
	private double[] theta;
	
	/**
	 * 単語分布のパラメータ
	 */
	private Map<Tuple<String, Integer>, Double> phi;
	private List<Map<String, Double>> phiZ;
		
	public LDA(List<String[]> corpus, int k){
		this.corpus = corpus;
		this.k = k;
		
		topic = new ArrayList<Integer[]>();
		for(String[] words : corpus){
			Integer[] tmp = new Integer[words.length];
			for(int i=0; i<words.length; i++){
				tmp[i] = 0;
			}
			topic.add(tmp);
		}
		
		wordset = new HashSet<>();
	}
	
	
	public void fit(int iteration){
		fit(iteration, false);
	}
	
	public void fit(int iteration, boolean debug){
		if(debug) System.out.println("Latent Dirichlet Allocation for Java\n");
		
		// 初期化
		if(debug) System.out.println("Initializing...");
		initialize();
		
		
		// ギブスサンプリング
		if(debug) System.out.println("Gibbs Sampling Start.");
		for(int i=0; i<iteration; i++){
			if(debug) System.out.print("\r" + "Iteration : " + i);
			sampling();
		}
		if(debug) System.out.println("\r" + "Iteration : finished");
		
		// パラメータの推定
		computeParameters();
	}
	
	/**
	 * パラメータの初期化を行う.
	 */
	private void initialize(){
		numWZ = new HashMap<>();
		numDZ = new HashMap<>();	
		
		numZ = new HashMap<Integer, Integer>();
		numD = new HashMap<Integer, Integer>();
		
		Random random = new Random();
		
		for(int d=0; d<corpus.size(); d++){
			String[] words = corpus.get(d);
			for(int i=0; i<words.length; i++){
				String word = words[i];
				wordset.add(word);
				
				int z = random.nextInt(k);
				
				topic.get(d)[i] = z;
				
				Tuple<String, Integer> wz = tuple(word, z);
				if(numWZ.containsKey(wz)){
					numWZ.put(wz, numWZ.get(wz) + 1);
				}else{
					numWZ.put(wz, 1);
				}
				
				Tuple<Integer, Integer> dz = tuple(d, z);
				if(numDZ.containsKey(dz)){
					numDZ.put(dz, numDZ.get(dz) + 1);
				}else{
					numDZ.put(dz, 1);
				}
				
				if(numD.containsKey(d)){
					numD.put(d, numD.get(d) + 1);
				}else{
					numD.put(d, 1);
				}
				
				if(numZ.containsKey(z)){
					numZ.put(z, numZ.get(z) + 1);
				}else{
					numZ.put(z, 1);
				}
			}			
		}
		
		theta = new double[k];
		for(int i=0; i<k; i++) theta[i]=0;
		phi = new HashMap<>();
		phiZ = new ArrayList<Map<String,Double>>();
	}
	
	private void sampling(){
		double sumAlpha = alpha * k;
		double sumBeta = beta * wordset.size();
		
		for(int d=0; d<corpus.size(); d++){
			String[] words = corpus.get(d);
			for(int i=0; i<words.length; i++){
				String word = words[i];
				
				// パラメータの削除
				int oldZ = topic.get(d)[i];
				Tuple<String, Integer> oldWZ = tuple(word, oldZ);
				numWZ.put(oldWZ, numWZ.get(oldWZ)-1);
				Tuple<Integer, Integer> oldDZ = tuple(d, oldZ);
				numDZ.put(oldDZ, numDZ.get(oldDZ)-1);
				numD.put(d, numD.get(d)-1);
				numZ.put(oldZ, numZ.get(oldZ)-1);
				
				// サンプリング
				// P(Z)を求める
				double[] probZ = new double[k];
				for(int z=0; z<k; z++){
					double numer1 = beta;
					Tuple<String, Integer> wz = tuple(word, z);
					if(numWZ.containsKey(wz)){
						numer1 += numWZ.get(wz);
					}
					double denom1 = numZ.get(z) + sumBeta;
					
					double numer2 = alpha;
					Tuple<Integer, Integer> dz = tuple(d, z);
					if(numDZ.containsKey(dz)){
						numer2 += numDZ.get(dz);
					}
					double denom2 = numD.get(d) + sumAlpha;
					
					probZ[z] = (double)(numer1*numer2)/(double)(denom1*denom2);
				}
				
				// 正規化
				double sumProbZ = 0;
				for(double pz : probZ) sumProbZ += pz;
				for(int z=0; z<k; z++) probZ[z] /= sumProbZ;
				
				// 新しいzのサンプリング
				int newZ = -1;
				double rv = Math.random();
				double tmp = 0;
				for(int z=0; z<k; z++){
					tmp += probZ[z];
					if(Double.compare(tmp, rv)>0){
						newZ = z;
						break;
					}
				}
				
				// パラメータの更新
				topic.get(d)[i] = newZ;
				Tuple<String, Integer> newWZ = tuple(word, newZ);
				if(numWZ.containsKey(newWZ)){
					numWZ.put(newWZ, numWZ.get(newWZ) + 1);
				}else{
					numWZ.put(newWZ, 1);
				}
				
				Tuple<Integer, Integer> newDZ = tuple(d, newZ);
				if(numDZ.containsKey(newDZ)){
					numDZ.put(newDZ, numDZ.get(newDZ) + 1);
				}else{
					numDZ.put(newDZ, 1);
				}
				numD.put(d, numD.get(d)+1);
				numZ.put(newZ, numZ.get(newZ)+1);
			}
		}
	}

	private void computeParameters(){
		// トピック分布のパラメータの計算
		double sumAlpha = alpha * k;
		int total = 0;
		for(Integer[] doc : topic){
			for(int z : doc){
				theta[z]+=1;
				total++;
			}
		}
		for(int z=0; z<k; z++) theta[z] = (theta[z] + alpha) / (total + sumAlpha);
		
		// 単語分布のパラメータの計算
		double sumBeta = beta * wordset.size();
		for(String word : wordset){
			for(int z=0; z<k; z++){
				double numer = beta;
				Tuple<String, Integer> wz = tuple(word, z);
				if(numWZ.containsKey(wz)){
					numer += numWZ.get(wz);
				}
				double denom = numZ.get(z) + sumBeta;
				phi.put(wz, (numer/denom));
			}
		}
	}
	
	public void summarize(double threshold, int wordnum, String path) throws IOException{
		
		PrintWriter writer = new PrintWriter(path);
		
		writeln(writer, "Summarization:");
		writeln(writer, "\tTopic num = " + k);
		writeln(writer, "\tprint Topic if theta > " + threshold + " and " + wordnum + " frequent words in Topic");
		
		// トピック毎に整理
		for(int z=0; z<k; z++){
			Map<String, Double> tmp = new HashMap<String, Double>();
				for(String word : wordset){
					tmp.put(word, phi.get(tuple(word, z)));
				}
			phiZ.add(tmp);
		}
		
		for(int z=0; z<k; z++){
			if(Double.compare(theta[z], threshold)<0) continue;
			Map<String, Double> map = phiZ.get(z);
			
			// 降順でソート
			List<Map.Entry<String, Double>> entries =
					new ArrayList<Map.Entry<String, Double>>(map.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<String, Double>>() {
				@Override
				public int compare(Entry<String, Double> o1,
						Entry<String, Double> o2) {
					return o2.getValue().compareTo(o1.getValue());
				}
			});
			
			writeln(writer, "Topic : " + z);
			writeln(writer, " (theta = " + theta[z] + ")");
			
			int count = 0;
			for(Entry<String, Double> entry : entries){
				String word = entry.getKey();
				double value = entry.getValue();
				
				writeln(writer, "\t" + word + " : " + value);
				
				count++;
				if(count>wordnum) break;
			}
		}
		writer.close();
	}

	public void saveParameters(String pathTheta, String pathPhi) throws IOException{
		
		PrintWriter writerTheta = new PrintWriter(pathTheta);
		for(int i=0; i<theta.length; i++){
			double p = theta[i];
			writerTheta.print(p);
			if(i!=(theta.length-1)) writerTheta.print(", ");
		}
		writerTheta.close();
		
		PrintWriter writerPhi = new PrintWriter(pathPhi);
		for(String word : wordset){
			writerPhi.print(word + ", ");
			for(int i=0; i<theta.length; i++){
				double p = phi.get(Tuple.tuple(word, i));
				writerPhi.print(p);
				if(i!=(theta.length-1)) writerPhi.print(", ");
			}
			writerPhi.println();
		}
		writerPhi.close();
		
	}
	
	private void writeln(PrintWriter writer, String str){
		System.out.println(str);
		writer.println(str);
	}
}
