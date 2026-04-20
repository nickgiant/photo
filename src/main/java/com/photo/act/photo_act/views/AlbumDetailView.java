package com.photo.act.photo_act.views;

import com.photo.act.photo_act.dto.PhotoAlbumDto;
import com.photo.act.photo_act.dto.PhotoAlbumPhotoDto;
import com.photo.act.photo_act.dto.PhotoMetaDto;
import com.photo.act.photo_act.services.PhotoAlbumService;
import com.photo.act.photo_act.services.PhotoMetaService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@AnonymousAllowed
@Route(value = "albums/:albumId", layout = MainLayout.class)
@PageTitle("Album · PhotoAct")
public class AlbumDetailView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger logger = LoggerFactory.getLogger(AlbumDetailView.class);

    private final PhotoAlbumService albumService;
    private final PhotoMetaService  photoMetaService;

    public AlbumDetailView(PhotoAlbumService albumService, PhotoMetaService photoMetaService) {
        this.albumService     = albumService;
        this.photoMetaService = photoMetaService;

        addClassName("album-detail-view");
        setPadding(false);
        setSpacing(false);
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String rawId = event.getRouteParameters().get("albumId").orElse(null);
        if (rawId == null) {
            event.forwardTo(AlbumsView.class);
            return;
        }

        Integer albumId;
        try {
            albumId = Integer.parseInt(rawId);
        } catch (NumberFormatException e) {
            event.forwardTo(AlbumsView.class);
            return;
        }

        Optional<PhotoAlbumDto> opt = albumService.getAlbumById(albumId);
        if (opt.isEmpty()) {
            event.forwardTo(AlbumsView.class);
            return;
        }

        PhotoAlbumDto album = opt.get();
        UI.getCurrent().getPage().setTitle(album.getTitle() + " · PhotoAct");

        removeAll();
        add(buildHeader(album), buildPhotoGrid(albumId));
    }

    private VerticalLayout buildHeader(PhotoAlbumDto album) {
        VerticalLayout header = new VerticalLayout();
        header.addClassNames(Width.FULL, Padding.MEDIUM, Margin.NONE, Gap.SMALL);
        header.addClassName("album-detail-header");

        Button btnBack = new Button("All Albums", VaadinIcon.ARROW_LEFT.create());
        btnBack.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btnBack.addClickListener(e -> UI.getCurrent().navigate(AlbumsView.class));

        H1 title = new H1(album.getTitle());
        title.addClassNames(FontSize.XXLARGE, FontWeight.BOLD, Margin.NONE);

        Div meta = new Div();
        meta.addClassNames(FontSize.SMALL, TextColor.SECONDARY, Gap.SMALL);
        if (album.getCategoryTitle() != null) {
            Span categoryBadge = new Span(album.getCategoryTitle());
            categoryBadge.addClassNames("badge", Background.CONTRAST_10,
                    BorderRadius.SMALL, Padding.Horizontal.SMALL);
            meta.add(categoryBadge);
        }
        Span photoCountBadge = new Span(album.getPhotoCount() + " photos");
        photoCountBadge.addClassNames(FontSize.SMALL, TextColor.SECONDARY);
        meta.add(photoCountBadge);

        if (album.getDescription() != null && !album.getDescription().isBlank()) {
            Paragraph description = new Paragraph(album.getDescription());
            description.addClassNames(FontSize.MEDIUM, TextColor.BODY, Margin.Top.SMALL);
            header.add(btnBack, title, meta, description);
        } else {
            header.add(btnBack, title, meta);
        }

        Div divider = new Div();
        divider.addClassNames(Border.BOTTOM, Width.FULL);
        header.add(divider);

        return header;
    }

    private Div buildPhotoGrid(Integer albumId) {
        Div grid = new Div();
        grid.addClassName("album-photo-grid");
        grid.addClassNames(Width.FULL, Padding.MEDIUM);
        grid.getStyle().set("display", "grid")
                       .set("grid-template-columns", "repeat(auto-fill, minmax(220px, 1fr))")
                       .set("gap", "12px");

        List<PhotoAlbumPhotoDto> albumPhotos = albumService.getAlbumPhotos(albumId);
        if (albumPhotos.isEmpty()) {
            Div empty = new Div(new Span("No photos in this album yet."));
            empty.addClassNames(TextColor.SECONDARY, FontSize.MEDIUM, Padding.LARGE);
            grid.add(empty);
            return grid;
        }

        for (PhotoAlbumPhotoDto albumPhoto : albumPhotos) {
            Optional<PhotoMetaDto> photoOpt = photoMetaService.getById(albumPhoto.getPhotoId());
            photoOpt.ifPresent(photo -> grid.add(buildPhotoCard(photo)));
        }

        logger.debug("Rendered {} photos for album {}", albumPhotos.size(), albumId);
        return grid;
    }

    private Div buildPhotoCard(PhotoMetaDto photo) {
        Div card = new Div();
        card.addClassName("album-photo-card");
        card.getStyle().set("border-radius", "8px")
                       .set("overflow", "hidden")
                       .set("background", "var(--lumo-contrast-5pct)")
                       .set("cursor", "pointer");

        Div imageHolder = new Div();
        imageHolder.getStyle().set("aspect-ratio", "4/3")
                              .set("overflow", "hidden")
                              .set("background", "var(--lumo-contrast-10pct)");

        if (photo.getNameNew() != null && !photo.getNameNew().isBlank()) {
            Image img = new Image();
            img.addClassName("album-photo-thumb");
            img.setAlt(photo.getTitle() != null ? photo.getTitle() : photo.getNameNew());
            img.getStyle().set("width", "100%").set("height", "100%").set("object-fit", "cover");
            imageHolder.add(img);
        } else {
            Div placeholder = new Div(VaadinIcon.PICTURE.create());
            placeholder.addClassNames(AlignItems.CENTER, JustifyContent.CENTER,
                    Width.FULL, Height.FULL, TextColor.TERTIARY);
            imageHolder.add(placeholder);
        }

        Div info = new Div();
        info.addClassNames(Padding.SMALL);
        if (photo.getTitle() != null && !photo.getTitle().isBlank()) {
            Span titleSpan = new Span(photo.getTitle());
            titleSpan.addClassNames(FontSize.SMALL, FontWeight.SEMIBOLD, Display.BLOCK);
            info.add(titleSpan);
        }
        if (photo.getLocationByUser() != null && !photo.getLocationByUser().isBlank()) {
            Span location = new Span(photo.getLocationByUser());
            location.addClassNames(FontSize.XSMALL, TextColor.SECONDARY);
            info.add(location);
        }

        card.add(imageHolder, info);
        return card;
    }
}
