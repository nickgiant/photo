package com.photo.act.photo_act.model;

public record ShareableResource(
        ShareType type,
        String id,
        String title,
        String description,
        String imageUrl,
        String publicUrl
) {}