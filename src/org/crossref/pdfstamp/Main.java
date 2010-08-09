package org.crossref.pdfstamp;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
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
 class Main {

    @Option(name="-l", usage="Required. Location on page to apply stamp.",
            required=true, multiValued=true, metaVar="PAGE,X,Y")
    private List<StampTuple> stampLocations = new ArrayList<StampTuple>();
    
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
    
    @Argument
    private List<String> paths = new ArrayList<String>();
    
    private Image stampImage = null;
    
    private static Image openImage(File f) throws BadElementException, 
            MalformedURLException, IOException {
        return Image.getInstance(f.getAbsolutePath());
    }
    
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
    
    private static void closePdf(PdfReader r) {
        r.close();
    }
    
    private static PdfStamper openStamper(File f, PdfReader r) 
            throws DocumentException, IOException {
        FileOutputStream fOut = new FileOutputStream(f);
        PdfStamper stamper = new PdfStamper(r, fOut);
        return stamper;
    }
    
    private static void stampPdf(PdfStamper s, Image i, String url,
                                 float x, float y, int page) 
            throws DocumentException {
        PdfContentByte content = s.getOverContent(page);
        content.saveState();
        content.addImage(i, i.getWidth(), 0.0f, 0.0f, i.getHeight(), x, y);
        content.setAction(new PdfAction(url), 
                          x, 
                          y + i.getHeight(), 
                          x + i.getWidth(), 
                          y);
        content.restoreState();
    }
    
    private static void closeStamper(PdfStamper s) throws DocumentException, 
            IOException {
        s.close();
    }
    
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
            for (StampTuple st : stampLocations) {
                stampPdf(s, stampImage, url, st.x, st.y, st.page);
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
    
    private File getOutFileForInFile(File in) {
        if (outputDirectory != null) {
            return new File(outputDirectory.getPath() + File.separator 
                    + in.getName());
        } else {
            return new File(in.getPath() + ".out");
        }
    }
    
    public static final void main(String... args) {
        new Main().doMain(args);
    }
    
    private void doMain(String... args) {
        CmdLineParser.registerHandler(StampTuple.class, StampTupleOptionHandler.class);
        CmdLineParser parser = new CmdLineParser(this);
        
        if (args.length == 0) {
            System.err.println("pdfstamp usage:");
            parser.printUsage(System.err);
            System.exit(0);
        }
        
        try {
            parser.parseArgument(args);
            
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