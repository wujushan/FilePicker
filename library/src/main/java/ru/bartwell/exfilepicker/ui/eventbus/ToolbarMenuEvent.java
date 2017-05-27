package ru.bartwell.exfilepicker.ui.eventbus;

/**
 * Created by wujs on 2017/5/27-14:45.
 * Email:wujs@paxsz.com
 */

public class ToolbarMenuEvent {
    public static final int OK = 0;
    public static final int SORT = 1;
    public static final int NEW_FOLDER = 2;
    public static final int SELECT_ALL = 3;
    public static final int DESELECT_ALL = 4;
    public static final int INVERT_SELECTION = 5;
    public static final int CHANGE_VIEW = 6;

    private int eventType = OK;

    public ToolbarMenuEvent(int eventType) {
        this.eventType = eventType;
    }

    public int getEventType() {
        return eventType;
    }
}
