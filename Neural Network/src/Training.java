

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.jblas.DoubleMatrix;

public class Training {

	public static void main(String[] args) {		
		String train_input_path = "C:\\Users\\chenx16\\Desktop\\CSSE413\\Neural Network\\src\\alexa_X_train_6B_50d.txt";		// Replace with your own train input file name
		String train_label_path = "C:\\Users\\chenx16\\Desktop\\CSSE413\\Neural Network\\src\\alexa_y_train.txt";				// Replace with your own train label file name
		
		String val_input_path = "C:\\Users\\chenx16\\Desktop\\CSSE413\\Neural Network\\src\\alexa_X_val_6B_50d.txt";			// Replace with your own validation input file name
		String val_label_path = "C:\\Users\\chenx16\\Desktop\\CSSE413\\Neural Network\\src\\alexa_y_val.txt";					// Replace with your own validation label file name
		
		int num_train = 2520;										// This should be your number of training examples.
		int num_val = 630;											// This should be your number of validation examples.
		int embedding_dim = 50;										// This should be the embedding dimension of the version you are using.
		int maxSequenceLength = 600;								// This should match your maxSequenceLength in the TextPreprocess class.
		int input_dim = embedding_dim * maxSequenceLength;
		
		DoubleMatrix train_X = DoubleMatrix.zeros(num_train, input_dim);
		DoubleMatrix train_y = DoubleMatrix.zeros(num_train, 1);
		
		DoubleMatrix val_X = DoubleMatrix.zeros(num_val, input_dim);
		DoubleMatrix val_y = DoubleMatrix.zeros(num_val, 1);
		
		// Load training inputs into DoubleMatrix train_X
		loadInputs(train_input_path, train_X);		
		// Load validation inputs into DoubleMatrix val_X
		loadInputs(val_input_path, val_X);
		// Load training labels into DoubleMatrix train_y
		loadLabels(train_label_path, train_y);
		// Load validation labels into DoubleMatrix val_y
		loadLabels(val_label_path, val_y);

		System.out.println(String.format("Training example size: %s", train_X.rows));
		System.out.println(String.format("Training example dimension: %s", train_X.columns));
		System.out.println(String.format("Training label size: %s", train_y.rows));
		
		System.out.println(String.format("Validation example size: %s", val_X.rows));
		System.out.println(String.format("Validation example dimension: %s", val_X.columns));
		System.out.println(String.format("Validation label size: %s", val_y.rows));
		
		NeuralArgs neuralArgs = new NeuralArgs();
		neuralArgs.output_size = 6;					// fixed for our data		
		neuralArgs.input_size = input_dim;			// fixed for our data
		neuralArgs.learning_rate_decay = 0.95;		// feel free to experiment
		neuralArgs.early_stop = true;				// use as you see fit
		neuralArgs.verbose = true;					// use as you see fit
		
		// Structure of network
		neuralArgs.num_hidden_layers = 8;			// experiment		
		neuralArgs.hidden_size = 8;				// experiment
		
		// Training regimen
		neuralArgs.batch_size = 10;  				// experiment       
		neuralArgs.num_epoch = 200; 				// experiment	
		neuralArgs.learning_rate = 0.0000002;			// experiment		

			
		FullyConnectedNet model = new FullyConnectedNet(neuralArgs);
		model.train(train_X, train_y, val_X, val_y, neuralArgs);
		
	}
	
	private static void loadInputs(String filename, DoubleMatrix val) {
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				String[] embedding = line.strip().split(" ");
				for (int j = 0; j < embedding.length; j++) {
					val.put(i, j, Double.valueOf(embedding[j]));
				}
				i++;
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void loadLabels(String filename, DoubleMatrix val) {
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				double y = Double.valueOf(line.strip());
				val.put(i, 0, y);
				i++;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}