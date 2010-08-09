package org.crossref.pdfstamp;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

/**
 * 
 * 
 * @author karl.j.ward@gmail.com
 */
public class StampTupleOptionHandler extends OptionHandler<StampTuple> {

    public StampTupleOptionHandler(CmdLineParser parser, OptionDef option,
            Setter<? super StampTuple> setter) {
        super(parser, option, setter);
    }

    @Override
    public String getDefaultMetaVariable() {
        return null;
    }

    @Override
    public int parseArguments(Parameters params) throws CmdLineException {
        final String paramValue = params.getParameter(0);
        final String[] components = paramValue.split(",");
        
        if (components.length != 2) {
            /* Each StampTuple should have x,y . */
            throw new CmdLineException("Must specify X,Y for location.");
        }
        
        StampTuple st = new StampTuple();
        
        try {
            st.x = Float.parseFloat(components[0]);
            st.y = Float.parseFloat(components[1]);
        } catch (NumberFormatException e) {
            throw new CmdLineException("X and Y must be specified as rational numbers.");
        }
        
        setter.addValue(st);
        
        return 1;
    }

}
