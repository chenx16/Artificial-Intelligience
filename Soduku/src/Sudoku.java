import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Sudoku {

	private static int boardSize = 0;
	private static int partitionSize = 0;
	static int[][] vals = null;

	public static void main(String[] args) {
		String filename = args[0];
		File inputFile = new File(filename);
		Scanner input = null;
		// int[][] vals = null;

		int temp = 0;
		int count = 0;

		try {
			input = new Scanner(inputFile);
			temp = input.nextInt();
			boardSize = temp;
			partitionSize = (int) Math.sqrt(boardSize);
			System.out.println("Boardsize: " + temp + "x" + temp);
			vals = new int[boardSize][boardSize];

			System.out.println("Input:");
			int i = 0;
			int j = 0;
			while (input.hasNext()) {
				temp = input.nextInt();
				count++;
				System.out.printf("%3d", temp);
				vals[i][j] = temp;
				if (temp == 0) {
					// TODO
					// leave it to 0
				}
				j++;
				if (j == boardSize) {
					j = 0;
					i++;
					System.out.println();
				}
				if (j == boardSize) {
					break;
				}
			}
			input.close();
		} catch (FileNotFoundException exception) {
			System.out.println("Input file not found: " + filename);
		}
		if (count != boardSize * boardSize)
			throw new RuntimeException("Incorrect number of inputs.");


		boolean solved = solve();
		// Write output to file
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter pw = null;
		File file = new File(filename.substring(0, filename.length() - 4) + " Solution.txt");
		try {
			fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(fw);
			if (!solved) {
				pw.print("No solution found.");
				pw.close();
				bw.close();
				fw.close();
			} else {
				for (int i = 0; i < boardSize; i++) {
					for (int j = 0; j < boardSize; j++) {
						pw.print(vals[i][j]);
					}
					pw.println();
				}
				pw.close();
				bw.close();
				fw.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Output
		if (!solved) {
			System.out.println("No solution found.");
			return;
		}
		
		System.out.println("\nOutput:\n");
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				System.out.printf("%3d", vals[i][j]);
			}
			System.out.println();
		}

	}

	public static boolean solve() {
		// TODO
		int row = 0;
		int col = 0;
		boolean ifstillblank = false;
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				if (vals[i][j] == 0) {
					row = i;
					col = j;
					ifstillblank = true;
					for (int k = 1; k <= boardSize; k++) {
						// check if the number is already been used
						if (IfcanbeUsed(row, col, k)) {
							vals[row][col] = k;

							if (solve()) {
								return true;
							}

							vals[row][col] = 0;
						}

					}
					return false;
				}
			}
			if (ifstillblank == true)
				break;
		}
		// all positions are filled
		if (ifstillblank == false)
			return true;

		// no solutions found
		return false;

	}

	public static boolean IfcanbeUsed(int row, int col, int num) {
		int sqrt = (int)Math.sqrt(boardSize);
		return (!UsedInCol(row, num) && !UsedInRow( col, num)
				&& !UsedInBox(row - (row % sqrt), col - (col % sqrt), num));
	}

	public static boolean UsedInCol( int row, int num) {
		for (int col = 0; col < boardSize; col++) {
			if (vals[row][col] == num) {
				return true;
			}
		}
		return false;
	}

	public static boolean UsedInRow(int col, int num) {
		for (int row = 0; row < boardSize; row++) {
			if (vals[row][col] == num) {
				return true;
			}
		}
		return false;
	}

	public static boolean UsedInBox(int startrow, int startcol, int num) {
		for (int row = startrow; row < startrow+(int)Math.sqrt(boardSize); row++) {
			for (int col = startcol; col < startcol+(int)Math.sqrt(boardSize); col++) {
				if (vals[row][col] == num) {
					return true;
				}
			}
		}
		return false;
	}

}
