
import static java.lang.Math.*;
import java.util.*;
import java.io.*;

public class ConnectFourGame
{
    static final int INF = Integer.MAX_VALUE/2;
    public static void main(String[] args) throws Exception
    {
        //AI goes first
        BufferedReader infile = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Do you want to go first? Enter Y or N");
        char cc = infile.readLine().charAt(0);
        State game = new State();
        int startingParity = 0;
        if(cc == 'Y')
        {
            System.out.println("Human enter a move (columns are 0-indexed)");
            int choice = Integer.parseInt(infile.readLine());
            game = makeMove(game, choice);
            startingParity = 1;
        }
        /**
         * Game Interaction begins
         */
        while(true)
        {
            //AI makes move
            int DEPTH = 7;
            State newGame = minimax(game, DEPTH, startingParity);
            int moveAI = -1;
            outer:for(int r=0; r < 6; r++)
                for(int c=0; c < 7; c++)
                    if(game.board[r][c] != newGame.board[r][c])
                    {
                        moveAI = c;
                        break outer;
                    }
            game = newGame;
            System.out.println("AI made move at column "+moveAI);
            System.out.println(game);

            if(game.findWinner() != 0)
            {
                System.out.println("AI wins");
                break;
            }
            //Human makes move
            System.out.println("Human enter a move (columns are 0-indexed)");
            int choice = Integer.parseInt(infile.readLine());
            game = makeMove(game, choice);
            if(game.findWinner() != 0)
            {
                System.out.println("Human wins");
                break;
            }
            System.out.println();
            System.out.println();
        }
    }
    //human is always second/yellow
    public static State makeMove(State curr, int place)
    {
        //place something here
        State next = new State(curr);
        next.board[curr.row[place]][place] = next.turn;
        next.row[place]++;
        next.turn = 3-next.turn;
        return next;
    }
    public static State minimax(State curr, int depth, int parity)
    {
        if(curr.findWinner() != 0 || depth == 0)
            return curr;
        ArrayList<State> next = curr.nextStates();
        if(next.size() == 0)
            return curr;
        if(parity == 0)
        {
            State res = null;
            int resScore = -INF;
            for(State s: next)
            {
                State temp = minimax(s, depth-1, parity^1);
                int ts = temp.getScore();
                if(res == null || resScore < ts)
                {
                    resScore = ts;
                    res = s;
                }
                else if(resScore == ts && (int)(Math.random()*2) == 0)
                    res = s;
            }
            return res;
        }
        else
        {
            State res = null;
            int resScore = INF;
            for(State s: next)
            {
                State temp = minimax(s, depth-1, parity^1);
                int ts = temp.getScore();
                if(res == null || resScore > ts)
                {
                    resScore = ts;
                    res = s;
                }
                else if(resScore == ts && (int)(Math.random()*2) == 0)
                    res = s;
            }
            return res;
        }
    }
}
class State
{
    int[][] board;
    int[] row;
    int turn;
    final int R = 6;
    final int C = 7;

