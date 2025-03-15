package com.photo.act.photo_act.views;


import com.photo.act.photo_act.model.Person;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;


import java.util.Arrays;
import java.util.List;

@PageTitle("Feed")
@Route("feed")
//@Menu(order = 2, icon = "line-awesome/svg/list-solid.svg")  //, icon = LineAwesomeIconUrl.LIST_SOLID)
public class FeedView extends Div implements AfterNavigationObserver {

    private Grid<Person> grid = new Grid<>();
    private boolean isMobile = false;      //TODO
    private String section = "feed";
    private String strUrlRequestToBeLogged;

    public FeedView() {
        addClassName("feed-view");
        setSizeFull();
        grid.setHeight("100%");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        grid.addComponentColumn(person -> createCard(person));
        add(grid);
    }

    private VerticalLayout loadHeader(String strHeader, String strSubHeader,String strSection){

        HorizontalLayout headerContainerMaster = new HorizontalLayout();
        if(isMobile){
            headerContainerMaster.addClassNames(
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.Margin.NONE,
                    LumoUtility.Padding.NONE,
                    LumoUtility.Gap.MEDIUM,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    LumoUtility.BorderRadius.NONE
            );
        }else {
            headerContainerMaster.addClassNames(
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.Margin.NONE,
                    LumoUtility.Padding.NONE,
                    LumoUtility.Gap.MEDIUM,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    LumoUtility.BorderRadius.LARGE
            );
        }

        VerticalLayout headerTextContainer = new VerticalLayout();
        headerTextContainer.addClassNames(
                LumoUtility.Margin.NONE, LumoUtility.Padding.NONE,
                LumoUtility.Gap.XSMALL);

        H3 header = new H3(strHeader+" ...");
        header.addClassNames(LumoUtility.Margin.Bottom.NONE, LumoUtility.Margin.Top.NONE, LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.BOLD, LumoUtility.TextColor.SECONDARY);
//        header.getStyle().set("font-family", "Times-New-Roman, serif");

        Div subheader = new Div(strSubHeader);
        subheader.addClassNames(LumoUtility.Margin.Bottom.NONE, LumoUtility.Margin.Top.NONE, LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        headerTextContainer.add(header,subheader);

        Select<String> sortBy = new Select<>();
        sortBy.setLabel("Sort by");
        sortBy.setItems("Most Viewed", "Least Viewed", "Most Favourite", "Least Favourite", "Newest First", "Oldest First", "Most Liked", "Least Liked");
        sortBy.setValue("Most Viewed");

        HorizontalLayout headerContainerSecondary = new HorizontalLayout();
        if(isMobile){
            headerContainerSecondary.addClassNames(
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.Margin.NONE,
                    LumoUtility.Padding.NONE,
                    LumoUtility.Gap.SMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    LumoUtility.BorderRadius.NONE
            );
        }else {
            headerContainerSecondary.addClassNames(
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.Margin.NONE,
                    LumoUtility.Padding.NONE,
                    LumoUtility.Gap.SMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    LumoUtility.BorderRadius.LARGE
            );
        }


        VerticalLayout layoutFilters = new VerticalLayout();
        if(isMobile){
            layoutFilters.addClassNames(
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Margin.NONE,
                    LumoUtility.Padding.NONE,
                    LumoUtility.Gap.SMALL,
                    LumoUtility.Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    LumoUtility.BorderRadius.NONE);
        }else {
            layoutFilters.addClassNames(
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Margin.NONE,
                    LumoUtility.Padding.NONE,
                    LumoUtility.Gap.SMALL,
                    LumoUtility.Width.FULL,
                    //  Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL, //Display.FLEX,
                    //  Background.CONTRAST_5,
                    LumoUtility.BorderRadius.LARGE);
        }

        CheckboxGroup<String> checkboxGroupSubject = new CheckboxGroup<>();
        checkboxGroupSubject.setTooltipText("Subject");
//        checkboxGroupSubject.setLabel("Subject");
        checkboxGroupSubject.setItems("Photography", "Street Photography", "Landscape", "Cityscape");
        //   "Friday", "Saturday", "Sunday");
        // checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
//        Div lblFilterSubject = new Div("Subject");

        layoutFilters.add(checkboxGroupSubject);


        CheckboxGroup<String> checkboxGroupFormat = new CheckboxGroup<>();
        checkboxGroupFormat.setTooltipText("Format");
//        checkboxGroupFormat.setLabel("Format");
        checkboxGroupFormat.setItems("Book", "Youtube");
//        Div lblFilterFormat = new Div("Format");
        layoutFilters.add(checkboxGroupFormat);



        CheckboxGroup<String> checkboxGroupLocation = new CheckboxGroup<>();
        checkboxGroupLocation.setTooltipText("Location");
//         checkboxGroupLocation.setLabel("Location");
        checkboxGroupLocation.setItems("Hungary", "UK", "Greece");//, "Thursday",

        layoutFilters.add(checkboxGroupLocation);


        VerticalLayout layoutHeaderParameters = new VerticalLayout();
        if(isMobile){
            layoutHeaderParameters.addClassNames(
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.Margin.SMALL,
                    LumoUtility.Padding.NONE,
                    LumoUtility.Gap.XSMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
                    //   Background.CONTRAST_5,
                    LumoUtility.BorderRadius.NONE
            );
        }else {
            layoutHeaderParameters.addClassNames(
                    LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.EVENLY,
                    LumoUtility.Overflow.HIDDEN, LumoUtility.Width.FULL,
                    LumoUtility.Margin.SMALL,
                    LumoUtility.Padding.NONE,
                    LumoUtility.Gap.XSMALL,
                    // Padding.Left.MEDIUM, Padding.Right.MEDIUM,
//                       Background.CONTRAST_5,
                    LumoUtility.BorderRadius.LARGE
            );
        }




        Select<String> cmbView = new Select<>();
        cmbView.setLabel("View");

        cmbView.setItems("Micro View", "Ordinary - No MetaData", "Ordinary - MetaData Bottom", "Ordinary - MetaData Right",
                "Wide - No MetaData", "Wide - MetaData Bottom","Wide - MetaData Right");
        cmbView.setValue("Ordinary - No MetaData");

        headerContainerMaster.add(headerTextContainer, cmbView);
        headerContainerSecondary.add(layoutFilters, sortBy);


        layoutHeaderParameters.add(headerContainerMaster,headerContainerSecondary);

        return layoutHeaderParameters;
    }

    private HorizontalLayout createCard(Person person) {
        HorizontalLayout card = new HorizontalLayout();
        card.addClassName("card");
        card.setSpacing(false);
        card.getThemeList().add("spacing-s");

        Image image = new Image();
        image.setSrc(person.getImage());
        VerticalLayout description = new VerticalLayout();
        description.addClassName("description");
        description.setSpacing(false);
        description.setPadding(false);

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("header");
        header.setSpacing(false);
        header.getThemeList().add("spacing-s");

        Span name = new Span(person.getName());
        name.addClassName("name");
        Span date = new Span(person.getDate());
        date.addClassName("date");
        header.add(name, date);

        Span post = new Span(person.getPost());
        post.addClassName("post");

        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("actions");
        actions.setSpacing(false);
        actions.getThemeList().add("spacing-s");

        Icon likeIcon = VaadinIcon.HEART.create();
        likeIcon.addClassName("icon");
        Span likes = new Span(person.getLikes());
        likes.addClassName("likes");
        Icon commentIcon = VaadinIcon.COMMENT.create();
        commentIcon.addClassName("icon");
        Span comments = new Span(person.getComments());
        comments.addClassName("comments");
        Icon shareIcon = VaadinIcon.CONNECT.create();
        shareIcon.addClassName("icon");
        Span shares = new Span(person.getShares());
        shares.addClassName("shares");

        actions.add(likeIcon, likes, commentIcon, comments, shareIcon, shares);

        description.add(header, post, actions);
        card.add(image, description);
        return card;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {

        // Set some data when this view is displayed.
        List<Person> persons = Arrays.asList( //
                createPerson("https://randomuser.me/api/portraits/men/42.jpg", "John Smith", "May 8",
                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/women/42.jpg", "Abagail Libbie", "May 3",
                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/men/24.jpg", "Alberto Raya", "May 3",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/women/24.jpg", "Emmy Elsner", "Apr 22",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/men/76.jpg", "Alf Huncoot", "Apr 21",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/women/76.jpg", "Lidmila Vilensky", "Apr 17",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/men/94.jpg", "Jarrett Cawsey", "Apr 17",
                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/women/94.jpg", "Tania Perfilyeva", "Mar 8",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/men/16.jpg", "Ivan Polo", "Mar 5",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/women/16.jpg", "Emelda Scandroot", "Mar 5",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/men/67.jpg", "Marcos Sá", "Mar 4",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20"),
                createPerson("https://randomuser.me/api/portraits/women/67.jpg", "Jacqueline Asong", "Mar 2",

                        "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document without relying on meaningful content (also called greeking).",
                        "1K", "500", "20")

        );

        grid.setItems(persons);
    }

    private static Person createPerson(String image, String name, String date, String post, String likes,
            String comments, String shares) {
        Person p = new Person();
        p.setImage(image);
        p.setName(name);
        p.setDate(date);
        p.setPost(post);
        p.setLikes(likes);
        p.setComments(comments);
        p.setShares(shares);

        return p;
    }

}
