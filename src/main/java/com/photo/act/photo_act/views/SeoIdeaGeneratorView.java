package com.photo.act.photo_act.views;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Value;

//import static java.lang.Thread.ofVirtual;

/**
 * SEO Traffic Idea Generator for PhotoAct.net
 *
 * A Vaadin 24 view that calls the Anthropic Claude API to generate
 * keyword-targeted content ideas for any photography niche or topic.
 *
 * Dependencies needed in pom.xml:
 *   - com.fasterxml.jackson.core:jackson-databind (usually transitive via Spring Boot)
 *
 * Set your API key in application.properties:
 *   anthropic.api.key=sk-ant-...
 *
 * Or inject via environment variable ANTHROPIC_API_KEY.
 */
@AnonymousAllowed
@Route("seo-ideas")
@PageTitle("SEO Idea Generator – PhotoAct")
public class SeoIdeaGeneratorView extends VerticalLayout {

    // ── Inject your API key via Spring @Value or environment variable ──────────

    @Value("${anthropic.api.key}")
    private String apiKey ; /*System.getenv().getOrDefault("ANTHROPIC_API_KEY", "YOUR_API_KEY_HERE");*/

    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-20250514";

    // ── UI components ──────────────────────────────────────────────────────────
    private final ComboBox<String> pillarCombo = new ComboBox<>("Content Pillar");
    private final TextField topicField = new TextField("Topic or Keyword Focus");
    private final ComboBox<String> audienceCombo = new ComboBox<>("Target Audience");
    private final ComboBox<String> difficultyCombo = new ComboBox<>("Keyword Difficulty");
    private final Button generateBtn = new Button("Generate Ideas");
    private final ProgressBar progressBar = new ProgressBar();
    private final Grid<SeoIdea> grid = new Grid<>(SeoIdea.class, false);
    private final Div statusDiv = new Div();

    // ── Data model ─────────────────────────────────────────────────────────────
    public static class SeoIdea {
        private String title;
        private String primaryKeyword;
        private String secondaryKeywords;
        private String contentType;
        private String estimatedDifficulty;
        private String whyItWorks;

        public SeoIdea() {}

        public SeoIdea(String title, String primaryKeyword, String secondaryKeywords,
                       String contentType, String estimatedDifficulty, String whyItWorks) {
            this.title = title;
            this.primaryKeyword = primaryKeyword;
            this.secondaryKeywords = secondaryKeywords;
            this.contentType = contentType;
            this.estimatedDifficulty = estimatedDifficulty;
            this.whyItWorks = whyItWorks;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getPrimaryKeyword() { return primaryKeyword; }
        public void setPrimaryKeyword(String primaryKeyword) { this.primaryKeyword = primaryKeyword; }
        public String getSecondaryKeywords() { return secondaryKeywords; }
        public void setSecondaryKeywords(String secondaryKeywords) { this.secondaryKeywords = secondaryKeywords; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public String getEstimatedDifficulty() { return estimatedDifficulty; }
        public void setEstimatedDifficulty(String estimatedDifficulty) { this.estimatedDifficulty = estimatedDifficulty; }
        public String getWhyItWorks() { return whyItWorks; }
        public void setWhyItWorks(String whyItWorks) { this.whyItWorks = whyItWorks; }
    }

    // ── Constructor ────────────────────────────────────────────────────────────
    public SeoIdeaGeneratorView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(buildHeader());
        add(buildForm());
        add(buildProgressArea());
        add(buildGrid());
    }

    // ── Header ─────────────────────────────────────────────────────────────────
    private VerticalLayout buildHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);

        H2 title = new H2("SEO Traffic Idea Generator");
        title.getStyle().set("margin-bottom", "4px");

        Paragraph subtitle = new Paragraph(
                "Generate keyword-targeted content ideas for PhotoAct.net. " +
                        "Select a pillar, describe your topic, and get 10 ready-to-use article ideas " +
                        "with primary keywords, content types, and rationale."
        );
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin-top", "0");

