/*
* current idea is try to re-do the first attempt with
*
* */




/*
for now run like this:

Learner learner = new Learner(width, height);
learner.makeTurn(board);    //board is the current exposed board
// in Minesweeper.java, uncheck the returned value from ^
alterValues(state); where state is what happened to the game (DEAD, ALIVE, or NOTHING)

loop the makeTurn and alterValues as long as needed
*/

import java.util.DoubleSummaryStatistics;

public class Learner {

    static int UNCHECKED = 9;   // index for unknown blocks on the field


    // indexes are: #mines*2 + 1 and #unknowns*2 (total)
    // 0 mines, 0 unknowns, 1 mines, 1 unknowns, 2 mines, 2 unknowns, etc (total 18)
    static int DU = 100;    // DU = DEFAULT_UTILITY
    static int DM = -200;
    static int DI = 1;
    static double[] DEFAULT_TABLE = {DM, DU, DM, DU, DM, DU, DM, DU, DM, DU, DM, DU, DM, DU, DM, DU, DM, DU};
    static double ALPHA = 0.1;  // this is how much we change these ^ values when something happens
    static int ALIVE_BONUS = 1;
    static int DEATH_PENALTY = -9;


    static int DEAD = 1;    // accidentally set off a bomb
    static int ALIVE = 2;   // still alive/still playing
    static int NOTHING = 3; // nothing happened/tried unchecking something already unchecked



    private double[] learningTable = new double[18];  // table containing learned values
    private int[] adjacent;


    private double[][] firstTable = new double[10][2];    //[max mine value (or ?)] [0 = # of times, 1 = current score]


    private double[] learningTable2; //biggest surrounding value, # times it shows up, # uncovered blocks
    static int BIGGEST_MINE = 9;
    static int AMOUNT_OF_BIGGEST = 9;
    static int NUM_UNCOVERED = 9;
    static int TABLE2_SIZE = BIGGEST_MINE * AMOUNT_OF_BIGGEST * NUM_UNCOVERED;
    int biggestMine;        // these are the last recorded values
    int amountOfBiggest;
    int numUncovered;


    private double explorationProb = 0.01;

    private int[] utility;
    private int[] board;
    private int width;
    private int height;

    private int lastUnchecked;
    private int lastType;

    private int moveCount;


    // TODO: this is ugly
    static int BOARD_UNCHECKED = -4; // this is the board says is unchecked
    static int BOARD_FLAGGED = -5;


    public Learner(int width, int height, double[] table, int[] adjacent){
        for(int i = 0; i < learningTable.length; i++)
            learningTable[i] = table[i];

        learningTable2 = new double[BIGGEST_MINE * AMOUNT_OF_BIGGEST * NUM_UNCOVERED];
        for(int i = 0; i < TABLE2_SIZE; i++)
            learningTable2[i] = DU;


        for(int i = 0; i < firstTable.length; i++)
            for(int j = 0; j < 2; j++)
                firstTable[i][j] = DI;  // sets them all to ones

        this.width = width;
        this.height = height;
        utility = new int[this.width * this.height];
        this.adjacent = adjacent;
        moveCount = 0;
    }

    public void setMoveCount(int moveCount) {
        this.moveCount = moveCount;
    }

    public Learner(int width, int height, int[] adjacent){
        this(width, height, DEFAULT_TABLE, adjacent);
    }



    public void alterValues(int state){
//        if(moveCount > 10){

            if(state == ALIVE){
//                System.out.println("Movecount: " + moveCount);
//            learningTable[lastType] += ALPHA * ALIVE_BONUS;
                firstTable[lastType][0] += 1;
                firstTable[lastType][1] += ALPHA * ALIVE_BONUS;
//                learningTable2[lastType] += ALPHA * ALIVE_BONUS;
            }

            else if(state == DEAD){
//        	learningTable[lastType] += ALPHA * DEATH_PENALTY;
//                System.out.println("Movecount: " + moveCount);
//                learningTable2[lastType] += ALPHA * DEATH_PENALTY;

                firstTable[lastType][0] += 1;
                firstTable[lastType][1] += ALPHA * DEATH_PENALTY;
            }
//        }
//        moveCount++;

//        if(state == NOTHING)  // this is if it tries to check something that's already been checked
    }


