pdfstamp
========

Add a clickable image to PDFs.

# Installation

Make sure you have a Java runtime installed on your computer. Run `pdfstamp.jar` from this
repository:

    java -jar pdfstamp.jar --help

# Options

| Option | Required? | Description |
|--------|-----------|-------------|
| -d N        | Optional | Target DPI. Defaults to 300. |
| -e EXT      | Optional | Extension appended to the PDF filename. |
| -i FILE     | Required | Image file containing image of the stamp. |
| -l X,Y      | Required | Location on page to apply stamp. |
| -o FILE     | Optional | Output directory. |
| -p N        | Optional | Page numbers to stamp. -1 is the last page. May be provided multiple times. |
| -pp N-N     | Optional | Range of page numbers to stamp, e.g. `5-10`. May be provided multiple times. |
| -r          | Optional | Descend recursively into directories. |
| -u URL      | Optional | Target URL of the stamp. |
| -v          | Optional | Verbose output. |

# Examples

Add an image to all pages at the top left with clickable target of `http://www.crossref.org`
to `my.pdf`:

    java -jar pdfstamp.jar -u http://www.crossref.org -i /path/to/an/image.png -l 10,10 my.pdf

Add image to pages 1 to 4, 6 to 8 and page 10.

    java -jar pdfstamp.jar -i /path/to/an/image.png -l 10,10 -pp 1-4 -pp 6-8 -p 10 my.pdf


The same as above, except for all PDF files within a given directory:

    java -jar pdfstamp.jar -u http://www.crossref.org -i /path/to/an/image.png -l 10,10 -r /my/folder

Scale the image by altering DPI and only apply it to the second page:

    java -jar pdfstamp.jar -u http://www.crossref.org -i /path/to/an/image.png -l 10,10 -d 600 my.pdf


