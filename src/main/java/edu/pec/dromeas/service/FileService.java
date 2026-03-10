package edu.pec.dromeas.service;

import edu.pec.dromeas.exception.ServerException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;

/**
 * Service responsible for creating and managing temporary local files for code execution. Files are
 * stored in a scratch folder on the local filesystem.
 */
@Service
public class FileService {

  /** Base directory for temporary files */
  final String BASE = new File("").getAbsolutePath() + "/scratch/";

  /**
   * Creates a local folder with a unique hash-based name inside the scratch directory, and writes
   * the provided code to a file with the given file type.
   *
   * @param code The source code content to write
   * @param fileType The file extension (including the dot), e.g., ".py", ".c"
   * @return The folder containing the created code file
   * @throws ServerException If the scratch folder or code file cannot be created
   */
  public File createLocalFile(String code, String fileType) {
    // First check if the scratch folder exists
    if (!Files.exists(Paths.get(BASE))) {
      boolean scratch = new File(BASE).mkdir();
      if (!scratch) throw new ServerException("Failed to create the scratch folder");
    }

    String dirPath = BASE + UUID.randomUUID();
    File folder = new File(dirPath);

    if (!folder.mkdir()) {
      throw new ServerException("Failed to create local folder");
    }

    try {
      String filePath = folder.getAbsolutePath() + "/code" + fileType;

      PrintWriter writer = new PrintWriter(filePath, StandardCharsets.UTF_8);
      writer.println(code);
      writer.close();

      File codeFile = new File(filePath);

      if (!codeFile.exists()) {
        throw new ServerException("File Creation Failed");
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new ServerException("Failed to create load run file", e);
    }

    return folder;
  }
}
