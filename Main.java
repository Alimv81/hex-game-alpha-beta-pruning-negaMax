import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import static javax.swing.JOptionPane.showMessageDialog;

public class Main {
    private final  static int MAXIMUM=2000, MINIMUM=-2000;
    private static int countOfActions = 0;
    private final static int DEPTH = 3;
    private static boolean took_action=true;
    static final int board_size=7;
    private static Players[][] Board = new Players[board_size][board_size];
    private enum Players{AI, Human, None};

    public static void main(String[] args) throws IOException {
        start();
    }

    private static void start() throws IOException {
        for (int i=0; i < board_size; i++){
            for (int j=0; j < board_size; j++) Board[i][j]=Players.None;
        }
        show_frame();
    }

    private static void show_frame() throws IOException {
        Integer[] one_to_seven = new Integer[]{1, 2, 3, 4, 5, 6, 7};
        jFrame = new JFrame();
        jLabel = new JLabel[7][7];
        jButton = new JButton("Take");
        JLabel jLabelx = new JLabel("ROW: ");
        JLabel jLabely = new JLabel("COLUMN: ");
        jComboBoxX = new JComboBox<>(one_to_seven);
        jComboBoxY = new JComboBox<>(one_to_seven);

        JLabel label = new JLabel();
        label.setIcon(new ImageIcon(ImageIO.read(new File("images/Hex Board.png"))));
        jFrame.setContentPane(label);

        jLabelx.setBounds(30, 510, 40, 30);
        jLabely.setBounds(170, 510, 60, 30);
        jLabelx.setForeground(Color.WHITE);
        jLabely.setForeground(Color.WHITE);
        jFrame.add(jLabelx);
        jFrame.add(jLabely);

        jButton.setVisible(true);
        jButton.setBounds(400, 510, 70, 30);
        jButton.addActionListener(event ->
        {
            if (!took_action){
                showMessageDialog(jFrame, "let the Ai take an action");
            }
            else{
                try {
                    int i=jComboBoxX.getSelectedIndex(), j=jComboBoxY.getSelectedIndex();
                    if (Board[i][j]!=Players.None){
                        showMessageDialog(jFrame, "this block is already taken!");
                    }
                    else{
                        player_human_action(i, j);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        jComboBoxX.setSelectedIndex(0);
        jComboBoxX.setVisible(true);
        jComboBoxX.setBounds(85, 510, 40, 30);

        jComboBoxY.setSelectedIndex(0);
        jComboBoxY.setVisible(true);
        jComboBoxY.setBounds(250, 510, 40, 30);

        jFrame.add(jButton);
        jFrame.add(jComboBoxX);
        jFrame.add(jComboBoxY);

        jFrame.setResizable(false);
        jFrame.getContentPane().setLayout(null);
        jFrame.setBounds(300, 50, 600, 600);
        jFrame.setVisible(true);
        jFrame.getContentPane().setBackground(Color.BLACK);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setTitle("PLAYER VS AI HEX GAME");

        player_ai_action();
    }

    private static void player_human_action(int i, int j) throws IOException {
        add_position(i, j, Players.Human);
        took_action = false;
        if (game_not_finished(Players.Human, Board)){
            player_ai_action();
        }
        else {
            showMessageDialog(jFrame, "YOU WIN!");
            System.exit(0);
        }
        countOfActions++;
    }

    private static void player_ai_action() throws IOException {
        ArrayList<Integer> ans = negamax(Math.min(DEPTH, 49-countOfActions), Board, MINIMUM, MAXIMUM, Players.AI);
        int x = ans.get(0);
        int y = ans.get(1);
        add_position(x, y, Players.AI);

        took_action=true;
        if (!game_not_finished(Players.AI, Board)){
            showMessageDialog(jFrame, "GAME OVER!");
            System.exit(0);
        }
        countOfActions++;
    }

    private static void add_position(int i, int j, Players player) throws IOException {
        Board[i][j] = player;
        jLabel[i][j] = new JLabel();
        if (player==Players.Human){
            jLabel[i][j].setIcon(new ImageIcon(ImageIO.read(new File("images/blue piece.png"))));
        }
        else{
            jLabel[i][j].setIcon(new ImageIcon(ImageIO.read(new File("images/red piece.png"))));
        }
        jLabel[i][j].setVisible(true);
        jLabel[i][j].setBounds(j*57+i*28+30, i*50+115, 40, 40);
        jFrame.add(jLabel[i][j]);
        jFrame.repaint();
    }

    public static ArrayList<Pair<Players[][], Pair<Integer, Integer>>> generatestates(Players[][] base_board, Players player){
        ArrayList<Pair<Players[][], Pair<Integer, Integer>>> ans = new ArrayList<>();
        for (int i=0; i < board_size; i++){
            for (int j=0; j < board_size; j++){
                if (base_board[i][j]==Players.None){
                    base_board[i][j] = player;
                    ans.add(new Pair<>(base_board, new Pair<>(i, j)));
                    base_board[i][j]=Players.None;
                }
            }
        }
        return ans;
    }

    private static ArrayList<Integer> negamax(int depthLeft, Players[][] board, int alpha, int beta, Players player){
        int x=0, y=0, stat = MINIMUM;
        ArrayList<Integer> ans = new ArrayList<>();
        if (depthLeft==1){
            for (Pair<Players[][], Pair<Integer, Integer>> possible_state: generatestates(board, player)){
                int oponentH  = heuristicscore((player==Players.AI?Players.Human: Players.AI), possible_state.first, 
                        possible_state.second.first, possible_state.second.second);
                int playerH = heuristicscore(player, possible_state.first, 
                        possible_state.second.first, possible_state.second.second);

                int possible_state_heuristic = playerH;
                if (oponentH==0) possible_state_heuristic = MAXIMUM;                
                if (possible_state_heuristic>stat){
                    stat = possible_state_heuristic;
                    x = possible_state.second.first;
                    y = possible_state.second.second;

                    alpha = Math.max(stat, alpha);
                    if (alpha>=beta){
                        ans.add(x);
                        ans.add(y);
                        ans.add(stat);
                        return ans;
                    }
                }
            }
        }
        else{
            for (Pair<Players[][], Pair<Integer, Integer>> possible_state: generatestates(board, player)){
                ArrayList<Integer> child_nodes_ans =negamax(depthLeft-1, possible_state.first,
                 -beta, -alpha, (player==Players.AI?Players.Human: Players.AI));
                if (-child_nodes_ans.get(2)>stat){
                    stat = -child_nodes_ans.get(2);
                    x = child_nodes_ans.get(0);
                    y = child_nodes_ans.get(1);

                    alpha = Math.max(stat, alpha);
                    if (alpha>=beta){
                        ans.add(x);
                        ans.add(y);
                        ans.add(stat);
                        return ans;
                    }
                }
            }
        }
        ans.add(x);
        ans.add(y);
        ans.add(stat);
        return ans;
    }

    private static boolean game_not_finished(Players player, Players[][] board){
        int[][] dis = new int[board_size*board_size][board_size*board_size];

        for (int i=0; i < board_size*board_size; i++)
            for (int j=0; j < board_size*board_size; j++) dis[i][j]=MAXIMUM;


        dis = connectNeighbors(board, dis, player);
        dis = floyd_warshall(dis);

        if (player==Players.Human){
            for (int ileft=0; ileft < board_size; ileft++){
                for (int iright=0; iright < board_size; iright++){
                    if (dis[ileft*board_size][iright*board_size+board_size-1]!=MAXIMUM){
                        return false;
                    }
                }
            }
        }
        else{
            for (int jup=0; jup < board_size; jup++){
                for (int jdown=0; jdown < board_size; jdown++){
                    if (dis[jup][(board_size-1)*board_size+jdown]!=MAXIMUM){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static int heuristicscore(Players player, Players[][] board, int x, int y){
        Pair<Boolean, Integer> g = g(board, x, y, player);
        if (!g.first) return MINIMUM;
        if (g.second==3) return 0;
        int h = heuristicPath(player, x, y, g.second);
        return -h;
    }

    public static Pair<Boolean, Integer> g(Players[][] board, int x, int y, Players player){
        int[][] dis = new int[board_size*board_size][board_size*board_size];
        for (int i=0; i < board_size*board_size; i++){
            for (int j=0; j < board_size*board_size; j++) {
                dis[i][j]=MAXIMUM;
                dis[j][j] = 0;
            }
            
        }

        board[x][y]=player;
        dis = connectNeighbors(board, dis, player);
        dis = floyd_warshall(dis);
        board[x][y] = Players.None;

        if (player == Players.AI){
            int toUp = 2;
            boolean connectedToUp = false, connectedToDown = false;
            for (int i=0; i < board_size; i++){
                if (dis[i][board_size*x+y]<MAXIMUM){
                    toUp = 0;
                    connectedToUp = true;
                }
                else if (dis[board_size*(board_size-1)+i][board_size*x+y]<MAXIMUM){
                    toUp = 1;
                    connectedToDown = true;
                }
                if (connectedToUp && connectedToDown) return new Pair<Boolean, Integer>(true, 3);
            }
            return new Pair<Boolean, Integer>(connectedToDown||connectedToUp, toUp);
        } 
        else{
            int toLeft = 2;
            boolean connectedToLeft = false, connectedToRight = false;
            for (int i=0; i < board_size; i++){
                if (dis[i*board_size][board_size*x+y]<MAXIMUM){
                    toLeft = 0;
                    connectedToLeft = true;
                }
                else if (dis[i*board_size+board_size-1][board_size*x+y]<MAXIMUM){
                    toLeft = 1;
                    connectedToRight = true;
                }
                if (connectedToLeft && connectedToRight) return new Pair<Boolean, Integer>(true, 3);
            }
            return new Pair<Boolean, Integer>(connectedToLeft||connectedToRight, toLeft);
        }
        
    }

    public static int heuristicPath(Players player, int x, int y, int to){
        if (player == Players.AI) 
            return (to==1?x: board_size-x-1)*2;
        return (to==1?y: board_size-y-1)*2;
    }

    public static int[][] floyd_warshall(int[][] dis){
        for (int k=0; k < board_size*board_size; k++){
            for (int i=0; i < board_size*board_size; i++){
                for (int j=0; j < board_size*board_size; j++){
                    if (dis[i][k]!=MAXIMUM && dis[k][j]!=MAXIMUM && dis[i][k]+dis[k][j]<dis[i][j])
                        dis[i][j]=dis[j][i]=dis[i][k]+dis[k][j];
                }
            }
        }
        return dis;
    }

    public static int[][] connectNeighbors(Players[][] board, int[][] dis, Players player){
        for (int i=0; i < board_size; i++){
            for (int j=0; j < board_size; j++){
                if (i!=board_size-1){
                    if (board[i][j]==player&&board[i+1][j]==player){
                        dis[i*board_size+j][(i+1)*board_size+j]=dis[(i+1)*board_size+j][i*board_size+j]=1;
                    }
                }
                if (j!=board_size-1){
                    if (board[i][j]==player&&board[i][j+1]==player){
                        dis[i*board_size+j][i*board_size+j+1]=dis[i*board_size+j+1][i*board_size+j]=1;
                    }
                }
                if (i!=0){
                    if (board[i][j]==player&&board[i-1][j]==player){
                        dis[i*board_size+j][(i-1)*board_size+j]=dis[(i-1)*board_size+j][i*board_size+j]=1;
                    }
                }
                if (j!=0){
                    if (board[i][j]==player&&board[i][j-1]==player){
                        dis[i*board_size+j][i*board_size+j-1]=dis[i*board_size+j-1][i*board_size+j]=1;
                    }
                }
                if (j!=0&&i!=board_size-1){
                    if (board[i][j]==player&&board[i+1][j-1]==player){
                        dis[i*board_size+j][(i+1)*board_size+j-1]=dis[(i+1)*board_size+j-1][i*board_size+j]=1;
                    }
                }
                if (j!=board_size-1&&i!=0){
                    if (board[i][j]==player&&board[i-1][j+1]==player){
                        dis[i*board_size+j][(i-1)*board_size+j+1]=dis[(i-1)*board_size+j+1][i*board_size+j]=1;
                    }
                }
            }
        }
        return dis;
    }

    private static JFrame jFrame;
    private static JLabel[][] jLabel;
    private static JButton jButton;
    private static JComboBox<Integer> jComboBoxX;
    private static JComboBox<Integer> jComboBoxY;
}

class Pair<F, S>{
    public F first;
    public S second;

    Pair(){}
    Pair(F f, S s){
        first=f;
        second=s;
    }
}
