package com.ca.apm.systemtest.fld.common.files;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class UpdateLinesOperation {

    public enum UpdateMethod {
        ADD_TO_BEGINNING, ADD_TO_END, REWRITE, CLEAR, DELETE;
    }

    public static class OneLineUpdate {
        private UpdateMethod updateMethod;
        private String updateText;

        public OneLineUpdate(UpdateMethod updateMethod) {
            this(updateMethod, null);
        }

        public OneLineUpdate(UpdateMethod updateMethod, String updateText) {
            this.updateMethod = updateMethod;
            this.updateText = updateText;
        }

        public UpdateMethod getUpdateMethod() {
            return updateMethod;
        }

        public String getUpdateText() {
            return updateText;
        }

        @Override
        public String toString() {
            return "[" + updateMethod + ":'" + updateText + "']";
        }
    }

    private String searchText;
    private List<OneLineUpdate> lineUpdates;
    private Pattern searchTextPattern;

    public UpdateLinesOperation(String searchText, List<OneLineUpdate> lineUpdates) {
        this.searchText = searchText;
        this.lineUpdates = lineUpdates;
    }

    public UpdateLinesOperation(String searchText, UpdateMethod updateMethod, String updateText) {
        this(searchText, new OneLineUpdate(updateMethod, updateText));
    }

    public UpdateLinesOperation(String searchText, OneLineUpdate lineUpdate,
        OneLineUpdate... lineUpdates) {
        this(searchText, new ArrayList<OneLineUpdate>(Collections.singletonList(lineUpdate)));
        if (lineUpdates != null) {
            this.lineUpdates.addAll(Arrays.asList(lineUpdates));
        }
    }

    public String getSearchText() {
        return searchText;
    }

    public List<OneLineUpdate> getLineUpdates() {
        return lineUpdates;
    }

    public int getLinesCount() {
        return lineUpdates == null ? 0 : lineUpdates.size();
    }

    public Pattern getSearchTextPattern() {
        if (searchTextPattern == null) {
            searchTextPattern = (searchText == null) ? null : Pattern.compile(searchText);
        }
        return searchTextPattern;
    }

    @Override
    public String toString() {
        return "UpdateLinesOperation [searchText='" + searchText + "', lineUpdates=" + lineUpdates
            + "]";
    }

    public static UpdateLinesOperation windowsScriptCommentLine(String searchText) {
        return windowsScriptCommentLine(searchText, 1);
    }

    public static UpdateLinesOperation windowsScriptCommentLine(String searchText, int linesCount) {
        List<OneLineUpdate> lineUpdates = new ArrayList<UpdateLinesOperation.OneLineUpdate>();
        for (int i = 0; i < linesCount; i++) {
            lineUpdates.add(new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "rem "));
        }
        return new UpdateLinesOperation(searchText, lineUpdates);
    }

    public static UpdateLinesOperation linuxScriptCommentLine(String searchText) {
        return linuxScriptCommentLine(searchText, 1);
    }

    public static UpdateLinesOperation linuxScriptCommentLine(String searchText, int linesCount) {
        List<OneLineUpdate> lineUpdates = new ArrayList<UpdateLinesOperation.OneLineUpdate>();
        for (int i = 0; i < linesCount; i++) {
            lineUpdates.add(new OneLineUpdate(UpdateMethod.ADD_TO_BEGINNING, "# "));
        }
        return new UpdateLinesOperation(searchText, lineUpdates);
    }

    public static UpdateLinesOperation deleteLines(String searchText, int linesCount) {
        List<OneLineUpdate> lineUpdates = new ArrayList<UpdateLinesOperation.OneLineUpdate>();
        for (int i = 0; i < linesCount; i++) {
            lineUpdates.add(new OneLineUpdate(UpdateMethod.DELETE));
        }
        return new UpdateLinesOperation(searchText, lineUpdates);
    }

}
