package edu.pec.dromeas.service;

import edu.pec.dromeas.config.Language;
import edu.pec.dromeas.exception.ServerException;
import edu.pec.dromeas.exception.ServiceNotImplementedException;
import edu.pec.dromeas.exception.BadRequestException;
import edu.pec.dromeas.payload.Code;
import edu.pec.dromeas.payload.Result;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;

import static org.apache.commons.io.FileUtils.deleteDirectory;

@Service
public class ExecuteService
{
    final long EXPIRATION = 5000L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteService.class);

    private final LanguageService languageService;
    private final FileService fileService;

    public ExecuteService(LanguageService languageService, FileService fileService)
    {
        this.languageService = languageService;
        this.fileService = fileService;
    }

    public Result runC(Code input)
    {
        return runGCC(input, Language.C);
    }

    public Result runCpp(Code input)
    {
        return runGCC(input, Language.CPP);
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
        return runLanguage(input,Language.JavaScript);
    }

    public Result runPython3(Code input)
    {
        return runLanguage(input,Language.Python3);
    }

    public Result runPython2(Code input)
    {
        return runLanguage(input,Language.Python2);
    }

    public Result runPhp(Code input)
    {
        return runLanguage(input,Language.Php);
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
        return runLanguage(input,Language.Ruby);
    }


    private Result runGCC(Code input, Language language)
    {
        //Step 1 - Create required directory structure
        String code = input.getCode();

        String extension = languageService.getExtension(language);
        String command = languageService.getCommand(language);
        File directory = fileService.createLocalFile(code,extension);

        //Step 2 - Execute the local run-file
        try
        {
            ProcessBuilder compile = new ProcessBuilder(command, "code"+extension);
            compile.directory(directory);
            Process temp = compile.start();

            //TODO there has to be a better way to synchronize the application
            // - Also had threading
            synchronized (temp)
            {
                temp.wait();
            }

            if(temp.exitValue() != 0)
            {
                throwError(temp,"Syntax/Compilation Error");
            }

            ProcessBuilder execute = new ProcessBuilder("./a.out");
            execute.directory(directory);

            return execute(execute,directory);
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

    private Result runLanguage(Code input, Language language)
    {
        //Create the required directory structure with code file
        String code = input.getCode();

        String extension = languageService.getExtension(language);
        String command = languageService.getCommand(language);

        File directory = fileService.createLocalFile(code,extension);

        ProcessBuilder execute = new ProcessBuilder(command, "code"+extension);
        execute.directory(directory);

        return execute(execute,directory);
    }

    private Result execute(ProcessBuilder execute, File dir)
    {
        try {
            Process process = execute.start();

            //TODO see how to properly implement a timeout
            boolean timeLimit = waitFor(process);

            if (timeLimit)
            {
                process.destroyForcibly();
                String message = "Process took long than " + EXPIRATION + " milliseconds to execute";
                throw new BadRequestException(message, new Exception("Exceeded Time Limit"));
            }

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
                LOGGER.info("!! CLEANUP FAIL " + folder.getName() + " !!");
                //TODO account for failure of deleting local file
                //  -maybe create a repo of failed deletes and run a cleanup daemon
            }

            Result result = new Result();
            result.setResult(output.toString());

            return (result);
        }

        catch (IOException e)
        {
            throw new ServerException("Failed to read output",e);
        }
    }

    //Thread Handling
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
