

import java.util.concurrent.ThreadLocalRandom;

import org.jblas.DoubleMatrix;

public class Linag {
	public static void matrix_tanh(DoubleMatrix M) {
		for (int i = 0; i < M.rows; i++) {
			for (int j = 0; j < M.columns; j++) {
				double v = M.get(i, j);
				M.put(i, j, Math.tanh(v));
			}
		}
	}

	public static void matrix_relu(DoubleMatrix M) {
		for (int i = 0; i < M.rows; i++) {
			for (int j = 0; j < M.columns; j++) {
				double v = M.get(i, j);
				M.put(i, j, Math.max(0, v));
			}
		}
	}

	public static void matrix_dtanh(DoubleMatrix M) {
		for (int i = 0; i < M.rows; i++) {
			for (int j = 0; j < M.columns; j++) {
				double v = M.get(i, j);
				M.put(i, j, 1 - Math.pow(Math.tanh(v), 2));
			}
		}
	}

	public static void matrix_drelu(DoubleMatrix M) {
		for (int i = 0; i < M.rows; i++) {
			for (int j = 0; j < M.columns; j++) {
				double v = M.get(i, j);
				if (v < 0.00001) {
					M.put(i, j, 0);
				}
			}
		}
	}
	
	public static DoubleMatrix arange(int n) {
		DoubleMatrix M = DoubleMatrix.zeros(1, n);
		for (int i = 0; i < n; i++) {
			M.put(0, i, i);
		}
		return M;
	}
	
	public static DoubleMatrix matrix_pow(DoubleMatrix M, double pow) {
		DoubleMatrix M_exp = DoubleMatrix.zeros(M.rows, M.columns);
		for (int i = 0; i < M.rows; i++) {
			for (int j = 0; j < M.columns; j++) {
				double v = M.get(i, j);
				M_exp.put(i, j, Math.pow(v, pow));
			}
		}
		return M_exp;
	}
	
	public static DoubleMatrix matrix_sqrt(DoubleMatrix M) {
		DoubleMatrix M_exp = DoubleMatrix.zeros(M.rows, M.columns);
		for (int i = 0; i < M.rows; i++) {
			for (int j = 0; j < M.columns; j++) {
				double v = M.get(i, j);
				M_exp.put(i, j, Math.sqrt(v));
			}
		}
		return M_exp;
	}
	
	public static DoubleMatrix matrix_exp(DoubleMatrix M) {
		DoubleMatrix M_exp = DoubleMatrix.zeros(M.rows, M.columns);
		for (int i = 0; i < M.rows; i++) {
			for (int j = 0; j < M.columns; j++) {
				double v = M.get(i, j);
				M_exp.put(i, j, Math.exp(v));
			}
		}
		return M_exp;
	}
	
	public static DoubleMatrix matrix_log(DoubleMatrix M) {
		DoubleMatrix M_exp = DoubleMatrix.zeros(M.rows, M.columns);
		for (int i = 0; i < M.rows; i++) {
			for (int j = 0; j < M.columns; j++) {
				double v = M.get(i, j);
				M_exp.put(i, j, Math.log(v));
			}
		}
		return M_exp;
	}
	
	public static DoubleMatrix generateMask(int num_train, int batch_size) {
		DoubleMatrix mask = DoubleMatrix.zeros(1, batch_size);
		for (int i = 0; i < batch_size; i++) {
			mask.put(i, ThreadLocalRandom.current().nextInt(0, num_train));
		}
		return mask;
	}
	
	public static DoubleMatrix maskMatrix(DoubleMatrix M, DoubleMatrix mask) {
		DoubleMatrix masked = DoubleMatrix.zeros(mask.columns, M.columns);
		for (int i = 0; i < mask.columns; i++) {
			Double rowi = mask.get(i);
			masked.putRow(i, M.getRow(rowi.intValue()));
		}
		return masked;
	}
	
	public static DoubleMatrix getSubLoss(DoubleMatrix M, DoubleMatrix s1, DoubleMatrix s2) {
		if((s1.columns != 1 && s1.rows != 1) || (s2.columns != 1 && s2.rows != 1)) {
			return null;
		}
		int d1 = Math.max(s1.columns, s1.rows);
		int d2 = Math.max(s2.columns, s2.rows);
		if(d1 != d2) {
			return null;
		}
		DoubleMatrix selected = DoubleMatrix.zeros(d1,1);
		for (int i = 0; i < d1; i++) {
			int r = (int) s1.get(0, i);
			int c = (int) s2.get(i, 0);
			selected.put(i, 0, M.get(r, c));
		}
		
		return selected;
	}
	
	public static void modifySub(DoubleMatrix M, DoubleMatrix s1, DoubleMatrix s2, int operation) {
		if((s1.columns != 1 && s1.rows != 1) || (s2.columns != 1 && s2.rows != 1)) {
			return;
		}

		int d1 = Math.max(s1.rows, s1.columns);
		int d2 = Math.max(s2.rows, s2.columns);
		if(d1 != d2) {
			return;
		}
		for (int i = 0; i < d1; i++) {
			int r = (int) s1.get(i);
			int c = (int) s2.get(i);
			M.put(r, c, M.get(r, c) + operation);
		}
		
	}
}
