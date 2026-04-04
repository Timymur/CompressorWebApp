package com.example.CompressorWebApp.websocket;

import com.example.CompressorWebApp.enums.CompressorState;
import com.example.CompressorWebApp.models.Compressor;
import com.example.CompressorWebApp.models.ModelParamRange;
import com.example.CompressorWebApp.services.CompressorEventService;
import com.example.CompressorWebApp.services.CompressorService;
import com.example.CompressorWebApp.services.ModelParamRangeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

public class CompressorWebSocketHandler extends TextWebSocketHandler {

    private final CompressorService compressorService;
    private final ModelParamRangeService modelParamRangeService;
    private final CompressorEventService compressorEventService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CompressorWebSocketHandler(CompressorService compressorService,
                                      ModelParamRangeService modelParamRangeService, CompressorEventService compressorEventService) {
        this.compressorService = compressorService;
        this.modelParamRangeService = modelParamRangeService;
        this.compressorEventService = compressorEventService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        Map<String, Object> clientData = objectMapper.readValue(message.getPayload(), Map.class);
        Long compressorId = ((Number) clientData.get("compressorId")).longValue();


        Compressor compressor = compressorService.findById(compressorId)
                .orElseThrow(() -> new IllegalArgumentException("Компрессор не найден"));

        CompressorState state = compressor.getState();


        double workHours = 0.0;
        Object rawWorkHours = compressor.getWorkHours();
        if (rawWorkHours != null && rawWorkHours instanceof Number) {
            workHours = ((Number) rawWorkHours).doubleValue();
        }

        String responseJson = buildResponseForCompressor(compressorId, state, workHours, compressor);

        session.sendMessage(new TextMessage(responseJson));
    }

    private String buildResponseForCompressor(Long compressorId, CompressorState state, double workHours, Compressor compressor) throws IOException {
        Long modelId = compressor.getCompressorModel().getId();
        List<ModelParamRange> ranges = modelParamRangeService.findByCompressorModelId(modelId);

        double newWorkHours = workHours + (state == CompressorState.WORKING ? 0.0003 : 0);
        compressor.setWorkHours(newWorkHours);
        compressorService.save(compressor);

        Map<String, Map<String, Double>> valuesForResponse;

        if (state == CompressorState.FALL) {

            valuesForResponse = new HashMap<>();
            valuesForResponse.computeIfAbsent("other", k -> new HashMap<>()).put("Наработка моточасов", newWorkHours);
            for (ModelParamRange range : ranges) {
                String paramName = range.getParameter().getParameterName();
                String group = resolveGroupByParamName(paramName);
                valuesForResponse.computeIfAbsent(group, g -> new HashMap<>()).put(paramName, 0.0);
            }
        } else {

            valuesForResponse = new HashMap<>();
            Random random = new Random();

            valuesForResponse.computeIfAbsent("other", k -> new HashMap<>()).put("Наработка моточасов", newWorkHours);

            for (ModelParamRange range : ranges) {
                String paramName = range.getParameter().getParameterName();


                if (state == CompressorState.OFF && !paramName.toLowerCase().contains("температура")) {
                    String group = resolveGroupByParamName(paramName);
                    valuesForResponse.computeIfAbsent(group, g -> new HashMap<>()).put(paramName, 0.0);
                    continue;
                }

                double min = range.getMinValue();
                double max = range.getMaxValue();
                String group = resolveGroupByParamName(paramName);

                double newValue;
                int chance = random.nextInt(100);

                if (chance < 0) {
                    newValue = createAccidentSituation(min, max);
                } else if (chance < 10) {
                    newValue = createWarningSituation(min, max);
                } else {
                    double currentValue = (min + max) / 2.0;
                    newValue = stableRandomValue(min, max, currentValue);
                }

                valuesForResponse.computeIfAbsent(group, g -> new HashMap<>()).put(paramName, newValue);
            }
        }


        boolean alarmTriggered = false;
        if (state == CompressorState.WORKING) {
            for (ModelParamRange range : ranges) {
                String paramName = range.getParameter().getParameterName();
                double min = range.getMinValue();
                double max = range.getMaxValue();
                double currentValue = getValueFromMap(valuesForResponse, paramName);

                if (currentValue < min || currentValue > max) {
                    compressorEventService.createEvent(compressor, range.getParameter(), currentValue, min, max);
                    alarmTriggered = true;
                }
            }

            if (alarmTriggered) {
                compressor.setState(CompressorState.FALL);
                compressorService.save(compressor);
                state = CompressorState.FALL;

            }
        }

        Map<String, Object> root = new HashMap<>();
        root.put("compressorId", compressorId);
        root.put("state", state.name().toLowerCase());
        root.put("workHours", newWorkHours);
        root.put("values", valuesForResponse);

        if (state == CompressorState.WORKING) {
            List<String> warnings = calculateWarnings(ranges, valuesForResponse);
            root.put("warnings", warnings);
        }

        return objectMapper.writeValueAsString(root);
    }

    private String resolveGroupByParamName(String paramName) {
        String p = paramName.toLowerCase();
        if (p.contains("масла")) return "oil";
        if (p.contains("охл")) return "coolant";
        if (p.contains("1-ой ступени")) return "gas-1";
        if (p.contains("2-ой ступени")) return "gas-2";
        if (p.contains("3-ой ступени")) return "gas-3";
        if (p.contains("4-ой ступени")) return "gas-4";
        if (p.contains("индекс влажности газа")) return "IVG";
        if (p.contains("вибрации")) return "vibration";
        if (p.contains("загазованности")) return "gas-pollution";
        if (p.contains("компрессорном блоке")) return "tempBKU";
        return "other";
    }

    private List<String> calculateWarnings(List<ModelParamRange> ranges, Map<String, Map<String, Double>> values) {
        List<String> warnings = new ArrayList<>();
        for (ModelParamRange range : ranges) {
            String paramName = range.getParameter().getParameterName();
            double min = range.getMinValue();
            double max = range.getMaxValue();
            double value = getValueFromMap(values, paramName);

            double threshold = 0.1;
            if ((max - min) > 0 &&
                    (value <= min + (max - min) * threshold || value >= max - (max - min) * threshold)) {
                warnings.add(paramName);
            }
        }
        return warnings;
    }

    private double getValueFromMap(Map<String, Map<String, Double>> values, String paramName) {
        for (Map<String, Double> group : values.values()) {
            if (group.containsKey(paramName)) {
                return group.get(paramName);
            }
        }
        return 0.0;
    }

    private double stableRandomValue(double min, double max, double currentValue) {
        double step = 0.1;
        double direction = Math.random() < 0.5 ? -1.0 : 1.0;
        double candidate = currentValue + (direction * step);

        if (candidate < min) return min;
        if (candidate > max) return max;

        return Math.round(candidate * 10.0) / 10.0;
    }

    private double createWarningSituation(double min, double max) {

        double range = max - min;
        double threshold = range * 0.1;

        if (Math.random() < 0.5) {
            double candidate = min + Math.random() * threshold;
            return Math.round(candidate * 10.0) / 10.0;

        } else {
            double candidate = max - Math.random() * threshold;
            return Math.round(candidate * 10.0) / 10.0;
        }
    }

    private double createAccidentSituation(double min, double max) {

        double offset = (max - min) * 0.05;
        if (Math.random() < 0.5) {
            double candidate = min - offset * (0.5 + Math.random() * 0.5);
            return Math.round(candidate * 10.0) / 10.0;
        } else {
            double candidate = max + offset * (0.5 + Math.random() * 0.5);
            return Math.round(candidate * 10.0) / 10.0;
        }
    }
}
