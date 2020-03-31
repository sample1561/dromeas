package edu.pec.dromeas.service;

import edu.pec.dromeas.config.Language;
import edu.pec.dromeas.exception.ServerException;
import org.springframework.stereotype.Service;

@Service
public class LanguageService
{
    public String getExtension(Language language)
    {
        switch (language)
        {
            case C:
                return ".c";

            case CPP:
                return ".cpp";

            case JavaScript:
                return ".js";

            case Python2:
            case Python3:
                return ".py";

            case Php:
                return ".php";

            case Ruby:
                return ".rb";

            default:
                throw new ServerException("Unknown Language "+language.name());
        }
    }

    public String getCommand(Language language)
    {
        switch (language)
        {
            case C:
                return "gcc";

            case CPP:
                return "g++";

            case JavaScript:
                return "node";

            case Php:
                return "php";

            case Python2:
                return "python2";

            case Python3:
                return "python3";

            case Ruby:
                return "ruby";

            default:
                throw new ServerException("Unknown Language "+language.name());
        }
    }
}
