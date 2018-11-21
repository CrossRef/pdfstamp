package org.crossref.pdfstamp;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

// -u "http://blah.com" -i somefile.jpeg -l 1,44.5,22.3,3,22.2,22.2 some/dir
//                                                          or some.file

// or:
//-u "http://blah.com" -i somefile.jpeg -l 1,44.5,22.3 -l 3,22.2,22.2 some/dir
//                                                          or some.file
public class Main {

    @Option(name="-p", usage="Optional. Page numbers to stamp. -1 is the last page.",
            required=false, multiValued=true, metaVar="N")
    private List<Integer> pages = new ArrayList<Integer>();

    @Option(name="-pp", usage="Optional. Page range to stamp, inclusive of start and end. e.g. 2-5.",
            required=false, multiValued=true, metaVar="N-N")
    private List<String> pageRanges = new ArrayList<String>();

    @Option(name="-l", usage="Required. Location on page to apply stamp.",
            required=true, multiValued=false, metaVar="X,Y")
    private StampTuple stampLocation = new StampTuple();
    
    @Option(name="-e", usage="Optional. Extension appended to the PDF filename.",
            required=false, multiValued=true, metaVar="EXT")
    private String outputExtension = "stamped";
    
    @Option(name="-r", usage="Optional. Descend recursively into directories.")
    private boolean recursive = false;
    
    @Option(name="-u", usage="Optional. Target URL of the stamp.", 
            required=false, multiValued=false, metaVar="URL")
    private String url = "";
    
    @Option(name="-i", usage="Required. Image file containing image of the stamp.", 
            required=true, multiValued=false)
    private File imageFile = new File(".");
    
    @Option(name="-o", usage="Optional. Output directory.",
            required=false, multiValued=false)
    private File outputDirectory = null;
    
    @Option(name="-v", usage="Optional. Verbose output.",
            required=false, multiValued=false)
    private boolean verbose = false;
    
    @Option(name="-d", usage="Optional. Target DPI. Defaults to 300.",
            required=false, multiValued=false)
    private int targetDpi = 300;
    
    @Argument
    private List<String> paths = new ArrayList<String>();
    
    private Image stampImage = null;
    
    private static Image openImage(File f) throws BadElementException, 
            MalformedURLException, IOException {
        return Image.getInstance(f.getAbsolutePath());
    }
    
    /**
     * @return Answers a PdfReader for the File f.
     */
    private static PdfReader openPdf(File f) throws IOException {
        FileInputStream fIn = new FileInputStream(f);
        PdfReader reader = null;
        try {
            reader = new PdfReader(fIn);
        } finally {
            fIn.close();
        }
        return reader;
    }
    
    /**
     * @return Answers true if the first four bytes of File f match the
     * PDF magic number - "%PDF".
     */
    private static boolean isPdfFile(File f) throws IOException {
        FileInputStream fIn = new FileInputStream(f);
        byte[] magic = new byte[4];
        boolean isPdf = false;
        try {
            fIn.read(magic);
            isPdf = magic[0] == '%' && magic[1] == 'P' 
                 && magic[2] == 'D' && magic[3] == 'F';
        } finally {
            fIn.close();
        }
        return isPdf;
    }
    
    /**
     * Close a PdfReader. Opposite of openPdf().
     */
    private static void closePdf(PdfReader r) {
        r.close();
    }
    
    /**
     * @return Answers a PdfStamper for the PdfReader r, whose output will
     * be placed in the File f.
     */
    private static PdfStamper openStamper(File f, PdfReader r) 
            throws DocumentException, IOException {
        FileOutputStream fOut = new FileOutputStream(f);
        PdfStamper stamper = new PdfStamper(r, fOut);
        return stamper;
    }
    
    /**
     * Performs stamping of a PDF via a PdfStamper. An Image is inserted into
     * the specified page number, 'page', along with a URL action for 'url',
     * at the same location. The action area covers the the image.
     */
    private void stampPdf(PdfStamper s, Image i, float x, float y, int page) 
            throws DocumentException {
        /* Assume 72 DPI images if not specified. */
        final float scaleFactorX = (i.getDpiX() == 0 ? 72f : i.getDpiX()) / targetDpi;
        final float scaleFactorY = (i.getDpiY() == 0 ? 72f : i.getDpiY()) / targetDpi;
        final float scaledImgWidth = scaleFactorX * i.getWidth();
        final float scaledImgHeight = scaleFactorY * i.getHeight();
        
        PdfContentByte content = s.getOverContent(page);
        if (content == null) {
            throw new DocumentException("PDF does not have a page " + page + ".");
        } else {
            content.saveState();
            content.addImage(i, scaledImgWidth, 0.0f, 0.0f, scaledImgHeight, x, y);
            if (!url.equals("")) {
                content.setAction(new PdfAction(url), 
                                  x,
                                  y + scaledImgHeight,
                                  x + scaledImgWidth,
                                  y);
            }
            content.restoreState();
        }
    }
    
