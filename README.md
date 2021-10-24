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

## Search/Planning
### Objectives
The purpose of this assignment is to learn and understand the ins and outs of search, a fundamental AI tool. This will be accomplished in the context of planning, a key application area in AI. You will implement two fundamental search algorithms that guide a search agent through a 2-dimensional environment with obstacles to targets.

### Assignment
Please download the Java starter code and modify the Robot.java file. Please do not modify any of the other files. If you need to implement additional classes, please ensure you submit them as part of this assignment. Ideally make any additional classes inner classes.

Work on this assignment by yourself.

Please implement the following behavior and run the experiments as detailed in the lab manual. The lab manual is included in the starter code.

There are four methods in the Robot.java file, called bfs(), bsfM(), astar() and astarM(). Use them to implement the breadth first and A* search algorithms. The M versions are to be used for the problems that have multiple targets and the plain versions are to be used for problems with a single target. As you figured out by now, you should be able to use the M versions to solve the single target problems too. However, for the purpose of simplifying this assignment, it is easier to write the single target versions first.
The search methods will be called from the VisualizeSimulation.java class.

The search methods should populate a data structure of your own design (think LinkedList) containing an ordered list of directions for getting from the start state to the goal state.

In the robot.java file, complete the getAction() method. It too gets called from the VisualizeSimulation.java class. Each time getAction() gets called, it should return the next action to be taken. In other words, this method processes the data structure populated by one of the search methods.

For grading purposes, each of the search methods has to perform the following:

Each time a state is added to the datastructure, increment the existing openCount field.

When a search algorithm terminates, set the:

pathFound field to true, if your code found a solution.

pathLength field to the length of the path that your algorithm found.

## Milestone 1: Setting up the System and Rudimentary NLP
### Objectives
The purpose of this project is to engineer a basic system for human robot collabration. The system will contain several key components, such an NLP, multi-agents, learning and collaborative problem solving. We will continue to work with the environment that was introduced in the Search assignment. However, this time we will focus on cleaning tiles. We will additionally introduce the Stanford NLP kit. The first milestone is about setting up our system and about getting to know key classes of the Stanford NLP kit.
### Assignment
Download and install the CSSE 413 HRC Project into our Eclipse IDE.

Download and unzip the Stanford Core NLP software.

Copy all .jar files into your Eclipse project folder. I counted 29 .jar archives.

While you are at it, you may also wish to copy the "StanfordDependenciesManual.pdf" file to your HRCProject folder.

Bookmark the documentation for the SemanticGraph class.

Next add the .jar files to the compile path. To do that, please complete the following steps:

For your HRCProject, select "Project" from the top menu bar.

In the drop-down menu, select "Properties".

In the window that pops up, select "Java Build Path"

In the new window, select the "Libraries" tab from the top menu bar.

In the new window contents, on the right side, select "Add External JARs..."

In the window that popos up, browse to the Eclipse project and select all 29 .jar files you copied there and click "Open"

Click on "Apply and Close"

You should be good to go now, without compile time errors.

Try to run the "VisualizeSimulation.java" file. At this point, it should not give you any errors.
Please have a look at the "Robot.java" file. This is the file you will be modifying and submitting. It is set up to invoke the Stanford parser. There is no rep-loop as that is taken care of by the visualization.

Here is a table exlaining the POS tags.

You are asked to modify the getAction() method by using the SemanticGraph object to determine which of the five actions are to be taken. You should default to DO_NOTHING. I have provided you with some starter code. Please extend it and use good function decompositionot avoid spaghetti code.

Please complete the lab manual, see below, to document your work. Your system should be able to process:

one-word commands, including: "left," "right," "up," "down" and "clean"

Small phrases that include the directions. For example, your system should be able to process "Move right," "Go right," "Move right please" and "Please move right."

Only return actions, if the instruction makes sense. For example, if the user enters "Pick up right" then DO_NOTHING should be returned.

All the procession must be accomplished by processing the SemanticGraph object as well as its derivations as returned by the Stanford Parser. You are not permitted to process the String returned by the Scanner, except for what is in the given code, i.e. to have the parser produce a SemanticGraph object from it.
