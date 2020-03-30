package edu.pec.dromeas.service;

import edu.pec.dromeas.exception.ServerException;
import edu.pec.dromeas.exception.ServiceNotImplementedException;
import edu.pec.dromeas.exception.BadRequestException;
import edu.pec.dromeas.payload.Code;
import edu.pec.dromeas.payload.Result;

import org.apache.commons.io.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.apache.commons.io.FileUtils.deleteDirectory;

@Service
public class RunService
{
    final String BASE = new File("").getAbsolutePath() + "/scratch/";
    final Long UPPER = (long)1.0E9;

    final String C = ".c";
    final String CPP = ".cpp";
    final String JS = ".js";
    final String PYTHON = ".py";
    final String PHP = ".php";
    final String RUBY = ".rb";

    //TODO add in application.property
    final long EXPIRATION = 5000L;

    public Result runC(Code input)
    {
        return runGCC(input, C);
    }

    public Result runCpp(Code input)
    {
        return runGCC(input, CPP);
    }

    private Result runGCC(Code input, String extension)
    {
        //Step 1 - Create required directory structure
        String code = input.getCode();
        //System.out.println(code);
        File directory = createDirectory(code,extension);

        //Step 2 - Execute the local run-file
        try
        {
            String command = (C.equalsIgnoreCase(extension))?"gcc":"g++";
            System.out.println("Running command - "+command);

            ProcessBuilder compile = new ProcessBuilder(command, "code"+extension);
            compile.directory(directory);
            Process temp = compile.start();

            System.out.println("Compilation Began");


            //TODO there has to be a better way to synchronize the application
            // - Also had threading
            synchronized (temp)
            {
                temp.wait();
            }

            System.out.println("Code Compiled");

            if(temp.exitValue() != 0)
            {
                throwError(temp,"Syntax/Compilation Error");
            }

            ProcessBuilder execute = new ProcessBuilder("./a.out");
            execute.directory(directory);

            return executeCode(execute,directory);
        }

        catch (IOException e)
        {
            e.printStackTrace();
            throw new ServerException("Failed to run the c file");
        }

        catch (InterruptedException e)
        {
            e.printStackTrace();
            throw new ServerException("Failed to Compile Code",e);
        }
    }

    public Result runJava(Code input)
    {
        /**
        * Create a file with extension .java, for example Hi.java.
        * Inside your Hi.java file create something like:
        *
        * public class Hi {
        *   public static void main(String[] args) {
        *       System.out.println("Hi");
        *   }
        * }
        *
        * In the terminal run
        * javac Hi.java
        * java Hi
        * */
        throw new ServiceNotImplementedException();
    }

    public Result runJavaScript(Code input)
    {
        return runLanguage(input,JS,getCorrespondingCommand(JS));
    }

    public Result runPython3(Code input)
    {
        return runLanguage(input,PYTHON,"python3");
    }

    public Result runPython2(Code input)
    {
        return runLanguage(input,PYTHON,"python2");
    }

    public Result runPhp(Code input)
    {
        return runLanguage(input,PHP,getCorrespondingCommand(PHP));
    }

    public ResponseEntity<?> runScala(Code code)
    {
        /**
        * object HelloWorld {
        *     def main(args: Array[String]): Unit = {
        *        println("Scala Hello World Example")
        *      }
        *    }
        *
        * scalac HelloWorld.scala
        *
        * the file name is same as object name
        *
        * scala HelloWorld
        * */
        throw new ServiceNotImplementedException();
    }

    public ResponseEntity<?> runGo(Code code)
    {
        /**
         * install GoLang on WSL
         * go run test.go
         * */
        throw new ServiceNotImplementedException();
    }

    public ResponseEntity<?> runKotlin(Code code) {
        throw new ServiceNotImplementedException();
    }

    public ResponseEntity<?> runRust(Code code) {
        throw new ServiceNotImplementedException();
    }

    public ResponseEntity<?> runCS(Code code) {
        throw new ServiceNotImplementedException();
    }

    public ResponseEntity<?> runSwift(Code code) {
        throw new ServiceNotImplementedException();
    }

    public Result runRuby(Code input)
    {
        return runLanguage(input,RUBY,getCorrespondingCommand(RUBY));
    }

    private Result runLanguage(Code input, String extension, String command)
    {
        //Create the required directory structure with code file
        String code = input.getCode();
        System.out.println(code);
        File directory = createDirectory(code,extension);

        ProcessBuilder execute = new ProcessBuilder(command, "code"+extension);
        execute.directory(directory);

        return executeCode(execute,directory);
    }

    private Result executeCode(ProcessBuilder execute,File dir)
    {
        try {
            Process process = execute.start();
            System.out.println("Execution began");

            //TODO see how to properly implement a timeout
            boolean timeLimit = waitFor(process);

            if (timeLimit)
            {
                process.destroyForcibly();
                String message = "Process took long than " + EXPIRATION + " milliseconds to execute";
                throw new BadRequestException(message, new Exception("Exceeded Time Limit"));
            }

            System.out.println("Execution Completed");
            return returnExecutionResult(process, dir);
        }

        catch (IOException e)
        {
            e.printStackTrace();
            throw new ServerException("Failed to execute code");
        }
    }

    private Result returnExecutionResult(Process process, File folder)
    {
        try
        {
            StringBuilder output = new StringBuilder();
            String line = "";

            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

            while ((line = in.readLine()) != null)
            {
                if(!output.toString().equals(""))
                    output.append("\n").append(line);

                else
                    output = new StringBuilder(line);
            }

            deleteDirectory(folder);

            if(folder.exists())
            {
                System.out.println("!! CLEANUP FAIL " + folder.getName() + " !!");
                //TODO account for failure of deleting local file
                //  -maybe create a repo of failed deletes and run a cleanup daemon
            }

            Result result = new Result();
            result.setResult(output.toString());

            return result;
        }

        catch (IOException e)
        {
            throw new ServerException("Failed to read output",e);
        }
    }

    private File createDirectory(String code, String fileType)
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

    private boolean waitFor(Process process)
    {
        long endTime = System.currentTimeMillis() + EXPIRATION;
        while(endTime >= System.currentTimeMillis() && process.isAlive());

        return process.isAlive();
    }

    private String getCorrespondingCommand(String extension)
    {
        if(extension.equalsIgnoreCase(JS))
            return "node";

        if(extension.equalsIgnoreCase(PHP))
            return "php";

        if(extension.equalsIgnoreCase(RUBY))
            return "ruby";

        throw new ServerException("Invalid Language Input");
    }

    void throwError(Process temp, String type)
    {
        InputStream error = temp.getErrorStream();

        try
        {
            StringWriter writer = new StringWriter();
            IOUtils.copy(error, writer, "UTF-8");
            String errorMessage = writer.toString();

            throw new BadRequestException(errorMessage,new Exception(type));
        }

        catch (IOException e)
        {
            e.printStackTrace();
            throw new ServerException("Failed to read error message");
        }
    }
}