    //TODO: USE THIS
    public int makeFirstTurn(){
        int startPoint = (int)(Math.random() * board.length);

        if(Math.random() < explorationProb){ // check something at random
            explorationProb -= 0.0001;
            lastUnchecked = startPoint;
            lastType = utility[startPoint];
        } else {                            // check the "first" square it finds with the appropriate utility value
            lastType = firstMaxValue();      // TODO: change this back to findMaxValue() for original
            for(int i = startPoint; i < board.length; i++)
                if(utility[i] == lastType)
                    return i;
            for(int i = 0; i < startPoint; i++)
                if(utility[i] == lastType)
                    return i;
        }

        return lastUnchecked;
    }

    private int firstMaxValue(){
        double currentMax = -1;
        int temp;
        int bestState = -1;

        for(int i = 0; i < board.length; i++){
            if(board[i] == BOARD_UNCHECKED){
                temp = firstSurroundingMax(i);
                utility[i] = temp;
                if(firstTable[temp][1] / firstTable[temp][0] > currentMax){
                    currentMax = firstTable[temp][1] / firstTable[temp][0];
                    bestState = temp;
                }
            }
        }
        return bestState;
    }

    private int firstSurroundingMax(int pos){

        int maxMine = -10;      // biggest mine count surrounding this (pos) block


        if(pos == 0){   // top left corner
            // check right and below

            if(board[pos + 1] != BOARD_UNCHECKED && board[pos + 1] != BOARD_FLAGGED && adjacent[pos + 1] > maxMine)
                maxMine = adjacent[pos + 1];

            for(int i = 0; i < 2; i++){
                if(board[pos + width + i] != BOARD_UNCHECKED &&
                        board[pos + width + i] != BOARD_FLAGGED && adjacent[pos + width + i] > maxMine)
                    maxMine = adjacent[pos + width + i];
            }

        } else if(pos == width - 1){    // top right corner
            // check left and below
            if(board[pos - 1] != BOARD_UNCHECKED && board[pos - 1] != BOARD_FLAGGED && adjacent[pos - 1] > maxMine)
                maxMine = adjacent[pos - 1];

            for(int i = 0; i < 2; i++){
                if(board[pos + width - 1 + i] != BOARD_UNCHECKED && board[pos + width - 1 + i] != BOARD_FLAGGED && adjacent[pos + width - 1 + i] > maxMine)
                    maxMine = adjacent[pos + width - 1 + i];
            }


        } else if(pos == board.length - 1){ // bottom right corner
            // check left and above
            if(board[pos - 1] != BOARD_UNCHECKED && board[pos - 1] != BOARD_FLAGGED && adjacent[pos - 1] > maxMine)
                maxMine = adjacent[pos - 1];

            for(int i = 0; i < 2; i++){
                if(board[pos - width - 1 + i] != BOARD_UNCHECKED && board[pos - width - 1 + i] != BOARD_FLAGGED && adjacent[pos - width - 1 + i] > maxMine)
                    maxMine = adjacent[pos - width - 1 + i];
            }


        } else if(pos == board.length - width){ // bottom left corner
            // check right and above
            if(board[pos + 1] != BOARD_UNCHECKED && board[pos + 1] != BOARD_FLAGGED && adjacent[pos + 1] > maxMine)
                maxMine = adjacent[pos + 1];

            for(int i = 0; i < 2; i++){
                if(board[pos - width + i] != BOARD_UNCHECKED && board[pos - width + i] != BOARD_FLAGGED && adjacent[pos - width + i] > maxMine)
                    maxMine = adjacent[pos - width + i];
            }


        } else if(pos < width){ // top row
            // check left, right, and below
            if(board[pos - 1] != BOARD_UNCHECKED && adjacent[pos - 1] > maxMine)
                maxMine = adjacent[pos - 1];


            if(board[pos + 1] != BOARD_UNCHECKED && adjacent[pos + 1] > maxMine)
                maxMine = adjacent[pos + 1];

            for(int i = 0; i < 3; i++){
                if(board[pos + width - 1 + i] != BOARD_UNCHECKED && adjacent[pos + width - 1 + i] > maxMine)
                    maxMine = adjacent[pos + width - 1 + i];
            }


        } else if(pos % width == 0){    // left side
            // check above & below, and right
            for(int i = 0; i < 2; i++){
                if(board[pos - width + i] != BOARD_UNCHECKED && adjacent[pos - width + i] > maxMine)
                    maxMine = adjacent[pos - width + i];
                if(board[pos + width + i] != BOARD_UNCHECKED && adjacent[pos + width + i] > maxMine)
                    maxMine = adjacent[pos + width + i];
            }
            if(board[pos + 1] != BOARD_UNCHECKED && adjacent[pos + 1] > maxMine)
                maxMine = adjacent[pos + 1];

        } else if((pos + 1) % width == 0){  // right side
            // check above & below, and left
            for(int i = 0; i < 2; i++){
                if(board[pos - width - 1 + i] != BOARD_UNCHECKED && adjacent[pos - width - 1 + i] > maxMine)
                    maxMine = adjacent[pos - width - 1 + i];

                if(board[pos + width - 1 + i] != BOARD_UNCHECKED && adjacent[pos + width - 1 + i] > maxMine)
                    maxMine = adjacent[pos + width - 1 + i];
            }
            if(board[pos - 1] != BOARD_UNCHECKED && adjacent[pos - 1] > maxMine)
                maxMine = adjacent[pos - 1];

        } else if(pos > board.length - width){  // bottom row
            // check above, left, and right
            for(int i = 0; i < 3; i++){
                if(board[pos - width - 1 + i] != BOARD_UNCHECKED && adjacent[pos - width - 1 + i] > maxMine)
                    maxMine = adjacent[pos - width - 1 + i];
            }
            if(board[pos - 1] != BOARD_UNCHECKED && adjacent[pos - 1] > maxMine)
                maxMine = adjacent[pos - 1];

            if(board[pos + 1] != BOARD_UNCHECKED && adjacent[pos + 1] > maxMine)
                maxMine = adjacent[pos + 1];

        } else {
            // NORMAL
            // 3 above
            for(int i = 0; i < 3; i++){
                if(board[pos - width - 1 + i] != BOARD_UNCHECKED && adjacent[pos - width - 1 + i] > maxMine)
                    maxMine = adjacent[pos - width - 1 + i];
            }
            // same row
            if(board[pos - 1] != BOARD_UNCHECKED && adjacent[pos - 1] > maxMine)
                maxMine = adjacent[pos - 1];
            // same row still
            if(board[pos + 1] != BOARD_UNCHECKED && adjacent[pos + 1] > maxMine)
                maxMine = adjacent[pos + 1];

            // 3 below
            for(int i = 0; i < 3; i++){
                if(board[pos + width - 1 + i] != BOARD_UNCHECKED && adjacent[pos + width - 1 + i] > maxMine)
                    maxMine = adjacent[pos + width - 1 + i];
            }

        }

        // in case there are no checked blocks
        if(maxMine < 0){
            maxMine = 9;
        }

        return maxMine;
    }

