package ru.bartwell.exfilepicker.ui.eventbus;

/**
 * Created by wujs on 2017/5/27-16:50.
 * Email:wujs@paxsz.com
 */

public class MultiChoiceEvent {
    private boolean multiChoiceEnable;
    private boolean gridModeEnable;

    public MultiChoiceEvent(boolean multiChoiceEnable,boolean gridModeEnable) {
        this.multiChoiceEnable = multiChoiceEnable;
        this.gridModeEnable = gridModeEnable;
    }

    public boolean isMultiChoiceEnable() {
        return multiChoiceEnable;
    }

    public boolean isGridModeEnable() {
        return gridModeEnable;
    }
}
