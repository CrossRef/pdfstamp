package org.crossref.pdfstamp;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

public class StampTupleOptionHandler extends OptionHandler<StampTuple> {

    protected StampTupleOptionHandler(CmdLineParser parser, OptionDef option,
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
        
        if (components.length % 3 != 0) {
            /* Each StampTuple should have page,x,y . */
            throw new CmdLineException("Must specify page,x,y for each stamp.");
        }
        
        for (int idx=0; idx<components.length; idx+=3) {
            StampTuple st = new StampTuple();
            
            try {
                st.page = Integer.parseInt(components[idx]);
            } catch (NumberFormatException e) {
                throw new CmdLineException("Page must be specified as an integer.");
            }
            
            try {
                st.x = Float.parseFloat(components[idx+1]);
                st.y = Float.parseFloat(components[idx+2]);
            } catch (NumberFormatException e) {
                throw new CmdLineException("X and y must be specified as rational numbers.");
            }
            
            setter.addValue(st);
        }
        
        return components.length / 3;
    }

}
