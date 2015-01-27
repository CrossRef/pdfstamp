# pdfstamp
Stamp a PDF with an image and clickable URL, AND text strings.

Code assumes 72dpi if it can't find out from file, then scales to 300dpi unless otherwise specified.

This is a work-in-progress. Needs configuration for text font (currently white).

Stamp a PDF with an image and clickable URL, AND text strings.

Code assumes 72dpi if it can't find out from file, then scales to 300dpi unless otherwise specified.

This is a work-in-progress. Needs configuration for text font (currently white).

```
  java -jar ./pdfstamp.jar 
  Usage: pdfstamp [options] <PDF-FILEs> | <DIR>
   -d N            : Optional. Target DPI. Defaults to 300.
   -e EXT          : Optional. Extension appended to the PDF filename.
   -i FILE         : Required. Image file containing image of the stamp.
   -l X,Y          : Required. Location on page to apply stamp.
   -o FILE         : Optional. Output directory.
   -p P1,P2...     : Optional. Page numbers to stamp. -1 is the last page.
   -r              : Optional. Descend recursively into directories.
   -t X,Y,TEXT ... : Optional. Text to stamp. (Multiple values allowed.)
   -u URL          : Optional. Target URL of the stamp.
   -v              : Optional. Verbose output.
```

New -t argument:

Separate multiple values by a space, e.g. -t 1,1,Hello 2,2,World.
To use the value of the -u argument, use "=URL" as the value for TEXT.

Original project: https://github.com/CrossRef/pdfstamp

Text stamping added by http://www.appazur.com
