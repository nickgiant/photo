package com.photo.act.photo_act.utils;

import com.vaadin.flow.component.UI;

public class PageSeoUtil {

    private PageSeoUtil() {}

    public static void setMetaDescription(String description) {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.getPage().executeJs(
                "var m=document.querySelector('meta[name=description]');" +
                "if(m)m.setAttribute('content',$0);",
                description
            );
        }
    }
}
