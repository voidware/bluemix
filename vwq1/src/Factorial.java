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

public class Factorial
{
    private final StringBuffer results = new StringBuffer();

    private static long factorial(int x)
    {
        // return factorial of `x'. valid for 0 <= x < 21
        long v = 1;
        for (int i = 2; i <= x; ++i) v *= i;
        return v;
    }

    public String calculate(String arg)
    {
        // receive input data from web page. Perform calculation and
        // return result to web page as a string.
        
        BufferedReader in = new BufferedReader(new StringReader(arg));
        try
        {
            String line;
            while ((line = in.readLine()) != null)
            {
                line = line.trim(); 
                if (line.startsWith("#")) break;
                if (line.startsWith("//")) continue; // skip line comments
                try
                {
                    if (line.length() > 0) // skip blank lines
                    {
                        int n = Integer.parseInt(line);

                        // ensure we're in a valid range 
                        if (n >= 0 && n < 21)
                            results.append(factorial(n));
                        else
                            results.append("value ").append(n).append(" out of range");
                        results.append('\n');
                    }
                }
                catch (Exception e)
                {
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
