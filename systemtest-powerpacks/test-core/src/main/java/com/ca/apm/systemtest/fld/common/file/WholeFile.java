package com.ca.apm.systemtest.fld.common.file;

/**
 * @Author rsssa02
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This is a utility class for reading or writing the entire contents
 * of a file. This is useful for relatively small files.
 *
 * TODO: need support for binary content.
 */
public abstract class WholeFile{

    /**
     * The system-specific end of line.
     */
    public static final String NEWLINE = System.getProperty("line.separator");


    public static String readFile( String path ) throws FileNotFoundException,
            IOException{

        return readFile( new File( path ) );

    }

    public static String readFile( File file ) throws FileNotFoundException,
            IOException{

        long size = file.length();
        StringBuffer result = new StringBuffer( (int) size );

        FileReader fileInput = new FileReader( file );
        BufferedReader input = new BufferedReader( fileInput );

        try{

            String line = input.readLine();

            while ( line != null ){

                result.append( line );
                result.append( NEWLINE );

                line = input.readLine();

            }

        }
        finally{

            if( input != null ){

                input.close();

            }

        }

        return result.toString();

    }

    public static void writeFile(String fileName, String content)
            throws IOException{

        writeFile( new File( fileName ), content );


    }

    public static void writeFile(File file, String content)
            throws IOException{

        writeFile( file, content, false );

    }


    public static void writeFile(File file, String content, boolean append)
            throws IOException{

        FileWriter writer = null;

        try{

            writer = new FileWriter( file, append );
            writer.write( content );
            writer.flush();

        }
        finally{
            if (writer != null) {
                writer.close();
            }
        }

    }

}
