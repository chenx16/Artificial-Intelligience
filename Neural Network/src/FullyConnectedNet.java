import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import org.jblas.DoubleMatrix;

/**
 * @author Zeming Chen
 * @version 1.0
 * @since 2021-10-03
 * 
 * A multi-layer fully-connected neural network. The net has an input dimension of
 * num_train (N), a hidden layer dimension of hidden_size (H), and performs 
 * classification over C classes. 
 * We train the network with a cross_entropy loss function on the
 * weight matrices. The network uses a ReLU nonlinearity after the 
 * first fully connected layer.
 *
 */
public class FullyConnectedNet {

	private double std = 1e-1;
	private int num_hidden_layers = 1;
	private Map<String, DoubleMatrix> grads = new HashMap<>();

	public Map<String, DoubleMatrix> params = new HashMap<>();
	public Map<String, DoubleMatrix> params_best_train = new HashMap<>();
	public Map<String, DoubleMatrix> params_best_val = new HashMap<>();

	public FullyConnectedNet(NeuralArgs neuralArgs) {
		int inputSize = neuralArgs.input_size;
		int hiddenSize = neuralArgs.hidden_size;
		int outputSize = neuralArgs.output_size;
		num_hidden_layers = neuralArgs.num_hidden_layers;

		DoubleMatrix W1 = DoubleMatrix.randn(inputSize, hiddenSize).mul(std);
		DoubleMatrix b1 = DoubleMatrix.zeros(1, hiddenSize);

		params.put("W1", W1);
		params.put("b1", b1);

		params_best_train.put("W1", W1);
		params_best_train.put("b1", b1);

		params_best_val.put("W1", W1);
		params_best_val.put("b1", b1);

		for (int i = 1; i < num_hidden_layers; i++) {
			DoubleMatrix W = DoubleMatrix.randn(hiddenSize, hiddenSize).mul(std);
			DoubleMatrix bias = DoubleMatrix.zeros(1, hiddenSize);

			params.put(String.format("W%s", i + 1), W);
			params.put(String.format("b%s", i + 1), bias);
			params_best_train.put(String.format("W%s", i + 1), W);
			params_best_train.put(String.format("b%s", i + 1), bias);
			params_best_val.put(String.format("W%s", i + 1), W);
			params_best_val.put(String.format("b%s", i + 1), bias);
		}

		DoubleMatrix W_out = DoubleMatrix.randn(hiddenSize, outputSize).mul(std);
		DoubleMatrix bias_out = DoubleMatrix.zeros(1, outputSize);

		params.put("W_out", W_out);
		params.put("b_out", bias_out);
		params_best_train.put("W_out", W_out);
		params_best_train.put("b_out", bias_out);
		params_best_val.put("W_out", W_out);
		params_best_val.put("b_out", bias_out);
	}

	/**
	 * Compute the loss and gradients for a fully connected neural network.
	 * 
	 * @param X: Input data of shape (num_train, input_size). Each X[i] is a training sample.
	 * @param y: Vector of training labels with shape (num_train, 1).
	 *     		 y[i] is the label for X[i], and each y[i] is
     *     		 an integer in the range 0 <= y[i] < C. 
	 * @return DoubleMatrix: Loss for this batch of training samples. 
	 */
	public DoubleMatrix forward(DoubleMatrix X, DoubleMatrix y) {
		int N = X.rows;
		Stack<DoubleMatrix> cache = new Stack<>();

		DoubleMatrix W1 = this.params.get("W1");
		DoubleMatrix b1 = this.params.get("b1");
		
		// Forward pass through input layer
		DoubleMatrix hidden = X.mmul(W1);
		hidden = hidden.addRowVector(b1);
		Linag.matrix_relu(hidden);
		cache.push(hidden);

		// Forward pass through hidden layers
		for (int i = 1; i < num_hidden_layers; i++) {
			DoubleMatrix weights = this.params.get(String.format("W%s", i + 1));
			DoubleMatrix bias = this.params.get(String.format("b%s", i + 1));
			hidden = hidden.mmul(weights);
			hidden = hidden.addRowVector(bias);
			Linag.matrix_relu(hidden);
			cache.push(hidden);
		}
		
		// Final forward pass through output layer
		DoubleMatrix W_out = this.params.get("W_out");
		DoubleMatrix bias_out = this.params.get("b_out");

		DoubleMatrix output = hidden.mmul(W_out);
		output = output.addRowVector(bias_out);

		// Calculate a SoftMax activation function
		DoubleMatrix scores = output.subColumnVector(output.rowMaxs());
		DoubleMatrix numerator = Linag.matrix_exp(scores);
		DoubleMatrix denominator = numerator.rowSums();
		DoubleMatrix logits = numerator.divColumnVector(denominator);

		// Calculate a Cross-Entropy loss function
		DoubleMatrix loss = Linag.getSubLoss(logits, Linag.arange(N), y);
		loss = Linag.matrix_log(loss).mul(-1.0).columnSums();
		loss = loss.div(N);

		// Backpropgation through gradient
		Linag.modifySub(logits, Linag.arange(N), y, -1);
		DoubleMatrix dscores = logits;
		dscores = dscores.div(N);

		grads.put("b_out", dscores.columnSums());
		grads.put("W_out", hidden.transpose().mmul(dscores));

		DoubleMatrix dhidden = dscores.mmul(W_out.transpose());

		for (int i = num_hidden_layers - 1; i > 0; i--) {
			Linag.matrix_drelu(dhidden);
			grads.put(String.format("W%s", i + 1), cache.pop().transpose().mmul(dhidden));
			grads.put(String.format("b%s", i + 1), dhidden.columnSums());
			dhidden = dhidden.mmul(params.get(String.format("W%s", i + 1)).transpose());
		}

		Linag.matrix_drelu(dhidden);
		grads.put("W1", X.transpose().mmul(dhidden));
		grads.put("b1", dhidden.columnSums());

		return loss;
	}

