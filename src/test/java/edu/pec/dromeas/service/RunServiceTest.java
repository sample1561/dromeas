package edu.pec.dromeas.service;

import edu.pec.dromeas.payload.Code;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

//TODO implement Junit testing to check against few reference codes
//We can also us our test service as it allows front end users to test the code
//Testing is not limited to backend

class RunServiceTest
{
    RunService runService = new RunService();
    String BASE;

    public void setUp()
    {
    }

    @Test
    void runC() throws IOException
    {
        File directory = new File(BASE+"/C");
        int counter = 0;

        String[] outputs = new String[10];

        for(File current : Objects.requireNonNull(directory.listFiles()))
        {
            Code input = new Code();
            String code = current.toString();
            input.setCode(code);

            String result = runService.runC(input).getResult();
            assertEquals(result,outputs[counter++]);
        }
    }

    @Test
    void runCpp()
    {
    }
}