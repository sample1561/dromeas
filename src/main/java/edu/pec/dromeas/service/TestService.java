package edu.pec.dromeas.service;

import edu.pec.dromeas.exception.ServerException;
import edu.pec.dromeas.exception.ServiceNotImplementedException;
import edu.pec.dromeas.payload.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Service
public class TestService
{
    private final RunService runService;
    String BASE = new File("").getAbsolutePath() + "/testCases";

    final String C = "C";
    final String CPP = "CPP";
    final String JS = "JavaScript";

    public TestService(RunService runService)
    {
        this.runService = runService;
    }

    public ResponseEntity<?> systemTest() {
        System.out.print("A string of text in the console");
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("I am a teapot");
    }

    public ResponseEntity<?> systemStat()
    {
        SystemStat stat = new SystemStat();
        File file = new File(BASE);

        stat.setMaxMemory(convertToMb(Runtime.getRuntime().maxMemory()));
        stat.setFreeMemory(convertToMb(Runtime.getRuntime().freeMemory()));
        stat.setTotalMemory(convertToMb(Runtime.getRuntime().totalMemory()));
        stat.setProcessors(Runtime.getRuntime().availableProcessors());
        stat.setFreeDisk(convertToMb(file.getFreeSpace()));
        stat.setTotalDisk(convertToMb(file.getTotalSpace()));

        return ResponseEntity.status(HttpStatus.OK).body(stat);
    }

    public Set<AllResults> testAllCodes()
    {
        throw new ServiceNotImplementedException("Service Under Maintenance");
//        String[] languages = {C,CPP,JS};
//        Set<AllResults> results = new HashSet<>();
//
//        for(String language : languages)
//        {
//            AllResults current = new AllResults();
//            current.setLanguage(language);
//            current.setTests(testCode(language));
//
//            results.add(current);
//        }
//
//        return results;
    }

    public Set<Tests> testCCode() {
        return testCode(C);
    }

    public Set<Tests> testCppCode() {
        return testCode(CPP);
    }

    public Set<Tests> testJsCode()
    {
        return testCode(JS);
    }

    public Set<Tests> testCode(String type)
    {
        File inputCode = new File(BASE+"/"+type+"/codes");
        //System.out.println("Input Folder -> "+inputCode);

        File codeOutput = new File(BASE+"/"+type+"/results");
        //System.out.println("Results Folder -> "+codeOutput);

        int numberOfFiles = Objects.requireNonNull(codeOutput.listFiles()).length;
        //System.out.println("Number of files -> "+numberOfFiles);

        String[] outputs = new String[numberOfFiles];
        int i = 0;

        for(File current : Objects.requireNonNull(codeOutput.listFiles()))
        {
            try
            {
                outputs[i] = readFileAsString(current.getAbsolutePath());
                i++;
            }

            catch (Exception e)
            {
                e.printStackTrace();
                throw new ServerException("Failed to read contents of "+current.getName());
            }
        }

        System.out.println("Read All the expected outputs");

        i = 0;
        Set<Tests> results = new HashSet<>();

        for(File current : Objects.requireNonNull(inputCode.listFiles()))
        {
            try {

                String code = readFileAsString(current.getAbsolutePath());

                String execution = executeCode(code, type);

                Tests currentTest = new Tests();
                currentTest.setTest(i+1);
                currentTest.setSuccess(execution.equals(outputs[i]));

                results.add(currentTest);

                System.out.println("Expected - "+(i+1)+"\t-> "+outputs[i]);
                System.out.println("Execution - "+(i+1)+"\t-> "+execution);
                System.out.println();
            }

            catch (Exception e)
            {
                e.printStackTrace();
                throw new ServerException("Failed to read contents of "+current.getName());
            }

            i++;
        }

        return results;

    }

    public ResponseEntity<?> testLanguages()
    {
        try
        {
            int i = -1;

            Languages languages = new Languages();
            //C
            languages.setC(getVersion("gcc","--version"));
            System.out.println(++i);

            //C++
            languages.setCPP(getVersion("g++","--version"));
            System.out.println(++i);

            //C#
            //TODO find command
            languages.setCS("Not Installed");
            System.out.println(++i);

            //Java
            languages.setJava(getVersion("java","--version"));
            System.out.println(++i);

            //Scala
            languages.setScala(getVersion("scala","-version"));
            System.out.println(++i);

            //JavaScript
            languages.setJavaScript(getVersion("node","--version"));
            System.out.println(++i);

            //Python 2
            languages.setPython2(getVersion("python2","--version"));
            System.out.println(++i);

            //Python 3
            languages.setPython3(getVersion("python3","--version"));
            System.out.println(++i);

            //Php
            languages.setPhp(getVersion("php","--version"));
            System.out.println(++i);

            //Go
            //languages.setGo(getVersion("go","version"));
            languages.setGo("Not Installed");
            System.out.println(++i);

            //Kotlin
            //languages.setKotlin(getVersion("kotlinc","version"));
            languages.setKotlin("Not Installed");
            System.out.println(++i);

            //Ruby
            languages.setRuby(getVersion("ruby","--version"));
            System.out.println(++i);

            //Rust
            //languages.setRust(getVersion("rustc","--version"));
            languages.setRust("Not Installed");
            System.out.println(++i);

            //Swift
            //languages.setSwift(getVersion("swift","-version"));
            languages.setSwift("Not Installed");
            System.out.println(++i);

            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(languages);
        }

        catch (Exception e)
        {
            e.printStackTrace();
            throw new ServerException();
        }
    }

    private String getVersion(String command, String command2)
    {
        try
        {
            ProcessBuilder checkVersion;
            String version = "Not Installed";

            //C
            checkVersion = new ProcessBuilder(command, command2);
            Process temp = checkVersion.start();

            synchronized (temp)
            {
                temp.waitFor(2, TimeUnit.SECONDS);

                if(temp.isAlive())
                {
                    temp.destroyForcibly();
                    return "Request Timeout";
                }
            }


            if(temp.exitValue() == 0)
            {
                version = readResult(temp);
            }

            return version;

        }

        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
            throw new ServerException("Failed to get "+command+" version", e);
        }

    }

    private String readResult(Process process) throws IOException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return in.readLine();
    }

    public static String readFileAsString(String fileName)throws Exception
    {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    private String executeCode(String code, String type)
    {
        InputCode input = new InputCode();
        input.setCode(code);

        if(type.equals(C))
            return runService.runC(input).getResult();

        else if(type.equals(CPP))
            return runService.runCpp(input).getResult();

        throw new ServerException("Server not configured for "+type);
    }

    private double convertToMb(Long bytes)
    {
        return (bytes/1000000);
    }
}
