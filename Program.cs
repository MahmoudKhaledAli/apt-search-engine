using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HexaBotImplementation
{
    class Program
    {
        static void Main(string[] args)
        {
            GameState state = new GameState();
            GameState otherState;
            GameState.ReadProbabilityTable();
            GameState.InitHistory();
            int[,] x = state.GetBoard();
            string inp;
            int i, j;
            AlphaBeta search = new AlphaBeta();

            while (true)
            {
                MoveValue cpuMove = search.Iterate(state, 3, double.MinValue, double.MaxValue, true);
                state.ApplyMove(cpuMove.GetMove(), true);
                state.AddToHistory();

                //inp = Console.ReadLine();
                //i = Int32.Parse(inp);
                //inp = Console.ReadLine();
                //j = Int32.Parse(inp);
                //state.ApplyMove(new Move(i, j), true);

                for (int z = 0; z < GameState.GetRows(); z++)
                {
                    for (int y = 0; y < GameState.GetCols(); y++)
                    {
                        Console.Write(state.GetBoard()[z, y]);
                    }
                    Console.Write("\n");
                    //for (int l = 0; l < z + 1; l ++)
                    //{
                    //    Console.Write(" ");
                    //}
                }

                if (state.CheckGameStatus() != GameState.GameStatus.ONGOING)
                {
                    break;
                }

                Console.WriteLine("\n" + state.GetYReduction());
                Console.WriteLine("\n" + state.GetOtherYReduction());


                otherState = state.Transpose();
                MoveValue cpu2Move = search.Iterate(otherState, 3, double.MinValue, double.MaxValue, true);
                state.ApplyMove(cpu2Move.GetMove().Tranpose(), false);
                state.AddToHistory();


                for (int z = 0; z < GameState.GetRows(); z++)
                {
                    for (int y = 0; y < GameState.GetCols(); y++)
                    {
                        Console.Write(state.GetBoard()[z, y]);
                    }
                    Console.Write("\n");
                    //for (int l = 0; l < z + 1; l++)
                    //{
                    //    Console.Write(" ");
                    //}
                }

                Console.WriteLine("\n" + state.GetYReduction());
                Console.WriteLine("\n" + state.GetOtherYReduction());

                if (state.CheckGameStatus() != GameState.GameStatus.ONGOING)
                {
                    break;
                }
                
            }
            Console.WriteLine(state.CheckGameStatus());
            GameState.UpdateProbabilityTable(state);
        }
    }
}
