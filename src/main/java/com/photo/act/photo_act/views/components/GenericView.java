package com.photo.act.photo_act.views.components;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import static com.photo.act.photo_act.views.MainLayout.APP_NAME;

public class GenericView {


    public GenericView() {

    }

    public VerticalLayout loadFooter(boolean isMobile) {

        Div logoLayout = new Div();
//        logoLayout.addClassNames(Display.FLEX, AlignItems.CENTER, JustifyContent.CENTER,
//                Gap.XSMALL,
//                Margin.Vertical.NONE,
//                Padding.Vertical.NONE, Padding.Horizontal.NONE);
        logoLayout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                LumoUtility.Gap.XSMALL,
                LumoUtility.Margin.SMALL,
                LumoUtility.Padding.NONE);

        H1 appName = new H1(APP_NAME);
        //appName.addClassNames(Margin.Vertical.MEDIUM, AlignItems.CENTER, Margin.End.AUTO, FontSize.LARGE, FontWeight.BOLD, TextColor.TERTIARY);
        appName.addClassNames( LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.SEMIBOLD, //LumoUtility.TextColor.TERTIARY,
                LumoUtility.Padding.NONE, LumoUtility.Margin.NONE);
        appName.getStyle().set("font-family", "Times-New-Roman, serif");
        appName.getStyle().set("font-stretch", "semi-expanded");
        appName.getStyle().setColor("#eaeae8");//"#f9943b");//""#bd3450");

        Div divLogo = new Div();
        divLogo.add(VaadinIcon.CAMERA.create());
        // divLogo.addClassNames(Margin.Vertical.MEDIUM, AlignItems.CENTER, Margin.End.LARGE, FontSize.LARGE, FontWeight.BOLD,TextColor.TERTIARY);
        divLogo.addClassNames(  LumoUtility.FontSize.MEDIUM, LumoUtility.FontWeight.BOLD,// LumoUtility.TextColor.TERTIARY,
                LumoUtility.Padding.NONE, LumoUtility.Margin.NONE);
        //divLogo.getStyle().setColor("rgba(231, 24, 24, 0.5)");
        //divLogo.getStyle().setColor("#d64f00");

        logoLayout.add(divLogo,appName);

        Div divPhotoActMoto = new Div("Act around Photography");
        divPhotoActMoto.addClassNames( LumoUtility.FontSize.MEDIUM, LumoUtility.FontWeight.SEMIBOLD, LumoUtility.TextColor.TERTIARY,
                LumoUtility.Padding.NONE, LumoUtility.Margin.MEDIUM);

//        HorizontalLayout layoutLine = new HorizontalLayout();
//        if(isMobile) {
//            layoutLine.addClassNames(
//                    Overflow.HIDDEN, Width.FULL,
//                    AlignItems.CENTER, JustifyContent.AROUND,
//                    Margin.NONE,
//                    Padding.NONE,
//                    Gap.XSMALL,
//                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    Background.CONTRAST_10,
//                    Border.BOTTOM, Border.RIGHT,
////                    BorderColor.CONTRAST_20,
//                    BorderRadius.NONE);
//        }else{
//            layoutLine.addClassNames(
//                    Overflow.HIDDEN, Width.FULL,
//                    AlignItems.CENTER, JustifyContent.AROUND,
//                    Margin.NONE,
//                    Padding.NONE,
//                    Gap.MEDIUM,
//                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    Background.CONTRAST_10,
//                    Border.BOTTOM, Border.RIGHT,
////                    BorderColor.CONTRAST_20,
//                    BorderRadius.NONE);
//        }
//
//        layoutLine.add(divTitle);


        VerticalLayout layoutFooter = new VerticalLayout();
        layoutFooter.setMinHeight("250px");
        layoutFooter.getStyle().setBackgroundColor("#78868f");
        layoutFooter.getStyle().setColor("#eaeae8");
//        layoutFooter.addClassName("bottom-radius-shadow");

        if (isMobile) {
            layoutFooter.addClassNames(
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Margin.NONE,
                    LumoUtility.Padding.NONE,
                    LumoUtility.Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    Background.CONTRAST_5,
                    LumoUtility.BorderRadius.NONE);
        } else {
            layoutFooter.addClassNames(
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.CENTER,
                    LumoUtility.Margin.NONE,
                    LumoUtility.Padding.MEDIUM,
                    LumoUtility.Gap.MEDIUM,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
//                    Background.CONTRAST_5,
                    LumoUtility.BorderRadius.NONE);
        }
//        layoutFooter.addClassName("footer");

        layoutFooter.add(logoLayout,divPhotoActMoto);
        return layoutFooter;
    }


    public MenuItem createIconItem(HasMenuItems menu, VaadinIcon iconName,
                                    String label, String ariaLabel) {
        return createIconItem(menu, iconName, label, ariaLabel, false);
    }

    public MenuItem createIconItem(HasMenuItems menu, VaadinIcon iconName,
                                    String label, String ariaLabel, boolean isChild) {
        Icon icon = new Icon(iconName);

        if (isChild) {
            icon.getStyle().set("width", "var(--lumo-icon-size-s)");
            icon.getStyle().set("height", "var(--lumo-icon-size-s)");
            icon.getStyle().set("marginRight", "var(--lumo-space-s)");
        }

        MenuItem item = menu.addItem(icon, e -> {
        });

        if (ariaLabel != null) {
            item.setAriaLabel(ariaLabel);
        }

        if (label != null) {
            item.add(new Text(label));
        }

        return item;
    }


}
