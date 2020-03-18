package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import org.globalbioticinteractions.dataset.DatasetFinderException;

public class RegistryNameValidator implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
        try {
            new DatasetRegistryFactoryImpl(in -> in).createRegistryByName(value);
        } catch (DatasetFinderException e) {
            throw new ParameterException("registry [" + value + "] not supported", e);
        }
    }
}
