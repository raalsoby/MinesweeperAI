
/*
for now run like this:

Learner learner = new Learner(width, height);
learner.makeTurn(board);    //board is the current exposed board
// in Minesweeper.java, uncheck the returned value from ^
alterValues(state); where state is what happened to the game (DEAD, ALIVE, or NOTHING)

loop the makeTurn and alterValues as long as needed
*/

public class Learner {

    static int UNCHECKED = 9;   // index for unknown blocks on the field

    // indexes are: #unknowns*2 + 1 and #mines*2 (total)
    // 0 mines, 0 unknowns, 1 mines, 1 unknowns, 2 mines, 2 unknowns, etc (total 18)
    static int DU = 100;    // DU = DEFAULT_UTILITY
    static double[] DEFAULT_TABLE = {DU, DU, DU, DU, DU, DU, DU, DU, DU, DU, DU, DU, DU, DU, DU, DU, DU, DU};
    static double ALPHA = 0.1;  // this is how much we change these ^ values when something happens
    static int ALIVE_BONUS = 10;
    static int DEATH_PENALTY = -50;


    static int DEAD = 1;    // accidentally set off a bomb
    static int ALIVE = 2;   // still alive/still playing
    static int NOTHING = 3; // nothing happened/tried unchecking something already unchecked



    double[] learningTable = new double[10];  // table containing learned values

    double explorationProb = 0.95;

    int[] utility;
    int[] board;
    int width;
    int height;

    int lastUnchecked;
    int lastType;


    // TODO: this is ugly
    static int BOARD_UNCHECKED = -4; // this is the board says is unchecked


    public Learner(int width, int height, double[] table){
        for(int i = 0; i < learningTable.length; i++)
            learningTable[i] = table[i];

        this.width = width;
        this.height = height;
        utility = new int[width * height];
    }


    public Learner(int width, int height){
        this(width, height, DEFAULT_TABLE);
    }

    //TODO: values for topLeft etc need to be changed eventually (in Minesweeper.java)


    public void alterValues(int state){
        if(state == ALIVE)
            learningTable[lastType] += ALPHA * ALIVE_BONUS;

        else if(state == DEAD)
            learningTable[lastType] += ALPHA * DEATH_PENALTY;
//        if(state == NOTHING)  // this is if it tries to check something that's already been checked
    }

