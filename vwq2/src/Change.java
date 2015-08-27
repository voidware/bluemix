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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* 
 * Find optimal solution to the Change Making Problem
 * 
 * https://en.wikipedia.org/wiki/Change-making_problem
 *
 * "How can a given amount of money be made with the least number of coins
 * of given denominations?"
 * 
 * For example, interesting cases occur with "old money", ie pre-decimalisation
 * (before 1971) UK coinage:
 *
 * The coins were:
 *   1 penny (1d)
 *   3 pennies (3d, thrupp'nce)
 *   6 sixpence (6d, a tanner)
 *   12 shilling (1s, a bob)
 *   24 florin (2s, two bob)
 *   30 half crown (2s 6d)
 *   60 crown (5s)
 * 
 * Consider making 50p.
 * Optimum solution is 24x2,1x2 (two florins and 2 pennies); that's 4 coins.
 * But the standard "counting out" solution (greedy) produces:
 *               30x1,12x1,6x1,1x2
 * which is 5 coins.              
 *
 * This shows that simply counting out the change, ie repeatedly dispensing
 * the biggest coin not exceeding the value to make, does not give the best
 * result in some _real world_ money systems.
 * 
 */
 
public class Change
{
    private final StringBuffer results = new StringBuffer();

    // the array of coin denominations
    private int[]   _coins;

    // the solution represented as an array of denomination counts.
    private int[]   _solution;

    // working upper limit to solution
    private int     _limit;
    
    // working partial solution
    private int[]   _s;

    // infeasibility cutoff
    private int[]   _cutoff;

    // debug counter
    private int     _count;

    private static int coinCount(int[] s)
    {
        // count the number of coins in the (partial) solution.
        int c = 0;
        for (int value : s) c += value;
        return c;
    }

    private int upperLimit(int val)
    {
        // calculate an upper limit > the optimal solution to make `val'
        // from `coins'.
        // assume `coins' array descending
        int lim = 0;
        int v = val;
        for (int ci : _coins)
        {
            int q = v / ci;
            v -= q * ci;
            lim += q;
        }

        if (v > 0)
            lim = val/_coins[_coins.length-1];

        return lim + 1;
    }

    private boolean greedy(int val, int[] sol)
    {
        // make `val' from `coins' using greedy method.
        // return false if failed.
        // assume `coins' array descending
        int n = _coins.length;
        int v = val;
        for (int i = 0; i < n; ++i)
        {
            int ci = _coins[i];
            int q = v/ci;
            v -= q * ci;
            sol[i] = q;
        }
        return v == 0;
    }

    private boolean mnt()
    {
        // Canonical test of Magazine-Nemhauser-Trotter, 1975
        //
        // true if coin system is canonical. ie the greedy method
        // finds the optimal answer for all values.
        // 
        // assume `coins' array descending
        int n = _coins.length;
        if (n < 2) return true;

        boolean res = false;
        if (_coins[n-1] == 1)
        {
            res = true;
            if (n >= 3) 
            {
                int[] sol = new int[n];
                for (int t = 1; t < n; ++t)
                {
                    int ct1 = _coins[t-1];
                    int ct = _coins[t];
                    int mt = ct1/ct;
                    int st = mt*ct;
                    if (ct1 != st)
                    {
                        st += ct;
                        ++mt;
                    }
                    greedy(st - ct1, sol);
                    if (coinCount(sol) >= mt)
                    {
                        res = false;
                        break;
                    }
                }
            }
        }
        return res;
    }

    private boolean changeMaker(int i, int val)
    {
        // Truncating Recursive method.
        // 
        // A tree search is made, where at each point, either the next
        // coin is taken or that coin is skipped.
        //
        // This would search all combinations except that the search can be
        // vastly accelerated by not taking branches (truncation) that can be
        // proven to lead to, at best, inferior solutions. The result is 
        // a method faster than the "usual" dynamic programming 
        // O(val*ncoins) method.
        
        int m = _coins.length;

        ++_count;


        // find the nest coin not exceeding current value.
        while (_coins[i] > val)
        {
            if (++i >= m)
            {
                _cutoff[val] = -1;
                return false;
            }
        }

        boolean more = i < m-1;
        int ci = _coins[i];
        int ni = val/ci;
        int lb = ni;

        // calculate a lower bound to the number of coins not yet added
        // to solution.
        if (more)
        {
            int v = val - lb*ci;
            int q = v/_coins[i+1];
            int r = v - q*_coins[i+1];
            if (r > 0) ++q;
            lb += q;
            ni = 1;
        }

        // if the coin count of the current partial solution plus the
        // lower bound to the number of coins not yet added is inferior
        // to the current best limit, this branch cannot work and is
        // truncated.
        int c = coinCount(_s);
        if (lb + c >= _limit)
        {
            _cutoff[val] = -1;
            return false;

        }

        // have we found a new solution?
        // if so, update best limit and keep copy of this solution.
        if (val == ni*ci)
        {
            _limit = c + ni;

            // update best solution
            System.arraycopy(_s, 0, _solution, 0, m);
            _solution[i] += ni;
            return true;
        }

        // consider now branching either taking the next coin or skipping it.
        boolean s2 = false;
        if (more)
        {
            // try taking next coin
            if (_cutoff[val - ci] >= 0)
            {
                ++_s[i];
                s2 = changeMaker(i, val - ci);
                --_s[i];
            }

            // try leaving next coin
            if (changeMaker(i+1, val)) s2 = true;
        }

        if (!s2)
            _cutoff[val] = -1;

        return s2;
    }

    private void handleLine(String line)
    {
        // split off the list of coin values from the amount to make
        String[] inputs = line.split(":");

        // `val' is the amount to make
        int val = Integer.parseInt(inputs[1].trim());

        // split the coin denominations and sort them in descending order
        String[] coinStr = inputs[0].split(",");
        List<Integer> coinInt = new ArrayList<>();
        for (String aCoinStr : coinStr)
        {
            int ci = Integer.parseInt(aCoinStr.trim());
            if (ci > 0) coinInt.add(ci); // ignore bent coins
        }

        boolean ok = false;
        int m = coinInt.size();
        
        if (m > 0)
        {
            Collections.sort(coinInt, Collections.reverseOrder());

            // make the array of the coin denominations
            _coins = new int[m];
            for (int i = 0; i < m; ++i) _coins[i] = coinInt.get(i);

            // create a solution array which has a coin count for each denomination
            _count = 0;
            _solution = new int[m];
            
            // test to see if this coin system is canonical.
            ok = mnt();

            if (ok)
            {
                // in canonical systems the greedy solution is optimal
                greedy(val, _solution);
            }
            else
            {
                // otherwise apply truncated recursion
                _s = new int[m];
                _cutoff = new int[val + 1];

                // calculate an upper limit for the number of coins in solution
                _limit = upperLimit(val);
            
                // perform search
                ok = changeMaker(0, val);
            }
            
            if (ok)
            {
                // print out the solution
                int c = 0;
                for (int i = 0; i < m; ++i)
                {
                    if (_solution[i] > 0)
                    {
                        if (c++ != 0) results.append(',');
                        results.append(_coins[i]).append("x").append(_solution[i]);
                    }
                }

                // debug
                //results.append(" trials:").append(_count);

                results.append('\n');
            }
        }

        if (!ok)
            results.append(line).append(" has no solution\n");
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
                        handleLine(line);
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
