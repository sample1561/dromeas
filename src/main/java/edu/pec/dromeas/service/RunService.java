package edu.pec.dromeas.service;

import edu.pec.dromeas.exception.ServerException;
import edu.pec.dromeas.exception.ServiceNotImplementedException;
import edu.pec.dromeas.exception.BadRequestException;
import edu.pec.dromeas.payload.InputCode;
import edu.pec.dromeas.payload.Result;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.naming.TimeLimitExceededException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.sizeOf;


@Service
public class RunService
{
    final String BASE = new File("").getAbsolutePath() + "/scratch/";
    final Long UPPER = (long)1.0E9;

    final String PYTHON = ".py";
    final String C = ".c";
    final String CPP = ".cpp";

    //Execution Time in milli-seconds
    final long EXPIRATION = 5000;

    //TODO error handling, code doesn't compile, infinite loop, include header
    public ResponseEntity<?> runC(InputCode input)
    {
        return runGCC(input, C);
    }

    public ResponseEntity<?> runCpp(InputCode input)
    {
        return runGCC(input, CPP);
    }

    private ResponseEntity<?> runGCC(InputCode input, String extension)
    {
        //Step 1 - Create required directory structure
        String code = input.getCode();
        System.out.println(code);
        File dir = createDirectory(code,extension);

        //Step 2 - Execute the local run-file
        try
        {
            String command = (C.equalsIgnoreCase(extension))?"gcc":"g++";
            ProcessBuilder compile = new ProcessBuilder(command, "code"+extension);
            compile.directory(dir);

            Process temp = compile.start();

            //TODO guarantee correctness of synchronization
            // - OR limit compilation time so avoid synchronization

            System.out.println("Compilation Began");

            synchronized (temp)
            {
                temp.waitFor(1000, TimeUnit.MILLISECONDS);
            }

            System.out.println("Code Compiled");

            if(temp.exitValue() != 0)
            {
                throwError(temp,"Syntax/Compilation Error");
            }

            ProcessBuilder execute = new ProcessBuilder("./a.out");
            execute.directory(dir);

            Process process = execute.start();
            System.out.println("Execution began");

            boolean timeLimit = waitFor(process);

            if(timeLimit)
            {
                process.destroyForcibly();
                String message = "Process took long than "+EXPIRATION+" milliseconds to execute";
                throw new BadRequestException(message,new Exception("Exceeded Time Limit"));
            }

            System.out.println("Execution Completed");
            return returnExecutionResult(process, dir);
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

    public ResponseEntity<?> runJava(InputCode input)
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

    public ResponseEntity<?> runJavaScript(InputCode input)
    {
        /**
        * Create a file code.js
        * Run node code.js
        * */
        throw new ServiceNotImplementedException();
    }

    //Fix using reference
    public ResponseEntity<?> runPython3(InputCode input)
    {
        String code = input.getCode();
        //System.out.println(code);
        File dir = createDirectory(code,PYTHON);

        try //Execute the local run-file
        {
            ProcessBuilder execute = new ProcessBuilder("python3", "code.py");
            execute.directory(dir);
            Process process = execute.start();
            process.waitFor();
            return returnExecutionResult(process, dir);
        }
        catch (InterruptedException e){
            e.printStackTrace();
            throw new ServerException("Failed to run the python file");

        }
        catch (IOException e)
        {
            throw new ServerException("Failed to run the python file", e);
        }
    }

    //Duplicate improvements from py3
    public ResponseEntity<?> runPython2(InputCode input)
    {
        String code = input.getCode();
        File dir = createDirectory(code,PYTHON);

        try //Execute the local run-file
        {
            ProcessBuilder execute = new ProcessBuilder("python2", "code.py");
            execute.directory(dir);
            Process process = execute.start();
            process.waitFor();
            return returnExecutionResult(process, dir);
        }
        catch (InterruptedException e){
            e.printStackTrace();
            throw new ServerException("Failed to run the python file");

        }
        catch (IOException e)
        {
            throw new ServerException("Failed to run the python file", e);
        }
    }

    public ResponseEntity<?> runPhp(InputCode code)
    {
        /**
        * create php file
        * php test.php
        * */
        throw new ServiceNotImplementedException();
    }

    public ResponseEntity<?> runScala(InputCode code)
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

    public ResponseEntity<?> runGo(InputCode code)
    {
        /**
         * install GoLang on WSL
         * go run test.go
         * */
        throw new ServiceNotImplementedException();
    }

    public ResponseEntity<?> runKotlin(InputCode code) {
        throw new ServiceNotImplementedException();
    }

    public ResponseEntity<?> runRust(InputCode code) {
        throw new ServiceNotImplementedException();
    }

    public ResponseEntity<?> runCS(InputCode code) {
        throw new ServiceNotImplementedException();
    }

    public ResponseEntity<?> runSwift(InputCode code) {
        throw new ServiceNotImplementedException();
    }

    public ResponseEntity<?> runRuby(InputCode code) {
        throw new ServiceNotImplementedException();
    }

    private ResponseEntity<?> returnExecutionResult(Process process, File folder)
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

            return ResponseEntity.status(HttpStatus.OK).body(result);
        }

        catch (IOException e)
        {
            throw new ServerException("Failed to read output",e);
        }
    }

    //TODO account for a missing scratch folder
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
        System.out.println(dirPath);

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
