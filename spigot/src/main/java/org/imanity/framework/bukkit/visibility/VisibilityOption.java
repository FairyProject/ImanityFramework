package org.imanity.framework.bukkit.visibility;

public enum VisibilityOption {

    SHOW,
    HIDE,
    NOTHING;

    public static VisibilityOption getByBoolean(Boolean flag) {
        return flag != null ? flag ? SHOW : HIDE : NOTHING;
    }

}
