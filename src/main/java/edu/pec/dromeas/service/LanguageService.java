package edu.pec.dromeas.service;

import edu.pec.dromeas.config.Language;
import edu.pec.dromeas.exception.ServerException;
import org.springframework.stereotype.Service;

/**
 * Service for handling operations related to programming languages,
 * such as retrieving file extensions and execution commands.
 */
@Service
public class LanguageService {

  /**
   * Returns the standard file extension for a given programming language.
   *
   * @param language The programming language
   * @return String representing the file extension (including the dot, e.g., ".py")
   * @throws ServerException if the language is unknown or unsupported
   */
  public String getExtension(Language language) {
    switch (language) {
      case C:
        return ".c";

      case CPP:
        return ".cpp";

      default:
        throw new ServerException("Unknown Language " + language.name());
    }
  }

  /**
   * Returns the command used to execute code for a given programming language.
   *
   * @param language The programming language
   * @return String representing the command to run code in that language
   * @throws ServerException if the language is unknown or unsupported
   */
  public String getCommand(Language language) {
    switch (language) {
      case C:
        return "gcc";

      case CPP:
        return "g++";

      default:
        throw new ServerException("Unknown Language " + language.name());
    }
  }
}
