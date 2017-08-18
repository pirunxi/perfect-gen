using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using cfg;

namespace test
{
    class Program
    {
        static void Main(string[] args)
        {
            cfg._Tables_.DataDir = "D:/workspace/perfect-gen.git/trunk/data";
            System.Console.WriteLine("++++");
            cfg._Tables_.Load();
            System.Console.WriteLine("++++");
        }
    }
}
