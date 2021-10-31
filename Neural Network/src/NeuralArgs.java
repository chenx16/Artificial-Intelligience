

public class NeuralArgs {
	public int batch_size;                  // Number of training examples to use per iteration.
	public int num_hidden_layers;			// Number of hidden feedforward layers in between the input and output layers.
	public int hidden_size;					// The number of neurons in the hidden layer.
	public int output_size;					// The number of classes C for the data.
	public int input_size;					// The dimension of the input data.
	
	public int num_epoch;					// Number of iterations to take when optimizing.
	public double learning_rate;			// Scalar giving learning rate for optimization.
	public double learning_rate_decay;		// Scalar giving factor used to decay the learning rate after each step.
	
	public boolean early_stop;				// boolean; if true stop the training automatically when val accuracy not improving.
	public boolean verbose;					// boolean; if true print progress during optimization.
}
