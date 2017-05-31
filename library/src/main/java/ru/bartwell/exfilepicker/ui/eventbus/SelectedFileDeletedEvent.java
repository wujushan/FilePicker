package ru.bartwell.exfilepicker.ui.eventbus;

/**
 * Created by wujs on 2017/5/31.
 */

public class SelectedFileDeletedEvent {
    private int position;

    public SelectedFileDeletedEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
