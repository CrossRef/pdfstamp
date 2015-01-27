/**
The MIT License (MIT)

Copyright (c) 2015 Appazur Solutions Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.appazur.pdfstamp;

import org.kohsuke.args4j.CmdLineException;

public class TextStampTuple {
    public float x;
    public float y;
    public String text;

    public TextStampTuple(String paramValue) throws CmdLineException {
        final String[] components = paramValue.split(",");
        
        if (components.length != 3) {
            /* Each TextStampTuple should have x,y,text . */
            throw new CmdLineException("Must specify X,Y,TEXT for text stamp.");
        }
        
        try {
            this.x = Float.parseFloat(components[0]);
            this.y = Float.parseFloat(components[1]);

            // Since spaces are used to separate arguments to -t,
            // need to provide an alternate way to provide spaces.
            this.text = components[2].replace('_',' ');

        } catch (NumberFormatException e) {
            throw new CmdLineException("X and Y must be specified as rational numbers.");
        }
    }
}