	/**
	 * Use the trained weights of this neural network to predict labels for
     * data points. For each data point we predict scores for each of the C
     * classes, and assign each data point to the class with the highest score.
     *
	 * @param X: A DoubleMatrix of shape (num_val, input_size) giving testing data.
	 * @return int[]: An array of shape (num_val, ) giving predicted labels for each of the elements of X
	 */
	public int[] predict(DoubleMatrix X) {
		DoubleMatrix W1 = this.params.get("W1");
		DoubleMatrix b1 = this.params.get("b1");

		DoubleMatrix hidden = X.mmul(W1);
		hidden = hidden.addRowVector(b1);
		Linag.matrix_relu(hidden);

		for (int i = 1; i < num_hidden_layers; i++) {
			DoubleMatrix weights = this.params.get(String.format("W%s", i + 1));
			DoubleMatrix bias = this.params.get(String.format("b%s", i + 1));
			hidden = hidden.mmul(weights);
			hidden = hidden.addRowVector(bias);
			Linag.matrix_relu(hidden);
		}

		DoubleMatrix W_out = this.params.get("W_out");
		DoubleMatrix bias_out = this.params.get("b_out");

		DoubleMatrix output = hidden.mmul(W_out);
		output = output.addRowVector(bias_out);

		int[] y_pred = output.rowArgmaxs();
		return y_pred;
	}

	/**
	 * Train this neural network using stochastic gradient descent.
	 * 
	 * @param X: A DoubleMatrix of shape (num_train, input_size) giving training data.
	 * @param y: A DoubleMatrix of shape (num_train, ) giving training labels.
	 * @param X_val: A DoubleMatrix of shape (num_val, input_size) giving validation data.
	 * @param y_val: A DoubleMatrix of shape (num_val, ) giving validation labels.
	 * @param neuralArgs: An argument class containing training related arguments and parameters.
	 */
	public void train(DoubleMatrix X, DoubleMatrix y, DoubleMatrix X_val, DoubleMatrix y_val, NeuralArgs neuralArgs) {
		double val_acc_max = 0;

		int num_train = X.getRows();
		int early_stopping_count = 0;

		int batch_size = neuralArgs.batch_size;
		int num_epoch = neuralArgs.num_epoch;
		double l_rate = neuralArgs.learning_rate;
		double l_rate_decay = neuralArgs.learning_rate_decay;
		boolean verbose = neuralArgs.verbose;
		boolean early_stop = neuralArgs.early_stop;
		
		List<Double> loss_history = new ArrayList<>();
		List<Double> train_acc_history = new ArrayList<>();

		for (int iter = 0; iter <= num_epoch; iter++) {
			DoubleMatrix mask = Linag.generateMask(num_train, batch_size);
			DoubleMatrix X_batch = Linag.maskMatrix(X, mask);
			DoubleMatrix y_batch = Linag.maskMatrix(y, mask);

			DoubleMatrix loss = forward(X_batch, y_batch);;
			loss_history.add(loss.get(0));
			optimizer_step(l_rate);


			if (iter % 10 == 0) {

				int[] y_pred = predict(X_batch);
				double train_acc = getAccuracy(y_pred, y_batch);

				y_pred = predict(X_val);
				double val_acc = getAccuracy(y_pred, y_val);

				if (val_acc > val_acc_max) {
					early_stopping_count = 0;
					val_acc_max = val_acc;
					params_best_val.putAll(params);
				} else {
					early_stopping_count++;
					params.putAll(params_best_val);
				}

				if (verbose) {
					System.out.println(String.format("iteration %s / %s: loss %s train_acc %f val_acc %f", iter,
							num_epoch, loss, train_acc, val_acc));
				}
			}

			if (iter % 100 == 0) {
				l_rate = l_rate * l_rate_decay;
			}

			if (early_stop && early_stopping_count > 50) {
				if (verbose) {
					System.out.println("========== No improvement for a long time, early stopping! ==========");
				}
				break;
			}
		}
	}


	public void optimizer_step(double l_rate) {
		for (int i = 0; i < num_hidden_layers; i++) {
			String weight_name = String.format("W%s", i + 1);
			DoubleMatrix W_new = params.get(weight_name).sub(grads.get(weight_name).mul(l_rate));
			params.put(weight_name, W_new);
			String bias_name = String.format("b%s", i + 1);
			DoubleMatrix bias_new = params.get(bias_name).sub(grads.get(bias_name).mul(l_rate));
			params.put(bias_name, bias_new);
		}

		DoubleMatrix W_out_new = params.get("W_out").sub(grads.get("W_out").mul(l_rate));
		params.put("W_out", W_out_new);
		DoubleMatrix b_out_new = params.get("b_out").sub(grads.get("b_out").mul(l_rate));
		params.put("b_out", b_out_new);
	}

	private double getAccuracy(int[] y_pred, DoubleMatrix y) {
		int ydim = Math.max(y.columns, y.rows);
		double yiter = Math.min(y_pred.length, ydim);
		double score = 0;
		for (int i = 0; i < yiter; i++) {
			if (y_pred[i] == y.get(i,0)) {
				score++;
			}
		}
		return score / yiter;
	}

	public void print_dim(String name, int row, int column) {
		System.out.println(String.format("Name: %s, row: %s, column:%s", name, row, column));
	}
}