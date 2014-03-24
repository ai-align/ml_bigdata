package LR_SGD;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Hashtable;

//Here, the first argument 10000 is the vocabulary size. 
//Second argument 0.5 is the initial value of the learning rate.   λ
//The third argument 0.1 is the regular- ization coefficient.      2μ = 0.1
//The fourth argument 20 is the max iteration (# of passes through data). 
//The fifth argument is the size of the training dataset (which allows you to determine the starting point of a new pass).

//  0    1   2   3   4        5
//10000 0.5 0.1 20 1000 testData.txt

public class LR {


	public static void main(String[] args) throws IOException {

		/**
		 *  Train Part 
		 */
		int k = 0; // Keep the iteration time
		String[] labelType = {"nl", "el", "ru", "sl", "pl", "ca", "fr", "tr", "hu", "de", "hr", "es", "ga", "pt"}; 
		Hashtable<String, Hashtable<Integer, Integer>> A = new Hashtable<String, Hashtable<Integer,Integer>>();
		Hashtable<String, Hashtable<Integer, Double>> B = new Hashtable<String, Hashtable<Integer,Double>>();
		HashSet<String> record = new HashSet<String>();
		for (String a : labelType) {
			record.add(a);
			A.put(a, new Hashtable<Integer, Integer>());
			B.put(a, new Hashtable<Integer, Double>());
		}

		int vocab_size = Integer.valueOf(args[0]);
		double lr = 0.5;
		double u = 0.1;
		int times = Integer.valueOf(args[3]);
		long datasize = Long.parseLong(args[4]);

		//FileInputStream in = new FileInputStream(args[5]); 
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String strLine;

		for (int i = 0; i < times; i++) {
			try { 
				k++;
				long num_line = 0;
				while ((strLine = br.readLine()) != null) {
					HashSet<String> zeroLabel = new HashSet<String>(record);
					String[] label_text = strLine.split("\t");
					String[] labels = label_text[0].split(",");
					String[] words = label_text[1].split(" ");
					int[] wordsValue = new int[words.length];
					for (int j = 0; j < words.length; j++) {
						int id = words[j].hashCode() % vocab_size;
						if (id < 0) {
							id += vocab_size;
						}
						wordsValue[j] = id;
					}
					/**
					 * For labels with value 1
					 */
					for (String label : labels) {
						zeroLabel.remove(label);   // Delete the labels with value 1
						Hashtable<Integer, Double> Btemp = B.get(label);
						Hashtable<Integer, Integer> Atemp = A.get(label);

						for (int value : wordsValue) {
							if (!Atemp.containsKey(value)) {
								Atemp.put(value, 0);
								Btemp.put(value, 0.0);
							} else {
								// Todo so only when Xj is not zero
								double updated_Bj = Btemp.get(value) * Math.pow((1-lr*u) , (k-Atemp.get(value)));
								Btemp.put(value, updated_Bj);
								//Atemp.put(value, k);        //TODO when to update K

							}
						}
						double temp = 0.0;
						for (int value : wordsValue) {
							temp += Btemp.get(value);
						}
						double P = 1 / (1 + Math.exp(-temp));
						for (int value : wordsValue) {
							double updated_Bj = Btemp.get(value);
							updated_Bj += lr*(1-P);
							Btemp.put(value, updated_Bj);
							Atemp.put(value, k);
						}

						// Update Hashtable for each label 
						B.put(label, Btemp);    
						A.put(label, Atemp);
					}
					/**
					 * For labels with value 0
					 */
					String[] zerolebels = (String[]) zeroLabel.toArray();
					for (String label : zerolebels) {
						Hashtable<Integer, Double> Btemp = B.get(label);
						Hashtable<Integer, Integer> Atemp = A.get(label);

						for (int value : wordsValue) {
							if (!Atemp.containsKey(value)) {
								Atemp.put(value, 0);
								Btemp.put(value, 0.0);
							} else {
								// Todo so only when Xj is not zero
								double updated_Bj = Btemp.get(value) * Math.pow((1-lr*u) , (k-Atemp.get(value)));
								Btemp.put(value, updated_Bj);
								//Atemp.put(value, k);        //TODO when to update K
							}
						}
						double temp = 0.0;
						for (int value : wordsValue) {
							temp += Btemp.get(value);
						}
						double P = 1 / (1 + Math.exp(-temp));
						for (int value : wordsValue) {
							double updated_Bj = Btemp.get(value);
							updated_Bj += lr*(0-P);
							Btemp.put(value, updated_Bj);
							Atemp.put(value, k);
						}

						// Update Hashtable for each label 
						B.put(label, Btemp);    
						A.put(label, Atemp);
					}

					num_line ++;
					// Next iteration 
					if (num_line == datasize) {
						break;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/**
		 *  Update Bj in the last time
		 */
		for (String a : B.keySet()) {
			Hashtable<Integer, Double> Btemp = new Hashtable<Integer, Double>();
			for (int b : B.get(a).keySet()) {
				double updated_Bj = Btemp.get(b);
				updated_Bj *= Math.pow((1-lr*u), k-A.get(a).get(b));
				Btemp.put(b, updated_Bj);
			}
			B.put(a, Btemp);
		}


		/**
		 *  Debug Train Part 
		 */
		for (String a : B.keySet()) {
			Hashtable<Integer, Double> Btemp = new Hashtable<Integer, Double>();
			for (int b : B.get(a).keySet()) {
				System.out.println("label :" + a +  "Integer :" + b + "value : " + Btemp.get(b));
			}
		}


		/**
		 *  Test Part
		 */
		FileInputStream in = new FileInputStream(args[5]);
		BufferedReader testBr = new BufferedReader(new InputStreamReader(in));
		String testLine;
		
		while ( (testLine = testBr.readLine()) != null) {
			String[] testWords = testLine.split(" ");
			
			int[] wordsValue = new int[testWords.length];
			for (int j = 0; j < testWords.length; j++) {
				int id = testWords[j].hashCode() % vocab_size;
				if (id < 0) {
					id += vocab_size;
				}
				wordsValue[j] = id;
			}
			int printRange = 0;
			for (String a : labelType) {
				Hashtable<Integer, Double> testTemp = B.get(a);
				double temp = 0.0;
				for (int value : wordsValue) {
					temp += testTemp.get(value);
				}
				double P = 1 / (1 + Math.exp(-temp));
				if (printRange == 13) {
					System.out.print(a + "\t" + P + "\n");
				} else {
					System.out.print(a + "\t" + P + ",");
				}
				printRange ++;
			}
		}
	}

}
