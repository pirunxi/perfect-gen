﻿using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

namespace cfg
{
    public class DataStream
    {

        private readonly string[] line;
        private int index;
        DataStream(string linestr)
        {
            line = linestr.Split(',');
            index = 0;
        }

        public string GetNext()
        {
            return index < line.Length ? line[index++] : null;
        }

        void Error(string err)
        {
            throw new Exception(err);
        }

        string GetNextAndCheckNotEmpty()
        {
            var s = GetNext();
            if (s == null)
                Error("read not enough");
            return s;
        }

        public string GetString()
        {
            return GetNextAndCheckNotEmpty().Replace("#enter#", "\n").Replace("#comma#", ",");
        }

        public float GetFloat()
        {
            return float.Parse(GetNextAndCheckNotEmpty());
        }

        public double GetDouble()
        {
            return double.Parse(GetNextAndCheckNotEmpty());
        }

        public int GetInt()
        {
            return int.Parse(GetNextAndCheckNotEmpty());
        }

        public long GetLong()
        {
            return long.Parse(GetNextAndCheckNotEmpty());
        }
		
        public bool GetBool()
        {
            var s = GetNextAndCheckNotEmpty().ToLower();
            if (s == "true")
                return true;
            if (s == "false")
                return false;
            Error(s + " isn't bool");
            return false;
        }

        public cfg.CfgObject GetObject(string name)
        {
            return (cfg.CfgObject)Type.GetType(name).GetConstructor(new[] { typeof(cfg.DataStream) }).Invoke(new object[] { this });
        }

        public static List<DataStream> Records(string file, string encoding)
        {
            var dss = new List<DataStream>();
            foreach(var line in File.ReadAllLines(file))
            {
                dss.Add(new DataStream(line));
            }
            return dss;
        }
		
    }
}