        header.add(title, subtitle);
        return header;
    }

    // ── Form ───────────────────────────────────────────────────────────────────
    private VerticalLayout buildForm() {
        VerticalLayout form = new VerticalLayout();
        form.setPadding(true);
        form.setSpacing(true);
        form.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-l)");

        H3 formTitle = new H3("Configure Your Idea Generator");
        formTitle.getStyle().set("margin-top", "0");

        // Pillar
        pillarCombo.setItems(
                "Photography Tutorials & Techniques",
                "Gear Reviews & Comparisons",
                "Photography Locations & Travel",
                "Community Spotlights & Challenges",
                "Photography Business & Career",
                "Photo Editing & Post-Processing",
                "Photography Genres & Niches"
        );
        pillarCombo.setPlaceholder("Choose a content pillar...");
        pillarCombo.setWidthFull();

        // Topic
        topicField.setPlaceholder("e.g. black and white photography, portrait lighting, macro lenses...");
        topicField.setWidthFull();
        topicField.setClearButtonVisible(true);

        // Audience
        audienceCombo.setItems(
                "Complete beginner (first camera)",
                "Enthusiast (hobbyist, 1–3 years)",
                "Advanced amateur",
                "Semi-professional",
                "All levels"
        );
        audienceCombo.setValue("Enthusiast (hobbyist, 1–3 years)");
        audienceCombo.setWidthFull();

        // Difficulty
        difficultyCombo.setItems("Easy (low competition)", "Medium", "Hard (high authority needed)", "Mix of all");
        difficultyCombo.setValue("Mix of all");
        difficultyCombo.setWidthFull();

        HorizontalLayout row1 = new HorizontalLayout(pillarCombo, topicField);
        row1.setWidthFull();
        row1.setFlexGrow(1, pillarCombo, topicField);

        HorizontalLayout row2 = new HorizontalLayout(audienceCombo, difficultyCombo);
        row2.setWidthFull();
        row2.setFlexGrow(1, audienceCombo, difficultyCombo);

        generateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        generateBtn.setWidth("200px");
        generateBtn.addClickListener(e -> generateIdeas());

        form.add(formTitle, row1, row2, generateBtn);
        return form;
    }

    // ── Progress ───────────────────────────────────────────────────────────────
    private VerticalLayout buildProgressArea() {
        VerticalLayout area = new VerticalLayout();
        area.setPadding(false);
        area.setSpacing(false);

        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setWidthFull();

        statusDiv.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)");

        area.add(progressBar, statusDiv);
        return area;
    }

    // ── Grid ───────────────────────────────────────────────────────────────────
    private Grid<SeoIdea> buildGrid() {
        grid.setWidthFull();
        grid.setHeight("550px");
        grid.setVisible(false);

        grid.addColumn(SeoIdea::getTitle)
                .setHeader("Article Title")
                .setFlexGrow(3)
                .setResizable(true)
                .setAutoWidth(false);

        grid.addColumn(SeoIdea::getPrimaryKeyword)
                .setHeader("Primary Keyword")
                .setFlexGrow(2)
                .setResizable(true);

        grid.addColumn(SeoIdea::getSecondaryKeywords)
                .setHeader("Secondary Keywords")
                .setFlexGrow(2)
                .setResizable(true);

        grid.addColumn(SeoIdea::getContentType)
                .setHeader("Type")
                .setFlexGrow(1)
                .setAutoWidth(true);

        grid.addComponentColumn(idea -> {
            Span badge = new Span(idea.getEstimatedDifficulty());
            String color = switch (idea.getEstimatedDifficulty().toLowerCase()) {
                case "easy" -> "success";
                case "medium" -> "contrast";
                case "hard" -> "error";
                default -> "contrast";
            };
            badge.getElement().setAttribute("theme", "badge " + color);
            return badge;
        }).setHeader("Difficulty").setFlexGrow(1).setAutoWidth(true);

        grid.addColumn(SeoIdea::getWhyItWorks)
                .setHeader("Why It Works")
                .setFlexGrow(3)
                .setResizable(true);

        return grid;
    }

    // ── Generate logic ─────────────────────────────────────────────────────────
    private void generateIdeas() {
        String pillar = pillarCombo.getValue();
        String topic = topicField.getValue().trim();
        String audience = audienceCombo.getValue();
        String difficulty = difficultyCombo.getValue();

        if (pillar == null || pillar.isEmpty()) {
            showError("Please select a content pillar.");
            return;
        }
        if (topic.isEmpty()) {
            showError("Please enter a topic or keyword focus.");
            return;
        }

        setLoading(true);
        grid.setItems(new ArrayList<>());
        grid.setVisible(false);

        // Run API call on a background thread to avoid blocking the UI thread
        getUI().ifPresent(ui -> {
            new Thread(() -> {
                try {
                    List<SeoIdea> ideas = callClaudeApi(pillar, topic, audience, difficulty);
                    ui.access(() -> {
                        grid.setItems(ideas);
                        grid.setVisible(true);
                        setLoading(false);
                        statusDiv.setText("Generated " + ideas.size() + " ideas for \"" + topic + "\"");
                    });
                } catch (Exception ex) {
                    ui.access(() -> {
                        setLoading(false);
                        showError("API error: " + ex.getMessage());
                    });
                }
            }).start();

        });
    }

    private List<SeoIdea> callClaudeApi(String pillar, String topic, String audience, String difficulty)
            throws IOException, InterruptedException {

        String prompt = buildPrompt(pillar, topic, audience, difficulty);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("model", MODEL);
        requestBody.put("max_tokens", 2000);

        ArrayNode messages = mapper.createArrayNode();
        ObjectNode userMsg = mapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.add(userMsg);
        requestBody.set("messages", messages);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ANTHROPIC_API_URL))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = mapper.readTree(response.body());
        String text = root.path("content").get(0).path("text").asText();

        return parseIdeas(text, mapper);
    }

    private String buildPrompt(String pillar, String topic, String audience, String difficulty) {
        return """
            You are an SEO content strategist for PhotoAct.net, a photography community website.
            Your job is to generate content ideas that will rank on Google and drive organic traffic.

            Generate exactly 10 content ideas based on these parameters:
            - Content Pillar: %s
            - Topic / Keyword Focus: %s
            - Target Audience: %s
            - Keyword Difficulty Preference: %s

            Return ONLY a JSON array with no extra text, preamble, or markdown. Each object must have:
            {
              "title": "The article title (compelling, includes primary keyword naturally)",
              "primaryKeyword": "the main keyword phrase to target",
              "secondaryKeywords": "2-3 related keyword phrases, comma-separated",
              "contentType": "one of: Tutorial, Comparison, Review, Listicle, Guide, Roundup, Spotlight",
              "estimatedDifficulty": "one of: Easy, Medium, Hard",
              "whyItWorks": "one sentence explaining the SEO/traffic opportunity"
            }
            """.formatted(pillar, topic, audience, difficulty);
    }

    private List<SeoIdea> parseIdeas(String text, ObjectMapper mapper) throws IOException {
        // Strip any accidental markdown fences
        String clean = text.strip();
        if (clean.startsWith("```")) {
            clean = clean.replaceAll("^```[a-z]*\\n?", "").replaceAll("```$", "").strip();
        }

        JsonNode array = mapper.readTree(clean);
        List<SeoIdea> ideas = new ArrayList<>();
        for (JsonNode node : array) {
            ideas.add(new SeoIdea(
                    node.path("title").asText(""),
                    node.path("primaryKeyword").asText(""),
                    node.path("secondaryKeywords").asText(""),
                    node.path("contentType").asText(""),
                    node.path("estimatedDifficulty").asText("Medium"),
                    node.path("whyItWorks").asText("")
            ));
        }
        return ideas;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private void setLoading(boolean loading) {
        progressBar.setVisible(loading);
        generateBtn.setEnabled(!loading);
        if (loading) {
            statusDiv.setText("Asking Claude for ideas...");
        }
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}