    // returns the value of the index it wants to check
    public int makeTurn(){
        int startPoint = (int)(Math.random() * board.length);

        if(Math.random() < explorationProb){ // check something at random
            explorationProb -= 0.0001;
            lastUnchecked = startPoint;
            lastType = utility[startPoint];
        } else {                            // check the "first" square it finds with the appropriate utility value
            lastType = findMaxValue2();      // TODO: change this back to findMaxValue() for original
            for(int i = startPoint; i < board.length; i++)
                if(utility[i] == lastType)
                    return i;
            for(int i = 0; i < startPoint; i++)
                if(utility[i] == lastType)
                    return i;
        }

        return lastUnchecked;

    }




    public void setBoard(int[] board) {
        this.board = board;
    }

    // fills up the maxValue array with maxes and finds best utility number
    // returns best utility number
    public int findMaxValue(){
        double currentMax = -Double.MAX_VALUE;
        int temp;
        int bestState = -1;
        for(int i = 0; i < board.length; i++){
            // get the surrounding 8 blocks
            if(board[i] == BOARD_UNCHECKED){
                //System.out.println("INSIDE IF loop 1");

                temp = surroundingMax(i);
                utility[i] = temp;
                //if(temp != 2 * 7 + 1 && temp != 2 * 8 + 1)
                	if(learningTable[temp] > currentMax){
                		currentMax = learningTable[temp];
                		bestState = temp;
                        //System.out.println("INSIDE IF loop 2 !!!!!!");

                	}
            }
        }
        //System.out.println("BEST STATE LAST: " + bestState);
        return bestState;
    }


