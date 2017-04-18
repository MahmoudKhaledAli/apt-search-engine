using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HexaBotImplementation
{
    public class AlphaBeta
    {
        bool maxPlayer = true;
        public AlphaBeta()
        {
        }
        public MoveValue Iterate(GameState state, int depth, double alpha, double beta, bool player)
        {
            MoveValue moveValue;
            MoveValue bestMove = null;

            if (depth == 0 || state.IsEnd())
            {
                return new MoveValue(state.GetTotalHeuristic());
            }

            if (player == maxPlayer)
            {
                foreach (GameState child in state.GenerateMoves(player))
                {
                    //if(child.CheckGameStatus() == GameState.GameStatus.WIN)
                    //{
                    //    return new MoveValue(double.MaxValue, new Move(state, child));
                    //}
                    moveValue = Iterate(child, depth - 1, alpha, beta, !player);
                    if (bestMove == null || (moveValue.GetValue() > bestMove.GetValue()))
                    {
                        bestMove = new MoveValue(moveValue.GetValue(), new Move(state, child));
                        alpha = bestMove.GetValue();
                    }

                    if (beta <= alpha)
                    {
                        break;
                    }

                }
                return bestMove;
            }
            else
            {
                foreach (GameState child in state.GenerateMoves(player))
                {
                    moveValue = Iterate(child, depth - 1, alpha, beta, !player);
                    if (bestMove == null || (moveValue.GetValue() < bestMove.GetValue()))
                    {
                        bestMove = new MoveValue(moveValue.GetValue(), new Move(state, child));
                        beta = bestMove.GetValue();
                    }

                    if (beta <= alpha)
                    {
                        break;
                    }
                }

                return bestMove;
            }
        }
    }
}
