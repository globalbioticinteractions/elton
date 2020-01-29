package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

public class ReviewCommentTypeConverter implements IStringConverter<ReviewCommentType> {

    @Override
    public ReviewCommentType convert(String value) {

        ReviewCommentType convertedValue = ReviewCommentType.fromString(value);

        if (convertedValue == null) {
            throw new ParameterException("Value [" + value + "] is not a review comment type. " +
                    "Available values are: comment, note, summary.");
        }
        return convertedValue;
    }
}