    public int findMaxValue2(){
        double currentMax = -Double.MAX_VALUE;
        int temp;
        int bestState = -1;

        for(int i = 0; i < board.length; i++){
            if(board[i] == BOARD_UNCHECKED){
                temp = surroundingMax2(i);
                utility[i] = temp;
                if(learningTable2[temp] > currentMax){
                    currentMax = learningTable2[temp];
                    bestState = temp;
                }
            }
        }
        return bestState;
    }


    private int surroundingMax2(int pos){
        int maxMine = -10;      // biggest mine count surrounding this (pos) block
        int maxCount = 0;
        int uncheckedCount = 0;

        if(pos == 0){   // top left corner
            // check right and below

            if(board[pos + 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos + 1] > maxMine){
                maxMine = adjacent[pos + 1];
                maxCount = 1;
            } else if(adjacent[pos + 1] == maxMine)
                maxCount++;

            for(int i = 0; i < 2; i++){
                if(board[pos + width + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos + width + i] > maxMine){
                    maxMine = adjacent[pos + width + i];
                    maxCount = 1;
                } else if(adjacent[pos + width + i] == maxMine)
                    maxCount++;

            }

            maxCount += 5; // corner bias


        } else if(pos == width - 1){    // top right corner
            // check left and below
            if(board[pos - 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos - 1] > maxMine){
                maxMine = adjacent[pos - 1];
                maxCount = 1;
            } else if(adjacent[pos - 1] == maxMine)
                maxCount++;

            for(int i = 0; i < 2; i++){
                if(board[pos + width - 1 + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos + width - 1 + i] > maxMine) {
                    maxMine = adjacent[pos + width - 1 + i];
                    maxCount = 1;
                } else if(adjacent[pos + width - 1 + i] == maxMine)
                    maxCount++;
            }

            maxCount += 5; // corner bias


        } else if(pos == board.length - 1){ // bottom right corner
            // check left and above
            if(board[pos - 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos - 1] > maxMine){
                maxMine = adjacent[pos - 1];
                maxCount = 1;
            } else if(adjacent[pos - 1] == maxMine)
                maxCount++;

            for(int i = 0; i < 2; i++){
                if(board[pos - width - 1 + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos - width - 1 + i] > maxMine){
                    maxMine = adjacent[pos - width - 1 + i];
                    maxCount = 1;
                } else if(adjacent[pos - width - 1 + i] == maxMine)
                    maxCount++;
            }


            maxCount += 5; // corner bias

        } else if(pos == board.length - width){ // bottom left corner
            // check right and above
            if(board[pos + 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos + 1] > maxMine){
                maxMine = adjacent[pos + 1];
                maxCount = 1;
            } else if(adjacent[pos + 1] == maxMine)
                maxCount++;

            for(int i = 0; i < 2; i++){
                if(board[pos - width + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos - width + i] > maxMine){
                    maxMine = adjacent[pos - width + i];
                    maxCount = 1;
                } else if(adjacent[pos - width + i] == maxMine)
                    maxCount++;
            }

            maxCount += 5; // corner bias

        } else if(pos < width){ // top row
            // check left, right, and below
            if(board[pos - 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos - 1] > maxMine){
                maxMine = adjacent[pos - 1];
                maxCount = 1;
            } else if(adjacent[pos - 1] == maxMine)
                maxCount++;

            if(board[pos + 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos + 1] > maxMine){
                maxMine = adjacent[pos + 1];
                maxCount = 1;
            } else if(adjacent[pos + 1] == maxMine)
                maxCount++;

            for(int i = 0; i < 3; i++){
                if(board[pos + width - 1 + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos + width - 1 + i] > maxMine){
                    maxMine = adjacent[pos + width - 1 + i];
                    maxCount = 1;
                } else if(adjacent[pos + width - 1 + i] == maxMine)
                    maxCount++;

            }

            maxCount += 3; // edge bias

        } else if(pos % width == 0){    // left side
            // check above & below, and right
            for(int i = 0; i < 2; i++){
                if(board[pos - width + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos - width + i] > maxMine){
                    maxMine = adjacent[pos - width + i];
                    maxCount = 1;
                } else if(adjacent[pos - width + i] == maxMine)
                    maxCount++;

                if(board[pos + width + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos + width + i] > maxMine){
                    maxMine = adjacent[pos + width + i];
                    maxCount = 1;
                } else if(adjacent[pos + width + i] == maxMine)
                    maxCount++;

            }
            if(board[pos + 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos + 1] > maxMine){
                maxMine = adjacent[pos + 1];
                maxCount = 1;
            } else if(adjacent[pos + 1] == maxMine)
                maxCount++;


            maxCount += 3; // edge bias

        } else if((pos + 1) % width == 0){  // right side
            // check above & below, and left
            for(int i = 0; i < 2; i++){
                if(board[pos - width - 1 + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos - width - 1 + i] > maxMine){
                    maxMine = adjacent[pos - width - 1 + i];
                    maxCount = 1;
                } else if(adjacent[pos - width - 1 + i] == maxMine)
                    maxCount++;

                if(board[pos + width - 1 + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos + width - 1 + i] > maxMine){
                    maxMine = adjacent[pos + width - 1 + i];
                    maxCount = 1;
                } else if(adjacent[pos + width - 1 + i] == maxMine)
                    maxCount++;

            }
            if(board[pos - 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos - 1] > maxMine){
                maxMine = adjacent[pos - 1];
                maxCount = 1;
            } else if(adjacent[pos - 1] == maxMine)
                maxCount++;


            maxCount += 3; // edge bias

        } else if(pos > board.length - width){  // bottom row
            // check above, left, and right
            for(int i = 0; i < 3; i++){
                if(board[pos - width - 1 + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos - width - 1 + i] > maxMine){
                    maxMine = adjacent[pos - width - 1 + i];
                    maxCount = 1;
                } else if(adjacent[pos - width - 1 + i] == maxMine)
                    maxCount++;

            }
            if(board[pos - 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos - 1] > maxMine){
                maxMine = adjacent[pos - 1];
                maxCount = 1;
            } else if(adjacent[pos - 1] == maxMine)
                maxCount++;

            if(board[pos + 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos + 1] > maxMine){
                maxMine = adjacent[pos + 1];
                maxCount = 1;
            } else if(adjacent[pos + 1] == maxMine)
                maxCount++;


            maxCount += 3; // edge bias

        } else {
            // NORMAL
            // 3 above
            for(int i = 0; i < 3; i++){
                if(board[pos - width - 1 + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos - width - 1 + i] > maxMine){
                    maxMine = adjacent[pos - width - 1 + i];
                    maxCount = 1;
                } else if(adjacent[pos - width - 1 + i] == maxMine)
                    maxCount++;

            }
            // same row
            if(board[pos - 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos - 1] > maxMine){
                maxMine = adjacent[pos - 1];
                maxCount = 1;
            } else if(adjacent[pos - 1] == maxMine)
                maxCount++;
            // same row still
            if(board[pos + 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos + 1] > maxMine){
                maxMine = adjacent[pos + 1];
                maxCount = 1;
            } else if(adjacent[pos + 1] == maxMine)
                maxCount++;

            // 3 below
            for(int i = 0; i < 3; i++){
                if(board[pos + width - 1 + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos + width - 1 + i] > maxMine){
                    maxMine = adjacent[pos + width - 1 + i];
                    maxCount = 1;
                } else if(adjacent[pos + width - 1 + i] == maxMine)
                    maxCount++;

            }

        }

        // in case there are no checked blocks
        if(maxMine < 0){
            maxMine = 0;
        }

        // http://stackoverflow.com/questions/7367770/how-to-flatten-or-index-3d-array-in-1d-array
        return maxMine + (AMOUNT_OF_BIGGEST * maxCount) + (AMOUNT_OF_BIGGEST * NUM_UNCOVERED * uncheckedCount);

//        biggestMine = maxMine;
//        amountOfBiggest = maxCount;
//        numUncovered = uncheckedCount;

    }


