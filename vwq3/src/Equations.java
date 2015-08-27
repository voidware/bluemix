/**
 * Copyright (c) Voidware ltd. 2015
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS," WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 * 
 * contact@voidware.com
 */

import java.io.*;

public class Equations
{
    private final StringBuffer results = new StringBuffer();
    private String    _line;
    private int       _pos;
    private int       _value;

    class MalformedInputException extends Exception
    {
        public MalformedInputException(String m) { super(m); }
    }

    // class to hold coefficients for an equation
    class Equation
    {
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(_cx);
            sb.append('x');
            if (_cy >= 0) sb.append('+');
            sb.append(_cy);
            sb.append('y');
            sb.append('=');
            sb.append(_val);
            return sb.toString();
        }

        // cx*X + cy*Y = val
        int _cx;
        int _cy;
        int _val;
    }

    private char currentChar()
    {
        char c = 0;
        if (_pos < _line.length())
            c = Character.toUpperCase(_line.charAt(_pos));
        return c;
    }

    private char nextChar()
    {
        ++_pos;
        return currentChar();
    }

    private void skipSpace()
    {
        while (Character.isWhitespace(currentChar())) ++_pos;
    }

    private boolean parseNumber()
    {
        StringBuilder sb = new StringBuilder();
        boolean neg;

        skipSpace();
        char c = currentChar();

        // handle leading minus sign or plus
        if ((neg = (c == '-')) || c == '+') c = nextChar();
        
        // collect digits
        while (Character.isDigit(c))
        {
            sb.append(c);
            c = nextChar();
        }

        // implied value
        _value = 1;

        boolean valid = sb.length() > 0;
        if (valid) _value = Integer.parseInt(sb.toString());
        if (neg) _value = -_value;
        return valid;
    }

    private Equation parseEquation(String s) throws MalformedInputException
    {
        _line = s;
        _pos = 0;
        Equation eq = new Equation();

        while (true)
        {
            boolean valid = parseNumber();
            
            skipSpace();
            char c = currentChar();
            
            if (c == 'X')
            {
                // collect X coefficients
                eq._cx += _value;
                ++_pos;
            }
            else if (c == 'Y')
            {
                // collect Y coefficients
                eq._cy += _value;
                ++_pos;
            }
            else if (c == '=')
            {
                if (valid) eq._val -= _value; // pickup eg x+y-1=2
                ++_pos;

                // get RHS value.
                valid = parseNumber();

                // check for trailing garbage, eg x+y=2z
                if (valid && currentChar() != 0) valid = false;
                
                if (!valid)
                    throw new MalformedInputException("malformed number " + _line);
                eq._val += _value;
                break;
            }
            else
                throw new MalformedInputException("unexpected term: " + _line);
        }
        return eq;
    }

    private String format(double v)
    {
        // format the output result neatly.
        int vi = (int)v;
        return vi == v ? String.valueOf(vi)
            : String.valueOf(v).replaceFirst("\\.?0+(e|$)", "$1");
    }

    public String calculate(String arg)
    {
        // receive input data from web page. Perform calculation and
        // return result to web page as a string.
        
        BufferedReader in = new BufferedReader(new StringReader(arg));
        try
        {
            Equation eqs[] = new Equation[2];
            int eqc = 0;
            String line;
            while ((line = in.readLine()) != null)
            {
                line = line.trim(); 
                if (line.startsWith("##")) break;
                if (line.startsWith("//")) continue; // skip line comments
                try
                {
                    if (line.length() > 0) // skip blank lines
                    {
                        if (line.startsWith("#"))
                        {
                            if (eqc > 0)
                                throw new MalformedInputException("require two equations");
                            eqc = 0;
                            continue;
                        }
                        eqs[eqc] = parseEquation(line);
                        if (++eqc == 2)
                        {
                            // we have two equations. solve them.
                            eqc = 0;

                            // extract values
                            long a = eqs[0]._cx;
                            long b = eqs[0]._cy;
                            long u = eqs[0]._val;
                            long c = eqs[1]._cx;
                            long d = eqs[1]._cy;
                            long v = eqs[1]._val;

                            // wish to solve:
                            // aX + bY = u
                            // cX + dY = v.

                            double x, y;
                            double det = (double)(a*d - b*c);

                            // if the determinant is non-zero, there is a 
                            // solution. Otherwise the equations are linearly
                            // dependent and no unique solution exists.
                            if (det != 0)
                            {
                                x = (d*u - b*v)/det;
                                y = (a*v - c*u)/det;
                                
                                // format the solution
                                results.append("x=").append(format(x)).append(" y=").append(format(y)).append('\n');
                            
                            }
                            else
                            {
                                results.append("No unique solution for, ").append(eqs[0].toString()).append(", ").
                                    append(eqs[1].toString()).append('\n');
                            }
                            
                        }
                    }
                }
                catch (MalformedInputException e)
                {
                    eqc = 0;
                    results.append(e.getMessage()).append('\n');
                }
                catch (Exception e)
                {
                    eqc = 0;
                    results.append("malformed input: \"").append(line).append("\"\n");
                }
            }
        }
        catch (IOException e)
        {
            results.append("error reading input\n");
        }

        // return results to web page.
        return results.toString();
    }
}
