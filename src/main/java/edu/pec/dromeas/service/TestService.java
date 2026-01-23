package edu.pec.dromeas.service;

import edu.pec.dromeas.config.Language;
import edu.pec.dromeas.exception.ServerException;
import edu.pec.dromeas.payload.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for executing and testing code in different programming languages.
 * Provides system statistics, language version information, and the ability to run and verify
 * test cases for C, C++, JavaScript, and PHP.
 */
@Service
public class TestService {

  private final ExecuteService executeService;
  private static final Logger LOGGER = LoggerFactory.getLogger(TestService.class);

  /** Base directory for test cases and results */
  String BASE = new File("").getAbsolutePath() + "/testCases";

  /**
   * Constructor for TestService.
   *
   * @param executeService Service used to execute code in various languages
   */
  public TestService(ExecuteService executeService) {
    this.executeService = executeService;
  }

  /**
   * Performs a simple system test that logs a message and returns a playful HTTP response.
   *
   * @return ResponseEntity with HTTP status 418 (I am a teapot) and message
   */
  public ResponseEntity<?> systemTest() {
    LOGGER.info("A string of text in the console");
    return ResponseEntity.status(HttpStatus.OK).body("I am a teapot");
  }

  /**
   * Retrieves statistics about the current system including memory, CPU, and disk usage.
   *
   * @return ResponseEntity containing system statistics
   */
  public ResponseEntity<?> systemStat() {
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

  /**
   * Runs all test cases for all supported languages.
   *
   * @return Set of AllResults, each containing results for a specific language
   */
  public Set<AllResults> testAllCodes() {
    Set<AllResults> results = new HashSet<>();
    Language[] supported = {Language.C, Language.CPP, Language.JavaScript, Language.Php};

    for (Language language : supported) {
      AllResults current = new AllResults();
      current.setLanguage(language.name());
      current.setTests(testCode(language));

      results.add(current);
    }

    return results;
  }

  /**
   * Runs all test cases for C code.
   *
   * @return Set of Tests results for C language
   */
  public Set<Tests> testCCode() {
    return testCode(Language.C);
  }

  /**
   * Runs all test cases for C++ code.
   *
   * @return Set of Tests results for C++ language
   */
  public Set<Tests> testCppCode() {
    return testCode(Language.CPP);
  }

  /**
   * Runs all test cases for JavaScript code.
   *
   * @return Set of Tests results for JavaScript language
   */
  public Set<Tests> testJsCode() {
    return testCode(Language.JavaScript);
  }

  /**
   * Runs all test cases for PHP code.
   *
   * @return Set of Tests results for PHP language
   */
  public Set<Tests> testPhpCode() {
    return testCode(Language.Php);
  }

  /**
   * Executes and verifies test cases for a specific language.
   *
   * @param language The programming language to test
   * @return Set of Tests containing the success status of each test case
   */
  public Set<Tests> testCode(Language language) {
    String type = getType(language);
    File inputCode = new File(BASE + "/" + type + "/codes");
    File codeOutput = new File(BASE + "/" + type + "/results");

    if (!inputCode.exists() || !codeOutput.exists()) {
      return null;
    }

    int numberOfFiles = Objects.requireNonNull(codeOutput.listFiles()).length;
    String[] outputs = new String[numberOfFiles];
    int i = 0;

    for (File current : Objects.requireNonNull(codeOutput.listFiles())) {
      try {
        outputs[i] = readFileAsString(current.getAbsolutePath());
        i++;
      } catch (Exception e) {
        e.printStackTrace();
        throw new ServerException("Failed to read contents of " + current.getName());
      }
    }

    i = 0;
    Set<Tests> results = new HashSet<>();

    for (File current : Objects.requireNonNull(inputCode.listFiles())) {
      try {
        String code = readFileAsString(current.getAbsolutePath());
        String execution = executeCode(code, language);

        Tests currentTest = new Tests();
        currentTest.setTest(i + 1);
        currentTest.setSuccess(execution.equals(outputs[i]));

        results.add(currentTest);

        LOGGER.info(
                "Test: " + (i + 1) + " | Expected: " + outputs[i] + " | Executed: " + execution);
      } catch (Exception e) {
        e.printStackTrace();
        throw new ServerException("Failed to read contents of " + current.getName());
      }
      i++;
    }

    return results;
  }

  /**
   * Retrieves the corresponding folder type for a given language.
   *
   * @param language Programming language
   * @return String representing folder type
   * @throws ServerException if language is unrecognized
   */
  private String getType(Language language) {
    switch (language) {
      case C: return "C";
      case CPP: return "CPP";
      case JavaScript: return "JavaScript";
      case Php: return "PHP";
      default: throw new ServerException("Unrecognised language " + language.name());
    }
  }

  /**
   * Tests installed language versions on the system.
   *
   * @return ResponseEntity containing versions of supported languages
   */
  public ResponseEntity<?> testLanguages() {
    try {
      Languages languages = new Languages();

      languages.setC(getVersion("gcc", "--version"));
      LOGGER.info("C = " + languages.getC());

      languages.setCPP(getVersion("g++", "--version"));
      LOGGER.info("CPP = " + languages.getCPP());

      languages.setCS("Not Installed");
      LOGGER.info("C# = " + languages.getCS());

      languages.setJava(getVersion("java", "--version"));
      LOGGER.info("Java = " + languages.getJava());

      languages.setScala(getVersion("scala", "-version"));
      LOGGER.info("Scala = " + languages.getScala());

      languages.setJavaScript(getVersion("node", "--version"));
      LOGGER.info("JavaScript = " + languages.getJavaScript());

      languages.setPython2(getVersion("python2", "--version"));
      LOGGER.info("Python2 = " + languages.getPython2());

      languages.setPython3(getVersion("python3", "--version"));
      LOGGER.info("Python3 = " + languages.getPython3());

      languages.setPhp(getVersion("php", "--version"));
      LOGGER.info("Php = " + languages.getPhp());

      languages.setGo("Not Installed");
      LOGGER.info("GoLang = " + languages.getGo());

      languages.setKotlin("Not Installed");
      LOGGER.info("Kotlin = " + languages.getKotlin());

      languages.setRuby(getVersion("ruby", "--version"));
      LOGGER.info("Ruby = " + languages.getRuby());

      languages.setRust("Not Installed");
      LOGGER.info("Rust = " + languages.getRust());

      languages.setSwift("Not Installed");
      LOGGER.info("Swift = " + languages.getSwift());

      return ResponseEntity.status(HttpStatus.OK).body(languages);

    } catch (Exception e) {
      e.printStackTrace();
      throw new ServerException();
    }
  }

  /**
   * Retrieves the version of a specific programming tool.
   *
   * @param command Primary command
   * @param command2 Argument for version retrieval
   * @return Version string, "Not Installed" if not found, or "Request Timeout" if command takes too long
   */
  private String getVersion(String command, String command2) {
    try {
      ProcessBuilder checkVersion = new ProcessBuilder(command, command2);
      Process temp = checkVersion.start();

      synchronized (temp) {
        temp.waitFor(2, TimeUnit.SECONDS);

        if (temp.isAlive()) {
          temp.destroyForcibly();
          return "Request Timeout";
        }
      }

      String version = "Not Installed";
      if (temp.exitValue() == 0) {
        version = readResult(temp);
      }
      return version;

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      throw new ServerException("Failed to get " + command + " version", e);
    }
  }

  /**
   * Reads the first line of the process output.
   *
   * @param process Process to read from
   * @return First line of output
   * @throws IOException if reading fails
   */
  private String readResult(Process process) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
    return in.readLine();
  }

  /**
   * Reads a file's content as a string.
   *
   * @param fileName File path
   * @return File content as string
   * @throws Exception if reading fails
   */
  public static String readFileAsString(String fileName) throws Exception {
    return new String(Files.readAllBytes(Paths.get(fileName)));
  }

  /**
   * Executes code in a specific language using ExecuteService.
   *
   * @param code     Code to execute
   * @param language Language to execute
   * @return Execution result as string
   * @throws ServerException if language is unsupported
   */
  private String executeCode(String code, Language language) {
    Code input = new Code();
    input.setCode(code);

      return switch (language) {
          case C -> executeService.runC(input).getResult();
          case CPP -> executeService.runCpp(input).getResult();
          case JavaScript -> executeService.runJavaScript(input).getResult();
          case Php -> executeService.runPhp(input).getResult();
          default -> throw new ServerException("Server not configured for " + language.name());
      };
  }

  /**
   * Converts bytes to megabytes.
   *
   * @param bytes Value in bytes
   * @return Value in megabytes
   */
  private double convertToMb(Long bytes) {
    return (bytes / 1000000.0);
  }
}
