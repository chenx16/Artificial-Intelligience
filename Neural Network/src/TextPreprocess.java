
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.DoubleStream;

public class TextPreprocess {
	private static String embedding_path = "C:\\Users\\chenx16\\Desktop\\CSSE413\\Neural Network\\src\\glove.6B.50d.txt";
	private static int maxSequenceLength = 12; // maximum number of words an input sentence can have
	private static int embedding_dim = 50; // dimension of a word vector in the embedding file.

	private static Map<String, double[]> embeddings;

	public static void main(String[] args) {
		loadEmbedding(embedding_path);

		String input_file_path_train = "C:\\Users\\chenx16\\Desktop\\CSSE413\\Neural Network\\src\\alexa_X_train.txt";
		String output_file_path_train = "C:\\Users\\chenx16\\Desktop\\CSSE413\\Neural Network\\src\\alexa_X_train_6B_50d.txt";

		String input_file_path_val = "C:\\Users\\chenx16\\Desktop\\CSSE413\\Neural Network\\src\\alexa_X_val.txt";
		String output_file_path_val = "C:\\Users\\chenx16\\Desktop\\CSSE413\\Neural Network\\src\\alexa_X_val_6B_50d.txt";

		processFile(input_file_path_train, output_file_path_train);
		processFile(input_file_path_val, output_file_path_val);

//		double[][] myArray = { { 1, 0 }, { 2, 0 }, { 5, 3 }, { 0, 4 } };
//		double[] arr = FlattenAndPadding(myArray);
//		for (int i = 0; i < arr.length; i++) {
//			System.out.println(arr[i]);
//		}
//		double[] arr = generateRand(5);
//		for (int i = 0; i < arr.length; i++) {
//			System.out.println(arr[i]);
//		}

	}

	public static void loadEmbedding(String embedding_path) {
		embeddings = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(embedding_path))) {
			String line;
			while ((line = br.readLine()) != null) {
				writeEmbeddingVector(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void processFile(String input_file_name, String output_file_name) {
		List<double[]> inputs = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(input_file_name))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] tokenized = line.strip().split(" "); // You can also use a professional tokenizer
				double[][] embedding = convertTextToEmbedding(tokenized);
				double[] input = FlattenAndPadding(embedding);
				inputs.add(input);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(output_file_name))) {
			for (int i = 0; i < inputs.size(); i++) {
				for (int j = 0; j < maxSequenceLength * embedding_dim; j++) {
					bw.write(String.valueOf(inputs.get(i)[j]));
					bw.write(" ");
				}
				bw.newLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeEmbeddingVector(String line) {
		String[] embedding = line.split(" ");
		double[] vector = new double[embedding.length - 1];
		for (int i = 1; i < embedding.length; i++) {
			vector[i - 1] = Double.valueOf(embedding[i]);
		}
		embeddings.put(embedding[0], vector);
	}

	// TODO: implement this procedure
	/*
	 * The array "tokenized" represents a line of data from either the training or
	 * validation set. In particular, each line is an array of Strings, i.e. words.
	 * For each line, look up each word in order and see whether it occurs in the
	 * "embeddings" HashMap. If it occurs in the HashMap, grab the embedding, the
	 * i.e. vector associated with each word and add it to the 2D array of doubles.
	 * Why do we return a 2D array, you ask? I can explain that. It is because each
	 * embedding itself is a vector. Do that for the length of the sentence, i.e.
	 * the "tokenized" vector, and you get a 2D matrix. If a word does not have an
	 * embedding, fill the corresponding entry in the matrix with random doubles in
	 * the range [-1..1[ How many doubles? How about the size length of the
	 * embedding?
	 */

	public static double[][] convertTextToEmbedding(String[] tokenized) {
		double[][] res = new double[tokenized.length][embedding_dim];
		double[] vec = new double[embedding_dim];
		for (String word : tokenized) {
			if (embeddings.containsKey(word)) {
				vec = embeddings.get(word);
				for (int i = 0; i < tokenized.length; i++) {
					for (int j = 0; j < embedding_dim; j++) {
						res[i][j] = vec[j];
					}
				}
			} else {
				vec = generateRand(embedding_dim);
				for (int i = 0; i < tokenized.length; i++) {
					for (int j = 0; j < embedding_dim; j++) {
						res[i][j] = vec[j];
					}
				}
			}
		}
		return res;

	}

	public static double[] generateRand(int length) {
		double[] l = new double[length];
		double min = -1;
		double max = 1;
		for (int i = 0; i < length; i++) {
			Random r = new Random();
			double randomValue = min + (max - min) * r.nextDouble();
			l[i] = randomValue;
//			System.out.println(randomValue);
		}
		return l;
	}

	// TODO: Implement this procedure.
	/*
	 * This procedure prepares the data so it can be processed by a NN. To do this.
	 * you need to first flatten the 2D embedding produced by the prior procedure.
	 * Then, you need to pad the embedding with zeros, so that each embedding has
	 * the same length. The length is the product of the size of the embeddings
	 * multiplied by the maximum length of a response that we accept. Example:
	 * embedding = [[1,0],[2,0],[5,3],[0,4]] flattened_embedding = [1,0,2,0,5,3,0,4]
	 * if maxSequenceLength == 12, then padded_embedding = [1,0,2,0,5,3,0,4,0,0,0,0]
	 */
	public static double[] FlattenAndPadding(double[][] embedding) {
		double[] res = new double[maxSequenceLength * embedding_dim];
		int index = 0;
		for (int i = 0; i < embedding.length; i++) {
			for (int j = 0; j < embedding[0].length; j++) {
				if (index == maxSequenceLength * embedding_dim)
					return res;
				res[index] = embedding[i][j];
				index++;
			}
		}
		while (index < res.length) {
			res[index] = 0;
			index++;
		}
		return res;
	}

}