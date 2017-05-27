package ru.bartwell.exfilepicker.ui.eventbus;

/**
 * Created by wujs on 2017/5/27-14:44.
 * Email:wujs@paxsz.com
 */

public class DirPathEvent {
    private String currentDir;

    public DirPathEvent(String currentDir) {
        this.currentDir = currentDir;
    }

    public String getCurrentDir() {
        return currentDir;
    }
}
