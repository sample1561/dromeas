package edu.pec.dromeas.service;

import edu.pec.dromeas.exception.ServerException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class FileService
{
    final String BASE = new File("").getAbsolutePath() + "/scratch/";
    final Long UPPER = (long)1.0E9;

    public File createLocalFile(String code, String fileType)
    {
        //First check if the scratch folder exists
        if(!Files.exists(Paths.get(BASE)))
        {
            boolean scratch = new File(BASE).mkdir();
            if(!scratch)
                throw new ServerException("Failed to create the scratch folder");
        }

        String dirPath = BASE + "dir" + getHash();
        //System.out.println(dirPath);

        File folder = new File(dirPath);

        if (!folder.mkdir())
        {
            throw new ServerException("Failed to create local folder");
        }

        try
        {
            String filePath = folder.getAbsolutePath()+"/code"+fileType;

            PrintWriter writer = new PrintWriter(filePath, "UTF-8");
            writer.println(code);
            writer.close();

            File codeFile = new File(filePath);

            if (!codeFile.exists())
            {
                throw new ServerException("File Creation Failed");
            }
        }

        catch (IOException e)
        {
            e.printStackTrace();
            throw new ServerException("Failed to create load run file", e);
        }

        return folder;
    }

    private Long getHash()
    {
        return (long)(Math.random() * UPPER);
    }
}
