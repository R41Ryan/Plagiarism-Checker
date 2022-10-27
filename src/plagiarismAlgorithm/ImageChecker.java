package plagiarismAlgorithm;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.contentstream.operator.Operator;

public class ImageChecker extends PDFStreamEngine
{
    private File fileToCheck;
    private int numOfImages;

    public ImageChecker() throws IOException
    {
        super();
    }

    public ImageChecker(File f) throws IOException
    {
        super();
        fileToCheck = f;
        numOfImages = 0;
    }

    public void loadFile(File f)
    {
        fileToCheck = f;
        numOfImages = 0;
    }

    public int getNumOfImages()
    {
        return numOfImages;
    }

    public void processFile() throws IOException 
    {
        numOfImages = 0;
        PDDocument document = new PDDocument();
        if (fileToCheck.exists())
        {
            document = Loader.loadPDF(fileToCheck);
            for (PDPage page : document.getPages())
            {
                processPage(page);
            }
        
            document.close();
        }
    }

    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException
    {
        String operation = operator.getName();
        if ("Do".equals(operation))
        {
            COSName objectName = (COSName) operands.get(0);
            PDXObject xObject = getResources().getXObject(objectName);
            if (xObject instanceof PDImageXObject)
            {
                numOfImages++;
            }
        }
    }
}