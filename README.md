# Artificial-Intelligience

## Sudoku
### Objectives
The purpose of this assignment is to gain first hand experience of what it means to develop artificially intelligent software. You will write software, with little effort, that solves Sudoku problems. Solving them is considered challenging, yet your software will solve them in a fraction of a second.

### Assignment
Implement and test a backtracking algorithm to solve Sudoku problems.

Work on the implementation of this assignment by yourself.

Your software needs to read input as follows. The first number, which appears on a row by itself, represents the size of the board. The following numbers represent the given problem. They appear in a grid that corresponds to the boardsize. Each number is separated by whitespace from the others. Below is an example for a 9x9 grid.

9

3 6 0 0 2 0 0 8 9

0 0 0 3 6 1 0 0 0

0 0 0 0 0 0 0 0 0

8 0 3 0 0 0 6 0 2

4 0 0 6 0 3 0 0 7

6 0 7 0 0 0 1 0 8

0 0 0 0 0 0 0 0 0

0 0 0 4 1 8 0 0 0

9 7 0 0 3 0 0 1 4

Your software should output a solution if it exists, or a comment stating that no solution exists. For grading purposes your software also needs to output the solution to a file. The filename should be the same as the input file with the string "Solution" appended after it. For example, if the input file is named "sudoku9Easy.txt" then the output should appear in the file "sudoku9EasySolution.txt". The solution file needs to have the same format as the input file, except, it does NOT contain the first line about the size of the grid. If no solution exists, then you should write -1 to the file.

[40 points] Implement a basic backtracking solver. While it will be able to solve 9x9 problems in a fraction of a second, computational complexity is such that it will not be able to solve 16x16 problems any time today.