    /**
     * Close a stamper. Opposite of openStamper().
     */
    private static void closeStamper(PdfStamper s) throws DocumentException, 
            IOException {
        s.close();
    }
    
    /**
     * Add stamps to a PDF file. A stamped PDF is written to 'out'.
     */
    private void addStampsToFile(File in, File out) {
        try {
            if (!isPdfFile(in)) {
                if (verbose) {
                    System.err.println("Skipping " + in.getPath() 
                            + " because it doesn't look like a PDF file.");
                }
                return;
            }
        } catch (IOException e) {
            System.err.println("Couldn't determine if " + in.getPath()
                    + " is a PDF because of:");
            System.err.println(e);
        }
        
        PdfReader r = null;
        PdfStamper s = null;
        try {
            r = openPdf(in);
            s = openStamper(out, r);
            for (int page : pages) {
                if (page < 0) {
                    page = r.getNumberOfPages() + 1 + page;
                }
                stampPdf(s, stampImage, stampLocation.x, stampLocation.y, page);
            }
        } catch (Exception e) {
            System.err.println("Failed on " + in.getPath() + " because of:");
            System.err.println(e);
        } finally {
            try {
                if (s != null) {
                    closeStamper(s);
                }
            } catch (Exception e) {
            }
            try {
                if (r != null) {
                    closePdf(r);
                }
            } catch (Exception e) {
                
            }
        }
    }
    
    /**
     * @return Answers the output filename for file 'in'. This will change
     * depending on command line argument values.
     */
    private File getOutFileForInFile(File in) {
        String[] parts = in.getName().split("\\.");
        StringBuffer outName = new StringBuffer();
        outName.append(parts[0]);
        outName.append('_');
        outName.append(outputExtension);
        for (int i=1; i<parts.length; i++) {
            outName.append('.');
            outName.append(parts[i]);
        }
        
        File outParent = outputDirectory == null ? in.getAbsoluteFile()
                                                     .getParentFile()
                                                 : outputDirectory;
        
        return new File(outParent.getPath() + File.separator + outName);
    }
    
    public static final void main(String... args) {
        new Main().doMain(args);
    }
    
    private void doMain(String... args) {
        CmdLineParser.registerHandler(StampTuple.class, StampTupleOptionHandler.class);
        CmdLineParser parser = new CmdLineParser(this);
        
        if (args.length == 0) {
            System.err.println("Usage: pdfstamp [options] <PDF-FILEs> | <DIR>");
            parser.printUsage(System.err);
            System.exit(0);
        }

        try {
            parser.parseArgument(args);

            for (String value : this.pageRanges) {
                try {
                    String[] pair = value.split("-");
                    for (Integer i = Integer.parseInt(pair[0]); i <= Integer.parseInt(pair[1]); i++) {
                        pages.add(i);
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    System.err.println("Invalid format for page range. Should be e.g. 5-10 .");
                    System.exit(0);
                }
            }

            if (pages.size() == 0) {
                /* Add a default page 1. */
                pages.add(1);
            }
            
            try {
                stampImage = openImage(imageFile);
            } catch (Exception e) {
                System.err.println("Couldn't open image file because of:");
                System.err.println(e);
                System.exit(0);
            }
            
            for (String path : paths) {
                File pathFile = new File(path);
                if (pathFile.isDirectory()) {
                    iterateDirectory(pathFile);
                } else {
                    addStampsToFile(pathFile, 
                                    getOutFileForInFile(pathFile));
                }
            }
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }
    }
    
    private void iterateDirectory(File dir) {
        for (String path : dir.list()) {
            File pathFile = new File(dir.getPath() + File.separator + path);
            if (pathFile.isDirectory() && recursive) {
                iterateDirectory(pathFile);
            } else {
                addStampsToFile(pathFile,
                                getOutFileForInFile(pathFile));
            }
        }
    }
}