    // returns the value of the index it wants to check
    public int makeTurn(){
        int startPoint = (int)(Math.random() * board.length);

        if(Math.random() < explorationProb){ // check something at random
            explorationProb -= 0.001;
            lastUnchecked = startPoint;
            lastType = utility[startPoint];
        } else {                            // check the "first" square it finds with the appropriate utility value
            lastType = findMaxValue();
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
        double currentMax = -10;
        int temp;
        int bestState = -1;
        for(int i = 0; i < board.length; i++){
            // get the surrounding 8 blocks
            if(board[i] == BOARD_UNCHECKED){
                temp = surroundingMax(i);
                utility[i] = temp;
                if(learningTable[temp] > currentMax){
                    currentMax = learningTable[temp];
                    bestState = temp;
                }
            }
        }
        return bestState;
    }



    // figures out what the max value around the current position is
    // has lots of special cases
    private int surroundingMax(int pos){
        int max = -10;
        int uncheckedCount = 0;
        int temp; // holds the current value of whatever adjacent square it's checking
        if(pos == 0){   // top left corner
            // check right and below

            if((temp = board[pos + 1]) > max)
                max = temp;
            if(temp == BOARD_UNCHECKED)
                uncheckedCount++;
            for(int i = 0; i < 2; i++){
                if((temp = board[pos + width + i]) > max)
                    max = temp;
                if(temp == BOARD_UNCHECKED)
                    uncheckedCount++;
            }


        } else if(pos == width - 1){    // top right corner
            // check left and below
            if((temp = board[pos - 1]) > max)
                max = temp;
            if(temp == BOARD_UNCHECKED)
                uncheckedCount++;
            for(int i = 0; i < 2; i++){
                if((temp = board[pos + width - 1 + i]) > max)
                    max = temp;
                if(temp == BOARD_UNCHECKED)
                    uncheckedCount++;
            }

        } else if(pos == board.length - 1){ // bottom right corner
            // check above and left
            for(int i = 0; i < 2; i++){
                if((temp = board[pos - width - 1 + i]) > max)
                    max = temp;
                if(temp == BOARD_UNCHECKED)
                    uncheckedCount++;
            }
            if((temp = board[pos - 1]) > max)
                max = temp;
            if(temp == BOARD_UNCHECKED)
                uncheckedCount++;

        } else if(pos == board.length - width){ // bottom left corner
            // check above and right
            for(int i = 0; i < 2; i++){
                if((temp = board[pos - width + i]) > max)
                    max = temp;
                if(temp == BOARD_UNCHECKED)
                    uncheckedCount++;
            }
            if((temp = board[pos + 1]) > max)
                max = temp;
            if(temp == BOARD_UNCHECKED)
                uncheckedCount++;

        } else if(pos < width){ // top row
            // check left, right, and below
            if((temp = board[pos - 1]) > max)
                max = temp;
            if(temp == BOARD_UNCHECKED)
                uncheckedCount++;
            if((temp = board[pos + 1]) > max)
                max = temp;
            if(temp == BOARD_UNCHECKED)
                uncheckedCount++;
            for(int i = 0; i < 3; i++){
                if((temp = board[pos + width - 1 + i]) > max)
                    max = temp;
                if(temp == BOARD_UNCHECKED)
                    uncheckedCount++;
            }

        } else if(pos % width == 0){    // left side
            // check above & below, and right
            for(int i = 0; i < 2; i++){
                if((temp = board[pos - width + i]) > max)
                    max = temp;
                if(temp == BOARD_UNCHECKED)
                    uncheckedCount++;
                if((temp = board[pos + width + i]) > max)
                    max = temp;
                if(temp == BOARD_UNCHECKED)
                    uncheckedCount++;
            }
            if((temp = board[pos + 1]) > max)
                max = temp;
            if(temp == BOARD_UNCHECKED)
                uncheckedCount++;

        } else if((pos + 1) % width == 0){  // right side
            // check above & below, and left
            for(int i = 0; i < 2; i++){
                if((temp = board[pos - width - 1 + i]) > max)
                    max = temp;
                if(temp == BOARD_UNCHECKED)
                    uncheckedCount++;
                if((temp = board[pos + width - 1 + i]) > max)
                    max = temp;
                if(temp == BOARD_UNCHECKED)
                    uncheckedCount++;
            }
            if((temp = board[pos - 1]) > max)
                max = temp;
            if(temp == BOARD_UNCHECKED)
                uncheckedCount++;

        } else if(pos > board.length - width){  // bottom row
            // check above, left, and right
            for(int i = 0; i < 3; i++){
                if((temp = board[pos - width - 1 + i]) > max)
                    max = temp;
                if(temp == BOARD_UNCHECKED)
                    uncheckedCount++;
            }
            if((temp = board[pos - 1]) > max)
                max = temp;
            if(temp == BOARD_UNCHECKED)
                uncheckedCount++;
            if((temp = board[pos + 1]) > max)
                max = temp;
            if(temp == BOARD_UNCHECKED)
                uncheckedCount++;

        } else {
            // NORMAL
            // 3 above
            for(int i = 0; i < 3; i++){
                if((temp = board[pos - width - 1 + i]) > max)
                    max = temp;
                if(temp == BOARD_UNCHECKED)
                    uncheckedCount++;
            }
            // same row
            if((temp = board[pos - 1]) > max)
                max = temp;
            if(temp == BOARD_UNCHECKED)
                uncheckedCount++;
            if((temp = board[pos + 1]) > max)
                max = temp;
            if(temp == BOARD_UNCHECKED)
                uncheckedCount++;
            // 3 below
            for(int i = 0; i < 3; i++){
                if((temp = board[pos + width - 1 + i]) > max)
                    max = temp;
                if(temp == BOARD_UNCHECKED)
                    uncheckedCount++;
            }
        }

        max *= 2;
        if(uncheckedCount * 2 + 1 > max)
            max = uncheckedCount * 2;

        return max;
    }

}
