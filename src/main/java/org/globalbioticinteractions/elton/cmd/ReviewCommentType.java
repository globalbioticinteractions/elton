package org.globalbioticinteractions.elton.cmd;

public enum ReviewCommentType {
    note("note"),
    summary("summary"),
    info("info");

    private String label;

    ReviewCommentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static ReviewCommentType fromString(String code) {
        for (ReviewCommentType type : ReviewCommentType.values()) {
            if (type.getLabel().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
