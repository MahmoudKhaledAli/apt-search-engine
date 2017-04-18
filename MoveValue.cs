using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HexaBotImplementation
{
    public class MoveValue
    {

        private double value;
        private Move move;

        public MoveValue(double value, Move move)
        {
            this.value = value;
            this.move = move;
        }
        public MoveValue(double value)
        {
            this.value = value;
        }
        public double GetValue()
        {
            return value;
        }
        public Move GetMove()
        {
            return move;
        }

    }
}
