package com.cineclub_backend.cineclub_backend.shared.controllers;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;
import static guru.nidi.graphviz.model.Factory.to;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizCmdLineEngine;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@Hidden
public class DiagramController {

  @Value("${server.port}")
  private String serverPort;

  @GetMapping(value = "/diagram.svg", produces = "image/svg+xml")
  public ResponseEntity<String> getDiagram() {
    Map<String, Set<String>> pathsByTag = new HashMap<>();
    Map<String, String> pathMethods = new HashMap<>();
    Set<String> securitySchemes = new HashSet<>();

    try {
      RestTemplate restTemplate = new RestTemplate();
      String apiDocsUrl = "http://localhost:" + serverPort + "/v3/api-docs";
      String jsonResponse = restTemplate.getForObject(apiDocsUrl, String.class);

      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(jsonResponse);

      JsonNode paths = root.get("paths");
      if (paths != null && paths.isObject()) {
        paths
          .fields()
          .forEachRemaining(pathEntry -> {
            String path = pathEntry.getKey();
            JsonNode pathItem = pathEntry.getValue();

            pathItem
              .fields()
              .forEachRemaining(methodEntry -> {
                String method = methodEntry.getKey().toUpperCase();
                JsonNode operation = methodEntry.getValue();

                Set<String> tags = new HashSet<>();
                JsonNode tagsNode = operation.get("tags");
                if (tagsNode != null && tagsNode.isArray()) {
                  tagsNode.forEach(tag -> tags.add(tag.asText()));
                }

                if (tags.isEmpty()) {
                  tags.add(getControllerNameFromPath(path));
                }

                String pathKey = method + " " + path;
                pathMethods.put(pathKey, method);

                for (String tag : tags) {
                  pathsByTag.computeIfAbsent(tag, k -> new HashSet<>()).add(pathKey);
                }
              });
          });
      }

      JsonNode components = root.get("components");
      if (components != null) {
        JsonNode securitySchemesNode = components.get("securitySchemes");
        if (securitySchemesNode != null && securitySchemesNode.isObject()) {
          securitySchemesNode.fieldNames().forEachRemaining(securitySchemes::add);
        }
      }
    } catch (Exception e) {
      System.err.println("Error fetching OpenAPI spec: " + e.getMessage());
      e.printStackTrace();
    }

    Graphviz.useEngine(new GraphvizCmdLineEngine());

    MutableGraph graph = mutGraph("API")
      .setDirected(true)
      .graphAttrs()
      .add("layout", "neato")
      .graphAttrs()
      .add("overlap", "false")
      .graphAttrs()
      .add("splines", "true")
      .graphAttrs()
      .add("bgcolor", "transparent")
      .graphAttrs()
      .add("pad", "1.0")
      .graphAttrs()
      .add("sep", "+25")
      .nodeAttrs()
      .add("fontsize", "12")
      .nodeAttrs()
      .add("fontname", "Arial")
      .nodeAttrs()
      .add("height", "0.7")
      .nodeAttrs()
      .add("width", "2.5")
      .linkAttrs()
      .add("color", "999999")
      .linkAttrs()
      .add("penwidth", "1.5");

    Map<String, MutableNode> controllerNodes = new HashMap<>();

    MutableNode apiGateway = mutNode("API_Gateway")
      .add(Style.FILLED)
      .add(Color.rgb("#2C3E50").fill())
      .add(Color.rgb("#1A252F"))
      .add(Shape.OCTAGON)
      .add(Label.of("🚪 API Gateway"))
      .add("fontcolor", "white")
      .add("fontsize", "16")
      .add("height", "1.5")
      .add("width", "3.0")
      .add("pin", "true")
      .add("pos", "0,0!");
    graph.add(apiGateway);

    for (String tag : pathsByTag.keySet()) {
      List<String> pathsExcluded = List.of("health-controller", "scalar-controller");

      if (pathsExcluded.contains(tag)) {
        continue;
      }

      controllerNodes.put(
        tag,
        mutNode(tag)
          .add(Style.FILLED)
          .add(Color.rgb("#4A90E2").fill())
          .add(Color.rgb("#2E5C8A"))
          .add(Shape.RECTANGLE)
          .add(Label.of("📋 " + tag))
          .add("fontcolor", "white")
          .add("fontsize", "14")
          .add("height", "0.9")
          .add("width", "3.0")
      );
    }

    if (controllerNodes.isEmpty()) {
      controllerNodes.put(
        "API",
        mutNode("API")
          .add(Style.FILLED)
          .add(Color.rgb("#4A90E2").fill())
          .add(Shape.RECTANGLE)
          .add("fontcolor", "white")
      );
    }

    controllerNodes
      .values()
      .forEach(c -> {
        graph.add(c);
        graph.add(apiGateway.addLink(to(c).with(Color.rgb("#4A90E2")).with("penwidth", "2.5")));
      });

    for (String secName : securitySchemes) {
      MutableNode secNode = mutNode(secName)
        .add(Style.FILLED)
        .add(Color.rgb("#E74C3C").fill())
        .add(Color.rgb("#C0392B"))
        .add(Shape.DIAMOND)
        .add(Label.of("🔐 " + secName))
        .add("fontcolor", "white")
        .add("fontsize", "14")
        .add("height", "1.0")
        .add("width", "2.5");

      graph.add(secNode);
      graph.add(
        secNode.addLink(
          to(apiGateway).with(Color.rgb("#E74C3C"), Style.DASHED).with("penwidth", "2.0")
        )
      );
    }

    MutableNode dataBases = mutNode("DataBases")
      .add(Style.FILLED)
      .add(Color.rgb("#4DB33D").fill())
      .add(Color.rgb("#3E9A2D"))
      .add(Shape.CYLINDER)
      .add(Label.of("🍃 DataBases"))
      .add("fontcolor", "white")
      .add("fontsize", "14")
      .add("height", "1.0")
      .add("width", "2.5");
    graph.add(dataBases);

    controllerNodes
      .values()
      .forEach(c -> {
        graph.add(
          c.addLink(to(dataBases).with(Color.rgb("#781BCF"), Style.DASHED).with("penwidth", "1.5"))
        );
      });

    String svg = Graphviz.fromGraph(graph).width(1200).render(Format.SVG).toString();

    return ResponseEntity.ok(svg);
  }

  private String getControllerNameFromPath(String path) {
    String clean = path.replaceAll("[{}]", "");
    if (clean.startsWith("/")) {
      clean = clean.substring(1);
    }
    if (clean.isEmpty()) {
      return "Root";
    }
    return clean.split("/")[0];
  }
}
