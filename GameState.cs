using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.IO;

namespace HexaBotImplementation
{
    public class GameState
    {
        public enum GameStatus { WIN, LOSE, ONGOING };

        private int[,] board;

        const double probfactor = 0.05;

        const int rows = 11;

        const int cols = 11;

        const int ySize = 2 * rows - 1;

        static Dictionary<string, double> probabilityTable;

        static List<GameState> gameHistory;
        private struct Position
        {
            public int i;
            public int j;
            public Position(int i, int j) : this()
            {
                this.i = i;
                this.j = j;
            }
        }
        public GameState()
        {
            this.board = new int[rows, cols];
            for (int i = 0; i < rows; i++)
            {
                for (int j = 0; j < cols; j++)
                {
                    board[i, j] = 0;
                }
            }
        }
        public GameState(int[,] board)
        {
            this.board = CopyBoard(board);
        }
        public static int GetRows()
        {
            return rows;
        }
        public static int GetCols()
        {
            return cols;
        }

        public int[,] GetBoard()
        {
            return board;
        }
        public GameState Copy()
        {
            return new GameState(board);
        }
        public static void ReadProbabilityTable()
        {
            //TODO reads probabilities from file
            probabilityTable = new Dictionary<string, double>();
            Deserialize();
            return;
        }
        private string GetString()
        {
            string boardState = "";
            for (int i = 0; i < rows; i++)
            {
                for (int j = 0; j < cols; j++)
                {
                    if (board[i, j] == 0)
                    {
                        boardState += "0";
                    }
                    else if (board[i, j] == 1)
                    {
                        boardState += "1";
                    }
                    else
                    {
                        boardState += "2";
                    }
                }
            }
            return boardState;
        }
        public static void InitHistory()
        {
            gameHistory = new List<GameState>();
        }
        public void AddToHistory()
        {
            gameHistory.Add(new GameState(board));
        }
        public static void UpdateProbabilityTable(GameState status)
        {
            if (status.CheckGameStatus() == GameStatus.WIN)
            {
                for (int i = 0; i < gameHistory.Count(); i++)
                {

                    if (probabilityTable.ContainsKey(gameHistory[i].GetString()))
                    {
                        probabilityTable[gameHistory[i].GetString()] += ((i * probfactor) / gameHistory.Count());
                    }
                    else
                    {
                        probabilityTable.Add(gameHistory[i].GetString(), (i * probfactor) / gameHistory.Count());
                    }
                }
            }
            else
            {
                for (int i = 0; i < gameHistory.Count(); i++)
                {

                    if (probabilityTable.ContainsKey(gameHistory[i].GetString()))
                    {
                        probabilityTable[gameHistory[i].GetString()] -= ((i * probfactor) / gameHistory.Count());
                    }
                    else
                    {
                        probabilityTable.Add(gameHistory[i].GetString(), -(i * probfactor) / gameHistory.Count());
                    }
                }
            }

            Serialize();

            //TODO updates and re-writes probability table based on gameHistory
            return;
        }
        private static int[,] CopyBoard(int[,] board)
        {
            int[,] temp = new int[rows, cols];
            for (int i = 0; i < rows; i++)
            {
                for (int j = 0; j < cols; j++)
                {
                    temp[i, j] = board[i, j];
                }
            }
            return temp;
        }
        public List<GameState> GenerateMoves(bool player)
        {
            //TODO Generate all possible game states from current state
            bool firstMove = true;
            bool oneMove = false;

            for (int i = 0; i < rows; i++)
            {
                for (int j = 0; j < cols; j++)
                {
                    if (board[i, j] != 0 && !oneMove) 
                    {
                        oneMove = true;
                    }
                    if (board[i, j] != 0 && oneMove)
                    {
                        firstMove = false;
                    }
                }
            }

            int move;
            List<GameState> generatedStates = new List<GameState>();

            if (firstMove)
            {
                generatedStates.Add(Transpose());
            }

            if (player)
            {
                move = 1; //mine
            }
            else
            {
                move = 2; //the opponent's
            }

            for (int i = 0; i < rows; i++)
            {
                for (int j = 0; j < cols; j++)
                {
                    if (board[i, j] == 0)
                    {
                        int[,] newBoard = CopyBoard(board);
                        newBoard[i, j] = move;
                        GameState generated = new GameState(newBoard);
                        generatedStates.Add(generated);
                    }
                }
            }
            return generatedStates;
        }
        private bool InRange(int i, int j)
        {
            return i >= 0 && i < rows && j >= 0 && j < cols;
        }
        private List<Position> GetAdjacent(Position pos)
        {
            List<Position> adjacents = new List<Position>();
            if (InRange(pos.i + 1, pos.j))
            {
                adjacents.Add(new Position(pos.i + 1, pos.j));
            }

            if (InRange(pos.i - 1, pos.j))
            {
                adjacents.Add(new Position(pos.i - 1, pos.j));
            }

            if (InRange(pos.i, pos.j + 1))
            {
                adjacents.Add(new Position(pos.i, pos.j + 1));
            }

            if (InRange(pos.i, pos.j - 1))
            {
                adjacents.Add(new Position(pos.i, pos.j - 1));
            }

            if (InRange(pos.i - 1, pos.j + 1))
            {
                adjacents.Add(new Position(pos.i - 1, pos.j + 1));
            }

            if (InRange(pos.i + 1, pos.j - 1))
            {
                adjacents.Add(new Position(pos.i + 1, pos.j - 1));
            }

            return adjacents;
        }
        public GameStatus CheckGameStatus()
        {
            //TODO
            //If I win
            Queue<Position> myPositions = new Queue<Position>();
            Position myPosition;
            int[,] dummyBoard = CopyBoard(board);

            for (int i = 0; i < rows; i++)
            {
                if (dummyBoard[i, 0] == 1)
                {
                    myPositions.Enqueue(new Position(i, 0));
                    dummyBoard[i, 0] = 0;
                }
            }

            while (myPositions.Count != 0)
            {
                myPosition = myPositions.Dequeue();
                foreach (Position pos in GetAdjacent(myPosition))
                {
                    if (dummyBoard[pos.i, pos.j] == 1)
                    {
                        if (pos.j == cols - 1)
                        {
                            return GameStatus.WIN;
                        }
                        else
                        {
                            myPositions.Enqueue(pos);
                            dummyBoard[pos.i, pos.j] = 0;
                        }
                    }
                }
            }

            dummyBoard = CopyBoard(board);
            Queue<Position> oppPositions = new Queue<Position>();
            Position oppPosition;

            for (int i = 0; i < cols; i++)
            {
                if (dummyBoard[0, i] == 2)
                {
                    oppPositions.Enqueue(new Position(0, i));
                    dummyBoard[0, i] = 0;
                }
            }

            while (oppPositions.Count != 0)
            {
                oppPosition = oppPositions.Dequeue();
                foreach (Position pos in GetAdjacent(oppPosition))
                {
                    if (dummyBoard[pos.i, pos.j] == 2)
                    {
                        if (pos.i == rows - 1)
                        {
                            return GameStatus.LOSE;
                        }
                        else
                        {
                            oppPositions.Enqueue(pos);
                            dummyBoard[pos.i, pos.j] = 0;
                        }
                    }
                }
            }

            //If game is still going
            return GameStatus.ONGOING;
        }
        public bool IsEnd()
        {
            GameStatus status = CheckGameStatus();
            return status == GameStatus.WIN || status == GameStatus.LOSE;
        }
        public double GetTurnNo()
        {
            double count = 0;
            for (int i = 0; i < rows; i++)
            {
                for (int j = 0; j < cols; j++)
                {
                    if (board[i, j] == 1)
                    {
                        count++;
                    }
                }
            }
            return count;
        }
        private double GetThreats()
        {
            //TODO Get the heuristic function of the threats
            return 1.0d;
        }
        public double GetYReduction()
        {
            //TODO Get the y reduction heuristic
            return MicroReduction(ConvertToY(), ySize);
        }
        public double GetOtherYReduction()
        {
            return MicroReduction(Transpose().ConvertToY(), ySize);
        }
        private double GetProbability()
        {
            if (probabilityTable.ContainsKey(this.GetString()))
            {
                return probabilityTable[this.GetString()];
            }
            else
            {
                return 0.5d;
            }
        }
        public double GetTotalHeuristic()
        {
            if (CheckGameStatus() == GameStatus.WIN)
            {
                return 20;
            }
            else if (CheckGameStatus() == GameStatus.LOSE)
            {
                return -1;
            }
            return GetThreats() * GetYReduction() * GetProbability() * 60.0d / GetTurnNo();
        }
        private static void Deserialize()
        {
            try
            {
                var f_fileStream = File.OpenRead(@"dictionarySerialized.xml");
                var f_binaryFormatter = new System.Runtime.Serialization.Formatters.Binary.BinaryFormatter();
                probabilityTable = (Dictionary<string, double>)f_binaryFormatter.Deserialize(f_fileStream);
                f_fileStream.Close();
            }
            catch (Exception ex)
            {
                ;
            }
        }
        private static void Serialize()
        {
            try
            {
                var f_fileStream = new FileStream(@"dictionarySerialized.xml", FileMode.Create, FileAccess.Write);
                var f_binaryFormatter = new System.Runtime.Serialization.Formatters.Binary.BinaryFormatter();
                f_binaryFormatter.Serialize(f_fileStream, probabilityTable);
                f_fileStream.Close();
            }
            catch (Exception ex)
            {
                ;
            }
        }
        private double[][] CreateNewY(int size)
        {
            double[][] gameOfY = new double[size][];
            for (int i = 0; i < size; i++)
            {
                gameOfY[i] = new double[i + 1];
            }
            return gameOfY;
        }
        private double[][] ConvertToY()
        {
            double[][] gameOfY = CreateNewY(ySize);

            for (int i = 0; i < rows - 1; i++)
            {
                for (int j = 0; j < i + 1; j++)
                {
                    gameOfY[i][j] = 0;
                }
            }
            for (int i = rows - 1; i < ySize; i++)
            {
                for (int j = 0; j < i - rows + 1; j++)
                {
                    gameOfY[i][j] = 1;
                }
                for (int j = i - rows + 1; j < i + 1; j++)
                {
                    switch (board[i - rows + 1, j - i + rows - 1])
                    {
                        case 0:
                            gameOfY[i][j] = 0.5d;
                            break;
                        case 1:
                            gameOfY[i][j] = 1.0d;
                            break;
                        case 2:
                            gameOfY[i][j] = 0.0d;
                            break;
                        default:
                            gameOfY[i][j] = 0.5d;
                            break;
                    }
                }
            }

            return gameOfY;
        }
        private double CalculateReductionProbability(double p1, double p2, double p3)
        {
            return p1 * p2 + p1 * p3 + p2 * p3 - 2 * p1 * p2 * p3;
        }
        private double MicroReduction(double[][] gameOfY, int size)
        {
            if (size == 1)
            {
                return gameOfY[0][0];
            }

            double[][] newGameOfY = CreateNewY(size - 1);

            for (int i = 0; i < size - 1; i++)
            {
                for (int j = 0; j < i + 1; j++)
                {
                    newGameOfY[i][j] = CalculateReductionProbability(gameOfY[i][j], gameOfY[i + 1][j + 1], gameOfY[i + 1][j]);
                }
            }

            return MicroReduction(newGameOfY, size - 1);
        }
        public void ApplyMove(Move move, bool player)
        {
            if(move.GetMoveI() == -1 && move.GetMoveJ() == -1)
            {
                board = Transpose().board;
            }
            int newHex = player ? 1 : 2;
            board[move.GetMoveI(), move.GetMoveJ()] = newHex;
        }
        public GameState Transpose()
        {
            GameState tranposed = new GameState();

            for (int i = 0; i < rows; i++)
            {
                for (int j = 0; j < cols; j++)
                {
                    switch (board[j,i])
                    {
                        case 1:
                            tranposed.board[i, j] = 2;
                            break;
                        case 2:
                            tranposed.board[i, j] = 1;
                            break;
                        default:
                            tranposed.board[i, j] = 0;
                            break;
                    }
                }
            }
            return tranposed;
        }
        public int GetEmptyCount()
        {
            int noOfMoves = 0;
            for (int i = 0; i < rows; i++)
            {
                for (int j = 0; j < cols; j++)
                {
                    if (board[i,j] == 0)
                    {
                        noOfMoves++;
                    }
                }
            }
            return noOfMoves;
        }
    }
}