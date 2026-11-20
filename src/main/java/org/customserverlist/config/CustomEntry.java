package org.customserverlist.config;

import com.google.gson.annotations.SerializedName;

public final class CustomEntry {
    public String id = "";
    public String name = "New Entry";
    @SerializedName(value = "descriptionLine1", alternate = "description")
    public String descriptionLine1 = "";
    public String descriptionLine2 = "";
    public String icon = "";
    public String display = "singleplayer"; // singleplayer, multiplayer, both
    public boolean pinned = false;
    public boolean allowDelete = true;
    public boolean allowEdit = true;
    public boolean enabled = true;
    public int sortOrder = 0;
    public String action = "none"; // none, open_url
    public String url = "";

    public CustomEntry copy() {
        CustomEntry result = new CustomEntry();
        result.id = id;
        result.name = name;
        result.descriptionLine1 = descriptionLine1;
        result.descriptionLine2 = descriptionLine2;
        result.icon = icon;
        result.display = display;
        result.pinned = pinned;
        result.allowDelete = allowDelete;
        result.allowEdit = allowEdit;
        result.enabled = enabled;
        result.sortOrder = sortOrder;
        result.action = action;
        result.url = url;
        return result;
    }
}
