package ru.bartwell.exfilepicker.ui.eventbus;

import java.util.List;

/**
 * Created by wujs on 2017/5/31.
 */

public class SelectedFilesUpdatedEvent {
    private List<String> selectedFiles;
    public SelectedFilesUpdatedEvent(List<String> selectedFiles) {
        this.selectedFiles = selectedFiles;
    }

    public List<String> getSelectedFiles() {
        return selectedFiles;
    }
}