    // figures out what the max value around the current position is
    // has lots of special cases
    private int surroundingMax(int pos){
        int max = -10;
        int uncheckedCount = 0;

        if(pos == 0){   // top left corner
            // check right and below

            if(board[pos + 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos + 1] > max)
                max = adjacent[pos + 1];


            for(int i = 0; i < 2; i++){
                if(board[pos + width + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos + width + i] > max)
                    max = adjacent[pos + width + i];
            }


        } else if(pos == width - 1){    // top right corner
            // check left and below
            if(board[pos - 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos - 1] > max)
                max = adjacent[pos - 1];
            for(int i = 0; i < 2; i++){
                if(board[pos + width - 1 + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos + width - 1 + i] > max)
                    max = adjacent[pos + width - 1 + i];
            }

        } else if(pos == board.length - 1){ // bottom right corner
            // check above and left
            for(int i = 0; i < 2; i++){
                if(board[pos - width - 1 + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos - width - 1 + i] > max)
                    max = adjacent[pos - width - 1 + i];
            }
            if(board[pos - 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos - 1] > max)
                max = adjacent[pos - 1];

        } else if(pos == board.length - width){ // bottom left corner
            // check above and right
            for(int i = 0; i < 2; i++){
                if(board[pos - width + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos - width + i] > max)
                    max = adjacent[pos - width + i];
            }
            if(board[pos + 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos + 1] > max)
                max = adjacent[pos + 1];

        } else if(pos < width){ // top row
            // check left, right, and below
            if(board[pos - 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos - 1] > max)
                max = adjacent[pos - 1];
            if(board[pos + 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos + 1] > max)
                max = adjacent[pos + 1];
            for(int i = 0; i < 3; i++){
                if(board[pos + width - 1 + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos + width - 1 + i] > max)
                    max = adjacent[pos + width - 1 + i];
            }

        } else if(pos % width == 0){    // left side
            // check above & below, and right
            for(int i = 0; i < 2; i++){
                if(board[pos - width + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos - width + i] > max)
                    max = adjacent[pos - width + i];
                if(board[pos + width + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos + width + i] > max)
                    max = adjacent[pos + width + i];
            }
            if(board[pos + 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos + 1] > max)
                max = adjacent[pos + 1];

        } else if((pos + 1) % width == 0){  // right side
            // check above & below, and left
            for(int i = 0; i < 2; i++){
                if(board[pos - width - 1 + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos - width - 1 + i] > max)
                    max = adjacent[pos - width - 1 + i];
                if(board[pos + width - 1 + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos + width - 1 + i] > max)
                    max = adjacent[pos + width - 1 + i];
            }
            if(board[pos - 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos - 1] > max)
                max = adjacent[pos - 1];

        } else if(pos > board.length - width){  // bottom row
            // check above, left, and right
            for(int i = 0; i < 3; i++){
                if(board[pos - width - 1 + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos - width - 1 + i] > max)
                    max = adjacent[pos - width - 1 + i];
            }
            if(board[pos - 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos - 1] > max)
                max = adjacent[pos - 1];
            if(board[pos + 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos + 1] > max)
                max = adjacent[pos + 1];

        } else {
            // NORMAL
            // 3 above
            for(int i = 0; i < 3; i++){
                if(board[pos - width - 1 + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos - width - 1 + i] > max)
                    max = adjacent[pos - width - 1 + i];
            }
            // same row
            if(board[pos - 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos - 1] > max)
                max = adjacent[pos - 1];
            if(board[pos + 1] == BOARD_UNCHECKED)
                uncheckedCount++;
            else if(adjacent[pos + 1] > max)
                max = adjacent[pos + 1];
            // 3 below
            for(int i = 0; i < 3; i++){
                if(board[pos + width - 1 + i] == BOARD_UNCHECKED)
                    uncheckedCount++;
                else if(adjacent[pos + width - 1 + i] > max)
                    max = adjacent[pos + width - 1 + i];
            }
        }

        max = max * 2 + 1;
        if(uncheckedCount * 2 > max)
            max = uncheckedCount * 2;

        return max;
    }
    public double[] getTable(){
    	return learningTable;
    }

}
