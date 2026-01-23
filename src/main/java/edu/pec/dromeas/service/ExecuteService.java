package edu.pec.dromeas.service;

import static org.apache.commons.io.FileUtils.deleteDirectory;

import edu.pec.dromeas.config.Language;
import edu.pec.dromeas.exception.BadRequestException;
import edu.pec.dromeas.exception.ServerException;
import edu.pec.dromeas.exception.ServiceNotImplementedException;
import edu.pec.dromeas.payload.Code;
import edu.pec.dromeas.payload.Result;
import java.io.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Service responsible for executing code in various programming languages.
 *
 * <p>Supports C, C++, JavaScript, Python, PHP, Ruby, and has placeholders for Java, Scala, Go,
 * Kotlin, Rust, C#, and Swift. Code is executed in a temporary directory and cleaned up after
 * execution.
 */
@Service
public class ExecuteService {

  /** Maximum allowed execution time in milliseconds */
  final long EXPIRATION = 5000L;

  private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteService.class);

  private final LanguageService languageService;
  private final FileService fileService;

  /**
   * Constructor for ExecuteService.
   *
   * @param languageService Service for retrieving language extensions and commands
   * @param fileService Service for managing temporary code files
   */
  public ExecuteService(LanguageService languageService, FileService fileService) {
    this.languageService = languageService;
    this.fileService = fileService;
  }

  /**
   * Executes C code using gcc.
   *
   * @param input Code payload containing the C code
   * @return Result containing the output of execution
   */
  public Result runC(Code input) {
    return runGCC(input, Language.C);
  }

  /**
   * Executes C++ code using g++.
   *
   * @param input Code payload containing the C++ code
   * @return Result containing the output of execution
   */
  public Result runCpp(Code input) {
    return runGCC(input, Language.CPP);
  }

  /**
   * Placeholder for Java execution.
   *
   * @param input Code payload
   * @return Throws ServiceNotImplementedException
   */
  public Result runJava(Code input) {
    throw new ServiceNotImplementedException();
  }

  /**
   * Placeholder for Scala execution.
   *
   * @param code Code payload
   * @return Throws ServiceNotImplementedException
   */
  public ResponseEntity<?> runScala(Code code) {
    throw new ServiceNotImplementedException();
  }

  /**
   * Placeholder for Go execution.
   *
   * @param code Code payload
   * @return Throws ServiceNotImplementedException
   */
  public ResponseEntity<?> runGo(Code code) {
    throw new ServiceNotImplementedException();
  }

  /**
   * Placeholder for Kotlin execution.
   *
   * @param code Code payload
   * @return Throws ServiceNotImplementedException
   */
  public ResponseEntity<?> runKotlin(Code code) {
    throw new ServiceNotImplementedException();
  }

  /**
   * Placeholder for Rust execution.
   *
   * @param code Code payload
   * @return Throws ServiceNotImplementedException
   */
  public ResponseEntity<?> runRust(Code code) {
    throw new ServiceNotImplementedException();
  }

  /**
   * Placeholder for C# execution.
   *
   * @param code Code payload
   * @return Throws ServiceNotImplementedException
   */
  public ResponseEntity<?> runCS(Code code) {
    throw new ServiceNotImplementedException();
  }

  /**
   * Placeholder for Swift execution.
   *
   * @param code Code payload
   * @return Throws ServiceNotImplementedException
   */
  public ResponseEntity<?> runSwift(Code code) {
    throw new ServiceNotImplementedException();
  }

  /**
   * Executes C or C++ code using GCC compiler.
   *
   * @param input Code payload
   * @param language Language (C or C++)
   * @return Result containing compilation and execution output
   * @throws ServerException if compilation or execution fails
   */
  private Result runGCC(Code input, Language language) {
    String code = input.getCode();
    String extension = languageService.getExtension(language);
    String command = languageService.getCommand(language);
    File directory = fileService.createLocalFile(code, extension);

    try {
      ProcessBuilder compile = new ProcessBuilder(command, "code" + extension);
      compile.directory(directory);
      Process temp = compile.start();

      synchronized (temp) {
        temp.wait();
      }

      if (temp.exitValue() != 0) {
        throwError(temp, "Syntax/Compilation Error");
      }

      ProcessBuilder execute = new ProcessBuilder("./a.out");
      execute.directory(directory);

      return execute(execute, directory);
    } catch (IOException e) {
      e.printStackTrace();
      throw new ServerException("Failed to run the C/C++ file");
    } catch (InterruptedException e) {
      e.printStackTrace();
      throw new ServerException("Failed to compile code", e);
    }
  }

  /**
   * Executes interpreted languages like Python, PHP, JavaScript, Ruby, etc.
   *
   * @param input Code payload
   * @param language Programming language
   * @return Result containing execution output
   */
  private Result runLanguage(Code input, Language language) {
    String code = input.getCode();
    String extension = languageService.getExtension(language);
    String command = languageService.getCommand(language);

    File directory = fileService.createLocalFile(code, extension);

    ProcessBuilder execute = new ProcessBuilder(command, "code" + extension);
    execute.directory(directory);

    return execute(execute, directory);
  }

  /**
   * Executes a ProcessBuilder and retrieves the output.
   *
   * @param execute ProcessBuilder configured to run the code
   * @param dir Directory containing the code file
   * @return Result containing execution output
   */
  private Result execute(ProcessBuilder execute, File dir) {
    try {
      Process process = execute.start();
      boolean timeLimit = waitFor(process);

      if (timeLimit) {
        process.destroyForcibly();
        String message = "Process took longer than " + EXPIRATION + " milliseconds to execute";
        throw new BadRequestException(message, new Exception("Exceeded Time Limit"));
      }

      return returnExecutionResult(process, dir);
    } catch (IOException e) {
      e.printStackTrace();
      throw new ServerException("Failed to execute code");
    }
  }

  /**
   * Reads the output of a process and deletes the temporary folder.
   *
   * @param process The executed process
   * @param folder Folder containing the code file
   * @return Result object with captured output
   */
  private Result returnExecutionResult(Process process, File folder) {
    try {
      StringBuilder output = new StringBuilder();
      BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = in.readLine()) != null) {
        if (!output.toString().equals("")) output.append("\n").append(line);
        else output = new StringBuilder(line);
      }

      deleteDirectory(folder);

      if (folder.exists()) {
        LOGGER.info("!! CLEANUP FAIL " + folder.getName() + " !!");
      }

      Result result = new Result();
      result.setResult(output.toString());

      return result;
    } catch (IOException e) {
      throw new ServerException("Failed to read output", e);
    }
  }

  /**
   * Waits for a process to complete or until the EXPIRATION time is reached.
   *
   * @param process The process to wait for
   * @return true if the process is still alive after EXPIRATION milliseconds, false otherwise
   */
  private boolean waitFor(Process process) {
    long endTime = System.currentTimeMillis() + EXPIRATION;
    while (endTime >= System.currentTimeMillis() && process.isAlive())
      ;
    return process.isAlive();
  }

  /**
   * Reads the error stream of a process and throws a BadRequestException.
   *
   * @param temp Process that failed
   * @param type Type of error (e.g., "Syntax/Compilation Error")
   */
  void throwError(Process temp, String type) {
    InputStream error = temp.getErrorStream();

    try {
      StringWriter writer = new StringWriter();
      IOUtils.copy(error, writer, "UTF-8");
      String errorMessage = writer.toString();

      throw new BadRequestException(errorMessage, new Exception(type));
    } catch (IOException e) {
      e.printStackTrace();
      throw new ServerException("Failed to read error message");
    }
  }
}