    public State()
    {
        board = new int[R][C];
        row = new int[C];
        turn = 1;
    }
    public State(State copy)
    {
        board = new int[R][C];
        for(int r=0; r < R; r++)
            for(int c=0; c < C; c++)
                board[r][c] = copy.board[r][c];
        row = copy.row.clone();
        turn = copy.turn;
    }
    /**
     * red = 1, yellow = 2
     * higher score means better for red
     * smaller score means better for yellow
     */
    public int getScore()
    {
        int winner = findWinner();
        if(winner > 0)
            return winner ==  1 ? 1000 : -1000;
        int score = 0;
        //count number of "open" center pieces
        for(int r=0; r < R-1; r++)
        {
            if(board[r][3] == 0)
                continue;
            if(min(board[r][2], board[r][4]) == 0)
                score += 2*(board[r][3] == 1 ? 1 : -1);
        }
        //consider three in a row that aren't cut off
        for(int r=2; r < R; r++)
            for(int c=0; c < C; c++)
            {
                if(board[r][c] == 0)
                    continue;
                if(board[r-2][c] == board[r-1][c] && board[r-1][c] == board[r][c])
                {
                    int open = 0;
                    if(r >= 3 && board[r-3][c] != 3-board[r][c])
                        open++;
                    if(r+1 < R && board[r+1][c] != 3-board[r][c])
                        open++;
                    if(open > 0)
                        score += 15*(board[r][c] == 1 ? 1 : -1);
                    if(open == 2)
                        score += 100*(board[r][c] == 1 ? 1 : -1);
                }
            }
        for(int r=0; r < R; r++)
            for(int c=2; c < C; c++)
            {
                if(board[r][c] == 0)
                    continue;
                if(board[r][c-2] == board[r][c-1] && board[r][c-1] == board[r][c])
                {
                    int open = 0;
                    if(c >= 3 && board[r][c-3] != 3-board[r][c])
                        open++;
                    if(c+1 < C && board[r][c+1] != 3-board[r][c])
                        open++;
                    if(open > 0)
                        score += 15*(board[r][c] == 1 ? 1 : -1);
                    if(open == 2)
                        score += 100*(board[r][c] == 1 ? 1 : -1);
                }
            }
        //score slightly higher for diagonals
        for(int r=2; r < R; r++)
            for(int c=2; c < C; c++)
            {
                if(board[r][c] == 0)
                    continue;
                if(board[r-2][c-2] == board[r-1][c-1] && board[r-1][c-1] == board[r][c])
                {
                    int open = 0;
                    if(min(r, c) >= 3 && board[r-3][c-3] != 3-board[r][c])
                        open++;
                    if(r+1 < R && c+1 < C && board[r+1][c+1] != 3-board[r][c])
                        open++;
                    if(open > 0)
                        score += 20*(board[r][c] == 1 ? 1 : -1);
                    if(open == 2)
                        score += 100*(board[r][c] == 1 ? 1 : -1);
                }
            }
        for(int r=2; r < R; r++)
            for(int c=4; c >= 0; c--)
            {
                if(board[r][c] == 0)
                    continue;
                if(board[r-2][c+2] == board[r-1][c+1] && board[r-1][c+1] == board[r][c])
                {
                    int open = 0;
                    if(r >= 3 && c+3 < C && board[r-3][c+3] != 3-board[r][c])
                        open++;
                    if(r+1 < R && c > 0 && board[r+1][c-1] != 3-board[r][c])
                        open++;
                    if(open > 0)
                        score += 20*(board[r][c] == 1 ? 1 : -1);
                    if(open == 2)
                        score += 100*(board[r][c] == 1 ? 1 : -1);
                }
            }
        return score;
    }
    public ArrayList<State> nextStates()
    {
        ArrayList<State> ls = new ArrayList<State>();
        for(int place=0; place < C; place++)
            if(row[place] < R)
            {
                //place something here
                State next = new State(this);
                next.board[row[place]][place] = turn;
                next.row[place]++;
                next.turn = 3-next.turn;
                ls.add(next);
            }
        return ls;
    }
    public int findWinner()
    {
        //check row win
        for(int r=0; r < R; r++)
            for(int c=3; c < C; c++)
                if(board[r][c-3] == board[r][c-2] && board[r][c-1] == board[r][c]
                    && board[r][c-2] == board[r][c] && board[r][c] != 0)
                        return board[r][c];
        //check column win
        for(int r=3; r < R; r++)
            for(int c=0; c < C; c++)
                if(board[r-3][c] == board[r-2][c] && board[r-1][c] == board[r][c]
                    && board[r-2][c] == board[r][c] && board[r][c] != 0)
                        return board[r][c];
        //check diagonals
        for(int r=3; r < R; r++)
            for(int c=3; c < C; c++)
                if(board[r-3][c-3] == board[r-2][c-2] && board[r-1][c-1] == board[r][c]
                    && board[r-2][c-2] == board[r][c] && board[r][c] != 0)
                        return board[r][c];
        //check other diagonals
        for(int r=3; r < R; r++)
            for(int c=3; c >= 0; c--)
                if(board[r-3][c+3] == board[r-2][c+2] && board[r-1][c+1] == board[r][c]
                    && board[r-2][c+2] == board[r][c] && board[r][c] != 0)
                        return board[r][c];
        return 0;
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for(int r=R-1; r >= 0; r--)
        {
            sb.append('\t');
            for(int c=0; c < C; c++)
            {
                char spot = '.';
                if(board[r][c] == 1)
                    spot = 'R';
                else if(board[r][c] == 2)
                    spot = 'Y';
                sb.append(spot);
                sb.append('\t');
            }
            sb.append("\n");
        }
        sb.append("-----------------------------------------\n");
        sb.append('\t');
        for(int i=0; i < C; i++)
            sb.append(i).append('\t');
        return sb.toString();
    }
}
