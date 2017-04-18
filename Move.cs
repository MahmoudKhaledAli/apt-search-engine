using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HexaBotImplementation
{
    public class Move
    {
        private int moveI;

        private int moveJ;
        public Move(int moveI, int moveJ)
        {
            this.moveI = moveI;
            this.moveJ = moveJ;
        }
        
        public Move(GameState prevState, GameState nextState)
        {
            int[,] prevBoard = prevState.GetBoard();
            int[,] nextBoard = nextState.GetBoard();
            for (int i = 0; i < GameState.GetRows(); i++)
            {
                for (int j = 0; j < GameState.GetCols(); j++)
                {
                    if (prevBoard[i, j] == 2 && nextBoard[j, i] == 1)
                    {
                        this.moveI = -1;
                        this.moveJ = -1;
                    }
                }
            }
            for (int i = 0; i < GameState.GetRows(); i++)
            {
                for (int j = 0; j < GameState.GetCols(); j++)
                {
                    if (prevBoard[i,j] != nextBoard[i,j])
                    {
                        this.moveI = i;
                        this.moveJ = j;
                        return;
                    }
                }
            }
        }
        public int GetMoveI()
        {
            return moveI;
        }
        public int GetMoveJ()
        {
            return moveJ;
        }
        public Move Tranpose()
        {
            return new Move(moveJ, moveI);
        }

    }
}
