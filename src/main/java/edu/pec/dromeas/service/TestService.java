package edu.pec.dromeas.service;

import edu.pec.dromeas.config.Language;
import edu.pec.dromeas.exception.ServerException;
import edu.pec.dromeas.exception.ServiceNotImplementedException;
import edu.pec.dromeas.payload.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Service
public class TestService
{
    private final ExecuteService executeService;
    String BASE = new File("").getAbsolutePath() + "/testCases";
    private static final Logger LOGGER = LoggerFactory.getLogger(TestService.class);


    public TestService(ExecuteService executeService)
    {
        this.executeService = executeService;
    }

    public ResponseEntity<?> systemTest()
    {
        LOGGER.info("A string of text in the console");
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("I am a teapot");
    }

    public ResponseEntity<?> systemStat()
    {
        SystemStatistics stat = new SystemStatistics();
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
        //throw new ServiceNotImplementedException("Service Under Maintenance");

        Set<AllResults> results = new HashSet<>();
        Language[] supported = {Language.C,Language.CPP,Language.JavaScript};

        for(Language language: supported)
        {
            AllResults current = new AllResults();
            current.setLanguage(language.name());
            current.setTests(testCode(language));

            results.add(current);
        }

        return results;
    }

    public Set<Tests> testCCode() {
        return testCode(Language.C);
    }

    public Set<Tests> testCppCode() {
        return testCode(Language.CPP);
    }

    public Set<Tests> testJsCode()
    {
        return testCode(Language.JavaScript);
    }

    public Set<Tests> testCode(Language language)
    {
        String type = getType(language);
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

        i = 0;
        Set<Tests> results = new HashSet<>();

        for(File current : Objects.requireNonNull(inputCode.listFiles()))
        {
            try {

                String code = readFileAsString(current.getAbsolutePath());

                String execution = executeCode(code, language);

                Tests currentTest = new Tests();
                currentTest.setTest(i+1);
                currentTest.setSuccess(execution.equals(outputs[i]));

                results.add(currentTest);

                LOGGER.info("Test: "+(i+1)+" | Expected: "+outputs[i]+" | Executed: "+execution);
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

    private String getType(Language language)
    {
        switch (language)
        {
            case C:
                return "C";

            case CPP:
                return "CPP";

            case JavaScript:
                return "JavaScript";

            default:
                throw new ServerException("Unrecognised language "+language.name());
        }
    }

    public ResponseEntity<?> testLanguages()
    {
        try
        {
            int i = -1;

            Languages languages = new Languages();

            //C
            languages.setC(getVersion("gcc","--version"));
            LOGGER.info("C = "+languages.getC());

            //C++
            languages.setCPP(getVersion("g++","--version"));
            LOGGER.info("CPP = "+languages.getCPP());

            //C#
            //TODO find command
            languages.setCS("Not Installed");
            LOGGER.info("C# = "+languages.getCS());

            //Java
            languages.setJava(getVersion("java","--version"));
            LOGGER.info("Java = "+languages.getJava());

            //Scala
            languages.setScala(getVersion("scala","-version"));
            LOGGER.info("Scala = "+languages.getScala());

            //JavaScript
            languages.setJavaScript(getVersion("node","--version"));
            LOGGER.info("JavaScript = "+languages.getJavaScript());

            //Python 2
            languages.setPython2(getVersion("python2","--version"));
            LOGGER.info("Python2 = "+languages.getPython2());

            //Python 3
            languages.setPython3(getVersion("python3","--version"));
            LOGGER.info("Python3 = "+languages.getPython3());

            //Php
            languages.setPhp(getVersion("php","--version"));
            LOGGER.info("Php = "+languages.getPhp());

            //Go
            //languages.setGo(getVersion("go","version"));
            languages.setGo("Not Installed");
            LOGGER.info("GoLang = "+languages.getGo());

            //Kotlin
            //languages.setKotlin(getVersion("kotlinc","version"));
            languages.setKotlin("Not Installed");
            LOGGER.info("Kotlin = "+languages.getKotlin());

            //Ruby
            languages.setRuby(getVersion("ruby","--version"));
            LOGGER.info("Ruby = "+languages.getRuby());

            //Rust
            //languages.setRust(getVersion("rustc","--version"));
            languages.setRust("Not Installed");
            LOGGER.info("Rust = "+languages.getRust());

            //Swift
            //languages.setSwift(getVersion("swift","-version"));
            languages.setSwift("Not Installed");
            LOGGER.info("Swift = "+languages.getSwift());

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

    private String executeCode(String code, Language language)
    {
        Code input = new Code();
        input.setCode(code);

        switch (language)
        {
            case C:
                return executeService.runC(input).getResult();

            case CPP:
                return executeService.runCpp(input).getResult();

            default:
                throw new ServerException("Server not configured for "+language.name());
        }
    }

    private double convertToMb(Long bytes)
    {
        return (bytes/1000000);
    }
}
